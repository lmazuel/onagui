/**
 * 
 */
package fr.onagui.alignment.method;

import java.util.Collection;
import java.util.SortedSet;

import fr.onagui.alignment.Mapping;
import fr.onagui.alignment.OntoContainer;


/** Method which match EXACTLY the label (no normalisation).
 * Which can be useful in some case (each ontology are ligned to the same third using an id).
 * @author Laurent Mazuel
 */
public class ExactAlignmentMethod<ONTORES1, ONTORES2> extends LabelAlignmentMethod<ONTORES1, ONTORES2> {
		
	/* (non-Javadoc)
	 * @see agui.alignment.AbstractAlignmentMethod#computeMapping(agui.alignment.OntoContainer, java.lang.Object, java.util.Collection, agui.alignment.OntoContainer, java.lang.Object, java.util.Collection)
	 */
	@Override
	public Mapping<ONTORES1, ONTORES2> computeMapping(
			OntoContainer<ONTORES1> model1,
			ONTORES1 cpt1Inst,
			OntoContainer<ONTORES2> model2,
			ONTORES2 cpt2Inst) {
		
		// Verifier que des labels existents, sinon je ne peux rien
		SortedSet<String> langs1 = getLangsFrom1();
		SortedSet<String> langs2 = getLangsFrom2();
		
		Collection<LabelInformation> cpt1Labels = getLabelsForAlignement(model1, cpt1Inst, langs1);
		if(cpt1Labels.isEmpty()) return null;
		Collection<LabelInformation> cpt2Labels = getLabelsForAlignement(model2, cpt2Inst, langs2);
		if(cpt2Labels.isEmpty()) return null;
		
		// Bon, maintenant au boulot!
		for(LabelInformation oneLabelFrom1 : cpt1Labels) {
			for(LabelInformation oneLabelFrom2 : cpt2Labels) {
				String rawLabel1 = oneLabelFrom1.getLabel();
				String rawLabel2 = oneLabelFrom2.getLabel();
				// The local meta map, if necessary
				LexicalisationMetaMap meta = LexicalisationMetaMap.createMetaMap(
						rawLabel1,
						rawLabel2,
						oneLabelFrom1.getPrefLabel(),
						oneLabelFrom2.getPrefLabel(),
						oneLabelFrom1.getOrigins().toString(),
						oneLabelFrom2.getOrigins().toString());
				
				if(rawLabel1.equalsIgnoreCase(rawLabel2)) {
					Mapping<ONTORES1, ONTORES2> mapping = new Mapping<ONTORES1, ONTORES2>(cpt1Inst, cpt2Inst);
//					System.out.println(mapping);
					mapping.setMeta(meta);
					return mapping; // Pas possible de faire mieux...
				}
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return "Alignement exact";
	}

}
