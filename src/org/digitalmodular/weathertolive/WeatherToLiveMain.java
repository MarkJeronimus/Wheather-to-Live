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
import java.util.concurrent.ExecutionException;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.jidesoft.plaf.LookAndFeelFactory;
import org.digitalmodular.weathertolive.dataset.Dataset;
import org.digitalmodular.weathertolive.dataset.WorldClimDatasetFactory;
import org.digitalmodular.weathertolive.util.ColorGradient;
import org.digitalmodular.weathertolive.util.GraphicsUtilities;

/**
 * @author Mark Jeronimus
 */
// Created 2022-08-30
public final class WeatherToLiveMain {
	public static void main(String... args) throws IOException, ExecutionException, InterruptedException {
		Dataset       dataSet  = WorldClimDatasetFactory.createFor("wc2.1_10m_wind.zip", true);
		ColorGradient gradient = new ColorGradient(new File("Inferno-mod.png"));

//		WeatherToLivePanel panel =
		GraphicsUtilities.getFromEDT(() -> {
			FlatLaf.setup(new FlatDarkLaf());
			LookAndFeelFactory.installJideExtension();

			JFrame f = new JFrame();
			f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

			WeatherToLivePanel weatherToLivePanel = new WeatherToLivePanel();
			f.setContentPane(weatherToLivePanel);

			f.pack();
			f.setLocationRelativeTo(null);
			f.setVisible(true);

			weatherToLivePanel.setDataset(dataSet);
			weatherToLivePanel.setGradient(gradient);
			weatherToLivePanel.rebuildAtlas();

			return weatherToLivePanel;
		});

//		try {
//			Thread.sleep(5000);
//		} catch (InterruptedException e) {
//			throw new RuntimeException(e);
//		}
//		SwingUtilities.invokeLater(() -> {
//			panel.setDataset(null);
//			panel.rebuildAtlas();
//			System.out.println("panel.setDataset(null);");
//		});
	}
}
