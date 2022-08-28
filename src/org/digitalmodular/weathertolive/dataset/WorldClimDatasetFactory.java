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
package org.digitalmodular.weathertolive.dataset;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

/**
 * @author Mark Jeronimus
 */
// Created 2022-08-28
public final class WorldClimDatasetFactory {
	private WorldClimDatasetFactory() {
		throw new AssertionError();
	}

	public static Dataset createFor(String filename) throws IOException {
		try (ZipFile zip = new ZipFile(new File(filename))) {
			InputStream   inputStream = zip.getInputStream(zip.getEntry("wc2.1_10m_tmax_01.tif"));
			BufferedImage geoData     = ImageIO.read(inputStream);

			return convertGeoDataToImage(geoData);
		}
	}

	private static Dataset convertGeoDataToImage(BufferedImage geoData) {
		DataBuffer dataBuffer = geoData.getRaster().getDataBuffer();

		Dataset dataset = new Dataset(geoData.getWidth(), geoData.getHeight());

		if (dataBuffer instanceof DataBufferFloat) {
			fromFloatDataSet((DataBufferFloat)dataBuffer, dataset.rawData);
		} else if (dataBuffer instanceof DataBufferShort) {
			fromShortDataSet((DataBufferShort)dataBuffer, dataset.rawData);
		} else if (dataBuffer instanceof DataBufferUShort) {
			fromUShortDataSet((DataBufferUShort)dataBuffer, dataset.rawData);
		}

		return dataset;
	}

	private static void fromFloatDataSet(DataBufferFloat dataBuffer, float[] rawData) {
		float[] floats = dataBuffer.getData();

		for (int i = 0; i < rawData.length; i++) {
			if (floats[i] < 1.0e-10f) {
				rawData[i] = Float.NaN;
			} else {
				rawData[i] = floats[i];
			}
		}
	}

	private static void fromUShortDataSet(DataBufferUShort dataBuffer, float[] rawData) {
		short[] shorts = dataBuffer.getData();

		for (int i = 0; i < rawData.length; i++) {
			if (shorts[i] == -1) {
				rawData[i] = Float.NaN;
			} else {
				rawData[i] = (shorts[i] & 0xFFFF) / 256.0f;
			}
		}
	}

	private static void fromShortDataSet(DataBufferShort dataBuffer, float[] rawData) {
		short[] shorts = dataBuffer.getData();

		for (int i = 0; i < rawData.length; i++) {
			if (shorts[i] == -32768) {
				rawData[i] = Float.NaN;
			} else {
				rawData[i] = (shorts[i]) / 2.0f;
			}
		}
	}
}
