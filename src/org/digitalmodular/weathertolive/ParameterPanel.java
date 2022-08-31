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
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jidesoft.swing.RangeSlider;

/**
 * @author Mark Jeronimus
 */
// Created 2022-08-30
public class ParameterPanel extends JPanel {
	private final JLabel      nameLabel      = new JLabel();
	private final JPanel      thumbnailPanel = new JPanel();
	private final RangeSlider slider         = new RangeSlider();

	@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
	public ParameterPanel() {
		super(new BorderLayout());

		add(nameLabel, BorderLayout.LINE_START);
		add(thumbnailPanel, BorderLayout.CENTER);
		add(slider, BorderLayout.SOUTH);
	}
}
