package fr.onagui.alignment.io;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

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
	private Property xmlProperty = null;
	private Property levelProperty = null;
	private Property typeProperty = null;
	private Property locationProperty = null;
	private Property formalismProperty = null;
	private Property entity1Property = null;
	private Property entity2Property = null;
	private Property measureProperty = null;
	private Property relationProperty = null;
	private Property onto1Property = null;
	private Property onto2Property = null;
	private Property mapProperty = null;
	private Property methodProperty = null;
	private Property validProperty = null;
	private Property creationDateProperty = null;
	private Property metamethodProperty = null;
	private Resource alignmentType = null;
	private Resource ontologyType = null;
//	private Resource formalismType = null;
	private Resource cellType = null;
	// Type et propriétés perso
	private Property nomapProperty = null;

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
		// Initialize relations and types
		Model model = ModelFactory.createDefaultModel();
		model.setNsPrefix("", ALIGN_NS);
		xmlProperty = model.createProperty(ALIGN_NS + "xml");
		levelProperty = model.createProperty(ALIGN_NS + "level");
		typeProperty = model.createProperty(ALIGN_NS + "type");
		locationProperty = model.createProperty(ALIGN_NS + "location");
		formalismProperty = model.createProperty(ALIGN_NS + "formalism");
		entity1Property = model.createProperty(ALIGN_NS + "entity1");
		entity2Property = model.createProperty(ALIGN_NS + "entity2");
		measureProperty = model.createProperty(ALIGN_NS + "measure");
		relationProperty = model.createProperty(ALIGN_NS + "relation");
		onto1Property = model.createProperty(ALIGN_NS + "onto1");
		onto2Property = model.createProperty(ALIGN_NS + "onto2");
		mapProperty = model.createProperty(ALIGN_NS + "map");
		methodProperty = model.createProperty(ALIGN_NS + "method");
		validProperty = model.createProperty(ALIGN_NS + "validity");
		creationDateProperty = model.createProperty(DCTERMS_NS + "date");
		metamethodProperty = model.createProperty(ALIGN_NS + "metamethod");
		alignmentType = model.createResource(ALIGN_NS + "Alignment");
		ontologyType = model.createResource(ALIGN_NS + "Ontology");
		cellType = model.createResource(ALIGN_NS + "Cell");

		nomapProperty = model.createProperty(ALIGN_NS + "nomap");

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

		Model model = ModelFactory.createDefaultModel();
		try {
			FileManager.get().readModel(model, file.getAbsolutePath());
		} catch (Exception e) {
			// Impossible, j'ai testé l'existence au dessus...
			e.printStackTrace();
			throw new IllegalArgumentException("Unable to read file. Right problem?");
		}
		// read the RDF/XML file
		Alignment<ONTORES1, ONTORES2> alignment = new Alignment<ONTORES1, ONTORES2>(
				onto1, onto2);

		ResIterator resIterator = model.listResourcesWithProperty(RDF.type,
				alignmentType);
		while (resIterator.hasNext()) {
			// Le noeud Alignment
			Resource alignmentResource = resIterator.nextResource();

			// FIXME ici il faudrait que je verifie la provenance de
			// l'alignement...

			// Ajouter chaque mapping
			StmtIterator iterator = alignmentResource
					.listProperties(mapProperty);
			while (iterator.hasNext()) {
				Statement cellNodeStmt = iterator.nextStatement();
				Resource cellNode = cellNodeStmt.getResource();
				// FIXME "cellNode" devrait être un un noeud de Type Cell.
				// Verification à faire?
				Statement entity1Stmt = cellNode
						.getRequiredProperty(entity1Property);
				Resource entity1 = entity1Stmt.getResource();
				Statement entity2Stmt = cellNode
						.getRequiredProperty(entity2Property);
				Resource entity2 = entity2Stmt.getResource();
				Statement measureStmt = cellNode
						.getRequiredProperty(measureProperty);
				// measureStmt.getDouble() makes weird results...
				double score = Double.valueOf(measureStmt.getString());
				// Manage relation, using different forms
				Statement relationStmt = cellNode
						.getRequiredProperty(relationProperty);
				final String relationStringFromRDF = relationStmt.getString();
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
				Statement methodStmt = cellNode.getProperty(methodProperty);
				String method = (methodStmt != null) ? methodStmt.getString()
						: Mapping.UNKNOW_METHOD;
				// Manage validity
				Statement validStmt = cellNode.getProperty(validProperty);
				VALIDITY valid = (validStmt != null) ? VALIDITY
						.valueOf(validStmt.getString()) : VALIDITY.TO_CONFIRM;
				// Manage creation date
				Statement creationDateStmt = cellNode
						.getProperty(creationDateProperty);
				DateTime date = (creationDateStmt != null) ? timeFormatter
						.parseDateTime(creationDateStmt.getString())
						: new DateTime();
				// Manage meta
				Statement metaStmt = cellNode.getProperty(metamethodProperty);
				Map<String, String> metaMap = new TreeMap<String, String>();
				if (metaStmt != null) {
					Resource anonymousNode = metaStmt.getResource();
					StmtIterator it = anonymousNode.listProperties();
					Statement metaValue;
					while (it.hasNext()) {
						metaValue = it.nextStatement();
						// Manage predidate
						Property metaProp = metaValue.getPredicate();
						String key = metaProp.getLocalName();
						// Manage object
						String value = metaValue.getString();
						// Adding to metamap
						metaMap.put(key, value);
					}
				}
				// Manage comment
				Statement commentStmt = cellNode.getProperty(RDFS.comment);

				try {
					String uri1 = entity1.getURI();
					String uri2 = entity2.getURI();
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
					if (commentStmt != null) {
						map.setComment(commentStmt.getString());
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
			iterator = alignmentResource.listProperties(nomapProperty);
			while (iterator.hasNext()) {
				Statement cellNodeStmt = iterator.nextStatement();
				Resource cellNode = cellNodeStmt.getResource();
				// FIXME "cellNode" devrait être un un noeud de Type Cell.
				// Verification à faire?
				Statement entity1Stmt = cellNode.getProperty(entity1Property);
				Statement entity2Stmt = cellNode.getProperty(entity2Property);

				try {
					if (entity1Stmt != null) {
						Resource entity1 = entity1Stmt.getResource();
						ONTORES1 cpt1 = onto1.getConceptFromURI(new URI(entity1
								.getURI()));
						NoMappingPossible<ONTORES1> nomap = new NoMappingPossible<ONTORES1>(
								cpt1);
						alignment.addImpossibleMappingFrom1(nomap);
					} else {
						Resource entity2 = entity2Stmt.getResource();
						ONTORES2 cpt2 = onto2.getConceptFromURI(new URI(entity2
								.getURI()));
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

	/* (non-Javadoc)
	 * @see fr.onagui.alignment.io.IOAlignment#saveAlignment(fr.onagui.alignment.Alignment, java.lang.String, fr.onagui.alignment.Mapping.VALIDITY)
	 */
	@Override
	public <ONTORES1, ONTORES2> void saveAlignment(
			Alignment<ONTORES1, ONTORES2> alignment, String pathToSave,
			VALIDITY validityWanted) throws IOException {

		Model model = ModelFactory.createDefaultModel();
		model.setNsPrefix("", ALIGN_NS);
		model.setNsPrefix("dcterms", DCTERMS_NS);

		// Creation de notre arbre
		Resource rootAlignment = createAnonymousType(model, alignmentType);

		final OntoContainer<ONTORES1> onto1 = alignment.getOnto1();
		final OntoContainer<ONTORES2> onto2 = alignment.getOnto2();

		// Meta-data about ontology
		buildOntoMetaData(model, rootAlignment, onto1, 1);
		buildOntoMetaData(model, rootAlignment, onto2, 2);

		// Build the mapping nodes
		for (Mapping<ONTORES1, ONTORES2> mapping : alignment.getMapping()) {
			buildCellNode(onto1, onto2, mapping, model, rootAlignment,
					validityWanted);
		}

		// Build the no mapping nodes
		for (NoMappingPossible<ONTORES1> noMap : alignment
				.getImpossibleMapping1().values()) {
			buildNoMapNode(onto1, noMap, 1, model, rootAlignment);
		}
		for (NoMappingPossible<ONTORES2> noMap : alignment
				.getImpossibleMapping2().values()) {
			buildNoMapNode(onto2, noMap, 2, model, rootAlignment);
		}

		// En dernier si je les veux en haut...
		rootAlignment.addLiteral(xmlProperty, model.createLiteral("yes"));
		rootAlignment.addLiteral(levelProperty, model.createLiteral("0"));
		rootAlignment.addLiteral(typeProperty, model.createLiteral("**"));

		OutputStream stream = Files.newOutputStream(Paths.get(pathToSave));
		stream.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes(ONAGUI_CHARSET));
		RDFWriter w = model.getWriter("RDF/XML-ABBREV");
		w.setProperty("showXmlDeclaration", "false");
		w.write(model, stream, ALIGN_NS);
	}

	private static Resource createNamedType(Model model, Resource type,
			String uri) {
		Resource rootAlignment = model.createResource(uri);
		rootAlignment.addProperty(RDF.type, type);
		return rootAlignment;
	}

	private static Resource createAnonymousType(Model model, Resource type) {
		Resource rootAlignment = model.createResource();
		rootAlignment.addProperty(RDF.type, type);
		return rootAlignment;
	}

	/**
	 * 
	 */
	private <ONTORES> void buildOntoMetaData(Model model, Resource root,
			OntoContainer<ONTORES> onto, int number) {
		Resource ontologyResource = createNamedType(model, ontologyType,
				ALIGN_NS + onto.getURI());
		ontologyResource.addLiteral(locationProperty,
				model.createLiteral(onto.getURI().toString()));
		// FIXME le noeud "formalism" n'est pas compatible avec le format
		// Euzenat car je ne sais pas ajouter un
		// attribut avec Jena pour faire un noeud: <Formalism name="OWL 1.0"
		// uri="http://www.w3.org/2002/07/owl#"/>
		// Je pourrai passer par un "typeLiteral", mais c'est moche...
		ontologyResource.addLiteral(formalismProperty,
				model.createLiteral(onto.getFormalism()));

		if (number == 1) {
			root.addProperty(onto1Property, ontologyResource);
		} else {
			root.addProperty(onto2Property, ontologyResource);
		}
	}

	private <ONTORES1, ONTORES2> void buildCellNode(
			OntoContainer<ONTORES1> onto1, OntoContainer<ONTORES2> onto2,
			Mapping<ONTORES1, ONTORES2> mapping, Model model,
			Resource alignmentRoot, VALIDITY validityWanted) {

		VALIDITY currentMappingValidity = mapping.getValidity();
		if (validityWanted != null
				&& !currentMappingValidity.equals(validityWanted))
			return;

		Resource cellNode = createAnonymousType(model, cellType);
		Resource res1 = model.createResource(onto1.getURI(
				mapping.getFirstConcept()).toString());
		Resource res2 = model.createResource(onto2.getURI(
				mapping.getSecondConcept()).toString());

		// Dans Jena, il faut ajouter les propriétés dans l'ordre inverse où
		// l'on les veut dans le fichier de sortie
		final String comment = mapping.getComment();
		if (comment != null && !"".equals(comment)) {
			cellNode.addLiteral(RDFS.comment, model.createLiteral(comment));
		}
		cellNode.addLiteral(validProperty, currentMappingValidity.toString());
		cellNode.addLiteral(relationProperty,
				model.createLiteral(mapping.getType().toString()));
		cellNode.addLiteral(measureProperty, (float) mapping.getScore());
		cellNode.addProperty(entity2Property, res2);
		cellNode.addProperty(entity1Property, res1);
		cellNode.addProperty(creationDateProperty,
				timeFormatter.print(mapping.getCreationDate()),
				XSDDatatype.XSDdateTime);

		final String method = mapping.getMethod();
		final Map<String, String> metamap = mapping.getMeta();
		if (!method.equals(Mapping.UNKNOW_METHOD)) {
			cellNode.addLiteral(methodProperty, model.createLiteral(method));
		}
		if (!metamap.isEmpty()) {
			Resource metaRoot = model.createResource();
			for (Map.Entry<String, String> entry : metamap.entrySet()) {
				Property localProp = model.createProperty(ALIGN_NS
						+ entry.getKey());
				metaRoot.addLiteral(localProp, entry.getValue());
			}
			cellNode.addProperty(metamethodProperty, metaRoot);
		}

		alignmentRoot.addProperty(mapProperty, cellNode);
	}

	private <ONTORES> void buildNoMapNode(OntoContainer<ONTORES> onto,
			NoMappingPossible<ONTORES> noMap, int number, Model model,
			Resource alignmentRoot) {

		Resource cellNode = createAnonymousType(model, cellType);
		Resource res1 = model.createResource(onto.getURI(noMap.getConcept())
				.toString());

		if (number == 1)
			cellNode.addProperty(entity1Property, res1);
		else
			cellNode.addProperty(entity2Property, res1);
		alignmentRoot.addProperty(nomapProperty, cellNode);
	}
}

