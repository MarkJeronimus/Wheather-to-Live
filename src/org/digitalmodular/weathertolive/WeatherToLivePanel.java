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
import java.awt.GraphicsDevice;
import javax.swing.JPanel;

import org.jetbrains.annotations.Nullable;

import org.digitalmodular.weathertolive.dataset.ClimateDataSet;
import org.digitalmodular.weathertolive.util.Animator;
import org.digitalmodular.weathertolive.util.ColorGradient;
import org.digitalmodular.weathertolive.util.GraphicsUtilities;
import org.digitalmodular.weathertolive.util.ZoomPanel;

/**
 * @author Mark Jeronimus
 */
// Created 2022-08-30
public class WeatherToLivePanel extends JPanel {
	public static final int SCALE_FACTOR;

	static {
		GraphicsDevice gd             = GraphicsUtilities.getDisplayDevice();
		int            realWidth      = gd.getDisplayMode().getWidth();
		float          effectiveWidth = gd.getDefaultConfiguration().getBounds().width;
		SCALE_FACTOR = Math.round(realWidth / effectiveWidth);
	}

	private final ZoomPanel   worldPanel  = new ZoomPanel();
	@SuppressWarnings("ThisEscapedInObjectConstruction")
	private final BottomPanel bottomPanel = new BottomPanel(this);

	private final Animator animator = new Animator(worldPanel::setImage);

	private final AtlasRenderer atlasRenderer = new AtlasRenderer(animator::setAnimation);

	@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
	public WeatherToLivePanel() {
		super(new BorderLayout());

		DisplayMode displayMode = GraphicsUtilities.getDisplayMode();
		setPreferredSize(new Dimension(displayMode.getWidth() * 3 / 8, displayMode.getHeight() * 3 / 8));

		add(worldPanel, BorderLayout.CENTER);

		bottomPanel.setParameterChangedCallback(this::dataChanged);
		add(bottomPanel, BorderLayout.SOUTH);

		animator.addAnimationListener(bottomPanel::setMonth);
	}

	/**
	 * A call of this must eventually be followed by a call to {@link #dataChanged(int)}!
	 */
	public void setClimateDataSet(ClimateDataSet climateDataSet) {
		if (climateDataSet == null) {
			throw new IllegalStateException("setClimateDataSet() has not been called");
		}

		atlasRenderer.setFilteredDataSets(climateDataSet.getDataSets());
		atlasRenderer.setBackgroundDatasetIndex(0);
		bottomPanel.prepareFilters(climateDataSet);

		setMonth(0);
	}

	/**
	 * A call of this must eventually be followed by a call to {@link #dataChanged(int)}!
	 */
	public void setGradient(@Nullable ColorGradient gradient) {
		atlasRenderer.setGradient(gradient);
	}

	public void dataChanged(int dataSetIndex) {
		long t = System.nanoTime();

		atlasRenderer.setBackgroundDatasetIndex(dataSetIndex);
		atlasRenderer.dataChanged();
		bottomPanel.dataChanged();

		System.out.println("Calculation took " + (System.nanoTime() - t) / 1.0e6f + " ms");
	}

	public void setFastPreview(boolean fastPreview) {
	}

	public void setAnimated(boolean animated) {
		if (animated) {
			animator.startAnimation();
		} else {
			animator.stopAnimation();
		}

		bottomPanel.setAnimated(animated);
	}

	public void setMonth(int month) {
		animator.setAnimationFrame(month);
		bottomPanel.setMonth(month);
		atlasRenderer.setCurrentMonth(month);
	}

	public void setAggregateYear(boolean aggregateYear) {
		atlasRenderer.setAggregateYear(aggregateYear);
		atlasRenderer.dataChanged();
	}
}
