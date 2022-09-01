/*
 * This file is part of AllUtilities.
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
package org.digitalmodular.weathertolive.util;

import java.util.Arrays;

import net.jcip.annotations.NotThreadSafe;

import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireArrayLengthAtLeast;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireAtLeast;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireThat;

/**
 * This class can convert from quantized geometric sequences (a.k.a. 'preferred
 * numbers') to indices and back.
 * <p>
 * The idea of preferred numbers is that there is a set of integer values in a
 * geometric sequence, that will be repeated for every magnitude increase or
 * decrease. For example, most currencies follow the 1-2-5 series with
 * magnitudes of 10. The result is the following sequence: [..., 0.1, 0.2, 0.5,
 * 1, 2, 5, 10, 20, 50, ...]. An integer base value of choice (in this case 1)
 * is assigned an index of 0, and numbers to the left and right have decreasing
 * and increasing indices, respectively. In this case, an index of 5 will
 * correspond to the value 50. Conversely, the value 25 will correspond to index
 * 4 (when rounded down).
 * <p>
 * Real-life examples of quantized geometric sequences include resistor
 * E-series, Renard series, camera exposure stops, paper sizes, 1-2-5 and 1-3
 * series, screen resolutions, and alternate currency denominations.
 * <p>
 * For example, the E6 series for resistors is defined as: {@code [10, 15, 22,
 * 33, 47, 68]}. Continuing the series repeats the same values, but
 * multiplied by 10, then by 100. Formally speaking, the number is constructed
 * with a mantissa chosen from the list (index modulo the list length) and an
 * exponent which is the index divided by the list length, rounded-down. The
 * base of the exponent is 10 in this case.
 * <p>
 * Counterexamples include musical note frequencies, because they're perfect
 * un-quantized geometric sequences. Technically, camera F-stops and paper sizes
 * are also not exactly quantized, but they're in a gray area because for small
 * numbers, they've simply been rounded to 1 decimal. This PreferredNumbers
 * class doesn't round and as such could be used for such sequences with some
 * extra processing.
 *
 * @author Mark Jeronimus
 */
// Created 2015-01-19
@NotThreadSafe
public class PreferredNumbers {

	private final int    base;
	private final int[]  values;
	private final double logBase;

	/**
	 * Examples of how to instantiate:
	 * <p>
	 * 1-2-5 series (subdivides each <i>decade</i> into 3 parts):
	 * <pre>    new PreferredNumbers(10, new int[]{1, 2, 5});</pre>
	 * E6 series (subdivides each decade into 6 parts):
	 * <pre>    new PreferredNumbers(10, 10, 15, 22, 33, 47, 68);</pre>
	 * Camera one-third-stop F-number scale (subdivides each <i>octave</i> into 6 parts):
	 * <pre>    new PreferredNumbers(2, 8, 9, 10, 11, 13, 14);</pre>
	 * In the last example, because the base is two, the next value after 14 will be 2*8 = 16.
	 *
	 * @param base   the base of the exponent. This defines by how much the values need to be multiplied by when
	 *               repeating the values.
	 * @param values two or more quantized values that define the geometric sequence. These must all be in increasing
	 *               order.
	 * @throws NullPointerException     when the values array is {@code null}.
	 * @throws IllegalArgumentException when base is less than 2, the values list is empty, the values are not in
	 *                                  increasing order, or when the last value divided by the first value equals or
	 *                                  exceeds the base.
	 */
	public PreferredNumbers(int base, int... values) {
		this.base = requireAtLeast(2, base, "base");
		requireArrayLengthAtLeast(2, values, "values");
		this.values = values.clone();
		requireThat(values[0] * base > values[values.length - 1],
		            "'base' must be larger than the quotient between the last and first value: " +
		            base + ", " + Arrays.toString(values));

		logBase = Math.log(base);
	}

	/**
	 * Calculates the value for the index. An index of 0 always corresponds to the first element of the values array
	 * used when instantiating.
	 * <p>
	 * The calculation equals:
	 * <pre>return values[modulo] * Math.pow(base, exponent);</pre>
	 * where exponent and modulo are derived from the index and array length.
	 */
	public double exp(int index) {
		int exponent = (int)Math.floor(index / (double)values.length);
		int modulo   = index - exponent * values.length;

		return values[modulo] * Math.pow(base, exponent);
	}

	/**
	 * Calculates the index for the value, rounding down to the next lower
	 * integer index. An index of 0 always corresponds to the first element of
	 * the values array used when instantiating.
	 * <p>
	 * In case the specified value is the result of a floating-point
	 * calculation, round-off errors might cause a too-low integer to be chosen.
	 * For example, if 2 was one of the specified geometric values, and the
	 * value parameter is 1.9999999999999998, the index will probably be lower
	 * than the user expects. In such cases it's recommended to multiply the
	 * value by a small modifier like 1.0001.
	 * <p>
	 * To get a rounded-up version, calculate
	 * {@code -log3(values[0] * values[0] / value)}. In cases where
	 * {@code values[0] = 1}, the calculation simplifies to
	 * {@code -log3(1 / value)}.
	 */
	public int log(double value) {
		// Get the coarse exponent.
		int exponent = (int)Math.floor(Math.log(value / values[0]) / logBase);

		// Get the mantissa.
		value /= Math.pow(base, exponent);

		// Find the exact exponent.
		for (int i = values.length - 1; i >= 0; i--) {
			if (value >= values[i]) {
				return exponent * values.length + i;
			}
		}

		return exponent * values.length;
	}

	public int getMantissa(int index) {
		int exponent = (int)Math.floor(index / (double)values.length);
		int modulo   = index - exponent * values.length;

		return values[modulo];
	}
}
