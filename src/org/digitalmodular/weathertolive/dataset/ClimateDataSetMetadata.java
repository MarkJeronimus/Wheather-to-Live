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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireNonNull;

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

	private final Path                     file;
	private final String                   name;
	private final String                   downloadRoot;
	private final List<ClimateDataSetData> climateDataSetData = new ArrayList<>(10);

	public ClimateDataSetMetadata(Path file) throws IOException {
		this.file = requireNonNull(file, "file");

		if (!Files.exists(file)) {
			throw new IOException("File is missing: " + file.getFileName());
		}

		List<String> lines = Files.readAllLines(file);
		if (lines.size() < 4) {
			throw new IOException("File is corrupt: " + file.getFileName());
		}

		name = parseName(file, lines.get(0));
		downloadRoot = parseDownloadRoot(file, lines.get(1));

		parseTable(file, lines.subList(2, lines.size()));
	}

	public Path getFile() {
		return file;
	}

	public String getName() {
		return name;
	}

	public String getDownloadRoot() {
		return downloadRoot;
	}

	public int getNumMetadata() {
		return climateDataSetData.size();
	}

	public ClimateDataSetData getMetadata(int i) {
		return climateDataSetData.get(i);
	}

	private static String parseName(Path file, String line) throws IOException {
		if (line.isEmpty()) {
			throw new IOException("First line should contain the name: " + file.getFileName());
		}

		return line;
	}

	private static String parseDownloadRoot(Path file, String line) throws IOException {
		if (line.isEmpty()) {
			throw new IOException("Second line should contain the download root URL: " + file.getFileName());
		}

		if (!line.endsWith("/")) {
			line += '/';
		}

		return line;
	}

	private void parseTable(Path file, List<String> lines) throws IOException {
		boolean tableStarted = false;

		for (int i = 0; i < lines.size(); i++) {
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
}
