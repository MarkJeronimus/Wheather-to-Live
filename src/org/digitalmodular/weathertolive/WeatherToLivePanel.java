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
import java.awt.Dimension;
import java.awt.DisplayMode;
import javax.swing.JPanel;

import org.digitalmodular.weathertolive.util.AnimationZoomPanel;
import org.digitalmodular.weathertolive.util.GraphicsUtilities;

/**
 * @author Mark Jeronimus
 */
// Created 2022-08-30
public class WeatherToLivePanel extends JPanel {
	private final AnimationZoomPanel worldPanel     = new AnimationZoomPanel();
	private final ParameterPanel     parameterPanel = new ParameterPanel();

	@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
	public WeatherToLivePanel() {
		super(new BorderLayout());

		DisplayMode displayMode = GraphicsUtilities.getDisplayMode();
		setPreferredSize(new Dimension(displayMode.getWidth() * 3 / 8, displayMode.getHeight() * 3 / 8));

		add(worldPanel, BorderLayout.CENTER);
		add(parameterPanel, BorderLayout.SOUTH);

		parameterPanel.init();
	}
}
