package fr.onagui.gui;

import javax.swing.RowFilter;

import fr.onagui.alignment.Mapping;

public class MappingRowFilter extends RowFilter<MappingTableModel, Integer> {
	
	private Object concept1 = null;
	private Object concept2 = null;

	/**
	 * @param concept1
	 * @param concept2
	 */
	public MappingRowFilter(Object concept1, Object concept2) {
		this.concept1 = concept1;
		this.concept2 = concept2;
	}

	@Override
	public boolean include(javax.swing.RowFilter.Entry<? extends MappingTableModel, ? extends Integer> entry) {
		
		// Test facile: si les deux parametre de classes sont "null", alors on affiche tout
		if(concept1 == null && concept2 == null) return true;
		
		// Maintenant, c'est plus subtil...
		MappingTableModel model = entry.getModel();
//		System.out.println("Identifier type: "+entry.getIdentifier().getClass().getCanonicalName());
//		System.out.println("Identifier value: "+entry.getIdentifier());
		Mapping map = model.getMappingAt(entry.getIdentifier());
		Object conceptFromMap1 = map.getFirstConcept();
		Object conceptFromMap2 = map.getSecondConcept();
		// Macro qui aide!
		boolean filter1 = concept1 != null && conceptFromMap1.equals(concept1);
		boolean filter2 = concept2 != null && conceptFromMap2.equals(concept2);

		if(filter1 && filter2) return true; // Double filtrage
		else if(filter1 && concept2 == null) return true; // Filtrage ok sur 1, pas de filtrage sur 2
		else if(concept1 == null && filter2) return true; // Filtrage ok sur 2, pas de filtrage sur 1
		// Sinon, c'est pas bon...
		return false;
	}

}
