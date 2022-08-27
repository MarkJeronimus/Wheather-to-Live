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
package org.digitalmodular.weathertolive;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.digitalmodular.weathertolive.util.NumberUtilities;
import org.digitalmodular.weathertolive.util.ZoomPanel;

/**
 * @author Mark Jeronimus
 */
// Created 2022-08-26
public class TestMain extends ZoomPanel {
	public static void main(String... args) throws IOException {
		BufferedImage image = loadImage();

		SwingUtilities.invokeLater(() -> {
			JFrame f = new JFrame();
			f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

			f.setContentPane(new TestMain(image));

			f.pack();
			f.setLocationRelativeTo(null);
			f.setVisible(true);
		});
	}

	private static BufferedImage loadImage() throws IOException {
		try (ZipFile zip = new ZipFile(new File("wc2.1_10m_prec.zip"))) {
			InputStream   inputStream = zip.getInputStream(zip.getEntry("wc2.1_10m_prec_01.tif"));
			BufferedImage geoData     = ImageIO.read(inputStream);

			return convertGeoDataToImage(geoData);
		}
	}

	private static BufferedImage convertGeoDataToImage(BufferedImage geoData) {
		BufferedImage img  = new BufferedImage(geoData.getWidth(), geoData.getHeight(), BufferedImage.TYPE_INT_RGB);
		int[]         ints = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();

		DataBuffer dataBuffer = geoData.getRaster().getDataBuffer();

		if (dataBuffer instanceof DataBufferShort) {
			short[] shorts = ((DataBufferShort)dataBuffer).getData();

			for (int i = 0; i < ints.length; i++) {
				if (shorts[i] == -32768) {
					ints[i] = 0x001020;
				} else {
					ints[i] = shorts[i] >> 1;
					ints[i] = NumberUtilities.clamp(ints[i], 0, 255) * 0x010101;
				}
			}
		} else if (dataBuffer instanceof DataBufferFloat) {
			float[] floats = ((DataBufferFloat)dataBuffer).getData();

			for (int i = 0; i < ints.length; i++) {
				if (floats[i] < 1.0e-10f) {
					ints[i] = 0x001020;
				} else {
					ints[i] = (int)(floats[i] * 255 / 12);
					ints[i] = NumberUtilities.clamp(ints[i], 0, 255) * 0x010101;
				}
			}
		}
		return img;
	}

	public TestMain(BufferedImage image) {
		setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
		setBackground(Color.BLACK);
		setImage(image);
		setZoom(0);
	}
}
