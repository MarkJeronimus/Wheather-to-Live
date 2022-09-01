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

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

import org.jetbrains.annotations.Nullable;

/**
 * @author Mark Jeronimus
 */
// Created 2022-08-28
public final class WorldClimDatasetFactory {
	private WorldClimDatasetFactory() {
		throw new AssertionError();
	}

	/**
	 * @param absoluteZero Whether the values start at 0 or can go negative (should find minimum)
	 */
	public static DataSet createFor(String filename, boolean absoluteZero) throws IOException {
		String prefix = filename.substring(0, 14); // e.g. "wc2.1_10m_prec"

		try (ZipFile zip = new ZipFile(new File(filename))) {
			float[][] rawData = new float[12][];

			int width  = 0;
			int height = 0;

			for (int month = 0; month < 12; month++) {
				String count = Integer.toString(month + 1);
				String pad   = "0".repeat(2 - count.length());

				String             tifFilename = prefix + '_' + pad + count + ".tif";
				@Nullable ZipEntry zipEntry    = zip.getEntry(tifFilename);
				if (zipEntry == null) {
					throw new IOException(tifFilename + " not found in " + filename);
				}

				InputStream   inputStream = zip.getInputStream(zipEntry);
				BufferedImage geoData     = ImageIO.read(inputStream);
				width = geoData.getWidth();
				height = geoData.getHeight();

				rawData[month] = convertGeoTiffToRawData(geoData);
			}

			String name = prefix.substring(prefix.length() - 4);
			return new DataSet(name, rawData, width, height, absoluteZero);
		}
	}

	private static float[] convertGeoTiffToRawData(BufferedImage geoData) {
		DataBuffer dataBuffer = geoData.getRaster().getDataBuffer();

		if (dataBuffer instanceof DataBufferFloat) {
			return fromFloatDataSet((DataBufferFloat)dataBuffer);
		} else if (dataBuffer instanceof DataBufferShort) {
			return fromShortDataSet((DataBufferShort)dataBuffer);
		} else if (dataBuffer instanceof DataBufferUShort) {
			return fromUShortDataSet((DataBufferUShort)dataBuffer);
		} else {
			throw new UnsupportedOperationException("unimplemented TIFF dataBuffer format: " +
			                                        dataBuffer.getClass().getSimpleName());
		}
	}

	private static float[] fromFloatDataSet(DataBufferFloat dataBuffer) {
		float[] floats  = dataBuffer.getData();
		float[] rawData = new float[floats.length];

		for (int i = 0; i < rawData.length; i++) {
			if (floats[i] < -1.0e+5f) {
				rawData[i] = Float.NaN;
			} else {
				rawData[i] = floats[i];
			}
		}

		return rawData;
	}

	private static float[] fromUShortDataSet(DataBufferUShort dataBuffer) {
		short[] shorts  = dataBuffer.getData();
		float[] rawData = new float[shorts.length];

		for (int i = 0; i < rawData.length; i++) {
			if (shorts[i] == -1) {
				rawData[i] = Float.NaN;
			} else {
				rawData[i] = shorts[i] & 0xFFFF;
			}
		}

		return rawData;
	}

	private static float[] fromShortDataSet(DataBufferShort dataBuffer) {
		short[] shorts  = dataBuffer.getData();
		float[] rawData = new float[shorts.length];

		for (int i = 0; i < rawData.length; i++) {
			if (shorts[i] == -32768) {
				rawData[i] = Float.NaN;
			} else {
				rawData[i] = shorts[i];
			}
		}

		return rawData;
	}
}
