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
package org.digitalmodular.weathertolive.dataset;

import org.digitalmodular.weathertolive.util.NumberUtilities;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireAtLeast;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireThat;

/**
 * @author Mark Jeronimus
 */
// Created 2022-08-28
public class Dataset {
	public static final int SEA_BLUE = 0x001020;

	private final int     width;
	private final int     height;
	protected     float[] rawData;

	private float[] data;
	private boolean dirty = true;

	protected Dataset(int width, int height) {
		this.width = requireAtLeast(360, width, "width");
		this.height = requireAtLeast(180, height, "height");
		requireThat(width == height * 2, "'width' should be double 'height': " + width + ", " + height);

		rawData = new float[width * height];
		data = new float[width * height];
	}

	protected void markDirty() {
		dirty = true;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	/**
	 * Returns a view into the (mutable!) internal data.
	 * <p>
	 * This data in this view is regenerated every time a parameter is changed.
	 */
	public float[] getData() {
		if (dirty) {
			regenerate();
			dirty = false;
		}

		return data;
	}

	protected void regenerate() {
		for (int i = 0; i < rawData.length; i++) {
			data[i] = NumberUtilities.clamp(rawData[i] / 50.0f, 0.0f, 1.0f);
		}
	}
}
