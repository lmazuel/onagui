package fr.onagui.gui;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import fr.onagui.alignment.Mapping.MAPPING_TYPE;

public class MappingTypeRenderer extends JLabel implements ListCellRenderer<MAPPING_TYPE> {

	/** To make Java happy */
	private static final long serialVersionUID = -7490180014632350816L;

	@Override
	public Component getListCellRendererComponent(JList<? extends MAPPING_TYPE> list, MAPPING_TYPE value, int index,
			boolean isSelected, boolean cellHasFocus) {
		setText(value.getLabel());
		return this;
	}	
}
