package fr.onagui.alignment.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.eclipse.rdf4j.rio.rdfxml.util.RDFXMLPrettyWriter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import fr.onagui.alignment.Alignment;
import fr.onagui.alignment.Mapping;
import fr.onagui.alignment.Mapping.MAPPING_TYPE;
import fr.onagui.alignment.Mapping.VALIDITY;
import fr.onagui.alignment.NoMappingPossible;
import fr.onagui.alignment.OntoContainer;

public class EuzenatRDFImpl implements IOAlignment {

	// Liste des namespaces
	public static final String ALIGN_NS = "http://knowledgeweb.semanticweb.org/heterogeneity/alignment#";
	public static final String DCTERMS_NS = "http://purl.org/dc/terms/";

	// Type et propriété conforme à J.Euzenat
	public static IRI XML_PROPERTY =SimpleValueFactory.getInstance().createIRI(ALIGN_NS + "xml");
	public static IRI LEVEL_PROPERTY = SimpleValueFactory.getInstance().createIRI(ALIGN_NS + "level");
	public static IRI TYPE_PROPERTY = SimpleValueFactory.getInstance().createIRI(ALIGN_NS + "type");
	public static IRI LOCATION_PROPERTY = SimpleValueFactory.getInstance().createIRI(ALIGN_NS + "location");
	public static IRI FORMALISM_PROPERTY = SimpleValueFactory.getInstance().createIRI(ALIGN_NS + "formalism");
	public static IRI ENTITY1_PROPERTY = SimpleValueFactory.getInstance().createIRI(ALIGN_NS + "entity1");
	public static IRI ENTITY2_PROPERTY = SimpleValueFactory.getInstance().createIRI(ALIGN_NS + "entity2");
	public static IRI MEASURE_PROPERTY =SimpleValueFactory.getInstance().createIRI(ALIGN_NS + "measure");
	public static IRI RELATION_PROPERTY = SimpleValueFactory.getInstance().createIRI(ALIGN_NS + "relation");
	public static IRI ONTO1_PROPERTY = SimpleValueFactory.getInstance().createIRI(ALIGN_NS + "onto1");
	public static IRI ONTO2_PROPERTY = SimpleValueFactory.getInstance().createIRI(ALIGN_NS + "onto2");
	public static IRI MAP_PROPERTY = SimpleValueFactory.getInstance().createIRI(ALIGN_NS + "map");
	public static IRI METHOD_PROPERTY = SimpleValueFactory.getInstance().createIRI(ALIGN_NS + "method");
	public static IRI VALID_PROPERTY = SimpleValueFactory.getInstance().createIRI(ALIGN_NS + "validity");
	public static IRI CREATION_DATE_PROPERTY = SimpleValueFactory.getInstance().createIRI(DCTERMS_NS + "date");
	public static IRI METAMETHOD_PROPERTY = SimpleValueFactory.getInstance().createIRI(ALIGN_NS + "metamethod");
	public static IRI ALIGNMENT_TYPE = SimpleValueFactory.getInstance().createIRI(ALIGN_NS + "Alignment");
	public static IRI ONTOLOGY_TYPE = SimpleValueFactory.getInstance().createIRI(ALIGN_NS + "Ontology");
	//	private Resource formalismType = null;
	public static IRI CELL_TYPE = SimpleValueFactory.getInstance().createIRI(ALIGN_NS + "Cell");
	// Type et propriétés perso
	public static IRI NOMAP_PROPERTY = SimpleValueFactory.getInstance().createIRI(ALIGN_NS + "nomap");

	private DateTimeFormatter timeFormatter = null;
	
	private IOEventManager ioEventManager = null;

