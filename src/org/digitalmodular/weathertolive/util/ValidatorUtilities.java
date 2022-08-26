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
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

/**
 * @author Mark Jeronimus
 */
// Created 2016-12-21
@SuppressWarnings("OverloadedMethodsWithSameNumberOfParameters")
public final class ValidatorUtilities {
	private ValidatorUtilities() {
		throw new AssertionError();
	}

	public static void requireThat(boolean condition, String message) {
		if (!condition)
			throw new IllegalArgumentException(message);
	}

	public static void requireState(boolean condition, String message) {
		if (!condition)
			throw new IllegalStateException(message);
	}

	public static void assertThat(boolean condition, String message) {
		if (!condition)
			throw new AssertionError(message);
	}

	public static <T> T requireNonNull(@Nullable T actual, String varName) {
		if (actual == null)
			throw new NullPointerException('\'' + varName + "' can't be null");
		return actual;
	}

	public static <T> T assertNonNull(T actual, String varName) {
		if (actual == null)
			throw new AssertionError('\'' + varName + "' can't be null");
		return actual;
	}

	public static <T> T requireType(Class<? extends T> type, T actual, String varName) {
		requireNonNull(actual, varName);
		if (!type.isInstance(actual))
			throw new IllegalArgumentException('\'' + varName + "' must be of type " + type.getName() +
			                                   " (type is " + actual.getClass() + ')');
		return actual;
	}

	public static int requireAtLeast(int min, int actual, String varName) {
		if (actual < min)
			throw new IllegalArgumentException('\'' + varName + " must be at least " + min + ": " + actual);
		return actual;
	}

	public static long requireAtLeast(long min, long actual, String varName) {
		if (actual < min)
			throw new IllegalArgumentException('\'' + varName + " must be at least " + min + ": " + actual);
		return actual;
	}

	public static double requireAtLeast(double min, double actual, String varName) {
		requireNotDegenerate(min, "min");
		requireNotDegenerate(actual, varName);
		if (actual < min)
			throw new IllegalArgumentException('"' + varName + "' must be at least " + min + ": " + actual);
		return actual;
	}

	public static int requireAtMost(int max, int actual, String varName) {
		if (actual > max)
			throw new IllegalArgumentException('\'' + varName + " must be at most " + max + ": " + actual);
		return actual;
	}

	public static long requireAtMost(long max, long actual, String varName) {
		if (actual > max)
			throw new IllegalArgumentException('\'' + varName + " must be at most " + max + ": " + actual);
		return actual;
	}

	public static int checkIndex(int length, int actual, String varName) {
		if (actual < 0 || actual >= length)
			throw new IllegalArgumentException(
					'\'' + varName + " must be at least 0 and below " + length + ": " + actual);
		return actual;
	}

	public static int requireRange(int min, int max, int actual, String varName) {
		if (actual < min || actual > max)
			throw new IllegalArgumentException(
					'\'' + varName + "' must be in the range [" + min + ", " + max + "]: " + actual);
		return actual;
	}

	public static long requireRange(long min, long max, long actual, String varName) {
		if (actual < min || actual > max)
			throw new IllegalArgumentException(
					'\'' + varName + "' must be in the range [" + min + ", " + max + "]: " + actual);
		return actual;
	}

	public static @Nullable Integer requireNullOrAtLeast(int min, @Nullable Integer actual, String varName) {
		if (actual != null && actual < min)
			throw new IllegalArgumentException(
					'\'' + varName + "' must either be null or be at least " + min + ": " + actual);
		return actual;
	}

	public static @Nullable Float requireNullOrNotDegenerate(@Nullable Float actual, String varName) {
		if (actual == null)
			return null;

		return requireNotDegenerate(actual, varName);
	}

	public static float requireRange(float min, float max, float actual, String varName) {
		requireNotDegenerate(actual, varName);
		if (actual < min || actual > max)
			throw new IllegalArgumentException(
					"Length of '" + varName + "' must be in the range [" + min + ", " + max + "]: " + actual);
		return actual;
	}

	public static double requireRange(double min, double max, double actual, String varName) {
		requireNotDegenerate(actual, varName);
		if (actual < min || actual > max)
			throw new IllegalArgumentException(
					"Length of '" + varName + "' must be in the range [" + min + ", " + max + "]: " + actual);
		return actual;
	}

