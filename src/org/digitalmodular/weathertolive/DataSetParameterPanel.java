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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;

import org.jetbrains.annotations.Nullable;

import com.jidesoft.swing.RangeSlider;
import org.digitalmodular.weathertolive.dataset.FilteredDataSet;
import org.digitalmodular.weathertolive.util.AnimationFrame;
import org.digitalmodular.weathertolive.util.Animator;
import org.digitalmodular.weathertolive.util.ImagePanel;
import org.digitalmodular.weathertolive.util.PreferredNumbers;
import org.digitalmodular.weathertolive.util.RangeF;
import static org.digitalmodular.weathertolive.WeatherToLivePanel.SCALE_FACTOR;
import static org.digitalmodular.weathertolive.dataset.DataSet.FILTER_HIGHLIGHT;
import static org.digitalmodular.weathertolive.dataset.DataSet.LAND_GREEN;
import static org.digitalmodular.weathertolive.dataset.DataSet.SEA_BLUE;
import static org.digitalmodular.weathertolive.dataset.DataSet.THUMBNAIL_HEIGHT;
import static org.digitalmodular.weathertolive.dataset.DataSet.THUMBNAIL_WIDTH;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireNonNull;

/**
 * @author Mark Jeronimus
 */
// Created 2022-08-30
public class DataSetParameterPanel extends JPanel {
	private static final int              MIN_SLIDER_STEPS = 100;
	private static final PreferredNumbers STEP_QUANTIZER   = new PreferredNumbers(10, 100, 125, 150, 200, 500);

	private final FilteredDataSet filteredDataSet;

	private final DecimalFormat numberFormat;

	private final ImagePanel thumbnailPanel = new ImagePanel(null, true);
	private final JLabel     beginLabel     = new JLabel();
	private final JSlider    slider         = new RangeSlider();
	private final JLabel     endLabel       = new JLabel();

	private final Animator animator = new Animator(thumbnailPanel::setImage);

	private final float sliderStepSize;

	@SuppressWarnings("FieldHasSetterButNoGetter")
	private @Nullable Runnable parameterChangedCallback = null;

	@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
	public DataSetParameterPanel(FilteredDataSet filteredDataSet) {
		super(new BorderLayout());
		this.filteredDataSet = requireNonNull(filteredDataSet, "filteredDataSet");

		RangeF minMax        = filteredDataSet.getDataSet().getMinMax();
		int    quantizerStep = calculateQuantizerStep(minMax);
		sliderStepSize = (float)STEP_QUANTIZER.exp(quantizerStep);
		numberFormat = makeNumberFormat(quantizerStep);
		prepareLabelWidths(minMax);
		prepareSliderRange(minMax);

		{
			JLabel nameLabel = new JLabel(filteredDataSet.getDataSet().getName() + ' ');
			add(nameLabel, BorderLayout.LINE_START);
		}
		{
			thumbnailPanel.setPreferredSize(new Dimension(THUMBNAIL_WIDTH / SCALE_FACTOR,
			                                              THUMBNAIL_HEIGHT / SCALE_FACTOR));
			thumbnailPanel.setBackground(Color.BLACK);
			add(thumbnailPanel, BorderLayout.CENTER);
		}
		{
			JPanel p = new JPanel(new BorderLayout());

			p.add(beginLabel, BorderLayout.LINE_START);
			{
				slider.setMajorTickSpacing(10000);
				slider.setMinorTickSpacing(10);
				slider.setPaintTicks(true);
				slider.setPaintLabels(true);
				slider.addChangeListener(this::valueChanged);

				int size = slider.getFont().getSize();
				slider.setFont(slider.getFont().deriveFont(size * 0.8f));

				p.add(slider, BorderLayout.CENTER);
			}
			p.add(endLabel, BorderLayout.LINE_END);

			add(p, BorderLayout.SOUTH);
		}

		valueChanged(null); // Initialize real label values
	}

	private static int calculateQuantizerStep(RangeF minMax) {
		float span          = minMax.getSpan();
		float stepSize      = span / MIN_SLIDER_STEPS;
		int   quantizerStep = STEP_QUANTIZER.log(stepSize);
		return quantizerStep;
	}

	private static DecimalFormat makeNumberFormat(int quantizerStep) {
		int numDecimals = -2 - (int)Math.floor(quantizerStep / (float)STEP_QUANTIZER.getNumValuesPerBase());
		if (numDecimals <= 0) {
			return new DecimalFormat("0");
		} else {
			String pattern = "0." + "0".repeat(numDecimals);
			return new DecimalFormat(pattern);
		}
	}

	private void prepareLabelWidths(RangeF minMax) {
		updateLabels(minMax);
		int width = beginLabel.getMinimumSize().width;
		width = Math.max(width, endLabel.getMinimumSize().width);

		beginLabel.setPreferredSize(new Dimension(width, beginLabel.getMinimumSize().height));
		endLabel.setPreferredSize(new Dimension(width, endLabel.getMinimumSize().height));
	}

	private void prepareSliderRange(RangeF minMax) {
		int min = (int)Math.floor(minMax.getBegin() / sliderStepSize);
		int max = (int)Math.ceil(minMax.getEnd() / sliderStepSize);
		slider.getModel().setRangeProperties(min, max - min, min, max, false);
	}

	// Slider listener
	private void valueChanged(@Nullable ChangeEvent e) {
		RangeF minMax = getMinMax();

		updateLabels(minMax);

		filteredDataSet.setFilterMinMax(minMax);

		if (parameterChangedCallback != null) {
			parameterChangedCallback.run();
		}
	}

	public RangeF getMinMax() {
		int begin = slider.getValue();
		int end   = begin + slider.getExtent();
		return RangeF.of(begin * sliderStepSize, end * sliderStepSize);
	}

	private void updateLabels(RangeF minMax) {
		beginLabel.setText(numberFormat.format(minMax.getBegin()));
		endLabel.setText(numberFormat.format(minMax.getEnd()));
	}

	public void setMonth(int month) {
		animator.setAnimationFrame(month);
	}

	public void setParameterChangedCallback(@Nullable Runnable parameterChangedCallback) {
		this.parameterChangedCallback = parameterChangedCallback;
	}

	/**
	 * Called from outside, to update the inside.
	 */
	public void dataChanged() {
		updateThumbnail();
	}

	public void updateThumbnail() {
		List<AnimationFrame> thumbnailSequence = new ArrayList<>(12);

		float[][] thumbnails = filteredDataSet.getFilteredThumbnails();
		int       length     = thumbnails[0].length;

		for (int month = 0; month < 12; month++) {
			BufferedImage image = new BufferedImage(THUMBNAIL_WIDTH,
			                                        THUMBNAIL_HEIGHT,
			                                        BufferedImage.TYPE_INT_RGB);
			int[] pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();

			float[] monthThumbnail = thumbnails[month];

			for (int i = 0; i < length; i++) {
				float thumbnailPixel = monthThumbnail[i];

				if (Float.isNaN(thumbnailPixel)) {
					pixels[i] = SEA_BLUE;
				} else if (thumbnailPixel > 0.0f) {
					pixels[i] = FILTER_HIGHLIGHT;
				} else {
					pixels[i] = LAND_GREEN;
				}
			}

			thumbnailSequence.add(new AnimationFrame(image, 1_500_000_000 / 12));
		}

		animator.setAnimation(thumbnailSequence);
	}
}