	/** Default constructor.
	 * All warning messages are sended to stderr.
	 * 
	 */
	public EuzenatRDFImpl() {
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
	
	/** Main constructor, with EventManager for warning
	 * @param ioe
	 */
	public EuzenatRDFImpl(IOEventManager ioe) {
		this.ioEventManager = ioe;
		timeFormatter = ISODateTimeFormat.dateTimeNoMillis();
	}

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

		Model model = new LinkedHashModel();
		
		// read the RDF/XML file
		try {
			RDFFormat format = Rio.getParserFormatForFileName(file.getAbsolutePath()).orElse(RDFFormat.RDFXML);
			model = Rio.parse(new FileInputStream(file), file.getAbsolutePath(), format);
		} catch (FileNotFoundException e) {
			// Impossible, j'ai testé l'existence au dessus...
			e.printStackTrace();
			throw new IllegalArgumentException("Unable to read file. Right problem?");
		} catch (RDFParseException e) {
			throw new RuntimeException("Unable to read RDF file "+file.getAbsolutePath(), e);
		} catch (UnsupportedRDFormatException e) {
			throw new RuntimeException("Unable to read RDF file "+file.getAbsolutePath(), e);
		} catch (IOException e) {
			throw new RuntimeException("Unable to read RDF file "+file.getAbsolutePath(), e);
		}
		
		
		Alignment<ONTORES1, ONTORES2> alignment = new Alignment<ONTORES1, ONTORES2>(
				onto1, onto2);
		
		
		for (Resource alignement: model.filter(null, RDF.TYPE, ALIGNMENT_TYPE).subjects()) {

			// Ajouter chaque mapping
			for (Value cellValue: model.filter(alignement, MAP_PROPERTY, null).objects()) {
				Resource cellNode = (Resource)cellValue;
				Resource entity1 = (Resource)getRequiredProperty(cellNode, ENTITY1_PROPERTY, model);
				Resource entity2 = (Resource)getRequiredProperty(cellNode, ENTITY2_PROPERTY, model);
				Value measureValue =getRequiredProperty(cellNode, MEASURE_PROPERTY, model);
				
				
				double score = Double.valueOf(((Literal)measureValue).stringValue());

				Value relationStmt = getRequiredProperty(cellNode, RELATION_PROPERTY, model);

				final String relationStringFromRDF = relationStmt.stringValue();
				MAPPING_TYPE type = MAPPING_TYPE
						.getTypeFromString(relationStringFromRDF);
				// FIXME Si je trouves une "Cell" mais que je ne reconnais pas
				// le type, j'affiche en erreur et je met equiv. Bonne idée?
				if (type == null) {
					type = MAPPING_TYPE.EQUIV;
					System.err.println("Relation type was not recognized: "
							+ relationStringFromRDF);
				}
				// Manage method

				Value methodValue = getProperty(cellNode,METHOD_PROPERTY,model);
				String method = (methodValue != null) ? methodValue.stringValue()
						: Mapping.UNKNOW_METHOD;
				
				// Manage validity				
				Value validValue = getProperty(cellNode,VALID_PROPERTY,model);
				VALIDITY valid = (validValue != null) ? VALIDITY
						.valueOf(validValue.stringValue()) : VALIDITY.TO_CONFIRM;
						
				// Manage creation date
				Value creationDateValue = getProperty(cellNode,CREATION_DATE_PROPERTY,model);
				DateTime date = (creationDateValue != null) ? timeFormatter
						.parseDateTime(creationDateValue.stringValue())
						: new DateTime();
						
				// Manage meta				
				Resource metaRes = (Resource)getProperty(cellNode,METAMETHOD_PROPERTY,model);
				Map<String, String> metaMap = new TreeMap<String, String>();
				if (metaRes != null) {
					
					for (Statement metaStmt: model.filter(metaRes, null,null)) {
						// Manage predidate
						IRI metaProp = metaStmt.getPredicate();
						String key = metaProp.getLocalName();
						// Manage object
						String value = metaStmt.getObject().stringValue();
						// Adding to metamap
						metaMap.put(key, value);
					}
				}
				
				// Manage comment
				Value commentValue = getProperty(cellNode,RDFS.COMMENT,model);

				try {
					String uri1 = entity1.stringValue();
					String uri2 = entity2.stringValue();
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

					Mapping<ONTORES1, ONTORES2> map = new Mapping<ONTORES1, ONTORES2>(
							cpt1, cpt2, score, type, method, valid, date);
					if (commentValue != null) {
						map.setComment(commentValue.stringValue());
					}
					if (!metaMap.isEmpty()) {
						map.setMeta(metaMap);
					}

					alignment.addMap(map);
				} catch (URISyntaxException e) {
					ioEventManager.inputEvent("One the value is not a valid uri");
					e.printStackTrace();
				}
			}

			// Ajouter chaque mapping impossible
			Model iterator = model.filter(alignement, NOMAP_PROPERTY, null);
			for(Statement stat:iterator) {
				Statement cellNodeStmt = stat;
				Resource cellNode = (Resource)cellNodeStmt.getObject();
				// FIXME "cellNode" devrait être un un noeud de Type Cell.
				// Verification à faire?
				Resource entity1 = (Resource)getProperty(cellNode, ENTITY1_PROPERTY, model);
				Resource entity2 = (Resource)getProperty(cellNode, ENTITY2_PROPERTY, model);

				try {
					if (entity1 != null) {
						ONTORES1 cpt1 = onto1.getConceptFromURI(new URI(entity1.stringValue()));
						NoMappingPossible<ONTORES1> nomap = new NoMappingPossible<ONTORES1>(
								cpt1);
						alignment.addImpossibleMappingFrom1(nomap);
					} else {
						ONTORES2 cpt2 = onto2.getConceptFromURI(new URI(entity2.stringValue()));
						NoMappingPossible<ONTORES2> nomap = new NoMappingPossible<ONTORES2>(
								cpt2);
						alignment.addImpossibleMappingFrom2(nomap);
					}
				} catch (URISyntaxException e) {
					ioEventManager.inputEvent("Your URI was not valid, statement ignored: "
									+ cellNodeStmt);
					e.printStackTrace();
				}
			}
		}
		return alignment;
	}
	
