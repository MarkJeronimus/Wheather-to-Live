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
package org.digitalmodular.weathertolive;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import org.digitalmodular.weathertolive.util.ColorGradient;

/**
 * @author Mark Jeronimus
 */
// Created 2022-09-03
public final class ColorGradientCache {
	private ColorGradientCache() {
		throw new AssertionError();
	}

	@SuppressWarnings("StaticCollection")
	private static final Map<String, ColorGradient> GRADIENTS = new HashMap<>(3);

	public static @Nullable ColorGradient getGradient(@Nullable String filename) {
		if (filename == null) {
			return null;
		}

		ColorGradient gradient = GRADIENTS.get(filename);

		if (gradient == null) {
			gradient = loadGradient(filename);
			GRADIENTS.put(filename, gradient);
		}

		return gradient;
	}

	private static ColorGradient loadGradient(String filename) {
		try {
			return new ColorGradient(new File(filename));
		} catch (IOException ignored) {
			return new ColorGradient(new float[][]{{0.1f, 0.1f, 0.1f}, {1.0f, 1.0f, 1.0f}}); // Fallback to gray scale
		}
	}
}
