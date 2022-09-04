package org.digitalmodular.weathertolive;

import java.awt.FlowLayout;
import java.awt.Frame;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * @author author
 */
// Created 2022-09-04
public final class AboutAndHelpDialog {
	private AboutAndHelpDialog() {
		throw new AssertionError();
	}

	public static void showAboutAndHelpDialog(JComponent parent) {
		JPanel p = new JPanel(new FlowLayout());

		Frame frame = (Frame)parent.getTopLevelAncestor();

		JLabel bigLabel = new JLabel("<html><center><h1>Weather to Live</h1><br>" +
		                             "A pun on both<br>" +
		                             "\"Where to live (for nice weather)\"<br>" +
		                             "and \"Whether to live\" (hehe.)<br>" +
		                             "<br>" +
		                             "<b>It's a search engine</b> to find coordinates on the globe<br>" +
		                             "that match your preferred weather parameters.<br>" +
		                             "<br>" +
		                             "<b>Usage:</b><br>" +
		                             "The bottom panel shows individual weather<br>" +
		                             "parameters, each with a min and max.<br>" +
		                             "Changing the min and max changes the range of<br>" +
		                             "that parameter that you consider pleasant.<br>" +
		                             "The filters are combined to find the coordinates that match<br>" +
		                             "<i>all</i> of the filters, and displayed in the main viewer.<br>" +
		                             "Each month is filtered individually, unless the<br>" +
		                             "'Filter entire year' checkbox is checked.<br>" +
		                             "<br>" +
		                             "The main viewer can be zoomed and dragged like a Maps app.<br>" +
		                             "The colors are from the last touched parameter.");
		bigLabel.setHorizontalAlignment(SwingConstants.CENTER);
		p.add(bigLabel);

		JOptionPane.showOptionDialog(frame,
		                             p,
		                             frame.getTitle(),
		                             JOptionPane.DEFAULT_OPTION,
		                             JOptionPane.PLAIN_MESSAGE,
		                             null,
		                             new Object[]{"Close"},
		                             null);
	}
}
