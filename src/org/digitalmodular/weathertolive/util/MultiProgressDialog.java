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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import org.jetbrains.annotations.Nullable;

import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireAtLeast;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireNonNull;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireRange;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireStringNotEmpty;

/**
 * @author Mark Jeronimus
 */
// Created 2012-05-12
public class MultiProgressDialog extends JDialog implements MultiProgressListener {
	private boolean autoShow  = false;
	private boolean autoClose = false;

	private final JLabel         taskNameLabel = new JLabel("<whatLabel>", SwingConstants.CENTER);
	private       JProgressBar[] progressBars;
	private final JButton        cancelButton  = new JButton("Cancel");

	@SuppressWarnings("OverridableMethodCallDuringObjectConstruction")
	public MultiProgressDialog(@Nullable Frame owner,
	                           String title,
	                           int numProgressBars) {
		super(owner, requireNonNull(title, "title"), false);
		requireAtLeast(1, numProgressBars, "numProgressBars");

		makeLayout(owner, numProgressBars);

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	}

	public boolean isAutoShow() {
		return autoShow;
	}

	public void setAutoShow(boolean autoShow) {
		this.autoShow = autoShow;
	}

	public boolean isAutoClose() {
		return autoClose;
	}

	public void setAutoClose(boolean autoClose) {
		this.autoClose = autoClose;
	}

	private void makeLayout(@Nullable Component owner, int numProgressBars) {
		setLayout(new BorderLayout());

		progressBars = new JProgressBar[numProgressBars];

		{
			JPanel p = new JPanel(new GridLayout(2, 1));

			p.add(taskNameLabel);
			p.add(Box.createGlue());

			add(p, BorderLayout.NORTH);
		}
		{
			JPanel p = new ListPanel(BoxLayout.Y_AXIS, 4);

			for (int i = 0; i < numProgressBars; i++) {
				progressBars[i] = new JProgressBar();
				progressBars[i].setPreferredSize(new Dimension(540, 20));
				p.add(progressBars[i]);
			}

			add(p, BorderLayout.CENTER);
		}
		{
			JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));

			cancelButton.setEnabled(false);
			p.add(cancelButton);

			add(p, BorderLayout.SOUTH);
		}

		pack();
		setLocationRelativeTo(owner);

		// Needs to be set after pack() to ensure layout stability
		taskNameLabel.setText("");
	}

	public void addCancelListener(ActionListener cancelListener) {
		cancelButton.addActionListener(cancelListener);
		cancelButton.setEnabled(cancelButton.getActionListeners().length != 0);
	}

	public void removeCancelListener(ActionListener cancelListener) {
		cancelButton.removeActionListener(cancelListener);
		cancelButton.setEnabled(cancelButton.getActionListeners().length != 0);
	}

	/**
	 * Sets the text above the progress bar.
	 */
	public void setTaskName(String taskName) {
		requireStringNotEmpty(taskName, "taskName");

		if (autoShow && !isVisible()) {
			setVisible(true);
		}

		taskNameLabel.setText(taskName);
	}

	@Override
	public void multiProgressUpdated(int progressBarIndex, ProgressEvent evt) {
		requireRange(0, progressBars.length - 1, progressBarIndex, "progressBarIndex");
		requireNonNull(evt, "evt");

		if (autoShow && !isVisible()) {
			setVisible(true);
		}

		JProgressBar progressBar = progressBars[progressBarIndex];

		boolean indeterminate = evt.getTotal() <= 0;

		if (indeterminate) {
			progressBar.setIndeterminate(true);
			progressBar.setMaximum(1);
			progressBar.setValue(1);
		} else {
			long total    = evt.getTotal();
			long progress = evt.getProgress();

			if (total >= 1 << 30) {
				long highestOneBit = Long.highestOneBit(total);
				total >>= (highestOneBit - 30);
				progress >>= (highestOneBit - 30);
			}

			if (autoClose && progressBarIndex == 0 && progress >= total) {
				setVisible(false);
				return;
			}

			progressBar.setIndeterminate(false);
			progressBar.setMaximum((int)total);
			progressBar.setValue((int)progress);
		}

		progressBar.setString(evt.getText());
		progressBar.setStringPainted(!evt.getText().isEmpty());

		if (!System.getProperty("os.name").toLowerCase().contains("win")) {
			Toolkit.getDefaultToolkit().sync(); // Recommended, except on Windows where this is superfluous.
		}
	}
}
