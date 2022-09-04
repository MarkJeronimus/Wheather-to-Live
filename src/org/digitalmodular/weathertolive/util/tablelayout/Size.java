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

import org.digitalmodular.weathertolive.util.NumberUtilities;

/**
 * @author Mark Jeronimus
 */
// Created 2017-02-10
public class Size {
	/**
	 * @author Mark Jeronimus
	 */
	// Created 2017-02-10
	public enum Priority {
		EXACT,
		MINIMUM,
		PREFERRED,
		RELATIVE,
	}

	private final Priority priority;
	private       int      minimum;
	private       double   preferred;
	private final double   stretchFactor;

	public Size(Size other) {
		priority = other.priority;
		minimum = other.minimum;
		preferred = other.preferred;
		stretchFactor = other.stretchFactor;
	}

	private Size(Priority priority, int minimum, double preferred) {
		this(priority, minimum, preferred, 0);
	}

	private Size(Priority priority, int minimum, double preferred, double stretchFactor) {
		this.priority = priority;
		this.minimum = minimum;
		this.preferred = preferred;
		this.stretchFactor = stretchFactor;
	}

	/**
	 * Minimum and Preferred size is specified.<br>
	 * Cannot contract.<br>
	 * Cannot stretch.
	 */
	static Size newExactSize(int size) {
		return new Size(Priority.EXACT, size, size, size);
	}

	/**
	 * Minimum size is determined by components.<br>
	 * Cannot contract, as preferred size is kept equal to minimum size.<br>
	 * Can stretch, unless the layout also contains Preferred or Relative sizes.
	 */
	static Size newMinimumSize() {
		return new Size(Priority.MINIMUM, 0, 0);
	}

	/**
	 * Minimum size and preferred size are determined by components.<br>
	 * Can contract down to minimum size of components.<br>
	 * Can stretch, unless the layout also contains Relative sizes.
	 */
	static Size newPreferredSize() {
		return new Size(Priority.PREFERRED, 0, 0);
	}

	/**
	 * Preferred size is determined by components.<br>
	 * Can contract down to 0.<br>
	 * Can stretch.
	 */
	static Size newRelativeSize(double factor) {
		return new Size(Priority.RELATIVE, 0, factor, factor);
	}

	public Priority getPriority() {
		return priority;
	}

	public int getMinimum() {
		return minimum;
	}

	public double getPreferred() {
		return preferred;
	}

	public void setPreferred(double preferred) {
		this.preferred = preferred;
	}

	public double getStretchFactor() {
		return stretchFactor;
	}

	public void recordComponent(int preferredSize, int minimumSize) {
		switch (priority) {
			case EXACT:
				break;
			case MINIMUM:
				minimum = Math.max(minimum, minimumSize);
				preferred = minimum;
				break;
			case PREFERRED:
			case RELATIVE:
				minimum = Math.max(minimum, minimumSize);
				preferred = NumberUtilities.clamp(preferredSize, 1, preferred);
				break;
		}
	}

	public static Size[] copy(Size[] componentSizes) {
		Size[] sizes = new Size[componentSizes.length];

		for (int i = 0; i < componentSizes.length; i++) {
			sizes[i] = new Size(componentSizes[i]);
		}

		return sizes;
	}

	public static boolean containsPriority(Size[] sizes, Priority priority) {
		for (Size size : sizes) {
			if (size.getPriority() == priority) {
				return true;
			}
		}

		return false;
	}

	public static boolean containsPriority(Size[] sizes, int length, Priority priority) {
		for (int i = 0; i < sizes.length; i++) {
			if (sizes[Math.min(i, sizes.length - 1)].getPriority() == priority) {
				return true;
			}
		}

		return false;
	}

	public static int getPriorityCount(Size[] sizes, Priority priority) {
		int count = 0;

		for (Size size : sizes) {
			if (size.getPriority() == priority) {
				count++;
			}
		}

		return count;
	}

	public static int getPriorityCount(Size[] sizes, int length, Priority priority) {
		int count = 0;

		for (int i = 0; i < sizes.length; i++) {
			if (sizes[Math.min(i, sizes.length - 1)].getPriority() == priority) {
				count++;
			}
		}

		return count;
	}

	public static int getTotalMinimum(Size[] sizes) {
		int totalMinimum = 0;

		for (Size size : sizes) {
			totalMinimum += size.getMinimum();
		}

		return totalMinimum;
	}

	public static int getTotalMinimum(Size[] sizes, int length) {
		int totalMinimum = 0;

		for (int i = 0; i < length; i++) {
			totalMinimum += sizes[Math.min(i, sizes.length - 1)].getMinimum();
		}

		return totalMinimum;
	}

	public static double getTotalPreferred(Size[] sizes) {
		double totalPreferred = 0;

		for (Size size : sizes) {
			totalPreferred += size.getPreferred();
		}

		return totalPreferred;
	}

	public static double getTotalPreferred(Size[] sizes, int length) {
		double totalPreferred = 0;

		for (int i = 0; i < length; i++) {
			totalPreferred += sizes[Math.min(i, sizes.length - 1)].getPreferred();
		}

		return totalPreferred;
	}

	public static double getTotalStretchFactor(Size[] sizes) {
		double totalStretch = 0;

		for (Size size : sizes) {
			totalStretch += size.getStretchFactor();
		}

		return totalStretch;
	}

	public static double getTotalStretchFactor(Size[] sizes, int length) {
		double totalStretch = 0;

		for (Size size : sizes) {
			totalStretch += size.getStretchFactor();
		}

		return totalStretch;
	}

	public static double getPriorityPreferred(Size[] sizes, Priority priority) {
		double totalPreferred = 0;

		for (Size size : sizes) {
			if (size.getPriority() == priority) {
				totalPreferred += size.getPreferred();
			}
		}

		return totalPreferred;
	}

	public static double getPriorityPreferred(Size[] sizes, int length, Priority priority) {
		double totalPreferred = 0;

		for (int i = 0; i < length; i++) {
			if (sizes[i].getPriority() == priority) {
				totalPreferred += sizes[Math.min(i, sizes.length - 1)].getPreferred();
			}
		}

		return totalPreferred;
	}

	@Override
	public String toString() {
		switch (priority) {
			case EXACT:
				return "ExactSize    {priority=" + priority + ", " + preferred + " (minimum=" + minimum + ")}";
			case MINIMUM:
				return "MinimumSize  {priority=" + priority + ", " + preferred + " (minimum=" + minimum + ")}";
			case PREFERRED:
				return "PreferredSize{priority=" + priority + ", " + preferred + " (minimum=" + minimum + ")}";
			case RELATIVE:
				return "RelativeSize {priority=" + priority + ", " + preferred + " (minimum=" + minimum + ")}" +
				       ", stretchFactor=" + stretchFactor + '}';
			default:
				throw new AssertionError();
		}
	}
}
