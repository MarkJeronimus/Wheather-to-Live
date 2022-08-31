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
package org.digitalmodular.weathertolive.util;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireAtLeast;

/**
 * @author Mark Jeronimus
 */
// Created 2022-08-31
public class ListPanel extends JPanel {
	private final JPanel topAlignPanel = new JPanel(null);

	@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
	public ListPanel(int axis, int spacing) {
		super(new BorderLayout());
		requireAtLeast(0, spacing, "spacing");

		topAlignPanel.setBorder(BorderFactory.createEmptyBorder(spacing, spacing, spacing, spacing));

		topAlignPanel.setLayout(new BoxLayout(topAlignPanel, axis));
		add(topAlignPanel, axis == BoxLayout.Y_AXIS ? BorderLayout.SOUTH : BorderLayout.LINE_START);
	}

	@Override
	public Component add(Component comp) {
		if (topAlignPanel.getComponentCount() > 0) {
			topAlignPanel.add(Box.createVerticalStrut(4));
		}

		JPanel p = new JPanel(new BorderLayout());
		p.add(comp, BorderLayout.CENTER);
		topAlignPanel.add(p);
		return p;
	}
}
