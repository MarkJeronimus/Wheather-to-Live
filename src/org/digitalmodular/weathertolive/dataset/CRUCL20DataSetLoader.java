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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.digitalmodular.weathertolive.util.ProgressEvent;
import org.digitalmodular.weathertolive.util.ProgressListener;
import static org.digitalmodular.weathertolive.dataset.ClimateDataSetMetadata.ClimateDataSetData;

/**
 * @author Mark Jeronimus
 */
// Created 2022-09-03
public final class CRUCL20DataSetLoader {
	public static final int PIXELS_PER_DEGREE = 6;
	public static final int WIDTH             = 360 * PIXELS_PER_DEGREE;
	public static final int HEIGHT            = 180 * PIXELS_PER_DEGREE;
	public static final int CRU_CL_TOTAL      = 566669;

	private static final Pattern SPACES_PATTERN = Pattern.compile(" +");

	private final AtomicBoolean cancelRequested = new AtomicBoolean();

	public FilterDataSet load(ClimateDataSetData setMetadata, ProgressListener progressListener)
			throws IOException, InterruptedException {
		cancelRequested.set(false);

		String filename = setMetadata.filename;

		progressListener.progressUpdated(new ProgressEvent(setMetadata, 1, -1, ""));

		float[][] rawData = new float[12][WIDTH * HEIGHT];
		for (int month = 0; month < 12; month++) {
			Arrays.fill(rawData[month], Float.NaN);
		}

		try (InputStream stream = Files.newInputStream(Paths.get(filename));
		     GZIPInputStream gzIn = new GZIPInputStream(stream)) {
			BufferedReader in = new BufferedReader(new InputStreamReader(gzIn, StandardCharsets.ISO_8859_1));

			int i = 0;
			while (true) {
				String line = in.readLine();
				if (line == null) {
					break;
				}

				if (cancelRequested.get()) {
					throw new InterruptedException("Canceled");
				}

				if ((i & 0xFFF) == 0xFFF) {
					progressListener.progressUpdated(new ProgressEvent(setMetadata, i, CRU_CL_TOTAL, ""));
				}

				String[] fields = SPACES_PATTERN.split(line.trim());
				if (fields.length != 3 && fields.length != 14 && fields.length != 26) {
					continue;
				}

				parseLine(fields, rawData);
				i++;
			}

			progressListener.progressUpdated(new ProgressEvent(setMetadata, CRU_CL_TOTAL, CRU_CL_TOTAL, ""));
		}

		DataSet dataSet = new DataSet(setMetadata.dataSetName,
		                              rawData,
		                              WIDTH,
		                              HEIGHT,
		                              setMetadata.absoluteZero,
		                              setMetadata.gamma,
		                              setMetadata.gradientFilename);
		return new FilterDataSet(dataSet);
	}

	public void cancel() {
		cancelRequested.set(true);
	}

	private static void parseLine(String[] fields, float[][] rawData) {
		float lat = Float.parseFloat(fields[0]);
		float lon = Float.parseFloat(fields[1]);

		int x = (int)((lon + 180) * PIXELS_PER_DEGREE);
		int y = (int)((90 - lat) * PIXELS_PER_DEGREE);
		int p = x + WIDTH * y;

		for (int month = 0; month < 12; month++) {
			String field = fields.length == 3 ? fields[2] : fields[month + 2];
			rawData[month][p] = Float.parseFloat(field);
		}
	}
}
