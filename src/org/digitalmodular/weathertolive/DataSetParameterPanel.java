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
import java.util.function.Consumer;
import java.util.function.DoubleUnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;

import org.jetbrains.annotations.Nullable;

import com.jidesoft.swing.RangeSlider;
import org.digitalmodular.weathertolive.dataset.FilterDataSet;
import org.digitalmodular.weathertolive.util.AnimationFrame;
import org.digitalmodular.weathertolive.util.Animator;
import org.digitalmodular.weathertolive.util.ImagePanel;
import org.digitalmodular.weathertolive.util.NumberUtilities;
import org.digitalmodular.weathertolive.util.PreferredNumbers;
import org.digitalmodular.weathertolive.util.RangeF;
import static org.digitalmodular.weathertolive.WeatherToLivePanel.SCALE_FACTOR;
import static org.digitalmodular.weathertolive.dataset.DataSet.FILTER_HIGHLIGHT;
import static org.digitalmodular.weathertolive.dataset.DataSet.LAND_GREEN;
import static org.digitalmodular.weathertolive.dataset.DataSet.SEA_BLUE;
import static org.digitalmodular.weathertolive.dataset.DataSet.THUMBNAIL_HEIGHT;
import static org.digitalmodular.weathertolive.dataset.DataSet.THUMBNAIL_WIDTH;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireAtLeast;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireNonNull;

/**
 * @author Mark Jeronimus
 */
// Created 2022-08-30
public class DataSetParameterPanel extends JPanel {
	private static final int              MIN_SLIDER_STEPS = 60;
	private static final PreferredNumbers STEP_QUANTIZER   = new PreferredNumbers(10, 100, 125, 150, 200, 500);

	private static final Pattern EXTRACT_UNIT_PATTERN = Pattern.compile("^(.+) \\((.+)\\)$");

	private final FilterDataSet filterDataSet;
	private final int           dataSetIndex;

	private final DecimalFormat numberFormat;

	private final JLabel     nameLabel      = new JLabel();
	private final ImagePanel thumbnailPanel = new ImagePanel(null, true);
	private final JLabel     beginLabel     = new JLabel();
	private final JSlider    slider         = new RangeSlider();
	private final JLabel     endLabel       = new JLabel();

	private final Animator animator = new Animator(thumbnailPanel::setImage);

	private final float sliderStepSize;

	private boolean             imperialUnits  = false;
	private DoubleUnaryOperator unitConversion = d -> d;

	@SuppressWarnings("FieldHasSetterButNoGetter")
	private @Nullable Consumer<Integer> parameterChangedCallback = null;

	@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
	public DataSetParameterPanel(FilterDataSet filterDataSet, int dataSetIndex) {
		super(new BorderLayout());
		this.filterDataSet = requireNonNull(filterDataSet, "filterDataSet");
		this.dataSetIndex = requireAtLeast(0, dataSetIndex, "dataSetIndex");

		RangeF minMax        = filterDataSet.getDataSet().getMinMax();
		int    quantizerStep = calculateQuantizerStep(minMax.getSpan() / filterDataSet.getDataSet().getGamma());
		sliderStepSize = (float)STEP_QUANTIZER.exp(quantizerStep);
		numberFormat = makeNumberFormat(quantizerStep);
		prepareLabelWidths(minMax);
		prepareSliderRange(minMax);

		{
			nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
			add(nameLabel, BorderLayout.NORTH);
		}
		{
			thumbnailPanel.setPreferredSize(new Dimension(THUMBNAIL_WIDTH / SCALE_FACTOR,
			                                              THUMBNAIL_HEIGHT / SCALE_FACTOR));
			thumbnailPanel.setBackground(Color.BLACK);
			add(thumbnailPanel, BorderLayout.CENTER);
		}
		{
			JPanel p = new JPanel(new BorderLayout());

			{
				beginLabel.setHorizontalAlignment(SwingConstants.TRAILING);
				p.add(beginLabel, BorderLayout.LINE_START);
			}
			{
				slider.setMajorTickSpacing(slider.getExtent() / 10);
				slider.setPaintTicks(true);
				slider.addChangeListener(this::sliderChanged);

				p.setPreferredSize(new Dimension(thumbnailPanel.getPreferredSize().width,
				                                 slider.getPreferredSize().height));
				p.add(slider, BorderLayout.CENTER);
			}
			{
				endLabel.setHorizontalAlignment(SwingConstants.LEADING);
				p.add(endLabel, BorderLayout.LINE_END);
			}

			add(p, BorderLayout.SOUTH);
		}

		setImperialUnits(false);

		sliderChanged(null); // Initialize real label values
	}

