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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JPanel;

import org.jetbrains.annotations.Nullable;

import org.digitalmodular.weathertolive.dataset.Dataset;
import org.digitalmodular.weathertolive.util.AnimationFrame;
import org.digitalmodular.weathertolive.util.AnimationZoomPanel;
import org.digitalmodular.weathertolive.util.ColorGradient;
import org.digitalmodular.weathertolive.util.GraphicsUtilities;

/**
 * @author Mark Jeronimus
 */
// Created 2022-08-30
public class WeatherToLivePanel extends JPanel {
	private final AnimationZoomPanel worldPanel  = new AnimationZoomPanel();
	private final BottomPanel        bottomPanel = new BottomPanel(this);

	private @Nullable Dataset       dataset  = null;
	private @Nullable ColorGradient gradient = null;

	@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
	public WeatherToLivePanel() {
		super(new BorderLayout());

		DisplayMode displayMode = GraphicsUtilities.getDisplayMode();
		setPreferredSize(new Dimension(displayMode.getWidth() * 3 / 8, displayMode.getHeight() * 3 / 8));

		add(worldPanel, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
	}

	public Dataset getDataset() {
		return dataset;
	}

	/**
	 * A call of this must be followed by a call to {@link #rebuildAtlas()}!
	 */
	public void setDataset(@Nullable Dataset dataset) {
		this.dataset = dataset;
	}

	public @Nullable ColorGradient getGradient() {
		return gradient;
	}

	/**
	 * A call of this must be followed by a call to {@link #rebuildAtlas()}!
	 */
	public void setGradient(@Nullable ColorGradient gradient) {
		this.gradient = gradient;
	}

	public void rebuildAtlas() {
		if (dataset == null) {
			worldPanel.setAnimation(Collections.emptyList());
			bottomPanel.setAnimatable(false);
		} else {
			List<AnimationFrame> atlasSequence = makeAtlasSequence(dataset);

			worldPanel.setAnimation(atlasSequence);
			worldPanel.zoomFit();
		}

		boolean canAnimate = dataset != null && dataset.getData().length > 1;
		bottomPanel.setAnimatable(canAnimate); // This will percolate to setAnimated()
	}

	private List<AnimationFrame> makeAtlasSequence(Dataset dataset) {
		List<AnimationFrame> atlasSequence = new ArrayList<>(12);

		float[][] monthlyData = dataset.getData();

		for (int month = 0; month < 12; month++) {
			BufferedImage image = new BufferedImage(dataset.getWidth(),
			                                        dataset.getHeight(),
			                                        BufferedImage.TYPE_INT_RGB);
			int[] pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();

			float[] data = monthlyData[month];

			for (int i = 0; i < data.length; i++) {
				if (Float.isNaN(data[i])) {
					pixels[i] = Dataset.SEA_BLUE;
				} else if (gradient != null) {
					pixels[i] = gradient.getColor(data[i]);
				} else {
					pixels[i] = (int)(data[i] * 255);
				}
			}

			atlasSequence.add(new AnimationFrame(image, 1_500_000_000 / 12));
		}

		return atlasSequence;
	}

	public void setFastPreview(boolean fastPreview) {
	}

	public void setAnimated(boolean animated) {
		if (animated) {
			worldPanel.startAnimation();
		} else {
			worldPanel.stopAnimation();
		}
	}
}
