package fr.onagui.alignment;


/** Quelques utilitaires classiques en TAL.
 * @author Laurent Mazuel
 */
public class NLPTools {
	
	public static final String DIACRITIC_CHAR = "ÀÁÂÃÄÅÇÈÉÊËÌÍÎÏÑÒÓÔÕÖÙÚÛÜÝàáâãäåçèéêëìíîïñòóôõöùúûüýÿ";
	public static final String NON_DIACRITIC_CHAR = "AAAAAACEEEEIIIINOOOOOUUUUYaaaaaaceeeeiiiinooooouuuuyy";

	/** Retire tous les accents d'un mot. Ne gère que la syntaxe française.
	 * @param s Une string.
	 * @return Une string sans accent.
	 */
	public static String retireDiacritique(String s) {
		String result = s;
		for(int i=0; i<DIACRITIC_CHAR.length(); i++) {
			result = result.replace(DIACRITIC_CHAR.charAt(i), NON_DIACRITIC_CHAR.charAt(i));
		}
		return result;
	}
	
	/** Retire les doubles commes "œ" contre des lettres classiques comme "oe".
	 * Gère actuellement œ, Œ, æ, Æ.
	 * Attention, la chaine finale peut être plus longue que la chaine initiale (logique,
	 * mais à tenir en compte si on boucle quelque part en utilisant cette fonction...)
	 * @param s Une string.
	 * @return Une string sans double lettre.
	 */
	public static String retireDoubleLettre(String s) {
		String toWork = new String(s);
		toWork = toWork.replace("œ", "oe");
		toWork = toWork.replace("Œ", "Oe");
		toWork = toWork.replace("æ", "ae");
		toWork = toWork.replace("Æ", "Ae");
		return toWork;
	}
	
	/** Gestion des mots vides de sens pour un alignement, comme "de" ou "l'".
	 * @author Laurent Mazuel
	 */
	public enum MotsVide {
		DE(" de "),
		L_AP(" l'");
		
		private String chaine = null;
		
		MotsVide(String chaine) {
			this.chaine = chaine;
		}
		
		public static String retireMotsVide(String phrase) {
			String result = new String(phrase);
			for(MotsVide mv : MotsVide.values()) {
				result = result.replace(mv.chaine, " ");
			}
			return result;
		}
	}
	
	/** Spécifique à SNOMED, retire les chaines "SAI" et "AT".
	 * A faire AVANT un appel à la fonction {@link #convertLabelForAlignment(String)}
	 * @param toWork
	 * @return
	 * @see #convertLabelForAlignment(String)
	 */
	public static String convertFromSnomed(String toWork) {
		if(toWork.contains(", SAI")) toWork = toWork.replace(", SAI", ""); // specifique à Snomed
		if(toWork.contains(", AT")) toWork = toWork.replace(", AT", ""); // specifique à Snomed
		return toWork;
	}

	/** Convertit un label en label utilisable pour un alignement.
	 * Peut renvoyer <code>null</code> si le label ne doit pas etre
	 * considéré pour un alignement.
	 * @param label
	 * @return
	 */
	public static String convertLabelForAlignment(String label) {
		if(label == null) return null;
		
		// Some classical NLP simplification
		String toWork = new String(label);
		toWork = retireDiacritique(toWork);
		toWork = retireDoubleLettre(toWork);
		toWork = toWork.toLowerCase();
		// Eventuellement, en fonction du contexte....
		toWork = MotsVide.retireMotsVide(toWork);

		// Retirer tous les caractères qui ne sont pas [A-Z][a-z]_[0-9]
		return toWork.replaceAll("[\\W_]", "");
	}
	
	public static void main(String[] args) {
		String test = "je suis d'accord et fatigué de faire ça pendant le pique-nique avec mon œdème _ test!!!";
		
		System.out.println("Avant: "+test);
		System.out.println("Apres: "+convertLabelForAlignment(test));
	}
	
}