	public Value getRequiredProperty(Resource node, IRI property, Model model){				
		for (Statement statement:model.filter(node, property, null)) {
			return statement.getObject();
		} 
		throw new RuntimeException("Unable to find required property "+property+" on resource "+node);
	}
	
	public Value getProperty(Resource node, IRI property, Model model){				
		for (Statement statement:model.filter(node, property, null)) {
			return statement.getObject();
		} 
		return null;
	}

	/* (non-Javadoc)
	 * @see fr.onagui.alignment.io.IOAlignment#saveAlignment(fr.onagui.alignment.Alignment, java.lang.String, fr.onagui.alignment.Mapping.VALIDITY)
	 */
	@Override
	public <ONTORES1, ONTORES2> void saveAlignment(
			Alignment<ONTORES1, ONTORES2> alignment, String pathToSave,
			VALIDITY validityWanted) throws IOException {
		
		Model model =new LinkedHashModel();
		ValueFactory factory = SimpleValueFactory.getInstance();
		model.setNamespace("",ALIGN_NS);
		model.setNamespace("dcterms",DCTERMS_NS);
		

		// Creation de notre arbre
		BNode rootAlignment= createAnonymousType(model,factory,ALIGNMENT_TYPE);
		model.add(rootAlignment,XML_PROPERTY, factory.createLiteral("yes"));
		model.add(rootAlignment,LEVEL_PROPERTY, factory.createLiteral("0"));
		model.add(rootAlignment,TYPE_PROPERTY, factory.createLiteral("**"));
		final OntoContainer<ONTORES1> onto1 = alignment.getOnto1();
		final OntoContainer<ONTORES2> onto2 = alignment.getOnto2();

		// Meta-data about ontology
		buildOntoMetaData(model, rootAlignment,factory,onto1, 1);
		buildOntoMetaData(model, rootAlignment, factory,onto2, 2);
		// Build the mapping nodes
		for (Mapping<ONTORES1, ONTORES2> mapping : alignment.getMapping()) {
			buildCellNode(onto1, onto2, mapping, model, rootAlignment,
					validityWanted);
		}

		// Build the no mapping nodes
		for (NoMappingPossible<ONTORES1> noMap : alignment
				.getImpossibleMapping1().values()) {
			buildNoMapNode(onto1, noMap, 1, model, factory, rootAlignment);
		}
		for (NoMappingPossible<ONTORES2> noMap : alignment
				.getImpossibleMapping2().values()) {
			buildNoMapNode(onto2, noMap, 2, model,factory, rootAlignment);
		}
		
		
		
		FileOutputStream stream = new FileOutputStream(pathToSave);
	
		RDFWriter prettyWriter = new RDFXMLPrettyWriter(stream);
		Rio.write(model, prettyWriter);
		
		
	}

	private static BNode createAnonymousType(Model model, ValueFactory factory, IRI type) {
		BNode rootAlignment= factory.createBNode();
		model.add(rootAlignment, RDF.TYPE, type);
		return rootAlignment;
		
	}

	
	private <ONTORES> void buildOntoMetaData(Model model, Resource root, ValueFactory factory,
			OntoContainer<ONTORES> onto, int number) {
		
		IRI ontologyResource = factory.createIRI(ALIGN_NS + onto.getURI());
		
		if (number == 1) {			
			model.add(root, ONTO1_PROPERTY, ontologyResource);			
		} else {
			model.add(root, ONTO2_PROPERTY, ontologyResource);
		}
		
		model.add(ontologyResource, RDF.TYPE, ONTOLOGY_TYPE);
		model.add(ontologyResource, LOCATION_PROPERTY,factory.createLiteral(onto.getURI().toString()));
		model.add(ontologyResource, FORMALISM_PROPERTY,factory.createLiteral(onto.getFormalism()));
		
	}

