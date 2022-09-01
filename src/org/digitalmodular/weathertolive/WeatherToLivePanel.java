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
import java.util.List;
import javax.swing.JPanel;

import org.jetbrains.annotations.Nullable;

import org.digitalmodular.weathertolive.dataset.ClimateDataSet;
import org.digitalmodular.weathertolive.dataset.DataSet;
import org.digitalmodular.weathertolive.util.AnimationFrame;
import org.digitalmodular.weathertolive.util.AnimationZoomPanel;
import org.digitalmodular.weathertolive.util.ColorGradient;
import org.digitalmodular.weathertolive.util.GraphicsUtilities;
import org.digitalmodular.weathertolive.util.RangeF;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireNonNull;

/**
 * @author Mark Jeronimus
 */
// Created 2022-08-30
public class WeatherToLivePanel extends JPanel {
	private final AnimationZoomPanel worldPanel  = new AnimationZoomPanel();
	private final BottomPanel        bottomPanel = new BottomPanel(this);

	private @Nullable ClimateDataSet climateDataSet = null;
	private @Nullable ColorGradient  gradient       = null;

	@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
	public WeatherToLivePanel() {
		super(new BorderLayout());

		DisplayMode displayMode = GraphicsUtilities.getDisplayMode();
		setPreferredSize(new Dimension(displayMode.getWidth() * 3 / 8, displayMode.getHeight() * 3 / 8));

		add(worldPanel, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);

		worldPanel.addAnimationListener(bottomPanel::setMonth);
	}

	public @Nullable ClimateDataSet getClimateDataSet() {
		return climateDataSet;
	}

	/**
	 * A call of this must eventually be followed by a call to {@link #dataChanged()}!
	 */
	public void setClimateDataSet(ClimateDataSet climateDataSet) {
		this.climateDataSet = requireNonNull(climateDataSet, "climateDataSet");
	}

	public @Nullable ColorGradient getGradient() {
		return gradient;
	}

	/**
	 * A call of this must eventually be followed by a call to {@link #dataChanged()}!
	 */
	public void setGradient(@Nullable ColorGradient gradient) {
		this.gradient = gradient;
	}

	public void dataChanged() {
		if (climateDataSet == null) {
			throw new IllegalStateException("setClimateDataSet() has not been called");
		}

		rebuildAtlas();
		rebuildFilterPanel();
	}

	private void rebuildAtlas() {
		assert climateDataSet != null;

		List<AnimationFrame> atlasSequence = makeAtlasSequence(climateDataSet.getDataSets().get(0).getDataSet());

		worldPanel.setAnimation(atlasSequence);
		worldPanel.zoomFit();

		boolean canAnimate = climateDataSet != null;
		bottomPanel.setAnimatable(canAnimate); // This will percolate to setAnimated()
		setMonth(0);
	}

	private List<AnimationFrame> makeAtlasSequence(DataSet dataset) {
		List<AnimationFrame> atlasSequence = new ArrayList<>(12);

		float[][] rawData = dataset.getRawData();
		RangeF    minMax  = dataset.getMinMax();
		int       length  = rawData[0].length;

		for (int month = 0; month < 12; month++) {
			BufferedImage image = new BufferedImage(dataset.getWidth(),
			                                        dataset.getHeight(),
			                                        BufferedImage.TYPE_INT_RGB);
			int[] pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();

			float[] rawMonthData = rawData[month];

			for (int i = 0; i < length; i++) {
				float value = minMax.unLerp(rawMonthData[i]);

				if (Float.isNaN(value)) {
					pixels[i] = DataSet.SEA_BLUE;
				} else if (gradient != null) {
					pixels[i] = gradient.getColor(value);
				} else {
					pixels[i] = (int)(value * 255);
				}
			}

			atlasSequence.add(new AnimationFrame(image, 1_500_000_000 / 12));
		}

		return atlasSequence;
	}

	private void rebuildFilterPanel() {
		assert climateDataSet != null;

		bottomPanel.prepareFilters(climateDataSet);
	}

	public void setFastPreview(boolean fastPreview) {
	}

	public void setAnimated(boolean animated) {
		if (animated) {
			worldPanel.startAnimation();
		} else {
			worldPanel.stopAnimation();
		}

		bottomPanel.setAnimated(animated);
	}

	public void setMonth(int month) {
		worldPanel.setAnimationFrame(month);
		bottomPanel.setMonth(month);
	}
}
