/**
 * 
 */
package fr.onagui.alignment.method;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import fr.onagui.alignment.AbstractAlignmentMethod;
import fr.onagui.alignment.Mapping;
import fr.onagui.alignment.OntoContainer;
import fr.onagui.alignment.Mapping.MAPPING_TYPE;
import fr.onagui.gui.OntologyType;


/**
 * @author Laurent Mazuel
 * 
 * Prise en compte d'un nouveau paramètre dans l'algorithme de l'alignement ISubAlignment
 * 
 *  
 * 
 * 1-Vérification de l'existence des labels
 * 
 * 	 voir les méthodes LabelAlignmentMethod.getLangsForm1 et 2  et LabelAlignmentMethod.getLabelsForAlignement(model1, cpt1Inst, langs1)
     
     //retourne les langues 
    
 		SortedSet<String> langs1 = getLangsFrom1();pour le premier fichier
		SortedSet<String> langs2 = getLangsFrom2();// retourne les langues pour le deuxième fichier
		
	Il faudra au préalable les définir les langues grâce aux méthodes LabelAlignmentMethod.setLangsFrom1 et 2
	
	(voir  la méthode AlignmentAlgorithmMenuListener.setLangsForm1 et 2 dans la classe AlignmentGUI)
	
	La classe AlignmentAlgorithmMenuListener se trouvant dans AlignmentGUI a pour constructeur
 * 
 *     AlignmentAlgorithmMenuListener(AbstractAlignmentMethod<ONTORES1, ONTORES2> method)
 *     
 *   Ce constructeur prend en paramètre la methode d'alignement : ISubAlignment,ExactAlignment etc... 
 *   
 *   Cette méthode est obtenue en faisant appel à la méthode getLoadedAlignmentMethods() de la classe AlignmentControl.
 *   
 *   Il est appelé dans la classe AlignmentGUI pour mettre un ActionListener(attente d'un click) sur le menu Alignement crée.
 
	
	//Renvoie les labels en fonction des langues choisies. Pour obtenir les labels sans tags de langue, ajouter la chaine vide "" dans la liste
		
	Collection<LabelInformation> cpt1Labels = getLabelsForAlignement(model1, cpt1Inst, langs1);
 *   
 *   
 *   2- Faudra créer une méthode pour lire et écrire la date de modification des fichiers au chargement des fichiers.
 *   
 *    FileTime getModifiedTime(File file) throws IOException {
    	 Path p = Paths.get(file.getAbsolutePath());
         BasicFileAttributes view= Files.getFileAttributeView(p, BasicFileAttributeView.class).readAttributes();
    	 FileTime fileTime=view.lastModifiedTime();
    	 
    //  also available view.lastAccessTine and view.creationTime()
     
    	return fileTime;
  }
  
  
 *   Il faudra appeler cette méthode dans la méthode loadOntologyWithFileChooser
 *   
 *   Exemple:
 *   
 *   if(OntologyType.FIRST_ONTO_SKOS){
 *   	DateTime date1=getModifiedTime(file).toMillis();
 *   	setDateFile1(date1);
 *   }
 *   if(OntologyType.SECOND_ONTO_SKOS){
 *   	DateTime date2=getModifiedTime(file).toMillis();
 *   	setDateFile2(date2);
 *   }
 *   
 *   3- Récupérer dans la méthode computeMapping les dates de modifications des fichiers
 *   
 *   	DateTime datefile1=new Alignment.getDateFile1();
 *      DateTime datefile2=new Alignment.getDateFile2();	
 *   
 *   4-Prendre en compte les dates dans les tests d'équivalence (à voir)
 *   
 *   if(datefile1.equals(datefile2)){
 *   	traitement 1
 *   }else{
 *   	traitement 2
 *   }
 *  
 *  NB: chaque de méthode d'alignement  est utilisée dans ALignmentControler dans le constructeur de la manière suivante :
 *  
 *  public void AlignmentControler(){
 *  	Set<Class<? extends AbstractAlignmentMethod>> classes = new HashSet<Class<? extends AbstractAlignmentMethod>>();
 *  	classes.add(LevenshteinAlignmentMethod.class.asSubclass(AbstractAlignmentMethod.class));
 *  	classes.add(ISubAlignmentMethod.class.asSubclass(AbstractAlignmentMethod.class));
 *  	classes.add(ExactAlignmentMethod.class.asSubclass(AbstractAlignmentMethod.class));
 *  }
 */
public class NewAlignment<ONTORES1, ONTORES2> extends LabelAlignmentMethod<ONTORES1, ONTORES2> {
	
	public static final double DEFAULT_ISUB_THRESHOLD = 0.90;
/**
	 * @param current_threshlod
	 */
	public NewAlignment(double current_threshlod) {
		setThreshold(current_threshlod);
	}
	
	/** Constructeur par défaut.
	 * Utilise le seuil {@link #DEFAULT_ISUB_THRESHOLD}.
	 * 
	 */
	public NewAlignment() {
		this(DEFAULT_ISUB_THRESHOLD);
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

				double isubScore = I_Sub.score(rawLabel1, rawLabel2);
				if(isubScore == 1.0) {
					Mapping<ONTORES1, ONTORES2> mapping = new Mapping<ONTORES1, ONTORES2>(cpt1Inst, cpt2Inst, isubScore, MAPPING_TYPE.EQUIV);
					mapping.setMeta(meta);
					return mapping;
				}
				else if(isubScore >= getThreshold() &&
						(currentBestMapping == null ||
						 (currentBestMapping != null && currentBestMapping.getScore() < isubScore))) { // Meilleur que celui qu'on a déjà
					
					Mapping<ONTORES1, ONTORES2> mapping = new Mapping<ONTORES1, ONTORES2>(cpt1Inst, cpt2Inst, isubScore, MAPPING_TYPE.EQUIV);
					mapping.setMeta(meta);
					currentBestMapping = mapping;
//					System.out.println(mapping);
				}
			}
		}
		return currentBestMapping;
	}
	
	@Override
	public String toString() {
		return "New Alignment"; 
	}

}