	private static int calculateQuantizerStep(float span) {
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

	public boolean isImperialUnits() {
		return imperialUnits;
	}

	public void setImperialUnits(boolean imperialUnits) {
		this.imperialUnits = imperialUnits;
		String              name        = filterDataSet.getDataSet().getName();
		String @Nullable [] nameAndUnit = extractNameAndUnit(name);

		if (imperialUnits && nameAndUnit != null) {
			switch (nameAndUnit[1]) {
				case "&deg;C":
					name = nameAndUnit[0] + " (&deg;F)";
					unitConversion = d -> d * 1.8 + 32.0;
					break;
				case "mm":
					name = nameAndUnit[0] + " (in)";
					unitConversion = d -> d / 25.4;
					break;
				case "m/s":
					name = nameAndUnit[0] + " (mph)";
					unitConversion = d -> d * 3.6 / 1.609344;
					break;
				case "km":
					name = nameAndUnit[0] + " (mi)";
					unitConversion = d -> d / 1.60934;
					break;
				default: // Not a unit
					name = nameAndUnit[0] + " (" + nameAndUnit[1] + ')';
			}
		} else {
			unitConversion = d -> d;
		}

		nameLabel.setText(name + ' ');

		prepareLabelWidths(getMinMax());
	}

	private static String @Nullable [] extractNameAndUnit(String name) {
		Matcher matcher = EXTRACT_UNIT_PATTERN.matcher(name);
		if (!matcher.matches()) {
			return null;
		}

		return new String[]{matcher.group(1), matcher.group(2)};
	}

	// Slider listener
	private void sliderChanged(@Nullable ChangeEvent e) {
		RangeF minMax = getMinMax();

		updateLabels(minMax);

		filterDataSet.setFilterMinMax(minMax);

		if (parameterChangedCallback != null) {
			parameterChangedCallback.accept(dataSetIndex);
		}
	}

	private RangeF getMinMax() {
		float begin = slider.getValue();
		float end   = begin + slider.getExtent();

		begin *= sliderStepSize;
		end *= sliderStepSize;

		int gamma = filterDataSet.getDataSet().getGamma();
		if (gamma > 1) {
			RangeF minMax = filterDataSet.getDataSet().getMinMax();

			begin = minMax.unLerp(begin);
			end = minMax.unLerp(end);
			begin = NumberUtilities.clamp(begin, 0.0f, 1.0f);
			end = NumberUtilities.clamp(end, 0.0f, 1.0f);
			begin = 1 - (float)Math.pow(1 - begin, 1.0 / gamma);
			end = 1 - (float)Math.pow(1 - end, 1.0 / gamma);
			begin = minMax.lerp(begin);
			end = minMax.lerp(end);
		}

		return RangeF.of(begin, end);
	}

	private void updateLabels(RangeF minMax) {
		double begin = unitConversion.applyAsDouble(minMax.getBegin());
		double end   = unitConversion.applyAsDouble(minMax.getEnd());

		beginLabel.setText(numberFormat.format(begin));
		endLabel.setText(numberFormat.format(end));
	}

	public void setMonth(int month) {
		animator.setAnimationFrame(month);
	}

	public void setParameterChangedCallback(@Nullable Consumer<Integer> parameterChangedCallback) {
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

		int[][] thumbnails = filterDataSet.getFilteredThumbnails();
		int     length     = thumbnails[0].length;

		for (int month = 0; month < 12; month++) {
			BufferedImage image = new BufferedImage(THUMBNAIL_WIDTH,
			                                        THUMBNAIL_HEIGHT,
			                                        BufferedImage.TYPE_INT_RGB);
			int[] pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();

			int[] monthThumbnail = thumbnails[month];

			for (int i = 0; i < length; i++) {
				float thumbnailPixel = monthThumbnail[i];

				if (thumbnailPixel == -1) {
					pixels[i] = SEA_BLUE;
				} else if (thumbnailPixel > 0) {
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
