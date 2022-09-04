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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.RootPaneContainer;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;

import org.jetbrains.annotations.Nullable;

import org.digitalmodular.weathertolive.action.HelpAction;
import org.digitalmodular.weathertolive.action.NewAction;
import org.digitalmodular.weathertolive.dataset.ClimateDataSet;
import org.digitalmodular.weathertolive.dataset.FilterDataSet;
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

	private final JButton   newButton             = new JButton("New");
	private final JButton   loadButton            = new JButton("Load");
	private final JButton   saveButton            = new JButton("Save");
	private final JButton   saveAsButton          = new JButton("Save As");
	private final JButton   helpButton            = new JButton("Help");
	private final JCheckBox imperialCheckbox      = new JCheckBox("Imperial units");
	private final JCheckBox fastPreviewCheckbox   = new JCheckBox("Fast previewing");
	private final JCheckBox animateCheckbox       = new JCheckBox("Animate");
	private final JSlider   monthSlider           = new LabelSlider(Arrays.asList(
			"j", "f", "m", "a", "m", "j", "j", "a", "s", "o", "n", "d"));
	private final JCheckBox aggregateYearCheckbox = new JCheckBox("Filter entire year");

	private final ListPanel filterPanel = new ListPanel(BoxLayout.X_AXIS, SPACING);

	@SuppressWarnings("FieldHasSetterButNoGetter")
	private @Nullable Consumer<Integer> parameterChangedCallback = null;

	private int machineEvent = 0;

	@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
	public BottomPanel(RootPaneContainer frame, WeatherToLivePanel parent) {
		super(new BorderLayout());
		this.parent = requireNonNull(parent, "parent");

		setOpaque(true);

		makeGUI();
		attachListeners(frame);
	}

	private void makeGUI() {
		{
			JPanel p = new ListPanel(BoxLayout.Y_AXIS, SPACING);
			p.add(newButton);
			p.add(loadButton);
			p.add(saveButton);
			p.add(saveAsButton);
			p.add(helpButton);
			add(p, BorderLayout.LINE_START);
		}
		{
			JScrollPane sp = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
			                                 ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			sp.setBorder(null);

			{
				JPanel p = new JPanel(new FlowLayout());

				p.add(filterPanel);

				sp.getViewport().add(p);
			}

			add(sp, BorderLayout.CENTER);
		}
		{
			JPanel p = new ListPanel(BoxLayout.Y_AXIS, SPACING);
			p.add(imperialCheckbox);
			p.add(fastPreviewCheckbox);
			p.add(animateCheckbox);
			p.add(monthSlider);
			p.add(aggregateYearCheckbox);
			add(p, BorderLayout.LINE_END);
		}

		aggregateYearCheckbox.setToolTipText("Combine the monthly filter results into a single result");
	}

	public void prepareFilters(@Nullable ClimateDataSet climateDataSet) {
		filterPanel.removeAll();

		List<FilterDataSet> filterDataSets = climateDataSet == null ?
		                                     Collections.emptyList() :
		                                     climateDataSet.getFilterDataSets();

		for (int i = 0; i < filterDataSets.size(); i++) {
			DataSetParameterPanel parameter = new DataSetParameterPanel(filterDataSets.get(i), i);
			parameter.setParameterChangedCallback(this::parameterChanged);

			filterPanel.add(parameter);
		}

		filterPanel.revalidate();
	}

	private void attachListeners(RootPaneContainer frame) {
		ActionListener actionPerformed = this::actionPerformed;

		newButton.setAction(new NewAction(frame, parent));
//		loadButton.setAction(); // TODO loadButton
//		saveButton.setAction(); // TODO saveButton
//		saveAsButton.setAction(); // TODO saveAsButton
		helpButton.setAction(new HelpAction(frame, parent));
		imperialCheckbox.addActionListener(actionPerformed);
		fastPreviewCheckbox.addActionListener(actionPerformed);
		animateCheckbox.addActionListener(actionPerformed);
		monthSlider.addChangeListener(this::monthChanged);
		aggregateYearCheckbox.addActionListener(actionPerformed);
	}

	@SuppressWarnings("ObjectEquality") // Comparing identity, not equality
	private void actionPerformed(ActionEvent e) {
		if (machineEvent > 0) {
			return;
		}

		machineEvent++;
		try {
			if (e.getSource() == imperialCheckbox) {
				setImperialUnits(imperialCheckbox.isSelected());
			} else if (e.getSource() == fastPreviewCheckbox) {
				parent.setFastPreview(animateCheckbox.isSelected());
			} else if (e.getSource() == animateCheckbox) {
				parent.setAnimated(animateCheckbox.isSelected());
			} else if (e.getSource() == aggregateYearCheckbox) {
				parent.setAggregateYear(aggregateYearCheckbox.isSelected());
			}
		} finally {
			machineEvent--;
		}
	}

	// Slider listener
	private void monthChanged(ChangeEvent e) {
		if (machineEvent > 0) {
			return;
		}

		machineEvent++;
		try {
			parent.setMonth(monthSlider.getValue());
			parent.setAnimated(false);
		} finally {
			machineEvent--;
		}
	}

	public void setImperialUnits(boolean imperialUnits) {
		machineEvent++;
		try {
			for (int i = 0; i < filterPanel.getNumChildren(); i++) {
				filterPanel.getChild(i).setImperialUnits(imperialUnits);
			}
		} finally {
			machineEvent--;
		}
	}

	// Called from the outside, so don't call this from listeners
	public void setAnimated(boolean animated) {
		machineEvent++;
		try {
			animateCheckbox.setSelected(animated);
		} finally {
			machineEvent--;
		}
	}

	// Called from the outside, so don't call this from listeners
	public void setMonth(int month) {
		machineEvent++;
		try {
			for (int i = 0; i < filterPanel.getNumChildren(); i++) {
				filterPanel.getChild(i).setMonth(month);
			}

			monthSlider.setValue(month);
		} finally {
			machineEvent--;
		}
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
		for (int i = 0; i < filterPanel.getNumChildren(); i++) {
			filterPanel.getChild(i).dataChanged();
		}
	}
}
