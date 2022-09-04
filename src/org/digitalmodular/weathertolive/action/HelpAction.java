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
package org.digitalmodular.weathertolive.action;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.RootPaneContainer;
import javax.swing.SwingConstants;

import org.jetbrains.annotations.Nullable;

import org.digitalmodular.weathertolive.WeatherToLivePanel;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireNonNull;

/**
 * @author Mark Jeronimus
 */
// Created 2022-09-04
public final class HelpAction extends AbstractAction {
	public static final String HELP_ACTION_KEY = "Help";

	private final WeatherToLivePanel parent;

	@SuppressWarnings("ThisEscapedInObjectConstruction")
	public HelpAction(RootPaneContainer frame, WeatherToLivePanel parent) {
		super(HELP_ACTION_KEY);
		requireNonNull(frame, "frame");
		this.parent = requireNonNull(parent, "parent");

		frame.getRootPane().getActionMap().put(HELP_ACTION_KEY, this);
	}

	@Override
	public void actionPerformed(@Nullable ActionEvent e) {
		Frame frame = (Frame)parent.getTopLevelAncestor();

		JLabel bigLabel = new JLabel(
				"<html><center><h1>Weather to Live</h1><br>" +
				"A pun on both<br>" +
				"\"Where to live (for nice weather)\" and \"Whether to live\" (hehe.)<br>" +
				"<br>" +
				"<b>It's a search engine</b> to find coordinates on the globe with your preferred weather.<br>" +
				"<br>" +
				"<b>Usage:</b><br>" +
				"The bottom panel shows individual weather parameters, each with a min and max.<br>" +
				"Changing the min and max changes the range of that climate element that you consider pleasant.<br>" +
				"The filters are combined to find the coordinates that match <i>all</i> of the filters.<br>" +
				"Coordinates that were filtered out are blacked out in the main viewer.<br>" +
				"Each month is filtered individually, unless the 'Filter entire year' checkbox is checked.<br>" +
				"<br>" +
				"The main viewer can be zoomed and dragged like a Maps app.<br>" +
				"The colors are from the last touched parameter.");
		bigLabel.setHorizontalAlignment(SwingConstants.CENTER);

		JOptionPane.showOptionDialog(frame,
		                             bigLabel,
		                             frame.getTitle(),
		                             JOptionPane.DEFAULT_OPTION,
		                             JOptionPane.PLAIN_MESSAGE,
		                             null,
		                             new Object[]{"Close"},
		                             "Close");
	}
}
