package fr.onagui.gui;


import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import fr.onagui.alignment.Mapping.VALIDITY;

public class ValidityRenderer extends JLabel implements TableCellRenderer {

	/** To make Java happy... */
	private static final long serialVersionUID = 4450361546001724576L;
	
	private Border unselectedBorder = null;
	private Border selectedBorder = null;

	private final Color VALID_COLOR  = new Color(98, 255, 20);
	private final Color TO_CONFIRM_COLOR = new Color(20, 219, 255);
	private final Color INVALID_COLOR = new Color(124, 25, 255);
	
	public ValidityRenderer() {
		setOpaque(true); //MUST do this for background to show up.
		setHorizontalAlignment(JLabel.CENTER);
	}
	
	public Component getTableCellRendererComponent(
			JTable table, Object object,
			boolean isSelected, boolean hasFocus,
			int row, int column) {
		VALIDITY validity = (VALIDITY)object;
		// Set the color
		switch(validity) {
		case VALID:
			setBackground(VALID_COLOR);
			setText("OK");
			break;
		case TO_CONFIRM:
			setBackground(TO_CONFIRM_COLOR);
			setText("-");
			break;
		default:
			setBackground(INVALID_COLOR);
			setText("KO");
			break;			
		}
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
		
		return this;
	}
}

