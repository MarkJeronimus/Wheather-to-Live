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
import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import org.digitalmodular.weathertolive.dataset.DataSet;
import org.digitalmodular.weathertolive.dataset.FilteredDataSet;
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

	private final Consumer<List<AnimationFrame>> renderUpdateCallback;

	private @Nullable List<FilteredDataSet> filteredDataSets       = null;
	private @Nullable ColorGradient         gradient               = null;
	private           int                   visibleMonth           = 0;
	private           int                   backgroundDatasetIndex = -1;

	private final List<@Nullable AnimationFrame> imageSequence = new ArrayList<>(12);

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
	public @Nullable List<FilteredDataSet> getFilteredDataSets() {
		return filteredDataSets;
	}

	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
	public void setFilteredDataSets(@Nullable List<FilteredDataSet> filteredDataSets) {
		this.filteredDataSets = filteredDataSets;
	}

	public @Nullable ColorGradient getGradient() {
		return gradient;
	}

	public void setGradient(@Nullable ColorGradient gradient) {
		this.gradient = gradient;
	}

	public int getVisibleMonth() {
		return visibleMonth;
	}

	public void setVisibleMonth(int visibleMonth) {
		this.visibleMonth = requireRange(0, 11, visibleMonth, "visibleMonth");
	}

	public int getBackgroundDatasetIndex() {
		return backgroundDatasetIndex;
	}

	public void setBackgroundDatasetIndex(int backgroundDatasetIndex) {
		if (filteredDataSets == null) {
			throw new IllegalStateException("'filteredDataSets' is null");
		}

		this.backgroundDatasetIndex = requireRange(-1,
		                                           filteredDataSets.size() - 1,
		                                           backgroundDatasetIndex,
		                                           "backgroundDatasetIndex");
	}

	// TODO offload work from the GUI thread.
	public void dataChanged() {
		if (filteredDataSets == null) {
			clear();
			renderUpdateCallback.accept(imageSequence);
			return;
		}

		int width  = filteredDataSets.get(0).getDataSet().getWidth();
		int height = filteredDataSets.get(0).getDataSet().getHeight();

		for (int i = 0; i < 12; i++) {
			int month = (visibleMonth + i) % 12;

			BufferedImage image  = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			int[]         pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();

			renderBackground(month, pixels);
			renderFilteredPixels(month, pixels);

			imageSequence.set(month, new AnimationFrame(image, FRAME_DURATION));
			renderUpdateCallback.accept(imageSequence);
		}
	}

	private void renderBackground(int month, int[] pixels) {
		assert filteredDataSets != null;

		if (backgroundDatasetIndex >= 0) {
			DataSet dataSet = filteredDataSets.get(backgroundDatasetIndex).getDataSet();
			renderParameterBackground(dataSet, month, pixels);
		} else {
			// TODO use NASA Blue Marble or something
			float[] atlas = filteredDataSets.get(0).getDataSet().getRawData()[0];
			renderAtlasBackground(atlas, pixels);
		}
	}

	private void renderParameterBackground(DataSet dataSet, int month, int[] pixels) {
		float[][] rawData = dataSet.getRawData();
		RangeF    minMax  = dataSet.getMinMax();
		int       length  = rawData[0].length;

		float[] rawMonthData = rawData[month];

		for (int i = 0; i < length; i++) {
			float value = minMax.unLerp(rawMonthData[i]);

			int color;
			if (Float.isNaN(value)) {
				color = DataSet.SEA_BLUE;
			} else if (gradient != null) {
				color = gradient.getColor(value);
			} else {
				color = (int)(value * 255) * 0x010101;
			}

			pixels[i] = color;
		}
	}

	private static void renderAtlasBackground(float[] atlas, int[] pixels) {
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
	}

	private void renderFilteredPixels(int month, int[] pixels) {
		assert filteredDataSets != null;

		for (FilteredDataSet filteredDataSet : filteredDataSets) {
			int[] filteredMonthData = filteredDataSet.getFilteredData()[month];
			int   length            = filteredMonthData.length;

			for (int i = 0; i < length; i++) {
				if (filteredMonthData[i] == 0) {
					pixels[i] = DataSet.FILTER_SHADE;
				}
			}
		}
	}
}
