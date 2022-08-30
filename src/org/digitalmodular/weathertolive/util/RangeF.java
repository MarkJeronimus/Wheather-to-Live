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
package org.digitalmodular.weathertolive.util;

import java.awt.geom.Rectangle2D;

import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireAtLeast;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireNotDegenerate;

/**
 * A one-dimensional, immutable, variant of {@link Rectangle2D.Float}, with the added constraint that negative size
 * is not allowed.
 *
 * @author Mark Jeronimus
 */
// Created 2022-08-29 Copied from Range (double-precision variant)
public class RangeF implements Comparable<RangeF> {
	public static final RangeF ZERO = new RangeF(0.0f, 0.0f);
	public static final RangeF UNIT = new RangeF(0.0f, 1.0f);

	private final float begin;
	private final float end;

	/**
	 * Creates a new range with specified endpoints. It's recommended to use the factory method
	 * {@link #of(float, float)} instead.
	 *
	 * @throws IllegalArgumentException when begin &gt; end. If thrown, split it
	 *                                  up in empty constructor and <tt>set</tt> method and handle swapping
	 *                                  properly.
	 */
	public RangeF(float begin, float end) {
		requireNotDegenerate(begin, "begin");
		requireNotDegenerate(end, "end");
		requireAtLeast(begin, end, "end");
		requireNotDegenerate(end - begin, "end - begin");

		this.begin = begin;
		this.end = end;
	}

	/**
	 * Creates a new range with specified endpoints. For some special values, a
	 * pre-made instance will be returned.
	 *
	 * @throws IllegalArgumentException when begin &gt; end. If thrown, split it
	 *                                  up in empty constructor and <tt>set</tt> method and handle swapping
	 *                                  properly.
	 */
	public static RangeF of(float begin, float end) {
		if (begin == 0.0f) {
			if (end == 0.0f) {
				return ZERO;
			} else if (end == 1.0f) {
				return UNIT;
			}
		}

		return new RangeF(begin, end);
	}

	/**
	 * Returns the begin position of the range.
	 */
	public float getBegin() {
		return begin;
	}

	/**
	 * Returns the end position of the range.
	 */
	public float getEnd() {
		return end;
	}

	/**
	 * Returns the span of this range.
	 * <p><!-- Watch out with the no-break-spaces inside the next @code block -->
	 * This returns the same as {@link #getEnd()}{@code  ﻿- }{@link #getBegin()}.
	 */
	public float getSpan() {
		return end - begin;
	}

	/**
	 * Determines whether the {@code Range} is empty. When the
	 * {@code Range} is empty, it's endpoints overlap.
	 */
	public boolean isEmpty() {
		return end == begin;
	}

	public float lerp(float position) {
		return NumberUtilities.lerp(begin, end, position);
	}

	public float unLerp(float value) {
		return NumberUtilities.unLerp(begin, end, value);
	}

	/**
	 * Returns the midpoint of this range.
	 */
	public float getCenter() {
		return NumberUtilities.lerp(begin, end, 0.5f);
	}

	public RangeF set(float newBegin, float newEnd) {
		if (newBegin == begin && newEnd == end) {
			return this;
		}

		return of(newBegin, newEnd);
	}

	public RangeF setBeginTo(float newBegin) {
		requireNotDegenerate(newBegin, "newBegin");

		return of(newBegin, end);
	}

	public RangeF setEndTo(float newEnd) {
		requireNotDegenerate(newEnd, "newEnd");

		return of(begin, newEnd);
	}

	/**
	 * Returns the smallest range that contains both given ranges, even if the given ranges don't intersect.
	 */
	public RangeF union(RangeF other) {
		if (equals(other)) {
			return this;
		}

		float begin = Math.min(this.begin, other.begin);
		float end   = Math.max(this.end, other.end);

		if (begin == this.begin && end == this.end) {
			return this;
		} else if (begin == other.begin && end == other.end) {
			return other;
		}

		return of(begin, end);
	}

	/**
	 * Returns the largest range that's within both given ranges.
	 * <p>
	 * If the ranges don't intersect, an empty range of {@code (other.begin, other.begin)} is returned.
	 * <p>
	 * Null-safe. If the parameter is {@code null}, it returns {@code this}.
	 */
	public RangeF intersect(RangeF other) {
		if (equals(other)) {
			return this;
		}

		float begin = Math.max(this.begin, other.begin);
		float end   = Math.min(this.end, other.end);
		if (begin > end) {
			return of(other.begin, other.begin);
		} else if (begin == this.begin && end == this.end) {
			return this;
		} else if (begin == other.begin && end == other.end) {
			return other;
		}

		return of(begin, end);
	}

	/**
	 * Tests whether the range contains the given value.
	 * <p>
	 * Both ends are inclusive.
	 */
	public boolean contains(float value) {
		return begin <= value && value <= end;
	}

	@Override
	public int compareTo(RangeF other) {
		int i = Float.compare(begin, other.begin);
		if (i != 0) {
			return i;
		}

		return Float.compare(end, other.end);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (!(o instanceof RangeF)) {
			return false;
		}

		RangeF other = (RangeF)o;
		return Float.floatToIntBits(begin) == Float.floatToIntBits(other.begin) &&
		       Float.floatToIntBits(end) == Float.floatToIntBits(other.end);
	}

	@Override
	public int hashCode() {
		int hashCode = 0x811C9DC5;
		hashCode = 0x01000193 * (hashCode ^ Float.hashCode(begin));
		hashCode = 0x01000193 * (hashCode ^ Float.hashCode(end));
		return hashCode;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + '[' + "begin=" + begin + ", end=" + end + ", size=" + (end - begin) + ']';
	}
}
