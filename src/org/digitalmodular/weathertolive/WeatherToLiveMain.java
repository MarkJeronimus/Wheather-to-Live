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

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
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
import org.digitalmodular.weathertolive.util.MultiProgressListener;
import org.digitalmodular.weathertolive.util.ProgressEvent;
import org.digitalmodular.weathertolive.util.ProgressListener;
import org.digitalmodular.weathertolive.util.TextProgressListener;

/**
 * @author Mark Jeronimus
 */
// Created 2022-08-30
public final class WeatherToLiveMain {
	public static void main(String... args) throws IOException, ExecutionException, InterruptedException {
		ClimateDataSetMetadata metadata = new ClimateDataSetMetadata(Paths.get("config-worldclim-2.1-30s.tsv"));

		ClimateDataSetDownloader.download(metadata, new MultiProgressListener() {
			private final ProgressListener listener = new TextProgressListener(System.out, 4000);

			@Override
			public void multiProgressUpdated(int progressBarIndex, ProgressEvent evt) {
				listener.progressUpdated(evt);
			}
		});

		ClimateDataSet climateDataSet = ClimateDataSetLoader.load(metadata, new MultiProgressListener() {
			private final ProgressListener listener = new TextProgressListener(System.out, 4000);

			@Override
			public void multiProgressUpdated(int progressBarIndex, ProgressEvent evt) {
				listener.progressUpdated(evt);
			}
		});

		SwingUtilities.invokeLater(() -> {
			FlatLaf.setup(new FlatDarkLaf());
			LookAndFeelFactory.installJideExtension();

			JFrame f = new JFrame();
			f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

			WeatherToLivePanel weatherToLivePanel = new WeatherToLivePanel();
			f.setContentPane(weatherToLivePanel);

			f.pack();
			f.setLocationRelativeTo(null);
			f.setVisible(true);

			weatherToLivePanel.setClimateDataSet(climateDataSet);
			weatherToLivePanel.dataChanged(-1);
		});
	}
}
