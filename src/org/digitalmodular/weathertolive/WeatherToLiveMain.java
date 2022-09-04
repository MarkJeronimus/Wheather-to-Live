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

import java.awt.Frame;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.jidesoft.plaf.LookAndFeelFactory;
import org.digitalmodular.weathertolive.dataset.ClimateDataSet;
import org.digitalmodular.weathertolive.dataset.ClimateDataSetDownloader;
import org.digitalmodular.weathertolive.dataset.ClimateDataSetLoader;
import org.digitalmodular.weathertolive.dataset.ClimateDataSetMetadata;
import org.digitalmodular.weathertolive.util.GraphicsUtilities;
import org.digitalmodular.weathertolive.util.MultiProgressDialog;

/**
 * @author Mark Jeronimus
 */
// Created 2022-08-30
public final class WeatherToLiveMain {
	public static void main(String... args) throws IOException, ExecutionException, InterruptedException {
		WeatherToLivePanel weatherToLivePanel = GraphicsUtilities.getFromEDT(() -> {
			FlatLaf.setup(new FlatDarkLaf());
			LookAndFeelFactory.installJideExtension();

			JFrame f = new JFrame("Weather to Live");
			f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

			WeatherToLivePanel panel = new WeatherToLivePanel();
			f.setContentPane(panel);

			f.pack();
			f.setLocationRelativeTo(null);
			f.setVisible(true);

			return panel;
		});

		assert weatherToLivePanel != null;

		Path           file           = Paths.get("config-worldclim-2.1-10min.tsv");
		ClimateDataSet climateDataSet = loadClimateDataSet(weatherToLivePanel, file);

		SwingUtilities.invokeLater(() -> {
			weatherToLivePanel.setClimateDataSet(climateDataSet);
			weatherToLivePanel.dataChanged(0);
		});
	}

	private static ClimateDataSet loadClimateDataSet(JComponent parent, Path file)
			throws IOException, InterruptedException {
		ClimateDataSetMetadata metadata = new ClimateDataSetMetadata(file);

		Frame frame = (Frame)parent.getTopLevelAncestor();

		MultiProgressDialog progressListener = new MultiProgressDialog(frame, frame.getTitle(), 2);
		progressListener.setAutoShow(true);
		progressListener.setAutoClose(true);

		progressListener.setTaskName("Downloading " + metadata.getName());
		ClimateDataSetDownloader.download(metadata, progressListener);

		progressListener.setTaskName("Loading " + metadata.getName());
		ClimateDataSetLoader climateDataSetLoader = new ClimateDataSetLoader();
		progressListener.addCancelListener(ignored -> {
			climateDataSetLoader.cancel();
			progressListener.setVisible(false);
		});
		ClimateDataSet climateDataSet = climateDataSetLoader.load(metadata, progressListener);

		return climateDataSet;
	}
}
