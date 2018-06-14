package fr.onagui.gui;

import javax.swing.filechooser.FileNameExtensionFilter;

public enum OntologyType {
	
	FIRST_ONTO_OWL(true, OntologyFormat.OWL),
	FIRST_ONTO_SKOS(true, OntologyFormat.SKOS),
	SECOND_ONTO_OWL(false, OntologyFormat.OWL),
	SECOND_ONTO_SKOS(false, OntologyFormat.SKOS);
	
	public enum OntologyFormat {
		OWL(new FileNameExtensionFilter(Messages.getString("OntologyTypeOwl"), "owl", "rdf", "ttl", "n3", "trig", "trix", "json")), //$NON-NLS-1$ //$NON-NLS-2$
		SKOS(new FileNameExtensionFilter(Messages.getString("OntologyTypeSkos"), "rdf", "ttl", "n3", "trig", "trix", "json")); //$NON-NLS-1$ //$NON-NLS-2$
				
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
