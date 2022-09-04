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

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import javax.imageio.ImageIO;

/**
 * A smooth color gradient designed to be used for power plots.
 * <p>
 * Given a floating point number, calculates a color that is smooth and differentiable. The color is interpolated from a
 * set of equally-spaced control points.
 * <p>
 * It supports linear gradients in the interval {@code [0, 1]}. Values outside the interval are clipped (no
 * wrapping is applied and no exception is thrown).
 *
 * @author Mark Jeronimus
 */
// Created 2013-02-16
@SuppressWarnings("OverloadedVarargsMethod")
public class ColorGradient {
	/**
	 * The pages list of RGB triples control points of the gradient. Last two indices should be equal to allow
	 * efficient interpolation at value=1 without having to check boundary conditions. This should be invisible to the
	 * outside.
	 */
	private float[][] gradient;

	/**
	 * Creates a smooth gradient that is a plain gray scale.
	 */
	public ColorGradient() {
		this(new float[][]{{0.0f, 0.0f, 0.0f}, {1.0f, 1.0f, 1.0f}});
	}

	/**
	 * Creates a smooth gradient from a given array of control points of R, G and B. The array layout should be
	 * {@code {{R0, G0, B0}, ...}}. where each component value is in the range [0,1].
	 *
	 * @param gradient the 2-dimensional array of RGB triples, or multiple 1-dimensional RGB triples separated by
	 *                 commas. The outer array is copied, but the triples are referenced, so changes to the triples are
	 *                 directly visible.
	 */
	public ColorGradient(float[]... gradient) {
		int n = gradient.length;
		this.gradient = Arrays.copyOf(gradient, n + 1);
		this.gradient[n] = this.gradient[n - 1];
	}

	/**
	 * Creates a smooth gradient that is a plain gray scale with a given gamma correction curve. If the specified gamma
	 * is negative, it creates a negative color gradient with the gamma of the positive value.
	 */
	public ColorGradient(float gamma) {
		gradient = new float[1026][3];

		if (gamma > 0) {
			for (int i = 0; i <= 1024; i++) {
				float c = 1 - (float)Math.pow(1 - i / 1024.0, 1 / gamma);
				gradient[i][2] = c;
				gradient[i][1] = c;
				gradient[i][0] = c;
			}
		} else {
			for (int i = 0; i <= 1024; i++) {
				float c = (float)Math.pow(1 - i / 1024.0, -1 / gamma);
				gradient[i][2] = c;
				gradient[i][1] = c;
				gradient[i][0] = c;
			}
		}

		gradient[1025] = gradient[1024];
	}

	/**
	 * Creates a smooth gradient from a gradient file.
	 * <p>
	 * All pixels on the first row of the image are used. Values from the range [0, 1] are scaled to an X coordinate in
	 * the line. If the X coordinate is not integer, the color is linearly interpolated.
	 */
	public ColorGradient(File file) throws IOException {
		BufferedImage img = ImageIO.read(file);

		fromImage(img);
	}

	/**
	 * Creates a smooth gradient from a gradient URL.
	 * <p>
	 * All pixels on the first row of the image are used. Values from the range [0, 1] are scaled to an X coordinate in
	 * the line. If the X coordinate is not integer, the color is linearly interpolated.
	 */
	public ColorGradient(URL url) throws IOException {
		BufferedImage img = ImageIO.read(url);

		fromImage(img);
	}

	/**
	 * Creates a smooth gradient from a gradient image.
	 * <p>
	 * All pixels on the first row of the image are used. Values from the range [0, 1] are scaled to an X coordinate in
	 * the line. If the X coordinate is not integer, the color is linearly interpolated.
	 */
	public ColorGradient(BufferedImage img) {
		fromImage(img);
	}

	/**
	 * Calculate a color from the gradient. The interval [0, 1] is mapped to indices 0..length-1 of the control point
	 * array before interpolating between two control points.
	 */
	public int getColor(float value) {
		// Handle out-of-bounds.
		if (value > 1) {
			value = 1;
		} else if (!(value >= 0)) {
			value = 0;
		}

		// Calculate interpolation parameter.
		value *= gradient.length - 2; // Remember the control point array a duplicate node at the end.
		int   index    = (int)Math.floor(value);
		float fraction = value - index;

		// Get colors to interpolate. The +1 is the reason why the last two indices should be the same for the last
		// control point.
		float[] left  = gradient[index];
		float[] right = gradient[index + 1];

		// Linear interpolation
		float r = left[0] + (right[0] - left[0]) * fraction;
		float g = left[1] + (right[1] - left[1]) * fraction;
		float b = left[2] + (right[2] - left[2]) * fraction;

		// Convert to an RGB packed integer.
		return ((int)(r * 255 + 0.49999) << 8 | (int)(g * 255 + 0.49999)) << 8 | (int)(b * 255 + 0.49999);
	}

	private void fromImage(BufferedImage img) {
		img.setAccelerationPriority(0);
		byte[] rgb = ((DataBufferByte)img.getRaster().getDataBuffer()).getData();

		int n = img.getWidth();

		gradient = new float[n + 1][3];

		int i = 0;
		for (int x = 0; x < n; x++) {
			gradient[x][2] = (rgb[i] & 0xFF) / 255.0f;
			i++;
			gradient[x][1] = (rgb[i] & 0xFF) / 255.0f;
			i++;
			gradient[x][0] = (rgb[i] & 0xFF) / 255.0f;
			i++;
		}

		gradient[n] = gradient[n - 1];
	}
}
