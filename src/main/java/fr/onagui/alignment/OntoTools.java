package fr.onagui.alignment;

import java.util.HashSet;
import java.util.Set;

public class OntoTools {

	/** Get all descendant of this concept (including this concept).
	 * Does NOT check for cycle
	 * @param container The container of this concept
	 * @param concept The concept
	 * @return All descendants
	 */
	public static <ONTORES> Set<ONTORES> getAllDescendants(OntoContainer<ONTORES> container, ONTORES concept) {
		Set<ONTORES> result = new HashSet<ONTORES>();
		result.add(concept);
		for(ONTORES child : container.getChildren(concept)) {
			result.addAll(getAllDescendants(container, child));
		}
		return result;
	}
		
}
