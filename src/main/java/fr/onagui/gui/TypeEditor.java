package fr.onagui.gui;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTable;

import fr.onagui.alignment.Mapping;
import fr.onagui.alignment.Mapping.MAPPING_TYPE;
import fr.onagui.alignment.Mapping.VALIDITY;

public class TypeEditor extends DefaultCellEditor implements ItemListener {

	/** To make Java happy... */
	private static final long serialVersionUID = -5400257707914612600L;

	private MappingTableModel model;
	private MAPPING_TYPE lasttype;
	private AlignmentGUI gui;

	public TypeEditor(JCheckBox checkBox, MappingTableModel model, AlignmentGUI gui) {
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
		Mapping map = model.getMappingAt(table.convertRowIndexToModel(row));
		MAPPING_TYPE val = (MAPPING_TYPE)value;
		
		// Rotate
		if(val.equals(MAPPING_TYPE.EQUIV))
			lasttype = MAPPING_TYPE.OVERLAP;
		else if(val.equals(MAPPING_TYPE.OVERLAP))
			lasttype = MAPPING_TYPE.RELATED;
		else if(val.equals(MAPPING_TYPE.RELATED))
			lasttype = MAPPING_TYPE.SUBSUMEDBY;
		else if(val.equals(MAPPING_TYPE.SUBSUMEDBY))
			lasttype = MAPPING_TYPE.SUBSUMES;
		else if(val.equals(MAPPING_TYPE.SUBSUMES))
			lasttype = MAPPING_TYPE.DISJOINT;
		else if(val.equals(MAPPING_TYPE.DISJOINT))
			lasttype = MAPPING_TYPE.EQUIV;
		
		map.setType(lasttype);
		// Repaint model
		gui.refreshGUIFromModel();
		return null;
	}

	@Override
	public Object getCellEditorValue() {
		return lasttype;
	}

	public void itemStateChanged(ItemEvent e) {
		super.fireEditingStopped();
	}
}
