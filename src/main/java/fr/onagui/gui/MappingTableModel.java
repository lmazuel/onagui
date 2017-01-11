/**
 * 
 */
package fr.onagui.gui;

import java.util.Set;
import java.util.TreeMap;

import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import fr.onagui.alignment.Mapping;
import fr.onagui.alignment.Mapping.VALIDITY;
import fr.onagui.control.AlignmentControler;

public class MappingTableModel<O1, O2> extends AbstractTableModel {

	/** To make Java happy */
	private static final long serialVersionUID = -7941069509325570107L;
	
	private AlignmentControler<O1, O2> controler = null;
	private String[] columnsNames = new String[] {"Label1", "Label2", "Type", "Score", "Validity", "Method", "Date"};
	private Object[][] data = null;
	private Mapping<O1, O2>[] maps = null;
	private TreeMap<Mapping<O1, O2>, Integer> index = null;
	
	private static final DateTimeFormatter timeFormatter = ISODateTimeFormat.basicDateTimeNoMillis();
	
	/**
	 * @param controler
	 * @param table
	 */
	@SuppressWarnings("unchecked")
	public MappingTableModel(AlignmentControler<O1, O2> controler) {
		this.controler = controler;
		// Vide par défaut
		this.data = new Object[0][columnsNames.length];
		this.maps = new Mapping[0];
		this.index = new TreeMap<Mapping<O1,O2>, Integer>();
	}

	@Override
	public String getColumnName(int column) {
		return columnsNames[column];
	}

	public int getColumnCount() {
		return columnsNames.length;
	}

	public int getRowCount() {
		return data.length;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if(rowIndex >= getRowCount())
			return null;
		return data[rowIndex][columnIndex];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if(getRowCount() == 0)
			return String.class; // Empty table, no need to be precise
		return getValueAt(0, columnIndex).getClass();
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		// Only validity is editable
		return columnIndex == 4;
	}

	public Mapping<O1, O2> getMappingAt(int index) {
		return maps[index];
	}
	
	public void setMapping(Set<Mapping<O1, O2>> mapping) {
		data = new Object[mapping.size()][columnsNames.length];
		maps = new Mapping[mapping.size()];
		index.clear();
		int i=0;
		for(Mapping<O1, O2> map : mapping) {
			Object[] line = new Object[columnsNames.length];
			DefaultMutableTreeNode node1 = controler.getTreeModel1().getFirstNodeFromConcept(map.getFirstConcept());
			DefaultMutableTreeNode node2 = controler.getTreeModel2().getFirstNodeFromConcept(map.getSecondConcept());
			// Validity first, may be overriden
			line[4] = map.getValidity();
			// Assign node 1
			if(node1 == null) {
				System.err.println("Error: first concept cannot be determine in JTree: "+map);
				line[0] = "!ERROR!";
				// A discuter...
				line[4] = VALIDITY.INVALID;
				map.setValidity(VALIDITY.INVALID);
			}
			else {
				line[0] = node1.getUserObject();
			}
			// Assign node 2
			if(node2 == null) {
				System.err.println("Error: first concept cannot be determine in JTree: "+map);
				line[1] = "!ERROR!";
				// A discuter...
				line[4] = VALIDITY.INVALID;
				map.setValidity(VALIDITY.INVALID);
			}
			else {
				line[1] = node2.getUserObject();
			}
			line[2] = map.getType();
			line[3] = map.getScore();
			// Le nom de la methode
			line[5] = map.getMethod();
			line[6] = timeFormatter.print(map.getCreationDate());
			maps[i] = map;
			
			// Store and index data
			index.put(map, i);
			data[i++] = line;
		}
		// Pour etre sur que le "row sorter" soit à jour quand le modèle change... Sinon ça plante...
//		TableRowSorter<MyTableModel> sorter = new TableRowSorter<MyTableModel>(this);
//		table.setRowSorter(sorter);
		
		// Data changed...
		fireTableDataChanged();
	}
	
	/** Return the index of this mapping. May be <code>null</code>;
	 * @param map
	 * @return The integer.
	 */
	public Integer getIndexOfMapping(Mapping<O1, O2> map) {
		return index.get(map);
	}
}