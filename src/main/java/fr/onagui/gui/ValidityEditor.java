package fr.onagui.gui;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;

import fr.onagui.alignment.Alignment;
import fr.onagui.alignment.Mapping;
import fr.onagui.alignment.Mapping.VALIDITY;
import fr.onagui.control.AlignmentControler;

public class ValidityEditor<O1, O2> extends DefaultCellEditor implements ItemListener {

	/** To make Java happy... */
	private static final long serialVersionUID = -5400257707914612600L;

	private MappingTableModel<O1, O2> model;
	private VALIDITY lastValidity;
	private AlignmentGUI gui;

	public ValidityEditor(JCheckBox checkBox, MappingTableModel<O1, O2> model, AlignmentGUI gui) {
		super(checkBox);
		this.model = model;
		this.gui = gui;
	}

	public Component getTableCellEditorComponent(
			JTable table,
			Object value,
			boolean isSelected,
			int row,
			int column
	) {
		Mapping<O1, O2> map = model.getMappingAt(table.convertRowIndexToModel(row));
		VALIDITY val = (VALIDITY)value;
		// Rotate
		if(val == VALIDITY.VALID)
			lastValidity = VALIDITY.INVALID;
		else if(val == VALIDITY.TO_CONFIRM)
			lastValidity = VALIDITY.VALID;
		else
			lastValidity = VALIDITY.TO_CONFIRM;
		
		//map.setValidity(lastValidity);
		
		// 1. enlever l'ancien mapping
		gui.getAlignmentControler().removeMapping(map);
		// 2. créer le nouveau mapping
		Mapping<O1, O2> newMap = new Mapping<O1, O2>(map, lastValidity);	
		// 3. ajouter le nouveau mapping
		gui.getAlignmentControler().addMapping(newMap);		
		
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
