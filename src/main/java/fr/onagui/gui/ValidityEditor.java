package fr.onagui.gui;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;

import fr.onagui.alignment.Mapping;
import fr.onagui.alignment.Mapping.VALIDITY;

public class ValidityEditor extends DefaultCellEditor implements ItemListener {

	/** To make Java happy... */
	private static final long serialVersionUID = -5400257707914612600L;

	private MappingTableModel model;
	private VALIDITY lastValidity;
	private AlignmentGUI gui;

	public ValidityEditor(JCheckBox checkBox, MappingTableModel model, AlignmentGUI gui) {
		super(checkBox);
		this.model = model;
		this.gui = gui;
	}

	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		Mapping map = model.getMappingAt(table.convertRowIndexToModel(row));
		VALIDITY val = (VALIDITY)value;
		// Rotate
		if(val == VALIDITY.VALID)
			lastValidity = VALIDITY.INVALID;
		else if(val == VALIDITY.TO_CONFIRM)
			lastValidity = VALIDITY.VALID;
		else
			lastValidity = VALIDITY.TO_CONFIRM;
		map.setValidity(lastValidity);
		// Repaint model
		gui.refreshGUIFromModel();
		return null;
	}

	@Override
	public Object getCellEditorValue() {
		return lastValidity;
	}

	public void itemStateChanged(ItemEvent e) {
		super.fireEditingStopped();
	}
}
