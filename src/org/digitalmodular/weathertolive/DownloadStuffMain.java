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
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.digitalmodular.weathertolive.util.HTTPDownloader;
import org.digitalmodular.weathertolive.util.TextProgressListener;

/**
 * @author Mark Jeronimus
 */
// Created 2022-08-26
public class DownloadStuffMain {
	public static void main(String... args) throws IOException {
		downloadDataSet("wc2.1_10m_tmin.zip");
//		downloadDataSet("wc2.1_10m_tmax.zip");
		downloadDataSet("wc2.1_10m_tavg.zip");
//		downloadDataSet("wc2.1_10m_prec.zip");
		downloadDataSet("wc2.1_10m_srad.zip");
//		downloadDataSet("wc2.1_10m_wind.zip");
	}

	private static void downloadDataSet(String filename) throws IOException {
		URL  url  = new URL("https://biogeo.ucdavis.edu/data/worldclim/v2.1/base/" + filename);
		Path file = Paths.get(filename);

		HTTPDownloader httpDownloader = new HTTPDownloader();
		httpDownloader.addProgressListener(new TextProgressListener(System.out, 4000));
		httpDownloader.downloadToFile(url, null, file);
	}
}
