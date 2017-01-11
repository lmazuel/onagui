package fr.onagui.config;

import java.io.File;

public interface OnaguiConfiguration {
	
	/** The last directory use in "open ontology" menu.
	 * @return
	 */
	public File getOntologyLastOpenDirectory();
	
	/** The color configuration to use
	 * @return
	 */
	public ScoreColorConfiguration getScoreColorConfiguration();

	/** Setting the last directory
	 * @param lastOpenDirectory
	 */
	public void setOntologyLastOpenDirectory(File lastOpenDirectory);

}
