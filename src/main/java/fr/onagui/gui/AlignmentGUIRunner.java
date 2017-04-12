package fr.onagui.gui;

import java.io.File;

/**
 * Classe de lancement permettant de lancer l'application en chargeant directement les vocabulaires 1 et 2
 * et optionnement un alignement.
 * Utilisé pour accélérer les tests !
 * 
 * @author thomas
 *
 */
public class AlignmentGUIRunner {

	public static void main(String[] args) {
		System.out.println("Starting OnaGUI..."); //$NON-NLS-1$
		
		String file1Path = args[0];
		String file2Path = args[1];
		String alignmentPath = (args.length >= 3)?args[2]:null;
		
		//Schedule a job for the event dispatch thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				AlignmentGUI gui = new AlignmentGUI();
				gui.loadOntologyFromFileReference(OntologyType.FIRST_ONTO_SKOS, new File(file1Path).toURI());
				gui.loadOntologyFromFileReference(OntologyType.SECOND_ONTO_SKOS, new File(file2Path).toURI());
				gui.loadAlignementFromFileReference(new File(alignmentPath));
			}
		});
	}

}
