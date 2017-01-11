package fr.onagui.alignment.io;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import fr.onagui.alignment.Alignment;
import fr.onagui.alignment.OntoContainer;
import fr.onagui.alignment.Mapping.VALIDITY;

public interface IOAlignment {
	
	public static final Charset ONAGUI_CHARSET = Charset.forName("UTF-8");
	
	/** Allows other class to follow what happens during "save" and "load"
	 * @author Laurent Mazuel
	 */
	public interface IOEventManager {
		
		public void inputEvent(String msg);
		public void outputEvent(String msg);
	}

	public static String CANNOT_BE_ALIGNED = "Non alignable";

	/**
	 * Load an alignment from an XML file.
	 * 
	 * @param <ONTORES1>
	 *            Primitive type of ontology 1
	 * @param <ONTORES2>
	 *            Primitive type of ontology 2
	 * @param onto1
	 *            The ontology 1
	 * @param onto2
	 *            The ontology 2
	 * @param file
	 *            A XML file which contains an alignement.
	 * @return An alignment instance.
	 */
	public <ONTORES1, ONTORES2> Alignment<ONTORES1, ONTORES2> loadAlignment(
			OntoContainer<ONTORES1> onto1, OntoContainer<ONTORES2> onto2,
			File file) throws IOException;

	public <ONTORES1, ONTORES2> void saveAlignment(
			Alignment<ONTORES1, ONTORES2> alignment, String pathToSave,
			VALIDITY validityWanted) throws IOException;

}