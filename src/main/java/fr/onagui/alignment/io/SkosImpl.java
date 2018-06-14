package fr.onagui.alignment.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.joda.time.DateTime;

import fr.onagui.alignment.Alignment;
import fr.onagui.alignment.Mapping;
import fr.onagui.alignment.Mapping.MAPPING_TYPE;
import fr.onagui.alignment.Mapping.VALIDITY;
import fr.onagui.alignment.OntoContainer;

public class SkosImpl implements IOAlignment {

	// Liste des namespaces
	public static final String ALIGN_NS = "http://knowledgeweb.semanticweb.org/heterogeneity/alignment#";
	public static final String DCTERMS_NS = "http://purl.org/dc/terms/";
	public static final String METHOD = "manual";

	// Type et propriétés perso
	private IOEventManager ioEventManager = null;

	/** Default constructor.
	 * All warning messages are sended to stderr.
	 * 
	 */
	public SkosImpl() {
		this(new IOEventManager() {			
			@Override
			public void outputEvent(String msg) {
				System.err.println(msg);
			}
			
			@Override
			public void inputEvent(String msg) {
				System.err.println(msg);
			}
		});
	}
	
	
	public SkosImpl(IOEventManager ioe) {
		this.ioEventManager = ioe;
	}
	
	/* (non-Javadoc)
	 * @see fr.onagui.alignment.io.IOAlignment#loadAlignment(fr.onagui.alignment.OntoContainer, fr.onagui.alignment.OntoContainer, java.io.File)
	 */
	@Override
	public <ONTORES1, ONTORES2> Alignment<ONTORES1, ONTORES2> loadAlignment(
			OntoContainer<ONTORES1> onto1,
			OntoContainer<ONTORES2> onto2,
			File file
	) {

		if (file == null || !file.exists() || !file.isFile())
			throw new IllegalArgumentException(
					"File parameter is not a valid existing file.");

		// la date des correspondances sera toujours mise à la date de création du fichier
		DateTime date=new DateTime(file.lastModified());
		
		Model model = new LinkedHashModel();

		try {
			model=readModel(file,model);
		} catch (Exception e) {
			// Impossible, j'ai testé l'existence au dessus...
			e.printStackTrace();
			throw new IllegalArgumentException("Unable to read file. Right problem?");
		}

		Alignment<ONTORES1, ONTORES2> alignment = new Alignment<ONTORES1, ONTORES2>(
				onto1, onto2);

		for (Statement unTriplet: model) {

			if(
					unTriplet.getPredicate().equals(SKOS.EXACT_MATCH)
					||
					unTriplet.getPredicate().equals(SKOS.CLOSE_MATCH)
					||
					unTriplet.getPredicate().equals(SKOS.BROAD_MATCH)
					||
					unTriplet.getPredicate().equals(SKOS.NARROW_MATCH)
					||
					unTriplet.getPredicate().equals(SKOS.RELATED_MATCH)
					) {

				// Manage method
				String method = METHOD;
				// Manage validity
				VALIDITY valid = VALIDITY.VALID;
				// Manage creation date				
				//DateTime date = new DateTime();
				double score =1.0;

				MAPPING_TYPE type = null;
				if(unTriplet.getPredicate().equals(SKOS.EXACT_MATCH)) {
					type = MAPPING_TYPE.EQUIV;
				}
				else if(unTriplet.getPredicate().equals(SKOS.CLOSE_MATCH)) {
					type = MAPPING_TYPE.OVERLAP;
				}
				else if(unTriplet.getPredicate().equals(SKOS.BROAD_MATCH)) {
					type = MAPPING_TYPE.SUBSUMES;
				}
				else if(unTriplet.getPredicate().equals(SKOS.NARROW_MATCH)) {
					type = MAPPING_TYPE.SUBSUMEDBY;
				}
				else if(unTriplet.getPredicate().equals(SKOS.RELATED_MATCH)) {
					type = MAPPING_TYPE.RELATED;
				} else {
					String errMsg = "Oops, should not happen, found an unexpected predicate "+unTriplet.getPredicate();
					ioEventManager.inputEvent(errMsg);
					continue;
				}


				Mapping<ONTORES1, ONTORES2> map;
				try {
					String uri1 = unTriplet.getSubject().stringValue();
					String uri2 = unTriplet.getObject().stringValue();
					// Prepare errMSg, maybe....
					String errMsg = "Loading alignment between " + uri1
							+ " and " + uri2
							+ " failed because this concepts does not exist: ";
					ONTORES1 cpt1 = onto1.getConceptFromURI(new URI(uri1));
					if (cpt1 == null) {
						ioEventManager.inputEvent(errMsg + uri1);
						continue;
					}
					ONTORES2 cpt2 = onto2.getConceptFromURI(new URI(uri2));
					if (cpt2 == null) {
						ioEventManager.inputEvent(errMsg + uri2);
						continue;
					}			

					map = new Mapping<ONTORES1, ONTORES2>(
							cpt1, cpt2, score, type, method, valid, date);

					alignment.addMap(map);
				} catch (URISyntaxException e) {
					ioEventManager.inputEvent("One of the value is not a valid uri");
					e.printStackTrace();
				}


			} else {
				System.out.println("Cannot understand predicate "+unTriplet.getPredicate());
			}


		}

		return alignment;
	}

