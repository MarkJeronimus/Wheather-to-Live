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

import java.awt.geom.Point2D;
import java.awt.geom.RectangularShape;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This Utility class contains static helper methods for working with numbers.
 *
 * @author Mark Jeronimus
 */
// 2007-11-16
// 2013-11-28 Major rewrite
// 2015-01-13 Bugfix: log3 & exp3 worked in float instead of double
@SuppressWarnings("OverloadedMethodsWithSameNumberOfParameters")
public final class NumberUtilities {
	private NumberUtilities() {
		throw new AssertionError();
	}

	public static final DecimalFormat COMMA_NUMBER = new DecimalFormat("#,###");
	public static final DecimalFormat DEC3_NUMBER  = new DecimalFormat("0.000");
	public static final DecimalFormat SCI3_NUMBER  = new DecimalFormat("0.000E0");
	public static final DecimalFormat ENG3_NUMBER  = new DecimalFormat("##0.000E0");
	public static final DecimalFormat DEC6_NUMBER  = new DecimalFormat("0.000000");
	public static final DecimalFormat SCI6_NUMBER  = new DecimalFormat("0.000000E0");
	public static final DecimalFormat ENG6_NUMBER  = new DecimalFormat("##0.000000E0");

	/**
	 * All possible chars for representing a number as a String
	 */
	public static final char[] DIGITS = {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
			'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

	private static SecureRandom secureRnd = null;

	static {
		// Canonicalize floating point numbers.
		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
		COMMA_NUMBER.setDecimalFormatSymbols(symbols);
		DEC3_NUMBER.setDecimalFormatSymbols(symbols);
		SCI3_NUMBER.setDecimalFormatSymbols(symbols);
		ENG3_NUMBER.setDecimalFormatSymbols(symbols);
		DEC6_NUMBER.setDecimalFormatSymbols(symbols);
		SCI6_NUMBER.setDecimalFormatSymbols(symbols);
		ENG6_NUMBER.setDecimalFormatSymbols(symbols);
	}

	public static Random getSecureRandom() {
		if (secureRnd == null) {
			secureRnd = new SecureRandom();
		}

		return secureRnd;
	}

	/**
	 * Returns the factorial of a value.
	 *
	 * @return value! Return values are undefined when value < 1 and for overflow conditions.
	 */
	public static long factorial(int value) {
		long out = 1;
		for (int i = 2; i <= value; i++) {
			out *= i;
		}

		return out;
	}

	/**
	 * Calculate the closest power of two equal to or higher than the int.
	 *
	 * @param value an int.
	 * @return the highest power of two that's equal to or higher than the input value, or 0 if the number was
	 * negative.
	 */
	public static int nextPowerOf2(int value) {
		return value < 0 ? 0 : Integer.highestOneBit(value - 1) << 1;
	}

	/**
	 * Calculate the closest power of two equal to or higher than the long.
	 *
	 * @param value a long.
	 * @return the highest power of two that's equal to or higher than the input value, or 0 if the number was
	 * negative.
	 */
	public static long nextPowerOf2(long value) {
		return value < 0 ? 0 : Long.highestOneBit(value - 1) << 1;
	}

	/**
	 * Checks if value is a power of two. This is to say, if there is an integer {@code x} such that
	 * <p>
	 * {@code value = 2<sup>x</sup>}
	 * <p>
	 * This is the sequence 0, 1, 2, 4, 8, 16, ...
	 */
	public static boolean isPowerOfTwo(int value) {
		return (value & value - 1) == 0;
	}

	/**
	 * Checks if value is a power of two. This is to say, if there is an integer {@code x} such that
	 * <p>
	 * {@code value = 2<sup>x</sup>}
	 * <p>
	 * This is the sequence 0, 1, 2, 4, 8, 16, ...
	 */
	public static boolean isPowerOfTwo(long value) {
		return (value & value - 1) == 0;
	}

	/**
	 * Returns the base-2 log of the unsigned int, rounded up.
	 * <p>
	 * The result is rounded up, as if by the idealized formula {@code ceil(log2(value))}.
	 * <p>
	 * The rounded down version of log<sub>2</sub> can be obtained by {@link #bitSize(int)}{@code  - 1}.
	 */
	public static int log2(int value) {
		return value == 0 ? 0 : bitCount(Integer.highestOneBit(value - 1) - 1) + 1;
	}

	/**
	 * Returns the base-2 log of the unsigned long, rounded up.
	 * <p>
	 * The result is rounded up, as if by the idealized formula {@code ceil(log2(value))}.
	 * <p>
	 * To get a rounded down version, use {@link #bitSize(long)}{@code  - 1}.
	 */
	public static int log2(long value) {
		return value == 0 ? 0 : Long.bitCount(Long.highestOneBit(value - 1) - 1) + 1;
	}

	/**
	 * Returns the minimum number of bits needed to represent the unsigned int.
	 * <p>
	 * The result is rounded down, as if calculated by the idealized formula {@code floor(log2(value)) + 1}.
	 * <p>
	 * To get a rounded up version, use {@link #log2(int)}.
	 */
	public static int bitSize(int value) {
		return value == 0 ? 0 : bitCount(Integer.highestOneBit(value) - 1) + 1;
	}

	/**
	 * Returns the minimum number of bits needed to represent the unsigned long.
	 * <p>
	 * The result is rounded down, as if calculated by the idealized formula {@code floor(log2(value)) + 1}.
	 * <p>
	 * To get a rounded up version, use {@link #log2(long)}.
	 */
	public static int bitSize(long value) {
		return value == 0 ? 0 : Long.bitCount(Long.highestOneBit(value) - 1) + 1;
	}

	/**
	 * Reverses the first {@code numBits} bits in an {@code int}. It discards the others.
	 */
	public static int reverseBits(int value, int numBits) {
		return Integer.reverse(value) >>> 32 - numBits;
	}

	/**
	 * Reverses the first {@code numBits} bits in a {@code long}. It discards the others.
	 */
	public static long reverseBits(long value, int numBits) {
		return Long.reverse(value) >>> 64 - numBits;
	}

	public static int bitCount1(int i) {
		// Java implementation
		i -= (i >>> 1 & 0x55555555);
		i = (i & 0x33333333) + (i >>> 2 & 0x33333333);
		i = i + (i >>> 4) & 0x0f0f0f0f;
		i += (i >>> 8);
		i += (i >>> 16);
		return i & 0x3f;
	}

	public static int bitCount(int i) {
		// Faster than Java implementation
		i -= (i >>> 1 & 0x55555555);
		i = (i & 0x33333333) + (i >>> 2 & 0x33333333);
		i = i + (i >>> 4) & 0x0f0f0f0f;
		return i * 0x01010101 >>> 24;
	}

	public static int compareUnsigned(int lhs, int rhs) {
		if (rhs >= 0 && lhs < 0) {
			return 1;
		}
		if (rhs < 0 && lhs >= 0) {
			return -1;
		}

		return Integer.signum(lhs - rhs);
	}

	/**
	 * Calculates a float number to the power of an integer. The implementation uses square-and-multiply to prevent
	 * calculation of expensive logarithms and exponents.
	 *
	 * @param base     the (floating point) base of the exponentiation.
	 * @param exponent the unsigned exponent.
	 * @return base<sup>exponent</sup>.
	 */
	public static float pow(float base, int exponent) {
		float result = 1;

		float squares = base;
		while (exponent > 0) {
			if ((exponent & 1) != 0) {
				result *= squares;
			}
			squares *= squares;
			exponent >>>= 1;
		}

		return result;
	}

	/**
	 * Calculates a double number to the power of a long. The implementation uses square-and-multiply to prevent
	 * calculation of expensive logarithms and exponents.
	 *
	 * @param base     the (floating point) base of the exponentiation.
	 * @param exponent the unsigned exponent.
	 * @return base<sup>exponent</sup>.
	 */
	public static double pow(double base, long exponent) {
		double result = 1;

		double squares = base;
		while (exponent > 0) {
			if ((exponent & 1) != 0) {
				result *= squares;
			}
			squares *= squares;
			exponent >>>= 1;
		}

		return result;
	}

	/**
	 * Calculates the greatest common divisor (GCD) of two integers.
	 *
	 * @param a the first number, generally the larger one.
	 * @param b the second number, generally the smaller one.
	 * @return the smallest number that divides both input numbers.
	 */
	public static int gcd(int a, int b) {
		// From Wikipedia
		if (a == 0) {
			return b;
		}

		while (b != 0) {
			if (a > b) {
				a -= b;
			} else {
				b -= a;
			}
		}

		return a;
	}

	/**
	 * Calculates the greatest common divisor (GCD) of two longs.
	 *
	 * @param a the first number, generally the larger one.
	 * @param b the second number, generally the smaller one.
	 * @return the smallest number that divides both input numbers.
	 */
	public static long gcd(long a, long b) {
		// From Wikipedia
		if (a == 0) {
			return b;
		}

		while (b != 0) {
			if (a > b) {
				a -= b;
			} else {
				b -= a;
			}
		}

		return a;
	}

	/**
	 * Calculates the equivalent of {@code (int)Math.rint(numerator / (double)denominator)} but without floating point
	 * operations, and in the case {@code denominator == 0} it returns 0.
	 */
	public static int divSafeRound(int numerator, int denominator) {
		return denominator == 0 ? 0 : (numerator + denominator / 2) / denominator;
	}

	/**
	 * Calculates {@code numerator / denominator}, and in the case {@code denominator == 0.0f} it returns 0.0f.
	 */
	public static float divSafe(float numerator, float denominator) {
		return denominator == 0 ? 0 : numerator / denominator;
	}

	/**
	 * Calculates {@code numerator / denominator}, and in the case {@code denominator == 0.0f} it returns 0.0.
	 */
	public static double divSafe(double numerator, double denominator) {
		return denominator == 0 ? 0 : numerator / denominator;
	}

	/**
	 * Returns the result of {@code numerator <b>mod</b> denominator}. This is different from Java's
	 * <i>remainder</i> operator ({@code numerator % denominator}). For remainder, the sign of the result is the
	 * same as the sign of the numerator. For modulo, however, the sign of the result is the same as the sign of the
	 * denominator, making it useful for array operations with negative indices that should wrap around.
	 */
	public static int modulo(int numerator, int denominator) {
		return numerator - (int)Math.floor(numerator / (double)denominator) * denominator;
	}

	/**
	 * Returns the result of {@code numerator mod denominator}. This is different from Java's {@code numerator %
	 * denominator} operator (a.k.a. remainder). For remainder, the sign of the result is the same as the sign of
	 * the numerator. For modulo, however, the sign of the result is the same as the sign of the denominator, making it
	 * useful for operations that need strict wrapping behavior.
	 */
	public static long modulo(long numerator, int denominator) {
		return numerator - (long)Math.floor(numerator / (double)denominator) * denominator;
	}

	/**
	 * Returns the result of {@code numerator mod denominator}. This is different from Java's {@code numerator %
	 * denominator} operator (a.k.a. remainder). For remainder, the sign of the result is the same as the sign of
	 * the numerator. For modulo, however, the sign of the result is the same as the sign of the denominator, making it
	 * useful for operations that need strict wrapping behavior.
	 * <p>
	 */
	public static float modulo(float numerator, float denominator) {
		return numerator - (float)Math.floor(numerator / denominator) * denominator;
	}

	/**
	 * Returns the result of {@code numerator mod denominator}. This is different from Java's {@code numerator %
	 * denominator} operator (a.k.a. remainder). For remainder, the sign of the result is the same as the sign of
	 * the numerator. For modulo, however, the sign of the result is the same as the sign of the denominator, making it
	 * useful for operations that need strict wrapping behavior.
	 * <p>
	 */
	public static double modulo(double numerator, double denominator) {
		return numerator - Math.floor(numerator / denominator) * denominator;
	}

	/**
	 * Calculates the final position when a virtual player in a playing field of length {@code size} starting at {@code
	 * 0} walks straight until it hits a wall and reverses direction, for {@code pos} steps.
	 * <p>
	 * For example, {@code bounce(10, 5) == 2}, as seen here:
	 *
	 * <pre>
	 * [X....] 0
	 * [.X...] 1
	 * [..X..] 2
	 * [...X.] 3
	 * [....X] 4
	 * [...X.] 5
	 * [..X..] 6
	 * [.X...] 7
	 * [X....] 8
	 * [.X...] 9
	 * [..X..] 10
	 *    &darr;
	 *  01234
	 * </pre>
	 * <p>
	 * It's like a zig-zagging version of {@link #modulo(int, int) modulo()} and Java's remainder ({@code %}) operator.
	 *
	 * @param pos  The number of steps to take.
	 * @param size The size of the playing field. Should be positive. Non-positive values result in undefined
	 *             behavior.
	 */
	public static int bounce(int pos, int size) {
		size--;
		int modulo = size * 2;
		pos = Math.floorMod(pos + size, modulo);
		return Math.abs(pos - size);
	}

	/**
	 * Calculates the final position when a virtual player in a playing field of length {@code size} starting at {@code
	 * 0} walks straight until it hits a wall and reverses direction, for {@code pos} steps.
	 * <p>
	 * For example, {@code bounce(10, 5) == 2}, as seen here:
	 *
	 * <pre>
	 * [X....] 0
	 * [.X...] 1
	 * [..X..] 2
	 * [...X.] 3
	 * [....X] 4
	 * [....X] 5
	 * [...X.] 6
	 * [..X..] 7
	 * [.X...] 8
	 * [X....] 9
	 * [X....] 10
	 *    &darr;
	 *  01234
	 * </pre>
	 * <p>
	 * It's like a zig-zagging version of {@link #modulo(int, int) modulo()} and Java's remainder ({@code %}) operator.
	 *
	 * @param pos  The number of steps to take.
	 * @param size The size of the playing field. Should be positive. Non-positive values result in undefined
	 *             behavior.
	 */
	public static int reflect(int pos, int size) {
		int modulo = size * 2;
		pos = Math.floorMod(pos, modulo);
		return pos < size ? pos : modulo - pos - 1;
	}

	/**
	 * Sign-preserving version of {@link Math#pow(double, double)}.
	 */
	public static float powSign(float base, float power) {
		return Float.intBitsToFloat(Float.floatToRawIntBits((float)Math.pow(Math.abs(base), power))
		                            | Float.floatToRawIntBits(base) & 0x80000000);
	}

	/**
	 * Sign-preserving version of {@link Math#pow(double, double)}.
	 */
	public static double powSign(double base, double power) {
		return Double.longBitsToDouble(Double.doubleToRawLongBits(Math.pow(Math.abs(base), power))
		                               | Double.doubleToRawLongBits(base) & 0x8000000000000000L);
	}

	/**
	 * Sign-preserving version of {@link Math#sqrt(double)}.
	 */
	public static float sqrtSign(float value) {
		return Float.intBitsToFloat(Float.floatToRawIntBits((float)Math.sqrt(Math.abs(value)))
		                            | Float.floatToRawIntBits(value) & 0x80000000);
	}

	/**
	 * Sign-preserving version of {@link Math#sqrt(double)}.
	 */
	public static double sqrtSign(double value) {
		return Double.longBitsToDouble(Double.doubleToRawLongBits(Math.sqrt(Math.abs(value)))
		                               | Double.doubleToRawLongBits(value) & 0x8000000000000000L);
	}

	public static double acosh(double value) {
		return Math.log(value + (value + 1) * Math.sqrt((value - 1) / (value + 1)));
	}

	public static double cosh(double value) {
		return (Math.exp(-value) + Math.exp(value)) * 0.5;
	}

	public static double sinc(double value) {
		return value == 0 ? 1 : Math.sin(value) / value;
	}

	/**
	 * Returns true if the {@link RectangularShape} has no size (surface area). This differs from {@link
	 * RectangularShape#isEmpty()} in that the latter also regards negative size as 'empty' whether this method regards
	 * such rectangles as having non-zero size. It is equivalent to the following code: {@code rect.getWidth() == 0 &&
	 * rect.getHeight() == 0}
	 * <p>
	 * Values of NaN are NOT regarded as zero-sized.
	 *
	 * @see #isDegenerate(RectangularShape)
	 */
	public static boolean isZeroSized(RectangularShape rect) {
		return rect.getWidth() == 0 || rect.getHeight() == 0;
	}

	public static int compareNumbers(Number a, Number b) {
		if ((a instanceof Double || a instanceof Float) && (b instanceof Double || b instanceof Float)) {
			return Double.compare(a.doubleValue(), b.doubleValue());
		}

		// Note UnsignedLong is missing, this's because it's longValue() method is lossy.
		if ((a instanceof Long || a instanceof Integer || a instanceof Short || a instanceof Byte
		     || a instanceof AtomicLong || a instanceof AtomicInteger)
		    && (b instanceof Long || b instanceof Integer || b instanceof Short || b instanceof Byte
		        || b instanceof AtomicLong || b instanceof AtomicInteger)) {
			return Long.compare(a.longValue(), b.longValue());
		}

		// Handle incompatible or larger types with BigDecimals.
		return toBigDecimal(a).compareTo(toBigDecimal(b));
	}

	public static BigDecimal toBigDecimal(Number a) {
		if (a instanceof Float || a instanceof Double) {
			return BigDecimal.valueOf(a.doubleValue());
		}
		if (a instanceof Long || a instanceof Integer || a instanceof Short || a instanceof Byte
		    || a instanceof AtomicLong || a instanceof AtomicInteger) {
			return new BigDecimal(a.longValue());
		}
		if (a instanceof BigInteger) {
			return new BigDecimal((BigInteger)a);
		}
		if (a instanceof BigDecimal) {
			return (BigDecimal)a;
		}

		try {
			return new BigDecimal(a.toString());
		} catch (NumberFormatException e) {
			throw new RuntimeException("The given Number (\"" + a + "\" of class " + a.getClass().getName()
			                           + ") doesn't have a parsable toString() representation",
			                           e);
		}
	}

	public static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	public static float clamp(float value, float min, float max) {
		return Math.max(min, Math.min(max, value));
	}

	public static double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}

