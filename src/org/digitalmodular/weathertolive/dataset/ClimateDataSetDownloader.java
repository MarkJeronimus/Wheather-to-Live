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
package org.digitalmodular.weathertolive.dataset;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.digitalmodular.weathertolive.util.HTTPDownloader;
import org.digitalmodular.weathertolive.util.MultiProgressListener;
import org.digitalmodular.weathertolive.util.ProgressEvent;
import org.digitalmodular.weathertolive.util.ProgressListener;

/**
 * @author Mark Jeronimus
 */
// Created 2022-09-03
public final class ClimateDataSetDownloader {
	private ClimateDataSetDownloader() {
		throw new AssertionError();
	}

	public static void download(ClimateDataSetMetadata metadata, MultiProgressListener progressListener)
			throws IOException {
		long t = System.nanoTime();

		int              numDataSets              = metadata.getNumMetadata();
		ProgressListener downloadProgressListener = progressListener.wrapAsSingleProgressListener(1);

		for (int i = 0; i < numDataSets; i++) {
			String filename = metadata.getMetadata(i).filename;

			URL  url  = new URL(metadata.getDownloadRoot() + filename);
			Path file = Paths.get(filename);

			progressListener.multiProgressUpdated(0, new ProgressEvent(metadata, i, numDataSets, filename));

			if (Files.exists(file)) {
				progressListener.multiProgressUpdated(1, new ProgressEvent(metadata, 1, -1, filename));
			} else {
				HTTPDownloader httpDownloader = new HTTPDownloader();
				httpDownloader.addProgressListener(downloadProgressListener);
				httpDownloader.downloadToFile(url, null, file);
			}
		}

		progressListener.multiProgressUpdated(0, new ProgressEvent(metadata, numDataSets, numDataSets, ""));

		System.out.println("Downloading took " + (System.nanoTime() - t) / 1.0e9f + " s");
	}
}