	public static double requireAtMost(double max, double actual, String varName) {
		requireNotDegenerate(max, "max");
		requireNotDegenerate(actual, varName);
		if (actual > max)
			throw new IllegalArgumentException('"' + varName + "' must be at most " + max + ": " + actual);
		return actual;
	}

	public static double requireAbove(double min, double actual, String varName) {
		requireNotDegenerate(min, "min");
		requireNotDegenerate(actual, varName);
		if (actual <= min)
			throw new IllegalArgumentException('"' + varName + "' must be above " + min + ": " + actual);
		return actual;
	}

	public static double requireBelow(double max, double actual, String varName) {
		requireNotDegenerate(max, "max");
		requireNotDegenerate(actual, varName);
		if (actual >= max)
			throw new IllegalArgumentException('"' + varName + "' must be below " + max + ": " + actual);
		return actual;
	}

	public static double requireNonZero(double actual, String varName) {
		requireNotDegenerate(actual, varName);
		if (actual == 0)
			throw new IllegalArgumentException('"' + varName + "' must be non-zero");
		return actual;
	}

	public static float requireNotDegenerate(float actual, String varName) {
		if (NumberUtilities.isDegenerate(actual))
			throw new IllegalArgumentException(varName + " is degenerate: " + actual);
		return actual;
	}

	public static double requireNotDegenerate(double actual, String varName) {
		if (NumberUtilities.isDegenerate(actual))
			throw new IllegalArgumentException(varName + " is degenerate: " + actual);
		return actual;
	}

	public static <P extends Point2D> P requireNotDegenerate(P actual, String varName) {
		requireNonNull(actual, varName);
		if (NumberUtilities.isDegenerate(actual))
			throw new IllegalArgumentException(varName + " is degenerate: " + actual);
		return actual;
	}

	public static String requireStringNotEmpty(String actual, String varName) {
		requireNonNull(actual, varName);
		if (actual.isEmpty())
			throw new IllegalArgumentException('\'' + varName + "' can't be empty");
		return actual;
	}

	public static String requireStringLengthAtLeast(int minLength, String actual, String varName) {
		requireNonNull(actual, varName);
		if (actual.length() < minLength)
			throw new IllegalArgumentException('\'' + varName + "' must be at least " +
			                                   minLength + " characters long: " + actual.length());
		return actual;
	}

	public static @Nullable String requireNullOrStringLengthAtLeast(
			int minLength, @Nullable String actual, String varName) {
		if (actual != null && actual.length() < minLength)
			throw new IllegalArgumentException('\'' + varName + "' must be null or at least " +
			                                   minLength + " characters long: " + actual.length());
		return actual;
	}

	public static String requireStringLengthBetween(int minLength, int maxLength, String actual, String varName) {
		requireNonNull(actual, varName);
		if (actual.length() < minLength || actual.length() > maxLength) {
			if (minLength == maxLength)
				throw new IllegalArgumentException('\'' + varName + "' must be at exactly " +
				                                   minLength + " characters long: " + actual.length());
			else
				throw new IllegalArgumentException('\'' + varName + "' must be between " + minLength + " and " +
				                                   maxLength + " characters long: " + actual.length());
		}
		return actual;
	}

	public static @Nullable String requireNullOrStringLengthBetween(
			int minLength, int maxLength, @Nullable String actual, String varName) {
		if (actual != null && (actual.length() < minLength || actual.length() > maxLength)) {
			if (minLength == maxLength)
				throw new IllegalArgumentException('\'' + varName + "' must be null or exactly " +
				                                   minLength + " characters long: " + actual.length());
			else
				throw new IllegalArgumentException('\'' + varName + "' must be null or between " + minLength + " and " +
				                                   maxLength + " characters long: " + actual.length());
		}
		return actual;
	}

	public static void requireArrayLengthExactly(int length, Object actual, String varName) {
		requireNonNull(actual, varName);
		if (!actual.getClass().isArray())
			throw new AssertionError(varName + " is not an array");
		else if (Array.getLength(actual) != length)
			throw new IllegalArgumentException(
					varName + "[] must be exactly " + length + " elements long: " + Array.getLength(actual));
	}

