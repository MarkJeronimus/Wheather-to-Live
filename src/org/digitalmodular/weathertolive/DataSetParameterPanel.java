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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;

import com.jidesoft.swing.RangeSlider;
import org.digitalmodular.weathertolive.dataset.DataSet;
import org.digitalmodular.weathertolive.util.PreferredNumbers;
import org.digitalmodular.weathertolive.util.RangeF;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireNonNull;

/**
 * @author Mark Jeronimus
 */
// Created 2022-08-30
public class DataSetParameterPanel extends JPanel {
	private static final int              MIN_SLIDER_STEPS = 100;
	private static final PreferredNumbers STEP_QUANTIZER   = new PreferredNumbers(10, 100, 125, 150, 200, 500);

	private final DataSet dataSet;

	private final JLabel      nameLabel        = new JLabel();
	private final JPanel      thumbnailPanel   = new JPanel();
	private final JLabel      sliderBeginLabel = new JLabel("000.0");
	private final RangeSlider slider           = new RangeSlider();
	private final JLabel      sliderEndLabel   = new JLabel("000.0");

	private final float sliderStepSize;

	@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
	public DataSetParameterPanel(DataSet dataSet) {
		super(new BorderLayout());
		this.dataSet = requireNonNull(dataSet, "dataSet");

		{
			nameLabel.setText(dataSet.getName() + ' ');
			add(nameLabel, BorderLayout.LINE_START);
		}
		{
			thumbnailPanel.setPreferredSize(new Dimension(180, 90));
			thumbnailPanel.setBackground(Color.BLACK);
			add(thumbnailPanel, BorderLayout.CENTER);
		}
		{
			JPanel p = new JPanel(new BorderLayout());

			{
				sliderBeginLabel.setPreferredSize(sliderBeginLabel.getMinimumSize());
				p.add(sliderBeginLabel, BorderLayout.LINE_START);
			}
			{
				slider.setMajorTickSpacing(10000);
				slider.setMinorTickSpacing(10);
				slider.setPaintTicks(true);
				slider.setPaintLabels(true);
				sliderStepSize = prepareSliderRange(dataSet.getMinMax());
				slider.addChangeListener(this::valueChanged);

				int size = slider.getFont().getSize();
				slider.setFont(slider.getFont().deriveFont(size * 0.8f));

				p.add(slider, BorderLayout.CENTER);
			}
			{
				sliderEndLabel.setPreferredSize(sliderEndLabel.getMinimumSize());
				p.add(sliderEndLabel, BorderLayout.LINE_END);
			}

			add(p, BorderLayout.SOUTH);
		}

//		setPreferredSize(getMinimumSize());
	}

	private float prepareSliderRange(RangeF minMax) {
		float span           = minMax.getSpan();
		float stepSize       = span / MIN_SLIDER_STEPS;
		float sliderStepSize = (float)STEP_QUANTIZER.exp(STEP_QUANTIZER.log(stepSize));

		int min = (int)Math.floor(minMax.getBegin() / sliderStepSize);
		int max = (int)Math.ceil(minMax.getEnd() / sliderStepSize);
		slider.getModel().setRangeProperties(min, max - min, min, max, false);

		return sliderStepSize;
	}

	private void valueChanged(ChangeEvent e) {
		RangeF minMax = getMinMax();

		sliderBeginLabel.setText("" + minMax.getBegin());
		sliderEndLabel.setText("" + minMax.getEnd());

		dataSet.setFilterMinMax(minMax);
		updateThumbnail();
	}

	public RangeF getMinMax() {
		int begin = slider.getValue();
		int end   = begin + slider.getExtent();
		return RangeF.of(begin * sliderStepSize, end * sliderStepSize);
	}

	public void updateThumbnail() {
	}
}
