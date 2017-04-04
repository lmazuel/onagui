package fr.onagui.gui;

import javax.swing.filechooser.FileNameExtensionFilter;

public enum OntologyType {
	
	FIRST_ONTO_OWL(true, OntologyFormat.OWL),
	FIRST_ONTO_SKOS(true, OntologyFormat.SKOS),
	FIRST_ONTO_RDF(true, OntologyFormat.RDF),
	SECOND_ONTO_OWL(false, OntologyFormat.OWL),
	SECOND_ONTO_SKOS(false, OntologyFormat.SKOS),
	SECOND_ONTO_RDF(false, OntologyFormat.RDF);
	
	public enum OntologyFormat {
		OWL(new FileNameExtensionFilter(Messages.getString("OntologyTypeOwl"), "owl", "rdf")), //$NON-NLS-1$ //$NON-NLS-2$
		SKOS(new FileNameExtensionFilter(Messages.getString("OntologyTypeSkos"), "skos", "rdf")), //$NON-NLS-1$ //$NON-NLS-2$
		RDF(new FileNameExtensionFilter(Messages.getString("OntologyTypeRdf"), "rdf", "rdf")); //$NON-NLS-1$ //$NON-NLS-2$
				
		private FileNameExtensionFilter filter = null;
		
		private OntologyFormat(FileNameExtensionFilter filter) {
			this.filter = filter;
		}
		
		public FileNameExtensionFilter getFilter() {
			return filter;
		}
	}
	
	private boolean isFirstOntology = false;
	private OntologyFormat ontoFormat = null;
	
	/**
	 * @param isFirstOntology
	 * @param ontoFormat
	 */
	private OntologyType(boolean isFirstOntology, OntologyFormat ontoFormat) {
		this.isFirstOntology = isFirstOntology;
		this.ontoFormat = ontoFormat;
	}
	
	public boolean isFirstOntology() {
		return isFirstOntology;
	}
	
	public OntologyFormat getOntoFormat() {
		return ontoFormat;
	}

}
