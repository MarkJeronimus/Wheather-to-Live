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
 * Weather to Live. If not, see <http://www.gnu.org/licenses/>.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.digitalmodular.weathertolive.util;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.digitalmodular.weathertolive.DataSetParameterPanel;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireAtLeast;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireRange;

/**
 * I guess this *could* be done with a layout manager, but that'll take me more time.
 *
 * @author Mark Jeronimus
 */
// Created 2022-08-31
public class ListPanel extends JPanel {
	private final int axis;

	private final JPanel topAlignPanel = new JPanel(null);

	@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
	public ListPanel(int axis, int spacing) {
		super(new BorderLayout());
		this.axis = requireRange(0, 1, axis, "axis");
		requireAtLeast(0, spacing, "spacing");

		topAlignPanel.setBorder(BorderFactory.createEmptyBorder(spacing, spacing, spacing, spacing));

		topAlignPanel.setLayout(new BoxLayout(topAlignPanel, axis));
		add(topAlignPanel, axis == BoxLayout.Y_AXIS ? BorderLayout.SOUTH : BorderLayout.LINE_START);
	}

	@Override
	public void removeAll() {
		topAlignPanel.removeAll();
	}

	@Override
	public Component add(Component comp) {
		if (topAlignPanel.getComponentCount() > 0) {
			if (axis == BoxLayout.Y_AXIS) {
				topAlignPanel.add(Box.createVerticalStrut(4));
			} else {
				topAlignPanel.add(Box.createHorizontalStrut(4));
			}
		}

		JPanel p = new JPanel(new BorderLayout());
		p.add(comp, BorderLayout.CENTER);
		topAlignPanel.add(p);
		return p;
	}

	public int getNumChildren() {
		// Odd indices are fillers
		return (topAlignPanel.getComponentCount() + 1) >> 1;
	}

	public DataSetParameterPanel getChild(int index) {
		// Odd indices are fillers
		JPanel p = (JPanel)topAlignPanel.getComponent(index * 2);
		return (DataSetParameterPanel)p.getComponent(0);
	}
}
