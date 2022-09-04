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
package org.digitalmodular.weathertolive.util.tablelayout;

import java.awt.Component;
import java.awt.Dimension;

import static org.digitalmodular.weathertolive.util.tablelayout.Size.Priority.RELATIVE;

/**
 * @author Mark Jeronimus
 */
// Created 2017-02-13
public final class PreferredSizeCalculator {
	private PreferredSizeCalculator() {
		throw new AssertionError();
	}

	public static Size[][] calculatePreferredSizes(Component[] components,
	                                               Number[] horizontalWeights,
	                                               Number[] verticalWeights) {
		Size[] preferredWidths  = createInitialSizes(horizontalWeights);
		Size[] preferredHeights = createInitialSizes(verticalWeights);

		processComponents(components, preferredWidths, preferredHeights);
		prepareRelativeSizes(preferredWidths);
		prepareRelativeSizes(preferredHeights);

		return new Size[][]{preferredWidths, preferredHeights};
	}

	private static Size[] createInitialSizes(Number[] weights) {
		int    num   = weights.length;
		Size[] sizes = new Size[num];

		for (int i = 0; i < num; i++) {
			if (weights[i].equals(TableLayout.PREFERRED)) {
				sizes[i] = Size.newPreferredSize();
			} else if (weights[i].equals(TableLayout.MINIMUM)) {
				sizes[i] = Size.newMinimumSize();
			} else if (weights[i] instanceof Integer) {
				sizes[i] = Size.newExactSize((Integer)weights[i]);
			} else if (weights[i] instanceof Double) {
				sizes[i] = Size.newRelativeSize((Double)weights[i]);
			}
		}

		return sizes;
	}

	private static void processComponents(Component[] components, Size[] preferredWidths, Size[] preferredHeights) {
		int numCols = preferredWidths.length;
		int numRows = preferredHeights.length;

		int col = 0;
		int row = 0;

		for (Component component : components) {
			Dimension preferredSize = component.getPreferredSize();
			Dimension minimumSize   = component.getMinimumSize();
			preferredWidths[col].recordComponent(preferredSize.width, minimumSize.width);
			preferredHeights[row].recordComponent(preferredSize.height, minimumSize.height);

			col++;
			if (col == numCols) {
				col = 0;
				row = Math.min(row + 1, numRows - 1);
			}
		}
	}

	private static void prepareRelativeSizes(Size[] layoutSizes) {
		double scale = 0;

		for (Size size : layoutSizes) {
			if (size.getPriority() == RELATIVE) {
				scale = Math.max(scale, size.getPreferred() / size.getStretchFactor());
			}
		}

		for (Size size : layoutSizes) {
			if (size.getPriority() == RELATIVE) {
				size.setPreferred(Math.max(1, size.getStretchFactor() * scale));
			}
		}
	}
}
