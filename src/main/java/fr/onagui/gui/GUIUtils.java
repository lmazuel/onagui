package fr.onagui.gui;

import java.io.File;
import java.net.URI;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import fr.onagui.alignment.container.DOEOWLContainer;
import fr.onagui.alignment.container.RDFModelContainer;
import fr.onagui.alignment.container.SKOSContainer;
import fr.onagui.control.ERASE_TYPE;


public class GUIUtils {
	
	/** Static class only */
	private GUIUtils() {}
		
	public static SKOSContainer loadSKOSOntologyWithGUI(JFrame frame, URI filename) throws OutOfMemoryError {
		SKOSContainer container = null;
		try {
			container = new SKOSContainer(new File(filename));
		} catch (Exception e) {
			String message = Messages.getString("GUIUtilsSkosLoadingErrorPreFilename")+filename; //$NON-NLS-1$
			JOptionPane.showMessageDialog(frame, message+Messages.getString("GUIUtilsSkosLoadingErrorPostFilename"), Messages.getString("GUIUtilsSkosLoadingErrorTitle"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			System.err.println(message);
			e.printStackTrace();
		}
		return container;
	}
	
	public static RDFModelContainer loadRDFOntologyWithGUI(JFrame frame, URI filename) throws OutOfMemoryError {
		RDFModelContainer container = null;
		try {
			container = new RDFModelContainer(new File(filename));
		} catch (Exception e) {
			String message = Messages.getString("GUIUtilsRdfLoadingErrorPreFilename")+filename; //$NON-NLS-1$
			JOptionPane.showMessageDialog(frame, message+Messages.getString("GUIUtilsRdfLoadingErrorPostFilename"), Messages.getString("GUIUtilsRdfLoadingErrorTitle"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			System.err.println(message);
			e.printStackTrace();
		}
		return container;
	}

	public static DOEOWLContainer loadDOEOntologyWithGUI(JFrame frame, URI filename) {
		DOEOWLContainer container = null;
		try {
			container = new DOEOWLContainer(filename);
		} catch (OWLOntologyCreationException e) {
			String message = Messages.getString("GUIUtilsOwlLoadingErrorPreFilename")+filename; //$NON-NLS-1$
			JOptionPane.showMessageDialog(frame, message+Messages.getString("GUIUtilsOwlLoadingErrorPostFilename"), Messages.getString("GUIUtilsOwlLoadingErrorTitle"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			System.err.println(message);
			e.printStackTrace();
		} catch (Exception e) {
			String message = Messages.getString("GUIUtilsOwlHierarchyErrorPreFilename")+filename; //$NON-NLS-1$
			JOptionPane.showMessageDialog(frame, message+Messages.getString("GUIUtilsOwlHierarchyErrorPostFilename"), Messages.getString("GUIUtilsOwlHierarchyErrorTitle"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			System.err.println(message);
			e.printStackTrace();
		}
		return container;
	}
	
	public static ERASE_TYPE chooseEraseType(JFrame frame) {
		String message = Messages.getString("GUIUtilsEraseChoice") ; //$NON-NLS-1$
		ERASE_TYPE return_val = (ERASE_TYPE)JOptionPane.showInputDialog(frame,
				message,
				Messages.getString("GUIUtilsEraseChoiceTitle"), //$NON-NLS-1$
				JOptionPane.WARNING_MESSAGE,
				null,
				ERASE_TYPE.values(),
				ERASE_TYPE.NO_ERASE);
		return return_val;
	}
}
