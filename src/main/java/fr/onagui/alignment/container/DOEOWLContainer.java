/**
 * 
 */
package fr.onagui.alignment.container;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.semanticweb.owlapi.vocab.SKOSVocabulary;

/**
 * @author Laurent Mazuel
 */
public class DOEOWLContainer extends OWLAPIContainer {
	
	private static String doeNS = "http://cwi.nl/~troncy/DOE";
	private static IRI prefLabelIRI = IRI.create(doeNS + "#prefLabel");
	private static IRI altLabelIRI = IRI.create(doeNS + "#altLabel");
	private static IRI hiddenLabelIRI = IRI.create(doeNS + "#hiddenLabel");

	public DOEOWLContainer(URI filename) throws OWLOntologyCreationException {
		super(filename);
	}
	
	@Override
	public Set<String> getPrefLabels(OWLEntity cpt) {
		Set<String> finalLabels = new HashSet<String>();
		Set<OWLAnnotation> annotations = cpt.getAnnotations(ontology);
		for(OWLAnnotation annot : annotations) {
			if(annot.getValue() instanceof OWLLiteral) {
				OWLAnnotationProperty prop = annot.getProperty();
				// The DOE prefLabel, if they exist
				if(prop.getIRI().equals(prefLabelIRI) ||
					prop.getIRI().equals(SKOSVocabulary.PREFLABEL.getIRI()) ||
					prop.getIRI().equals(OWLRDFVocabulary.RDFS_LABEL.getIRI())) {

					OWLLiteral literal = (OWLLiteral)annot.getValue();
					finalLabels.add(literal.getLiteral());
				}
			}
		}
		return finalLabels;
	}

	@Override
	public Set<String> getPrefLabels(OWLEntity cpt, String lang) {
		Set<String> finalLabels = new HashSet<String>();
		Set<OWLAnnotation> annotations = cpt.getAnnotations(ontology);
		for(OWLAnnotation annot : annotations) {
			if(annot.getValue() instanceof OWLLiteral) {
				OWLAnnotationProperty prop = annot.getProperty();
				// The DOE prefLabel, if they exist
				if(prop.getIRI().equals(prefLabelIRI) ||
					prop.getIRI().equals(SKOSVocabulary.PREFLABEL.getIRI()) ||
					prop.getIRI().equals(OWLRDFVocabulary.RDFS_LABEL.getIRI())) {

					OWLLiteral literal = (OWLLiteral)annot.getValue();
					if((literal.hasLang() && literal.getLang().toLowerCase().equals(lang.toLowerCase()))
							// ou si il n'y en a pas et que c'est voulu
							|| (!literal.hasLang() && lang.equals(""))) { 
						
						finalLabels.add(literal.getLiteral());
					}
				}
			}
		}
		return finalLabels;
	}

	@Override
	public Set<String> getAltLabels(OWLEntity cpt) {
		Set<String> finalLabels = new HashSet<String>();
		Set<OWLAnnotation> annotations = cpt.getAnnotations(ontology);
		for(OWLAnnotation annot : annotations) {
			if(annot.getValue() instanceof OWLLiteral) {
				OWLAnnotationProperty prop = annot.getProperty();
				// The DOE prefLabel, if they exist
				if(prop.getIRI().equals(altLabelIRI) ||
					prop.getIRI().equals(hiddenLabelIRI) ||
					prop.getIRI().equals(SKOSVocabulary.ALTLABEL.getIRI()) ||
					prop.getIRI().equals(SKOSVocabulary.HIDDENLABEL.getIRI())) {

					OWLLiteral literal = (OWLLiteral)annot.getValue();
					finalLabels.add(literal.getLiteral());
				}
			}
		}
		return finalLabels;
	}

	@Override
	public Set<String> getAltLabels(OWLEntity cpt, String lang) {
		Set<String> finalLabels = new HashSet<String>();
		Set<OWLAnnotation> annotations = cpt.getAnnotations(ontology);
		for(OWLAnnotation annot : annotations) {
			if(annot.getValue() instanceof OWLLiteral) {
				OWLAnnotationProperty prop = annot.getProperty();
				// The DOE prefLabel, if they exist
				if(prop.getIRI().equals(altLabelIRI) ||
					prop.getIRI().equals(hiddenLabelIRI) ||
					prop.getIRI().equals(SKOSVocabulary.ALTLABEL.getIRI()) ||
					prop.getIRI().equals(SKOSVocabulary.HIDDENLABEL.getIRI())) {

					OWLLiteral literal = (OWLLiteral)annot.getValue();
					if((literal.hasLang() && literal.getLang().toLowerCase().equals(lang.toLowerCase()))
							// ou si il n'y en a pas et que c'est voulu
							|| (!literal.hasLang() && lang.equals(""))) { 
						
						finalLabels.add(literal.getLiteral());
					}
				}
			}
		}
		return finalLabels;
	}
}
