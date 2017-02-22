package fr.onagui.alignment.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;

import org.eclipse.rdf4j.rio.rdfxml.util.RDFXMLPrettyWriterFactory;
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
	private IRI xmlProperty = null;
	private IRI levelProperty = null;
	private IRI typeProperty = null;
	private IRI locationProperty = null;
	private IRI formalismProperty = null;
	private IRI entity1Property = null;
	private IRI entity2Property = null;
	private IRI measureProperty = null;
	private IRI relationProperty = null;
	private IRI onto1Property = null;
	private IRI onto2Property = null;
	private IRI mapProperty = null;
	private IRI methodProperty = null;
	private IRI validProperty = null;
	private IRI creationDateProperty = null;
	private IRI metamethodProperty = null;
	private IRI alignmentType = null;
	private IRI ontologyType = null;
//	private Resource formalismType = null;
	private IRI cellType = null;
	// Type et propriétés perso
	private IRI nomapProperty = null;

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
	
		ValueFactory factory = SimpleValueFactory.getInstance();
		
		xmlProperty =factory.createIRI(ALIGN_NS + "xml");
		levelProperty = factory.createIRI(ALIGN_NS + "level");
		typeProperty = factory.createIRI(ALIGN_NS + "type");
		locationProperty = factory.createIRI(ALIGN_NS + "location");
		formalismProperty = factory.createIRI(ALIGN_NS + "formalism");
		entity1Property = factory.createIRI(ALIGN_NS + "entity1");
		entity2Property = factory.createIRI(ALIGN_NS + "entity2");
		measureProperty =factory.createIRI(ALIGN_NS + "measure");
		relationProperty = factory.createIRI(ALIGN_NS + "relation");
		onto1Property = factory.createIRI(ALIGN_NS + "onto1");
		onto2Property = factory.createIRI(ALIGN_NS + "onto2");
		mapProperty = factory.createIRI(ALIGN_NS + "map");
		methodProperty = factory.createIRI(ALIGN_NS + "method");
		validProperty = factory.createIRI(ALIGN_NS + "validity");
		creationDateProperty = factory.createIRI(DCTERMS_NS + "date");
		metamethodProperty = factory.createIRI(ALIGN_NS + "metamethod");
		alignmentType = factory.createIRI(ALIGN_NS + "Alignment");
		ontologyType = factory.createIRI(ALIGN_NS + "Ontology");
		cellType = factory.createIRI(ALIGN_NS + "Cell");

		nomapProperty = factory.createIRI(ALIGN_NS + "nomap");

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
		try {
			model=readModel(file,model);
			System.out.println("taille model: "+model.size());
		} catch (Exception e) {
			// Impossible, j'ai testé l'existence au dessus...
			e.printStackTrace();
			throw new IllegalArgumentException("Unable to read file. Right problem?");
		}
		// read the RDF/XML file
		Alignment<ONTORES1, ONTORES2> alignment = new Alignment<ONTORES1, ONTORES2>(
				onto1, onto2);
		
		
		for (Resource alignement: model.filter(null, RDF.TYPE, alignmentType).subjects()) {

			// Ajouter chaque mapping
			for (Value cellValue: model.filter(alignement, mapProperty, null).objects()) {
				Resource cellNode = (Resource)cellValue;
				System.out.println("cellnode: "+cellNode.stringValue());
				Resource entity1 = (Resource)getRequiredProperty(cellNode, entity1Property, model);
				System.out.println("statement: "+entity1.stringValue());
				Resource entity2 = (Resource)getRequiredProperty(cellNode, entity2Property, model);
				Value measureValue =getRequiredProperty(cellNode, measureProperty, model);
				
				
				double score = Double.valueOf(((Literal)measureValue).stringValue());

				Value relationStmt = getRequiredProperty(cellNode, relationProperty, model);
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

				Value methodValue = getRequiredProperty(cellNode,methodProperty,model);
				
				String method = (methodValue != null) ? methodValue.stringValue()
						: Mapping.UNKNOW_METHOD;
				// Manage validity
				
				Value validValue = getRequiredProperty(cellNode,validProperty,model);
				VALIDITY valid = (validValue != null) ? VALIDITY
						.valueOf(validValue.stringValue()) : VALIDITY.TO_CONFIRM;
				// Manage creation date
				Value creationDateValue =getRequiredProperty(cellNode,creationDateProperty,model);
				DateTime date = (creationDateValue != null) ? timeFormatter
						.parseDateTime(creationDateValue.stringValue())
						: new DateTime();
				// Manage meta
				
				Resource metaRes = (Resource)getRequiredProperty(cellNode,metamethodProperty,model);
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
				Value commentValue =getRequiredProperty(cellNode,RDFS.COMMENT,model);
		

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
			Model iterator = model.filter(alignement, nomapProperty, null);
			for(Statement stat:iterator) {
				Statement cellNodeStmt = stat;
				Resource cellNode = (Resource)cellNodeStmt.getObject();
				// FIXME "cellNode" devrait être un un noeud de Type Cell.
				// Verification à faire?
				Resource entity1 = (Resource)getRequiredProperty(cellNode, entity1Property, model);
				Resource entity2 = (Resource)getRequiredProperty(cellNode, entity2Property, model);

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
	
	public Value getRequiredProperty(Resource node, IRI property, Model model){		
		Value v=null;		
		for (Statement statement:model.filter(node, property, null)) {
			v=statement.getObject();
			System.out.println("statementrequiredproperty: "+v);
		} 
		return v;
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
		BNode rootAlignment= createAnonymousType(model,factory,alignmentType);
		model.add(rootAlignment,xmlProperty, factory.createLiteral("yes"));
		model.add(rootAlignment,levelProperty, factory.createLiteral("0"));
		model.add(rootAlignment,typeProperty, factory.createLiteral("**"));
		System.out.println("// Creation de notre arbre");
		final OntoContainer<ONTORES1> onto1 = alignment.getOnto1();
		final OntoContainer<ONTORES2> onto2 = alignment.getOnto2();

		// Meta-data about ontology
		buildOntoMetaData(model, rootAlignment,factory,onto1, 1);
		buildOntoMetaData(model, rootAlignment, factory,onto2, 2);
		System.out.println("// Meta-data about ontology");
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
	
		RDFWriter prettyWriter = new RDFXMLPrettyWriterFactory().getWriter(stream);
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
			model.add(root, onto1Property, ontologyResource);			
		} else {
			model.add(root, onto2Property, ontologyResource);
		}
		
		model.add(ontologyResource, RDF.TYPE, ontologyType);
		model.add(ontologyResource, locationProperty,factory.createLiteral(onto.getURI().toString()));
		model.add(ontologyResource, formalismProperty,factory.createLiteral(onto.getFormalism()));
		
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
		model.add(alignmentRoot, mapProperty,cellNode);
		// on donne le type de l'objet "Cell"
		model.add(cellNode, RDF.TYPE, cellType);
		
		IRI res1 = factory.createIRI(onto1.getURI(
				mapping.getFirstConcept()).toString());
		IRI res2 = factory.createIRI(onto2.getURI(
				mapping.getSecondConcept()).toString());

		
		final String comment = mapping.getComment();
		if (comment != null && !"".equals(comment)) {
			model.add(cellNode, RDFS.COMMENT,factory.createLiteral(comment));
		}
		model.add(cellNode, validProperty,factory.createLiteral(currentMappingValidity.toString()));
		model.add(cellNode, relationProperty,factory.createLiteral(mapping.getType().toString()));
		model.add(cellNode, measureProperty,factory.createLiteral((float) mapping.getScore()));
		model.add(cellNode, entity2Property,res2);
		model.add(cellNode, entity1Property,res1);
		
		model.add(cellNode, creationDateProperty,factory.createLiteral(mapping.getCreationDate().toDate()));
		
		
		final String method = mapping.getMethod();
		final Map<String, String> metamap = mapping.getMeta();
		if (!method.equals(Mapping.UNKNOW_METHOD)) {
			model.add(cellNode, methodProperty,factory.createLiteral(method));
		}
		if (!metamap.isEmpty()) {
			Resource metaRoot = factory.createBNode();
			model.add(cellNode, metamethodProperty,metaRoot);
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
		model.add(alignmentRoot,nomapProperty,cellNode);
		// on donne le type de l'objet "Cell"
		model.add(cellNode, RDF.TYPE, cellType);		
		
		IRI res1 = factory.createIRI(onto.getURI(noMap.getConcept())
				.toString());
		if (number == 1)
			model.add(cellNode,entity1Property,res1);
		else
			model.add(cellNode,entity2Property,res1);
		
	}
}

