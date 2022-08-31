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
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;

import org.digitalmodular.weathertolive.util.LabelSlider;
import org.digitalmodular.weathertolive.util.ListPanel;

/**
 * @author Mark Jeronimus
 */
// Created 2022-08-31
public class BottomPanel extends JPanel {
	public static final int SPACING = 6;

	private final JButton   newButton           = new JButton("New");
	private final JButton   loadButton          = new JButton("Load");
	private final JButton   saveButton          = new JButton("Save");
	private final JCheckBox fastPreviewCheckbox = new JCheckBox("Fast previewing");
	private final JCheckBox animateCheckbox     = new JCheckBox("Animate");
	private final JSlider   monthSlider         = new LabelSlider(Arrays.asList(
			"j", "f", "m", "a", "m", "j", "j", "a", "s", "o", "n", "d"));

	private final JPanel parametersPanel = new ListPanel(BoxLayout.X_AXIS, SPACING);

	@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
	public BottomPanel() {
		super(new BorderLayout());
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

		parametersPanel.setLayout(new BoxLayout(parametersPanel, BoxLayout.Y_AXIS));
		parametersPanel.add(new ParameterPanel());
		add(parametersPanel, BorderLayout.CENTER);

		JPanel optionsPanel = new ListPanel(BoxLayout.Y_AXIS, SPACING);
		optionsPanel.add(fastPreviewCheckbox);
		optionsPanel.add(animateCheckbox);
		optionsPanel.add(monthSlider);
		add(optionsPanel, BorderLayout.LINE_END);
	}

	private void attachListeners() {
		ActionListener actionPerformed = this::actionPerformed;

		fastPreviewCheckbox.addActionListener(actionPerformed);
		animateCheckbox.addActionListener(actionPerformed);
		monthSlider.addChangeListener(this::valueChanged);
	}

	private void actionPerformed(ActionEvent e) {
	}

	private void valueChanged(ChangeEvent changeEvent) {
	}

	public void setAnimatable(boolean animatable) {
		if (!animatable) {
			animateCheckbox.setSelected(false);
			monthSlider.setValue(0);
		}

		animateCheckbox.setEnabled(animatable);
		monthSlider.setEnabled(animatable);
	}
}