	/**
	 * Linearly interpolate two values.
	 */
	public static float lerp(float first, float second, float position) {
		// Numerically stable version. f+(s-f)*p is faster but unstable.
		return first * (1 - position) + second * position;
	}

	/**
	 * Linearly interpolate two values.
	 */
	public static double lerp(double first, double second, double position) {
		// Numerically stable version. f+(s-f)*p is faster but unstable.
		return first * (1 - position) + second * position;
	}

	public static float unLerp(float first, float second, float value) {
		return (value - first) / (second - first);
	}

	public static double unLerp(double first, double second, double value) {
		return (value - first) / (second - first);
	}

	/**
	 * Returns the floor modulus of the {@code double} arguments.
	 * <p>
	 * The floor modulus is {@code x - (floor(x / y) * y)},
	 * has the same sign as the divisor {@code y}, and
	 * is in the range of {@code -abs(y) < r < +abs(y)}.
	 * <p>
	 * The relationship between {@code floorDiv} and {@code floorMod} is such that:
	 * <ul>
	 * <li>{@code floor(x / y) * y + floorMod(x, y) == x}
	 * </ul>
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>If the signs of the arguments are the same, the results
	 * of {@code floorMod} and the {@code %} operator are the same.  <br>
	 * <ul>
	 * <li>{@code floorMod(4, 3) == 1}; &nbsp; and {@code (4 % 3) == 1}</li>
	 * </ul>
	 * <li>If the signs of the arguments are different, the results differ from the {@code %} operator.<br>
	 * <ul>
	 * <li>{@code floorMod(+4, -3) == -2}; &nbsp; and {@code (+4 % -3) == +1} </li>
	 * <li>{@code floorMod(-4, +3) == +2}; &nbsp; and {@code (-4 % +3) == -1} </li>
	 * <li>{@code floorMod(-4, -3) == -1}; &nbsp; and {@code (-4 % -3) == -1 } </li>
	 * </ul>
	 * </li>
	 * </ul>
	 * <p>
	 * If the signs of arguments are unknown and a positive modulus
	 * is needed it can be computed as {@code (floorMod(x, y) + abs(y)) % abs(y)}.
	 *
	 * @param x the dividend
	 * @param y the divisor
	 * @return the floor modulus {@code x - (floor(x / y) * y)}
	 * @throws ArithmeticException if the divisor {@code y} is zero
	 */
	public static double floorMod(double x, double y) {
		return x - Math.floor(x / y) * y;
	}

	public static boolean isDegenerate(float value) {
		return Float.isInfinite(value) || Float.isNaN(value);
	}

	public static boolean isDegenerate(double value) {
		return Double.isInfinite(value) || Double.isNaN(value);
	}

	public static boolean isDegenerate(Point2D value) {
		return isDegenerate(value.getX()) || isDegenerate(value.getY());
	}

	/**
	 * Returns true if this {@link RectangularShape} doesn't specify valid, countable, coordinates. If either of the
	 * four coordinates is NaN or &plusmn;Infinite, it is degenerate. Zero-sized or negative-sized rectangles are still
	 * valid. Also, arithmetic between the coordinates must not be infinite. For example, a very positive X added to a
	 * very positive Width might result in an infinite value.
	 */
	public static boolean isDegenerate(RectangularShape rect) {
		return isDegenerate(rect.getX()) || isDegenerate(rect.getY()) ||
		       isDegenerate(rect.getWidth()) || isDegenerate(rect.getHeight()) ||
		       Double.isInfinite(rect.getX() + rect.getWidth()) ||
		       Double.isInfinite(rect.getY() + rect.getHeight());
	}
}