	private <ONTORES1, ONTORES2> void buildCellNode(
			OntoContainer<ONTORES1> onto1, OntoContainer<ONTORES2> onto2,
			Mapping<ONTORES1, ONTORES2> mapping, Model model,
			Resource alignmentRoot, VALIDITY validityWanted) {
		ValueFactory factory = SimpleValueFactory.getInstance();
		
		VALIDITY currentMappingValidity = mapping.getValidity();
		if (validityWanted != null
				&& !currentMappingValidity.equals(validityWanted))
			return;

		// ordre d'insertion des proprietes necessaires pour avoir une "belle"
		// ecriture dans le fichier RDF/XML
		
		// on créé le noeud correspondant a la Cell		
		BNode cellNode = factory.createBNode();
		// on relie l'objet "Alignement" a ce noeud par la propriete "map"
		model.add(alignmentRoot, MAP_PROPERTY,cellNode);
		// on donne le type de l'objet "Cell"
		model.add(cellNode, RDF.TYPE, CELL_TYPE);
		
		IRI res1 = factory.createIRI(onto1.getURI(
				mapping.getFirstConcept()).toString());
		IRI res2 = factory.createIRI(onto2.getURI(
				mapping.getSecondConcept()).toString());

		
		final String comment = mapping.getComment();
		if (comment != null && !"".equals(comment)) {
			model.add(cellNode, RDFS.COMMENT,factory.createLiteral(comment));
		}
		model.add(cellNode, VALID_PROPERTY,factory.createLiteral(currentMappingValidity.toString()));
		model.add(cellNode, RELATION_PROPERTY,factory.createLiteral(mapping.getType().toString()));
		model.add(cellNode, MEASURE_PROPERTY,factory.createLiteral((float) mapping.getScore()));
		model.add(cellNode, ENTITY2_PROPERTY,res2);
		model.add(cellNode, ENTITY1_PROPERTY,res1);
		
		// we force the date format to be exactly what we want
		// see http://stackoverflow.com/questions/10614771/java-simpledateformat-pattern-for-w3c-xml-dates-with-timezone
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
		model.add(cellNode, CREATION_DATE_PROPERTY,factory.createLiteral(sdf.format(mapping.getCreationDate().toDate()), XMLSchema.DATETIME));
		
		
		final String method = mapping.getMethod();
		final Map<String, String> metamap = mapping.getMeta();
		if (!method.equals(Mapping.UNKNOW_METHOD)) {
			model.add(cellNode, METHOD_PROPERTY,factory.createLiteral(method));
		}
		if (!metamap.isEmpty()) {
			Resource metaRoot = factory.createBNode();
			model.add(cellNode, METAMETHOD_PROPERTY,metaRoot);
			for (Map.Entry<String, String> entry : metamap.entrySet()) {
				IRI localProp = factory.createIRI(ALIGN_NS
						+ entry.getKey());
				model.add(metaRoot, localProp,factory.createLiteral(entry.getValue()));
			}			
		}	
		
	}

	private <ONTORES> void buildNoMapNode(OntoContainer<ONTORES> onto,
			NoMappingPossible<ONTORES> noMap, int number, Model model, ValueFactory factory,
			Resource alignmentRoot) {
		
		// ordre d'insertion des proprietes necessaires pour avoir une "belle"
		// ecriture dans le fichier RDF/XML
		
		// on créé le noeud correspondant a la Cell		
		BNode cellNode = factory.createBNode();
		// on relie l'objet "Alignement" a ce noeud par la propriete "nomap"
		model.add(alignmentRoot,NOMAP_PROPERTY,cellNode);
		// on donne le type de l'objet "Cell"
		model.add(cellNode, RDF.TYPE, CELL_TYPE);		
		
		IRI res1 = factory.createIRI(onto.getURI(noMap.getConcept())
				.toString());
		if (number == 1)
			model.add(cellNode,ENTITY1_PROPERTY,res1);
		else
			model.add(cellNode,ENTITY2_PROPERTY,res1);
		
	}
}