	public static void requireArrayLengthAtLeast(int minLength, Object actual, String varName) {
		requireNonNull(actual, varName);
		if (!actual.getClass().isArray())
			throw new AssertionError(varName + " is not an array");
		else if (Array.getLength(actual) < minLength)
			throw new IllegalArgumentException(
					varName + "[] must be at least " + minLength + " elements long: " + Array.getLength(actual));
	}

	public static void requireArrayLengthAtMost(int maxLength, Object actual, String varName) {
		requireNonNull(actual, varName);
		if (!actual.getClass().isArray())
			throw new AssertionError(varName + " is not an array");
		else if (Array.getLength(actual) > maxLength)
			throw new IllegalArgumentException(
					varName + "[] must be at most " + maxLength + " elements long: " + Array.getLength(actual));
	}

	public static void requireNullOrLengthAtLeast(int min, @Nullable Object actual, String varName) {
		if (actual == null)
			return;
		else if (!actual.getClass().isArray())
			throw new AssertionError(varName + " is not an array");
		else if (Array.getLength(actual) < min)
			throw new IllegalArgumentException('\'' + varName + "' must either be null or be at least " + min +
			                                   " elements long: " + Array.getLength(actual));
	}

	public static int[] requireValuesAtLeast(int min, int[] actual, String varName) {
		requireNonNull(actual, varName);
		for (int i = 0; i < actual.length; i++)
			if (actual[i] < min)
				throw new IllegalArgumentException(varName + '[' + i + "] must be at least " + min + ": " + actual[i]);
		return actual;
	}

	public static <T> T[] requireNonNullElements(T[] actual, String varName) {
		requireNonNull(actual, varName);
		for (int i = 0; i < actual.length; i++)
			if (actual[i] == null)
				throw new IllegalArgumentException(varName + '[' + i + "] can't be null");
		return actual;
	}

	public static <E, T extends Iterable<E>> T requireNonNullElements(T actual, String varName) {
		requireNonNull(actual, varName);
		for (E obj : actual)
			if (obj == null)
				throw new IllegalArgumentException('\'' + varName + "' contains a null element");
		return actual;
	}

	public static <K, V> Map<K, V> requireNonNullElements(Map<K, V> actual, String varName) {
		requireNonNull(actual, varName);
		for (V obj : actual.values())
			if (obj == null)
				throw new IllegalArgumentException('\'' + varName + "' contains a null element");
		return actual;
	}

	public static <T extends Iterable<String>> T requireNonEmptyElements(T actual, String varName) {
		requireNonNull(actual, varName);
		for (String element : actual) {
			if (element == null)
				throw new IllegalArgumentException('\'' + varName + "' contains a null element");
			if (element.isEmpty())
				throw new IllegalArgumentException('\'' + varName + "' contains an empty string");
		}
		return actual;
	}

	public static <T extends Collection<Integer>> T requireElementsAtLeast(int min, T actual, String varName) {
		requireNonNull(actual, varName);
		for (Integer element : actual) {
			if (element == null)
				throw new IllegalArgumentException('\'' + varName + "' contains a null element");
			if (element < min)
				throw new IllegalArgumentException('\'' + varName + "' contains an element smaller than " +
				                                   min + ": " + element);
		}
		return actual;
	}

	public static <T, C extends Collection<T>> @Nullable C requireNullOrNotEmpty(@Nullable C actual, String varName) {
		if (actual != null && actual.isEmpty())
			throw new IllegalArgumentException(
					'\'' + varName + "' must be null or not be empty");
		return actual;
	}

	public static <T> Collection<T> requireSizeAtLeast(int min, Collection<T> actual, String varName) {
		if (actual.size() < min)
			throw new IllegalArgumentException(
					'\'' + varName + "' must contain at least " + min + " elements: " + actual.size());
		return actual;
	}

	public static <K, V> Map<K, V> requireSizeAtLeast(int min, Map<K, V> actual, String varName) {
		if (actual.size() < min)
			throw new IllegalArgumentException(
					'\'' + varName + "' must contain at least " + min + " elements: " + actual.size());
		return actual;
	}

	public static <T> T requireOneOf(T[] allowed, T actual, String varName) {
		requireNonNull(actual, varName);
		for (T element : allowed)
			if (Objects.equals(element, actual))
				return actual;
		throw new IllegalArgumentException(
				'\'' + varName + "' must be one of " + Arrays.toString(allowed) + ": " + actual);
	}
}
