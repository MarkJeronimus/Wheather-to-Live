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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.RootPaneContainer;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.jetbrains.annotations.Nullable;

import org.digitalmodular.weathertolive.WeatherToLivePanel;
import org.digitalmodular.weathertolive.dataset.ClimateDataSet;
import org.digitalmodular.weathertolive.dataset.ClimateDataSetDownloader;
import org.digitalmodular.weathertolive.dataset.ClimateDataSetLoader;
import org.digitalmodular.weathertolive.dataset.ClimateDataSetMetadata;
import org.digitalmodular.weathertolive.util.ListPanel;
import org.digitalmodular.weathertolive.util.MultiProgressDialog;
import static org.digitalmodular.weathertolive.dataset.ClimateDataSetMetadata.ClimateDataSetData;
import static org.digitalmodular.weathertolive.util.ValidatorUtilities.requireNonNull;

/**
 * @author Mark Jeronimus
 */
// Created 2022-09-04
public final class SelectAction extends AbstractAction {
	public static final String SELECT_ACTION_KEY = "Select";

	private static final Path[] EXPECTED_METADATA_FILES = {
			Paths.get("config-cru-cl-2.0-10min.tsv"),
			Paths.get("config-worldclim-2.1-10min.tsv"),
			Paths.get("config-worldclim-2.1-5min.tsv"),
			Paths.get("config-worldclim-2.1-2.5min.tsv"),
			};

	private static final String[] DOWNLOAD_SIZES = {
			"93 MB",
			"140 MB",
			"470MB",
			"1.5GB"
	};

	public static final int SPACING = 8;

	private final WeatherToLivePanel parent;

	private final List<ClimateDataSetMetadata> allMetadata = new ArrayList<>(EXPECTED_METADATA_FILES.length);

	private final JButton            selectButton = new JButton("Select");
	private final JButton            cancelButton = new JButton("Cancel");
	private final ButtonGroup        buttonGroup  = new ButtonGroup();
	private final List<JRadioButton> radioButtons = new ArrayList<>(10);
	private final JPanel             layout;

	private int selectedClimateSetIndex = -1;

	private final ClimateDataSetLoader climateDataSetLoader = new ClimateDataSetLoader();

	@SuppressWarnings("ThisEscapedInObjectConstruction")
	public SelectAction(RootPaneContainer frame, WeatherToLivePanel parent) {
		super(SELECT_ACTION_KEY);
		requireNonNull(frame, "frame");
		this.parent = requireNonNull(parent, "parent");

		selectButton.addActionListener(this::buttonPressed);
		cancelButton.addActionListener(this::buttonPressed);

		loadAllMetadata();

		layout = makeLayout(allMetadata);

		frame.getRootPane().getActionMap().put(SELECT_ACTION_KEY, this);
	}

	private JPanel makeLayout(List<ClimateDataSetMetadata> allMetadata) {
		JPanel p = new ListPanel(BoxLayout.Y_AXIS, SPACING);

		p.add(newCenterLabel("<html><h1>Select a Climate Data Set"));
		p.add(Box.createVerticalStrut(SPACING));

		{
			JPanel tablePanel = new ListPanel(BoxLayout.X_AXIS, SPACING);

			for (int climateSetIndex = 0; climateSetIndex < EXPECTED_METADATA_FILES.length; climateSetIndex++) {
				if (climateSetIndex > 0) {
					tablePanel.add(new JSeparator(SwingConstants.VERTICAL));
				}

				ClimateDataSetMetadata climateMetadata = allMetadata.get(climateSetIndex);

				StringBuilder labelText = new StringBuilder(1024);
				labelText.append("<html><b>").append(climateMetadata.getName()).append("</b><br>");
				labelText.append("<br>");
				labelText.append("<b>Available climate elements:</b><br>");

				for (int dataSetIndex = 0; dataSetIndex < climateMetadata.getNumMetadata(); dataSetIndex++) {
					ClimateDataSetData metadata = climateMetadata.getMetadata(dataSetIndex);
					labelText.append(metadata.dataSetName).append("<br>");
				}
				labelText.append("<br>".repeat(Math.max(0, 9 - climateMetadata.getNumMetadata())));
				labelText.append("<br>");
				labelText.append("<b>Download size:</b> ").append(DOWNLOAD_SIZES[climateSetIndex]).append("<br>");

				JRadioButton radioButton = new JRadioButton(labelText.toString());
				radioButton.setHorizontalAlignment(SwingConstants.CENTER);
				radioButton.setHorizontalTextPosition(SwingConstants.CENTER);
				radioButton.setVerticalTextPosition(SwingConstants.BOTTOM);
				radioButton.addActionListener(this::radioButtonPressed);
				tablePanel.add(radioButton);

				buttonGroup.add(radioButton);
				radioButtons.add(radioButton);
			}

			p.add(tablePanel);
		}

		return p;
	}

