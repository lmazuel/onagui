package fr.onagui.alignment.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;





import fr.onagui.alignment.Alignment;
import fr.onagui.alignment.Mapping;
import fr.onagui.alignment.Mapping.MAPPING_TYPE;
import fr.onagui.alignment.Mapping.VALIDITY;
import fr.onagui.alignment.OntoContainer;

public class SkosImpl implements IOAlignment {

	// Liste des namespaces
	public static final String ALIGN_NS = "http://knowledgeweb.semanticweb.org/heterogeneity/alignment#";
	public static final String DCTERMS_NS = "http://purl.org/dc/terms/";
	
	/* (non-Javadoc)
	 * @see fr.onagui.alignment.io.IOAlignment#loadAlignment(fr.onagui.alignment.OntoContainer, fr.onagui.alignment.OntoContainer, java.io.File)
	 */
	@Override
	public <ONTORES1, ONTORES2> Alignment<ONTORES1, ONTORES2> loadAlignment(
			OntoContainer<ONTORES1> onto1, OntoContainer<ONTORES2> onto2,
			File file) {

		if (file == null || !file.exists() || !file.isFile())
			throw new IllegalArgumentException(
					"File parameter is not a valid existing file.");
		throw new UnsupportedOperationException("SKOS load is not yet supported");
	}

	@Override
	public <ONTORES1, ONTORES2> void saveAlignment(
			Alignment<ONTORES1, ONTORES2> alignment, String pathToSave,
			VALIDITY validityWanted) throws IOException {
		ValueFactory factory = SimpleValueFactory.getInstance();
		Model model = new LinkedHashModel();
		model.setNamespace("skos", "http://www.w3.org/2004/02/skos/core#");
		
		/*
		Model model = ModelFactory.createDefaultModel();
		model.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
		model.setNsPrefix("", "http://www.w3.org/2004/02/skos/core#");*/
		
		
		final OntoContainer<ONTORES1> onto1 = alignment.getOnto1();
		final OntoContainer<ONTORES2> onto2 = alignment.getOnto2();
		
		Statement typeStatement =null;
		
		/*final Property exactMatch = model.createProperty("http://www.w3.org/2004/02/skos/core#exactMatch");
		final Property broadMatch = model.createProperty("http://www.w3.org/2004/02/skos/core#broadMatch");
		final Property narrowMatch = model.createProperty("http://www.w3.org/2004/02/skos/core#narrowMatch");
		final Property closeMatch = model.createProperty("http://www.w3.org/2004/02/skos/core#closeMatch");*/
		
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
			    
			else
				continue;
			IRI res1= factory.createIRI(onto1.getURI(mapping.getFirstConcept()).toString());
			IRI res2= factory.createIRI(onto2.getURI(mapping.getSecondConcept()).toString());
			typeStatement=factory.createStatement(res1, propertyToUsed, res2);
			model.add(typeStatement);
			
			/*Resource res1 = model.createResource(onto1.getURI(
					mapping.getFirstConcept()).toString());
			Resource res2 = model.createResource(onto2.getURI(
					mapping.getSecondConcept()).toString());
			res1.addProperty(propertyToUsed, res2);*/
		}
		
		FileOutputStream stream = new FileOutputStream(pathToSave);
		Rio.write(model, stream, RDFFormat.RDFXML);
		
	  /*OutputStream stream = Files.newOutputStream(Paths.get(pathToSave));
		stream.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes(ONAGUI_CHARSET));
		RDFWriter w = model.getWriter("RDF/XML-ABBREV");
		w.setProperty("showXmlDeclaration", "false");
		w.write(model, stream, "http://www.w3.org/2004/02/skos/core#");*/
//		FileOutputStream stream = new FileOutputStream(pathToSave);
//		RDFWriter writer = Rio.createWriter(RDFFormat.RDFXML,stream);
//		
//		
//		
//		try {
//			  writer.startRDF();
//			  for (Statement st: model) {
//			    writer.handleStatement(st);
//			  }
//			  writer.endRDF();
//			}
//			catch (RDFHandlerException e) {
//			 // oh no, do something!
//			}
		
	}
	
	
}


