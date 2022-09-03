/*
 * This file is part of Weather to Live.
 *
 * Copyleft 2022 Mark Jeronimus. All Rights Reversed.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AllUtilities. If not, see <http://www.gnu.org/licenses/>.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.digitalmodular.weathertolive;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import net.jcip.annotations.GuardedBy;
import org.jetbrains.annotations.Nullable;

import org.digitalmodular.weathertolive.dataset.DataSet;
import org.digitalmodular.weathertolive.dataset.FilterDataSet;
import org.digitalmodular.weathertolive.util.AnimationFrame;
import org.digitalmodular.weathertolive.util.ColorGradient;
import org.digitalmodular.weathertolive.util.RangeF;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireNonNull;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireRange;

/**
 * @author Mark Jeronimus
 */
// Created 2022-09-02
public class AtlasRenderer {
	private static final int            FRAME_DURATION = 1_500_000_000 / 12;
	private static final AnimationFrame EMPTY_FRAME    = new AnimationFrame(
			new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB), FRAME_DURATION);

	private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

	private final Consumer<List<AnimationFrame>> renderUpdateCallback;

	private @Nullable List<FilterDataSet> filterDataSets         = null;
	private           AtomicInteger       currentMonth           = new AtomicInteger();
	private           int                 backgroundDatasetIndex = -1;
	private           boolean             aggregateYear          = false;

	private final List<@Nullable AnimationFrame> imageSequence = new ArrayList<>(12);

	private final Lock      lock      = new ReentrantLock();
	private final Condition condition = lock.newCondition();

	@GuardedBy("lock")
	private @Nullable Future<?>     rootFuture   = null;
	private final     AtomicBoolean taskAborting = new AtomicBoolean();
	private final     AtomicBoolean taskRunning  = new AtomicBoolean();

	public AtlasRenderer(Consumer<List<AnimationFrame>> renderUpdateCallback) {
		this.renderUpdateCallback = requireNonNull(renderUpdateCallback, "renderUpdateCallback");

		clear();
	}

	private void clear() {
		for (int i = 0; i < 12; i++) {
			imageSequence.add(EMPTY_FRAME);
		}
	}

	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
	public @Nullable List<FilterDataSet> getFilterDataSets() {
		return filterDataSets;
	}

	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
	public void setFilterDataSets(@Nullable List<FilterDataSet> filterDataSets) {
		this.filterDataSets = filterDataSets;
	}

	public int getCurrentMonth() {
		return currentMonth.get();
	}

	public void setCurrentMonth(int currentMonth) {
		this.currentMonth.set(requireRange(0, 11, currentMonth, "currentMonth"));
	}

	public int getBackgroundDatasetIndex() {
		return backgroundDatasetIndex;
	}

	public void setBackgroundDatasetIndex(int backgroundDatasetIndex) {
		if (filterDataSets == null) {
			throw new IllegalStateException("'filterDataSets' is null");
		}

		this.backgroundDatasetIndex = requireRange(-1,
		                                           filterDataSets.size() - 1,
		                                           backgroundDatasetIndex,
		                                           "backgroundDatasetIndex");
	}

	public boolean isAggregateYear() {
		return aggregateYear;
	}

	public void setAggregateYear(boolean aggregateYear) {
		this.aggregateYear = aggregateYear;
	}

	// TODO offload work from the GUI thread.
	public void dataChanged() {
		lock.lock();
		try {
			System.out.println("dataChanged()");
			if (taskRunning.get()) {
				taskAborting.set(true);

				try {
					while (taskRunning.get()) {
						condition.await(100, TimeUnit.MILLISECONDS);
					}

				} catch (InterruptedException ex) {
					throw new RuntimeException(ex);
				}
			}

			taskAborting.set(false);
			rootFuture = EXECUTOR.submit(this::renderTask);
		} finally {
			lock.unlock();
		}
	}

	void renderTask() {
		int currentMonthCopy = currentMonth.get();

		System.out.println("renderTask()");
		long t = System.nanoTime();

		taskRunning.set(true);

		try {
			if (filterDataSets == null) {
				clear();
				renderUpdateCallback.accept(imageSequence);
				return;
			}

			int @Nullable [] aggregateFilteredPixels = null;
			if (aggregateYear) {
				aggregateFilteredPixels = renderAggregateYear();

				// Note to self: Don't abort here. Let it render at least a frame (at the cost of responsiveness)
			}

			for (int i = 0; i < 12; i++) {
				int month = (currentMonthCopy + i) % 12;
				System.out.println("month: " + month);

				renderMonth(month, aggregateFilteredPixels);

				if (taskAborting.get()) {
					return;
				}
			}
		} finally {
			if (taskAborting.get()) {
				System.out.println("Calculation aborted");
			}
			System.out.println("Calculation took " + (System.nanoTime() - t) / 1.0e6f + " ms");

			lock.lock();
			try {
				taskRunning.set(false);
				condition.signalAll();
			} finally {
				lock.unlock();
			}
		}
	}

	private void renderMonth(int month, int @Nullable [] aggregateFilteredPixels) {
		assert filterDataSets != null;

		int width  = filterDataSets.get(0).getDataSet().getWidth();
		int height = filterDataSets.get(0).getDataSet().getHeight();

		BufferedImage image  = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int[]         pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();

		renderBackground(month, pixels);

		if (taskAborting.get()) {
			return;
		}

		if (aggregateFilteredPixels != null) {
			renderAggregateFilteredPixels(aggregateFilteredPixels, pixels);
		} else {
			renderFilteredPixels(month, pixels);
		}

		imageSequence.set(month, new AnimationFrame(image, FRAME_DURATION));
		renderUpdateCallback.accept(imageSequence);
	}

	private int[] renderAggregateYear() {
		assert filterDataSets != null;

		int width  = filterDataSets.get(0).getDataSet().getWidth();
		int height = filterDataSets.get(0).getDataSet().getHeight();

		int[] aggregateFilteredPixels = new int[width * height];
		Arrays.fill(aggregateFilteredPixels, 1);

		for (int month = 0; month < 12; month++) {
			for (FilterDataSet filterDataSet : filterDataSets) {
				int[] filteredMonthData = filterDataSet.getFilteredData()[month];
				int   length            = filteredMonthData.length;

				for (int i = 0; i < length; i++) {
					if (filteredMonthData[i] == 0) {
						aggregateFilteredPixels[i] = 0;
					}
				}

				if (checkStopCondition()) {
					return aggregateFilteredPixels;
				}
			}
		}

		return aggregateFilteredPixels;
	}

	private void renderBackground(int month, int[] pixels) {
		assert filterDataSets != null;

		if (backgroundDatasetIndex >= 0) {
			DataSet dataSet = filterDataSets.get(backgroundDatasetIndex).getDataSet();
			renderParameterBackground(dataSet, month, pixels);
		} else {
			// TODO use NASA Blue Marble or something
			float[] atlas = filterDataSets.get(0).getDataSet().getRawData()[0];
			renderAtlasBackground(atlas, pixels);
		}
	}

	private void renderParameterBackground(DataSet dataSet, int month, int[] pixels) {
		float[][]               rawData  = dataSet.getRawData();
		RangeF                  minMax   = dataSet.getMinMax();
		int                     gamma    = dataSet.getGamma();
		@Nullable ColorGradient gradient = ColorGradientCache.getGradient(dataSet.getGradientFilename());
		int                     length   = rawData[0].length;

		float[] rawMonthData = rawData[month];

		for (int i = 0; i < length; i++) {
			float value = minMax.unLerp(rawMonthData[i]);

			int color;
			if (Float.isNaN(value)) {
				color = DataSet.SEA_BLUE;
			} else {
				if (gamma > 1) {
					value = applyGamma(value, gamma);
				}

				if (gradient != null) {
					color = gradient.getColor(value);
				} else {
					color = (int)(value * 255) * 0x010101;
				}
			}

			pixels[i] = color;
		}

		checkStopCondition();
	}

	private void renderAtlasBackground(float[] atlas, int[] pixels) {
		int length = atlas.length;

		for (int i = 0; i < length; i++) {
			int color;
			if (Float.isNaN(atlas[i])) {
				color = DataSet.SEA_BLUE;
			} else {
				color = DataSet.LAND_GREEN;
			}

			pixels[i] = color;
		}

		checkStopCondition();
	}

	private void renderAggregateFilteredPixels(int[] aggregateFilteredPixels, int[] pixels) {
		int length = aggregateFilteredPixels.length;

		for (int i = 0; i < length; i++) {
			if (aggregateFilteredPixels[i] == 0) {
				pixels[i] = DataSet.FILTER_SHADE;
			}
		}

		checkStopCondition();
	}

	private void renderFilteredPixels(int month, int[] pixels) {
		assert filterDataSets != null;

		for (FilterDataSet filterDataSet : filterDataSets) {
			int[] filteredMonthData = filterDataSet.getFilteredData()[month];
			int   length            = filteredMonthData.length;

			for (int i = 0; i < length; i++) {
				if (filteredMonthData[i] == 0) {
					pixels[i] = DataSet.FILTER_SHADE;
				}
			}
		}

		checkStopCondition();
	}

	private boolean checkStopCondition() {
		lock.lock();
		try {
			if (taskAborting.get()) {
				return true;
			}

			return false;
		} finally {
			lock.unlock();
		}
	}

	public static float applyGamma(float value, int gamma) {
		value = 1 - value;

		int power = gamma;
		assert power > 0 : power;

		float result = 1;

		while (true) {
			if ((power & 1) != 0) {
				result *= value;
			}

			value *= value;
			power >>>= 1;

			if (power == 0) {
				return 1 - result;
			}
		}
	}
}
