package fr.onagui.alignment.io;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;

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

		Model model = ModelFactory.createDefaultModel();
		model.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
		model.setNsPrefix("", "http://www.w3.org/2004/02/skos/core#");
		
		final OntoContainer<ONTORES1> onto1 = alignment.getOnto1();
		final OntoContainer<ONTORES2> onto2 = alignment.getOnto2();
		
		// Build the mapping nodes
		final Property exactMatch = model.createProperty("http://www.w3.org/2004/02/skos/core#exactMatch");
		final Property broadMatch = model.createProperty("http://www.w3.org/2004/02/skos/core#broadMatch");
		final Property narrowMatch = model.createProperty("http://www.w3.org/2004/02/skos/core#narrowMatch");
		final Property closeMatch = model.createProperty("http://www.w3.org/2004/02/skos/core#closeMatch");
		for (Mapping<ONTORES1, ONTORES2> mapping : alignment.getMapping()) {
			if(validityWanted != null && !mapping.getValidity().equals(validityWanted))
				continue;
			
			Property propertyToUsed = null;
			if(mapping.getType() == MAPPING_TYPE.EQUIV)
				propertyToUsed = exactMatch;
			else if(mapping.getType() == MAPPING_TYPE.SUBSUMES)
				propertyToUsed = broadMatch;
			else if(mapping.getType() == MAPPING_TYPE.SUBSUMEDBY)
				propertyToUsed = narrowMatch;
			else if(mapping.getType() == MAPPING_TYPE.OVERLAP)
				propertyToUsed = closeMatch;
			else
				continue;
			
			Resource res1 = model.createResource(onto1.getURI(
					mapping.getFirstConcept()).toString());
			Resource res2 = model.createResource(onto2.getURI(
					mapping.getSecondConcept()).toString());
			res1.addProperty(propertyToUsed, res2);
		}
		
		OutputStream stream = Files.newOutputStream(Paths.get(pathToSave));
		stream.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes(ONAGUI_CHARSET));
		RDFWriter w = model.getWriter("RDF/XML-ABBREV");
		w.setProperty("showXmlDeclaration", "false");
		w.write(model, stream, "http://www.w3.org/2004/02/skos/core#");		
	}
}


