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
import java.awt.Component;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

/**
 * @author Mark Jeronimus
 */
// Created 2022-08-31
public class BottomPanel extends JPanel {
	private final JButton   newButton           = new JButton("New");
	private final JButton   loadButton          = new JButton("Load");
	private final JButton   saveButton          = new JButton("Save");
	private final JCheckBox fastPreviewCheckbox = new JCheckBox("Fast previewing");

	private final JPanel parametersPanel = new JPanel(null);

	@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
	public BottomPanel() {
		super(new BorderLayout());

		JPanel buttonsPanel = new JPanel(new GridLayout(3, 1, 4, 4));
		buttonsPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		buttonsPanel.add(stretchComponent(newButton));
		buttonsPanel.add(stretchComponent(loadButton));
		buttonsPanel.add(stretchComponent(saveButton));
		add(buttonsPanel, BorderLayout.LINE_START);

		parametersPanel.setLayout(new BoxLayout(parametersPanel, BoxLayout.Y_AXIS));
		buttonsPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
		parametersPanel.add(new ParameterPanel());
		add(parametersPanel, BorderLayout.CENTER);

		JPanel optionsPanel = new JPanel(null);
		buttonsPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
		optionsPanel.add(stretchComponent(fastPreviewCheckbox));
		add(optionsPanel, BorderLayout.LINE_END);
	}

	private static Component stretchComponent(Component newButton) {
		JPanel p = new JPanel(new BorderLayout());
		p.add(newButton, BorderLayout.CENTER);
		return p;
	}
}
