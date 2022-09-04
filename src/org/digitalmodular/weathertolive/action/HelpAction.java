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
