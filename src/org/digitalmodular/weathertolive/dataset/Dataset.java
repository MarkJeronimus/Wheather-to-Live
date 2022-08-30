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

import org.digitalmodular.weathertolive.util.RangeF;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireArrayLengthExactly;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireAtLeast;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireNonNull;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireThat;

/**
 * @author Mark Jeronimus
 */
// Created 2022-08-28
public class Dataset {
	public static final int SEA_BLUE = 0x001020;

	private final int       width;
	private final int       height;
	private final float[][] rawData;
	private final RangeF    minMax;

	private final float[][] data;
	private       boolean   dirty = true;

	/**
	 * @param absoluteZero Whether the values start at 0 or can go negative (should find minimum)
	 */
	protected Dataset(float[][] rawData, int width, int height, boolean absoluteZero) {
		this.rawData = requireNonNull(rawData, "rawData");
		requireArrayLengthExactly(12, rawData, "rawData");
		this.width = requireAtLeast(360, width, "width");
		this.height = requireAtLeast(180, height, "height");
		requireThat(width == height * 2, "'width' should be double 'height': " + width + ", " + height);
		for (int month = 0; month < 12; month++) {
			requireThat(rawData[month].length == width * height,
			            "'rawData[" + month + "].length' should equal 'width' * 'height': " +
			            rawData[month].length + ", " + width + " * " + height + " (" + width * height + ')');
		}

		minMax = findMinMax(rawData, absoluteZero);

		data = new float[12][rawData[0].length];
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

	protected void markDirty() {
		dirty = true;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public RangeF getMinMax() {
		return minMax;
	}

	/**
	 * Returns a view into the (mutable!) internal data.
	 * <p>
	 * The array has dimensions [month 0..12][pixel 0..width*height].
	 * <p>
	 * This data in this view is regenerated every time a parameter is changed.
	 */
	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
	public float[][] getData() {
		if (dirty) {
			regenerate();
			dirty = false;
		}

		return data;
	}

	protected void regenerate() {
		for (int month = 0; month < 12; month++) {
			float[] rawMonthData = rawData[month];
			float[] monthData    = data[month];

			for (int i = 0; i < rawMonthData.length; i++) {
				monthData[i] = minMax.unLerp(rawMonthData[i]);
			}
		}
	}
}
