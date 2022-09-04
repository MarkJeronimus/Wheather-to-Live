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
package org.digitalmodular.weathertolive.util;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import static java.util.Objects.requireNonNull;

/**
 * @author Mark Jeronimus
 */
// Created 2017-07-18
public class AnimationFrame {
	private final BufferedImage image;
	private final long          durationNanos;

	public AnimationFrame(BufferedImage image, long durationNanos) {
		this.image = requireNonNull(image);
		this.durationNanos = durationNanos;

		if (durationNanos < 1) {
			throw new IllegalArgumentException("'durationNanos' must be at least 1: " + durationNanos);
		}
	}

	public BufferedImage getImage() {
		return image;
	}

	public long getDurationNanos() {
		return durationNanos;
	}

	public Dimension getSize() {
		return new Dimension(image.getWidth(), image.getHeight());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (!(o instanceof AnimationFrame)) {
			return false;
		}

		AnimationFrame other = (AnimationFrame)o;
		return getDurationNanos() == other.getDurationNanos() &&
		       getImage().equals(other.getImage());
	}

	@Override
	public int hashCode() {
		int hashCode = 0x811C9DC5;
		hashCode = 0x01000193 * (hashCode ^ image.hashCode());
		hashCode = 0x01000193 * (hashCode ^ Long.hashCode(durationNanos));
		return hashCode;
	}
}
