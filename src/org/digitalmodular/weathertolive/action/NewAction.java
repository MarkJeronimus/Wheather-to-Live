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
public final class NewAction extends AbstractAction {
	public static final String NEW_ACTION_KEY = "New";

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

	private final JButton            selectButton = new JButton("Select");
	private final JButton            cancelButton = new JButton("Cancel");
	private final List<JRadioButton> radioButtons = new ArrayList<>(10);

	private final List<ClimateDataSetMetadata> allMetadata = new ArrayList<>(EXPECTED_METADATA_FILES.length);

	private int selectedClimateSetIndex = -1;

	private final ClimateDataSetLoader climateDataSetLoader = new ClimateDataSetLoader();

	@SuppressWarnings("ThisEscapedInObjectConstruction")
	public NewAction(RootPaneContainer frame, WeatherToLivePanel parent) {
		super(NEW_ACTION_KEY);
		requireNonNull(frame, "frame");
		this.parent = requireNonNull(parent, "parent");

		selectButton.addActionListener(this::buttonPressed);
		cancelButton.addActionListener(this::buttonPressed);

		loadAllMetadata();

		frame.getRootPane().getActionMap().put(NEW_ACTION_KEY, this);
	}

	@Override
	public void actionPerformed(@Nullable ActionEvent e) {
		Frame frame = (Frame)parent.getTopLevelAncestor();

		JPanel layout = makeLayout(allMetadata);

		selectButton.setEnabled(false);
		selectedClimateSetIndex = -1;

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

	private JPanel makeLayout(List<ClimateDataSetMetadata> allMetadata) {
		JPanel p = new ListPanel(BoxLayout.Y_AXIS, SPACING);

		p.add(newCenterLabel("<html><h1>Select a Climate Data Set"));
		p.add(Box.createVerticalStrut(SPACING));

		ButtonGroup buttonGroup = new ButtonGroup();

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
