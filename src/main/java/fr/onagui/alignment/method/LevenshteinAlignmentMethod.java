/**
 * 
 */
package fr.onagui.alignment.method;

import java.util.Collection;
import java.util.SortedSet;

import fr.onagui.alignment.Mapping;
import fr.onagui.alignment.OntoContainer;
import fr.onagui.alignment.Mapping.MAPPING_TYPE;


/**
 * @author Laurent Mazuel
 */
public class LevenshteinAlignmentMethod<ONTORES1, ONTORES2> extends LabelAlignmentMethod<ONTORES1, ONTORES2> {
	
	public static final double DEFAULT_LEVENSHTEIN_THRESHOLD = 0.97;
	
	/**
	 * @param current_threshlod
	 */
	public LevenshteinAlignmentMethod(double current_threshlod) {
		setThreshold(current_threshlod);
	}
	
	/** Constructeur par défaut.
	 * Utilise le seuil {@link #DEFAULT_LEVENSHTEIN_THRESHOLD}.
	 * 
	 */
	public LevenshteinAlignmentMethod() {
		this(DEFAULT_LEVENSHTEIN_THRESHOLD);
	}

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
		LabelAlignmentMethod.applyNLPFilterToLabels(cpt1Labels);
		LabelAlignmentMethod.applyNLPFilterToLabels(cpt2Labels);
		
		// Bon, maintenant au boulot!
		Mapping<ONTORES1, ONTORES2> currentBestMapping = null;
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
				
				if(rawLabel1.equals(rawLabel2)) {
					Mapping<ONTORES1, ONTORES2> mapping = new Mapping<ONTORES1, ONTORES2>(cpt1Inst, cpt2Inst);
//					System.out.println(mapping);
					mapping.setMeta(meta);
					return mapping; // Pas possible de faire mieux...
				}
				
				double levenScore = 1.0 - LevenshteinDistance.computeNormalizedLevenshteinDistance(rawLabel1.toCharArray(), rawLabel2.toCharArray());
				
				if(levenScore >= getThreshold() &&
						(currentBestMapping == null ||
						 (currentBestMapping != null && currentBestMapping.getScore() < levenScore))) { // Meilleur que celui qu'on a déjà
					
					Mapping<ONTORES1, ONTORES2> mapping = new Mapping<ONTORES1, ONTORES2>(cpt1Inst, cpt2Inst, levenScore, MAPPING_TYPE.EQUIV);
					mapping.setMeta(meta);
					currentBestMapping = mapping;
//					System.out.println(mapping);
				}
				// Tentative de detection de sous-classe:
				// Trop violent comme test: [delivrance_du_placenta_suite_a_un_accouchement_hors_hopital-cou:1.0-SUB]
//				else if(snoCptLabel.contains(pneCptLabel) || pneCptLabel.contains(snoCptLabel)) {
//				LabelMapping<String> mapping = new LabelMapping<String>(snoCptLabel, pneCptLabel, 1.0, MAPPING_TYPE.SUB);
//				result.add(mapping);
//				}
			}
		}
		return currentBestMapping;
	}
	
	@Override
	public String toString() {
		return "Distance de Levenshtein";
	}

}
