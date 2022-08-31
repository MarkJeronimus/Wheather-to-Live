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

import java.awt.Color;
import java.awt.Dimension;
import java.util.Dictionary;
import java.util.Hashtable;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;

/**
 * @author Mark Jeronimus
 */
// Created 2022-08-31
public class MonthSlider extends JSlider {
	@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
	public MonthSlider() {
		super(0, 11);

		setMajorTickSpacing(1);
		setPaintTicks(true);
		setPaintLabels(true);
		setLabelTable(monthLabels());
		setPreferredSize(new Dimension(getPreferredSize().width / 2, getPreferredSize().height));
		setBorder(BorderFactory.createLineBorder(Color.RED, 1));
	}

	private static Dictionary<Integer, JComponent> monthLabels() {
		@SuppressWarnings("UseOfObsoleteCollectionType")
		Hashtable<Integer, JComponent> monthLabels = new Hashtable<>(12);
		monthLabels.put(0, smallLabel("j"));
		monthLabels.put(1, smallLabel("f"));
		monthLabels.put(2, smallLabel("m"));
		monthLabels.put(3, smallLabel("a"));
		monthLabels.put(4, smallLabel("m"));
		monthLabels.put(5, smallLabel("j"));
		monthLabels.put(6, smallLabel("j"));
		monthLabels.put(7, smallLabel("a"));
		monthLabels.put(8, smallLabel("s"));
		monthLabels.put(9, smallLabel("o"));
		monthLabels.put(10, smallLabel("n"));
		monthLabels.put(11, smallLabel("d"));
		return monthLabels;
	}

	private static JLabel smallLabel(String text) {
		JLabel label = new JLabel(text);
		int    size  = label.getFont().getSize();
		label.setFont(label.getFont().deriveFont(size * 0.66f));
		return label;
	}
}
