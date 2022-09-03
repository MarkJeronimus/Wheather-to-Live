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
package org.digitalmodular.weathertolive.dataset;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.digitalmodular.weathertolive.util.HTTPDownloader;
import org.digitalmodular.weathertolive.util.MultiProgressListener;
import org.digitalmodular.weathertolive.util.ProgressEvent;
import org.digitalmodular.weathertolive.util.ProgressListener;

/**
 * @author Mark Jeronimus
 */
// Created 2022-09-03
public class ClimateDataSetMetadata {
	/**
	 * @author Mark Jeronimus
	 */
	// Created 2022-09-03
	@SuppressWarnings("PublicField")
	public static class ClimateDataSetData {
		public String  filename;
		public String  dataSetName;
		public boolean absoluteZero;
		public int     gamma;
		public String  gradientFilename;

		public ClimateDataSetData(String filename,
		                          String dataSetName,
		                          boolean absoluteZero,
		                          int gamma,
		                          String gradientFilename) {
			this.filename = filename;
			this.dataSetName = dataSetName;
			this.absoluteZero = absoluteZero;
			this.gamma = gamma;
			this.gradientFilename = gradientFilename;
		}
	}

	private final String                   downloadRoot;
	private final List<ClimateDataSetData> climateDataSetData = new ArrayList<>(10);

	public ClimateDataSetMetadata(Path file) throws IOException {
		if (!Files.exists(file)) {
			throw new IOException("File is missing: " + file.getFileName());
		}

		List<String> lines = Files.readAllLines(file);
		if (lines.isEmpty()) {
			throw new IOException("File is empty: " + file.getFileName());
		}

		downloadRoot = parseDownloadRoot(file, lines);

		parseTable(file, lines);
	}

	private static String parseDownloadRoot(Path file, List<String> lines) throws IOException {
		String downloadRoot = lines.get(0);
		if (downloadRoot.isEmpty()) {
			throw new IOException("First line should contain the download root URL: " + file.getFileName());
		}

		if (!downloadRoot.endsWith("/")) {
			downloadRoot += '/';
		}

		return downloadRoot;
	}

	private void parseTable(Path file, List<String> lines) throws IOException {
		boolean tableStarted = false;

		for (int i = 1; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.startsWith("filename")) {
				tableStarted = true;
				continue;
			}

			if (tableStarted) {
				parseTableLine(file, i, line);
			}
		}
	}

	private void parseTableLine(Path file, int lineNr, String line) throws IOException {
		String[] fields = line.split("\t");
		if (fields.length != 5) {
			throw new IOException("Expected 5 fields on line " + (lineNr + 1) + " of " + file.getFileName());
		}

		for (int i = 0; i < fields.length; i++) {
			fields[i] = fields[i].trim();
		}

		String  filename         = ioParseString(file, lineNr, fields, 0);
		String  dataSetName      = ioParseString(file, lineNr, fields, 1);
		boolean absoluteZero     = ioParseBoolean(file, lineNr, fields, 2);
		int     gamma            = ioParseGamma(file, lineNr, fields, 3);
		String  gradientFilename = ioParseString(file, lineNr, fields, 4);

		ClimateDataSetData data = new ClimateDataSetData(filename, dataSetName, absoluteZero, gamma, gradientFilename);
		climateDataSetData.add(data);
	}

	private static String ioParseString(Path file, int lineNr, String[] fields, int index)
			throws IOException {
		if (!fields[index].isEmpty()) {
			return fields[index];
		}

		throw new IOException("Expected a string in field " + (index + 1) +
		                      " on line " + (lineNr + 1) + " of " + file.getFileName());
	}

	private static boolean ioParseBoolean(Path file, int lineNr, String[] fields, int index)
			throws IOException {
		if (!fields[index].isEmpty()) {
			try {
				return Boolean.parseBoolean(fields[index]);
			} catch (NumberFormatException ignored) {
			}
		}

		throw new IOException("Expected a positive integer in field " + (index + 1) +
		                      " on line " + (lineNr + 1) + " of " + file.getFileName());
	}

	private static int ioParseGamma(Path file, int lineNr, String[] fields, int index)
			throws IOException {
		if (!fields[index].isEmpty()) {
			try {
				int value = Integer.parseInt(fields[index]);
				if (value > 0) {
					return value;
				}
			} catch (NumberFormatException ignored) {
			}
		}

		throw new IOException("Expected a positive integer in field " + (index + 1) +
		                      " on line " + (lineNr + 1) + " of " + file.getFileName());
	}

	public void download(MultiProgressListener progressListener) throws IOException {
		int              numDataSets              = climateDataSetData.size();
		ProgressListener downloadProgressListener = progressListener.wrapAsSingleProgressListener(1);

		for (int i = 0; i < numDataSets; i++) {
			String filename = climateDataSetData.get(i).filename;
			URL    url      = new URL(downloadRoot + filename);
			Path   file     = Paths.get(filename);

			progressListener.multiProgressUpdated(0, new ProgressEvent(this, i, numDataSets, filename));

			HTTPDownloader httpDownloader = new HTTPDownloader();
			httpDownloader.addProgressListener(downloadProgressListener);
			httpDownloader.downloadToFile(url, null, file);
		}

		progressListener.multiProgressUpdated(0, new ProgressEvent(this, numDataSets, numDataSets, ""));
	}
}