	public Model readModel(File file,Model model ){
		try {
			RDFFormat format = Rio.getParserFormatForFileName(file.getAbsolutePath()).orElse(RDFFormat.RDFXML);
			InputStream inputStream= new FileInputStream(file);
			try {
				model= Rio.parse(inputStream, file.getAbsolutePath(),format);
			} catch (RDFParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedRDFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return model;
	}

	@Override
	public <ONTORES1, ONTORES2> void saveAlignment(
			Alignment<ONTORES1, ONTORES2> alignment, String pathToSave,
			VALIDITY validityWanted) throws IOException {


		ValueFactory factory = SimpleValueFactory.getInstance();
		Model model = new LinkedHashModel();
		model.setNamespace("skos", "http://www.w3.org/2004/02/skos/core#");
		model.setNamespace("", "http://www.w3.org/2004/02/skos/core#");

		final OntoContainer<ONTORES1> onto1 = alignment.getOnto1();
		final OntoContainer<ONTORES2> onto2 = alignment.getOnto2();

		Statement typeStatement =null;

		for (Mapping<ONTORES1, ONTORES2> mapping : alignment.getMapping()) {
			if(validityWanted != null && !mapping.getValidity().equals(validityWanted))
				continue;

			IRI propertyToUsed = null;

			if(mapping.getType() == MAPPING_TYPE.EQUIV)
				propertyToUsed = SKOS.EXACT_MATCH;

			else if(mapping.getType() == MAPPING_TYPE.SUBSUMES)
				propertyToUsed = SKOS.BROAD_MATCH;
			else if(mapping.getType() == MAPPING_TYPE.SUBSUMEDBY)
				propertyToUsed = SKOS.NARROW_MATCH;		
			else if(mapping.getType() == MAPPING_TYPE.OVERLAP)
				propertyToUsed = SKOS.CLOSE_MATCH;			   
			else if(mapping.getType() == MAPPING_TYPE.RELATED)
				propertyToUsed = SKOS.RELATED;
			else
				continue;

			IRI res1= factory.createIRI(onto1.getURI(mapping.getFirstConcept()).toString());
			IRI res2= factory.createIRI(onto2.getURI(mapping.getSecondConcept()).toString());
			typeStatement=factory.createStatement(res1, propertyToUsed, res2);
			model.add(typeStatement);
		}

		FileOutputStream stream = new FileOutputStream(pathToSave);
		Rio.write(model, stream, RDFFormat.RDFXML);	
	}
}


