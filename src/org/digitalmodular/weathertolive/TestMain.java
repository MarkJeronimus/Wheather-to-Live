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
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.digitalmodular.weathertolive.dataset.Dataset;
import org.digitalmodular.weathertolive.dataset.WorldClimDatasetFactory;
import org.digitalmodular.weathertolive.util.HTTPDownloader;
import org.digitalmodular.weathertolive.util.NumberUtilities;
import org.digitalmodular.weathertolive.util.ZoomPanel;

/**
 * @author Mark Jeronimus
 */
// Created 2022-08-26
public class TestMain extends ZoomPanel {
	public static void main(String... args) throws IOException {
//		downloadDataSet("wc2.1_10m_tmax.zip");

		Dataset dataSet = WorldClimDatasetFactory.createFor("wc2.1_10m_prec.zip", true);

		BufferedImage image = makeImage(dataSet);

		SwingUtilities.invokeLater(() -> {
			JFrame f = new JFrame();
			f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

			f.setContentPane(new TestMain(image));

			f.pack();
			f.setLocationRelativeTo(null);
			f.setVisible(true);
		});
	}

	private static void downloadDataSet(String filename) throws IOException {
		URL  url  = new URL("https://biogeo.ucdavis.edu/data/worldclim/v2.1/base/" + filename);
		Path file = Paths.get(filename);

		HTTPDownloader httpDownloader = new HTTPDownloader();
		httpDownloader.addProgressListener(System.out::println);
		httpDownloader.downloadToFile(url, null, file);
	}

	private static BufferedImage makeImage(Dataset dataSet) {
		BufferedImage image  = new BufferedImage(dataSet.getWidth(), dataSet.getHeight(), BufferedImage.TYPE_INT_RGB);
		int[]         pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();

		float[] data = dataSet.getData();
		for (int i = 0; i < data.length; i++) {
			if (Float.isNaN(data[i])) {
				pixels[i] = Dataset.SEA_BLUE;
			} else {
				pixels[i] = NumberUtilities.clamp((int)(data[i] * 255), 0, 255) * 0x010101;
			}
		}

		return image;
	}

	public TestMain(BufferedImage image) {
		setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
		setBackground(Color.BLACK);
		setImage(image);
		setZoom(0);
	}
}
