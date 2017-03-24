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

	private Border unselectedBorder = null;
	private Border selectedBorder = null;

	public TypeRenderer() {
		setOpaque(true); //MUST do this for background to show up.
		setHorizontalAlignment(JLabel.CENTER);
	}
	@Override
	public Component getTableCellRendererComponent(
			JTable table, Object object,
			boolean isSelected, boolean hasFocus,
			int row, int column) {
		MAPPING_TYPE type = (MAPPING_TYPE)object;
		
		switch(type) {
		case EQUIV:

			setText(MAPPING_TYPE.OVERLAP.getLabel());
			break;
		case OVERLAP:

			setText(MAPPING_TYPE.EQUIV.getLabel());
			break;
		case DISJOINT:

			setText(MAPPING_TYPE.SUBSUMES.getLabel());
			break;
		case RELATED:
			setText(MAPPING_TYPE.DISJOINT.getLabel());
			break;
		case SUBSUMES:
			setText(MAPPING_TYPE.SUBSUMEDBY.getLabel());
			break;
		case SUBSUMEDBY:
			setText(MAPPING_TYPE.EQUIV.getLabel());
			break;

		default:
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

