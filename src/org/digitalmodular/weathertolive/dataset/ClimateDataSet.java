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
 * Weather to Live. If not, see <http://www.gnu.org/licenses/>.
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

import java.util.Collections;
import java.util.List;

import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireNonNull;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireSizeAtLeast;

/**
 * Encapsulates {@link DataSet}s for each parameter in the climate data set.
 *
 * @author Mark Jeronimus
 */
// Created 2022-08-31
public class ClimateDataSet {
	private final ClimateDataSetMetadata metadata;
	private final List<FilterDataSet>    filterDataSets;

	public ClimateDataSet(ClimateDataSetMetadata metadata, List<FilterDataSet> filterDataSets) {
		this.metadata = requireNonNull(metadata, "metadata");
		requireSizeAtLeast(1, filterDataSets, "filterDataSets");

		this.filterDataSets = Collections.unmodifiableList(filterDataSets);
	}

	public ClimateDataSetMetadata getMetadata() {
		return metadata;
	}

	public List<FilterDataSet> getFilterDataSets() {
		return filterDataSets;
	}
}
