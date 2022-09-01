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
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireNonNull;

/**
 * @author Mark Jeronimus
 */
// Created 2022-09-01
public class FilteredDataSet {
	private final DataSet dataSet;

	private       RangeF    filterMinMax;
	private final float[][] filteredData;
	private       boolean   dirty = true;

	public FilteredDataSet(DataSet dataSet) {
		this.dataSet = requireNonNull(dataSet, "dataSet");

		filterMinMax = dataSet.getMinMax();

		filteredData = new float[12][dataSet.getRawData()[0].length];
	}

	public DataSet getDataSet() {
		return dataSet;
	}

	public RangeF getFilterMinMax() {
		return filterMinMax;
	}

	public void setFilterMinMax(RangeF filterMinMax) {
		requireNonNull(filterMinMax, "filterMinMax");

		if (this.filterMinMax.equals(filterMinMax)) {
			return;
		}

		this.filterMinMax = filterMinMax;
		markDirty();
	}

	protected void markDirty() {
		dirty = true;
	}

	/**
	 * Returns a view into the (mutable!) raw data.
	 * <p>
	 * The array has dimensions [month 0..12][pixel 0..width*height].
	 * <p>
	 * This data in this view is regenerated every time a parameter is changed.
	 */
	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
	public float[][] getFilteredData() {
		if (dirty) {
			regenerate();
			dirty = false;
		}

		return filteredData;
	}

	protected void regenerate() {
		float[][] rawData = dataSet.getRawData();

		for (int month = 0; month < 12; month++) {
			float[] rawMonthData      = rawData[month];
			float[] filteredMonthData = filteredData[month];

			for (int i = 0; i < rawMonthData.length; i++) {
				if (filterMinMax.contains(rawMonthData[i])) {
					filteredMonthData[i] = 1;
				} else {
					filteredMonthData[i] = 0;
				}
			}
		}
	}
}
