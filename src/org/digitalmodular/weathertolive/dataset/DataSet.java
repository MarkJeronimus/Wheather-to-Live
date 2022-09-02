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
package org.digitalmodular.weathertolive.dataset;

import org.jetbrains.annotations.Nullable;

import org.digitalmodular.weathertolive.util.RangeF;
import org.digitalmodular.weathertolive.util.RangeFBuilder;
import static org.digitalmodular.weathertolive.WeatherToLivePanel.SCALE_FACTOR;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireArrayLengthExactly;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireAtLeast;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireNonNull;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireStringLengthAtLeast;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireThat;

/**
 * Encapsulates the climate data for a specific parameter, for all months of an average year.
 * <p>
 * The data for each month is a linear array of width*height grid cells spanning the entire globe.
 * <p>
 * Data values of {@link Float#NaN} are sea and have no data.
 *
 * @author Mark Jeronimus
 */
// Created 2022-08-28
public class DataSet {
	public static final int SEA_BLUE         = 0x001020;
	public static final int LAND_GREEN       = 0x104000;
	public static final int FILTER_HIGHLIGHT = 0xFFFF00;
	public static final int FILTER_SHADE     = 0x000000;

	public static final int THUMBNAIL_HEIGHT = 90 * SCALE_FACTOR;
	public static final int THUMBNAIL_WIDTH  = THUMBNAIL_HEIGHT * 2;

	static final int THUMBNAIL_PIXELS = THUMBNAIL_WIDTH * THUMBNAIL_HEIGHT;

	private final String    name;
	private final int       width;
	private final int       height;
	private final float[][] rawData;
	private final RangeF    minMax;

	// Confusing syntax for: Non-null array (pointer / object) of non-null arrays of @Nullable elements.
	// https://checkerframework.org/jsr308/specification/java-annotation-design.html#array-syntax
	private final @Nullable RangeF[][] thumbnails = new RangeF[12][THUMBNAIL_PIXELS];

	/**
	 * @param rawData      The data to store. The object is stored as-is without copying.
	 * @param absoluteZero Whether the values start at 0 or can go negative (should find minimum)
	 */
	protected DataSet(String name, float[][] rawData, int width, int height, boolean absoluteZero) {
		this.name = requireStringLengthAtLeast(1, name, "name");
		this.rawData = requireNonNull(rawData, "rawData");
		requireArrayLengthExactly(12, rawData, "rawData");
		this.width = requireAtLeast(360, width, "width");
		this.height = requireAtLeast(180, height, "height");
		requireThat(width / THUMBNAIL_WIDTH * THUMBNAIL_WIDTH == width,
		            THUMBNAIL_WIDTH + " doesn't divide 'width': " + width);
		requireThat(width == height * 2, "'width' should be double 'height': " + width + ", " + height);
		for (int month = 0; month < 12; month++) {
			requireThat(rawData[month].length == width * height,
			            "'rawData[" + month + "].length' should equal 'width' * 'height': " +
			            rawData[month].length + ", " + width + " * " + height + " (" + width * height + ')');
		}

		minMax = findMinMax(rawData, absoluteZero);

		prepareThumbnails();
	}

	private static RangeF findMinMax(float[][] rawData, boolean absoluteZero) {
		float min = Float.POSITIVE_INFINITY;
		float max = Float.NEGATIVE_INFINITY;

		for (int month = 0; month < 12; month++) {
			for (float value : rawData[month]) {
				if (!Float.isNaN(value)) {
					min = Math.min(min, value);
					max = Math.max(max, value);
				}
			}
		}

		if (absoluteZero) {
			if (min < 0.0f) {
				throw new AssertionError("Dataset that should be 'absoluteZero' has negative minimum: [" +
				                         min + ", " + max + ']');
			}

			min = 0.0f;
		}

		return RangeF.of(min, max);
	}

	private void prepareThumbnails() {
		int blockSize = width / THUMBNAIL_WIDTH;

		RangeFBuilder rb = new RangeFBuilder();

		for (int month = 0; month < 12; month++) {
			float[]            rawMonthData   = rawData[month];
			@Nullable RangeF[] monthThumbnail = thumbnails[month];
			int                thumbI         = 0;

			for (int y = 0; y < THUMBNAIL_HEIGHT; y++) {
				for (int x = 0; x < THUMBNAIL_WIDTH; x++) {
					rb.reset();

					int dataI = x * blockSize + y * width * blockSize;
					for (int v = 0; v < blockSize; v++) {
						for (int u = 0; u < blockSize; u++) {
							rb.add(rawMonthData[dataI]);
							dataI++;
						}

						dataI += width - blockSize;
					}

					monthThumbnail[thumbI] = rb.buildOrNull();
					thumbI++;
				}
			}
		}
	}

	public String getName() {
		return name;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	/**
	 * Returns a view into the (mutable!) internal data.
	 * <p>
	 * The array has dimensions [month 0..12][pixel 0..width*height].
	 * <p>
	 * This array is the same as provided to the constructor.
	 */
	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
	public float[][] getRawData() {
		return rawData;
	}

	public RangeF getMinMax() {
		return minMax;
	}

	/**
	 * Returns a view into the (mutable!) internal data.
	 * <p>
	 * The array has dimensions [month 0..12][pixel 0..THUMBNAIL_WIDTH*THUMBNAIL_HEIGHT].
	 * <p>
	 * This data is generated once in the constructor.
	 */
	public @Nullable RangeF[][] getThumbnails() {
		return thumbnails;
	}
}
