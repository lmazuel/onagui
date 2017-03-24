package fr.onagui.gui;


import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import fr.onagui.alignment.Mapping.MAPPING_TYPE;
import fr.onagui.alignment.Mapping.VALIDITY;

public class TypeRenderer extends JLabel implements TableCellRenderer {

	/** To make Java happy... */
	private static final long serialVersionUID = 4450361546001724576L;

	public TypeRenderer() {
		setOpaque(true); //MUST do this for background to show up.
		setHorizontalAlignment(JLabel.CENTER);
	}
	
	public Component getTableCellRendererComponent(
			JTable table, Object object,
			boolean isSelected, boolean hasFocus,
			int row, int column) {
		MAPPING_TYPE type = (MAPPING_TYPE)object;
		// Set the color
		switch(type) {
		case EQUIV:
			setText(MAPPING_TYPE.OVERLAP.getLabel());
			break;
		case OVERLAP:
			setText(MAPPING_TYPE.EQUIV.getLabel());
			break;
		default:
			break;			
		}
		
		return this;
	}
}

