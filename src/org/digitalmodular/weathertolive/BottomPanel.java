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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;

import org.jetbrains.annotations.Nullable;

import org.digitalmodular.weathertolive.dataset.ClimateDataSet;
import org.digitalmodular.weathertolive.dataset.FilteredDataSet;
import org.digitalmodular.weathertolive.util.LabelSlider;
import org.digitalmodular.weathertolive.util.ListPanel;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireNonNull;

/**
 * @author Mark Jeronimus
 */
// Created 2022-08-31
public class BottomPanel extends JPanel {
	public static final int SPACING = 6;

	private final WeatherToLivePanel parent;

	private final JButton   newButton           = new JButton("New");
	private final JButton   loadButton          = new JButton("Load");
	private final JButton   saveButton          = new JButton("Save");
	private final JCheckBox fastPreviewCheckbox = new JCheckBox("Fast previewing");
	private final JCheckBox animateCheckbox     = new JCheckBox("Animate");
	private final JSlider   monthSlider         = new LabelSlider(Arrays.asList(
			"j", "f", "m", "a", "m", "j", "j", "a", "s", "o", "n", "d"));

	private final ListPanel filterPanel = new ListPanel(BoxLayout.X_AXIS, SPACING);

	@SuppressWarnings("FieldHasSetterButNoGetter")
	private @Nullable Consumer<Integer> parameterChangedCallback = null;

	@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
	public BottomPanel(WeatherToLivePanel parent) {
		super(new BorderLayout());
		this.parent = requireNonNull(parent, "parent");

		setOpaque(true);

		makeGUI();
		attachListeners();
	}

	private void makeGUI() {
		JPanel buttonsPanel = new ListPanel(BoxLayout.Y_AXIS, SPACING);
		buttonsPanel.add(newButton);
		buttonsPanel.add(loadButton);
		buttonsPanel.add(saveButton);
		add(buttonsPanel, BorderLayout.LINE_START);

		add(filterPanel, BorderLayout.CENTER);

		JPanel optionsPanel = new ListPanel(BoxLayout.Y_AXIS, SPACING);
		optionsPanel.add(fastPreviewCheckbox);
		optionsPanel.add(animateCheckbox);
		optionsPanel.add(monthSlider);
		add(optionsPanel, BorderLayout.LINE_END);
	}

	public void prepareFilters(ClimateDataSet climateDataSet) {
		filterPanel.removeAll();

		List<FilteredDataSet> dataSets = climateDataSet.getDataSets();

		for (int i = 0; i < dataSets.size(); i++) {
			DataSetParameterPanel parameter = new DataSetParameterPanel(dataSets.get(i), i);
			parameter.setParameterChangedCallback(this::parameterChanged);

			filterPanel.add(parameter);
		}
	}

	private void attachListeners() {
		ActionListener actionPerformed = this::actionPerformed;

		fastPreviewCheckbox.addActionListener(actionPerformed);
		animateCheckbox.addActionListener(actionPerformed);
		monthSlider.addChangeListener(this::monthChanged);
	}

	@SuppressWarnings("ObjectEquality") // Comparing identity, not equality
	private void actionPerformed(ActionEvent e) {
		if (e.getSource() == fastPreviewCheckbox) {
			parent.setFastPreview(animateCheckbox.isSelected());
		} else if (e.getSource() == animateCheckbox) {
			parent.setAnimated(animateCheckbox.isSelected());
		}
	}

	// Slider listener
	private void monthChanged(ChangeEvent e) {
		parent.setMonth(monthSlider.getValue());
	}

	public void setAnimated(boolean animated) {
		animateCheckbox.setSelected(animated);

		if (animateCheckbox.isEnabled()) {
			monthSlider.setEnabled(!animated);
		}
	}

	// Called from the outside, so don't call this from monthChanged()
	public void setMonth(int month) {
		for (int i = 0; i < filterPanel.getComponentCount(); i++) {
			filterPanel.getParameterPanel(i).setMonth(month);
		}

		monthSlider.setValue(month);
	}

	public void setParameterChangedCallback(@Nullable Consumer<Integer> parameterChangedCallback) {
		this.parameterChangedCallback = parameterChangedCallback;
	}

	/**
	 * Called from inside, to update the outside.
	 */
	private void parameterChanged(int dataSetIndex) {
		if (parameterChangedCallback != null) {
			parameterChangedCallback.accept(dataSetIndex);
		}
	}

	/**
	 * Called from outside, to update the inside.
	 */
	public void dataChanged() {
		for (int i = 0; i < filterPanel.getComponentCount(); i++) {
			filterPanel.getParameterPanel(i).dataChanged();
		}
	}
}
