package fr.onagui.gui;


import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import fr.onagui.config.ScoreColorConfiguration;

import java.awt.Color;
import java.awt.Component;

public class ScoreColorRenderer extends JLabel
implements TableCellRenderer {

	/** To make Java happy... */
	private static final long serialVersionUID = 8269983184162200636L;

	private Border unselectedBorder = null;
	private Border selectedBorder = null;

	private ScoreColorConfiguration scc;
	
	public ScoreColorRenderer(ScoreColorConfiguration scc) {
		this.scc = scc;
		setOpaque(true); //MUST do this for background to show up.
		setHorizontalAlignment(JLabel.CENTER);
	}

	public Component getTableCellRendererComponent(
			JTable table, Object color,
			boolean isSelected, boolean hasFocus,
			int row, int column) {
		Double score = (Double)color;
		Color newColor = scc.getColorFromScore(score);
		// Set the color
		setBackground(newColor);
		if (isSelected) {
			if (selectedBorder == null) {
				selectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
						table.getSelectionBackground());
			}
			setBorder(selectedBorder);
		} else {
			if (unselectedBorder == null) {
				unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
						table.getBackground());
			}
			setBorder(unselectedBorder);
		}
		// Set the text
		setText(String.valueOf(score));

		return this;
	}
}