	@Override
	public void actionPerformed(@Nullable ActionEvent e) {
		Frame frame = (Frame)parent.getTopLevelAncestor();

		selectButton.setEnabled(false);

		selectedClimateSetIndex = findDataSetIndex(parent.getClimateDataSet());
		selectInitialRadioButton();

		Object[] options = {selectButton, cancelButton};

		// Windows swaps the buttons compared to Linux.
		boolean isYesLast = UIManager.getDefaults().getBoolean("OptionPane.isYesLast");
		if (isYesLast) {
			Collections.reverse(Arrays.asList(options));
		}

		int result = JOptionPane.showOptionDialog(frame,
		                                          layout,
		                                          frame.getTitle(),
		                                          JOptionPane.DEFAULT_OPTION,
		                                          JOptionPane.PLAIN_MESSAGE,
		                                          null,
		                                          options,
		                                          options[0]);

		if (isYesLast) {
			result = options.length - result - 1;
		}

		if (result == 0) { // index of selectButton
			loadClimateSet(selectedClimateSetIndex);
		}
	}

	private int findDataSetIndex(@Nullable ClimateDataSet climateDataSet) {
		if (climateDataSet == null) {
			return -1;
		}

		for (int i = 0; i < allMetadata.size(); i++) {
			ClimateDataSetMetadata climateMetadata = allMetadata.get(i);
			if (climateMetadata.getName().equals(climateDataSet.getMetadata().getName())) {
				return i;
			}
		}

		return -1;
	}

	private void selectInitialRadioButton() {
		if (selectedClimateSetIndex == -1) {
			buttonGroup.clearSelection();
		} else {
			for (int i = 0; i < EXPECTED_METADATA_FILES.length; i++) {
				radioButtons.get(i).setSelected(i == selectedClimateSetIndex);
			}
		}
	}

	private static JLabel newCenterLabel(String text) {
		JLabel titleLabel = new JLabel(text);
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		return titleLabel;
	}

	private void loadAllMetadata() {
		for (Path file : EXPECTED_METADATA_FILES) {
			try {
				allMetadata.add(new ClimateDataSetMetadata(file));
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	@SuppressWarnings("ObjectEquality") // Comparing identity, not equality
	private void buttonPressed(ActionEvent e) {
		@Nullable JOptionPane optionPane = findOptionPane((JComponent)e.getSource());
		if (optionPane == null) {
			return;
		}

		if (e.getSource() == selectButton) {
			optionPane.setValue(selectButton);
		} else if (e.getSource() == cancelButton) {
			optionPane.setValue(cancelButton);
		}
	}

	private void radioButtonPressed(ActionEvent e) {
		selectedClimateSetIndex = -1;
		for (int i = 0; i < radioButtons.size(); i++) {
			if (radioButtons.get(i).isSelected()) {
				selectedClimateSetIndex = i;
				break;
			}
		}

		selectButton.setEnabled(true);
	}

	private static @Nullable JOptionPane findOptionPane(JComponent comp) {
		while (comp != null) {
			if (comp instanceof JOptionPane) {
				return (JOptionPane)comp;
			}

			comp = (JComponent)comp.getParent();
		}

		return null;
	}

	public void loadClimateSet(int selectedClimateSetIndex) {
		if (selectedClimateSetIndex < 0 || selectedClimateSetIndex >= EXPECTED_METADATA_FILES.length) {
			return;
		}

		loadAllMetadata();

		ClimateDataSetMetadata metadata = allMetadata.get(selectedClimateSetIndex);

		{
			@Nullable ClimateDataSet climateDataSet = parent.getClimateDataSet();
			if (climateDataSet != null &&
			    climateDataSet.getMetadata().getName().equals(metadata.getName())) {
				return;
			}
		}

		Frame frame = (Frame)parent.getTopLevelAncestor();

		parent.setClimateDataSet(null);
		climateDataSetLoader.cancel();
		ForkJoinPool.commonPool().submit(() -> downloadProcess(frame, parent, metadata));
	}

	public void downloadProcess(Frame frame, WeatherToLivePanel parent, ClimateDataSetMetadata metadata) {
		MultiProgressDialog progressListener = new MultiProgressDialog(frame, frame.getTitle(), 2);
		progressListener.setAutoShow(true);
		progressListener.setAutoClose(true);

		try {
			progressListener.setTaskName("Downloading " + metadata.getName());
			ClimateDataSetDownloader.download(metadata, progressListener);

			progressListener.setTaskName("Loading " + metadata.getName());
			progressListener.addCancelListener(ignored -> {
				climateDataSetLoader.cancel();
				progressListener.setVisible(false);
			});

			ClimateDataSet climateDataSet = climateDataSetLoader.load(metadata, progressListener);

			parent.setClimateDataSet(climateDataSet);
			parent.dataChanged(0);
		} catch (IOException e) {
			//noinspection ProhibitedExceptionThrown
			throw new RuntimeException(e);
		} catch (InterruptedException ignored) {
		}
	}
}
