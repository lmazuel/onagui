package fr.onagui.control;

public enum ERASE_TYPE {
	ERASE_ALL,
	FOUND_ONLY,
	FOUND_BUT_NOT_BY_RED,
	NOTFOUND_ONLY,
	NO_ERASE;

	@Override
	public String toString() {
		switch(this) {
		case ERASE_ALL: return "Ecraser tout";
		case FOUND_ONLY: return "Ecraser les concepts \"verts\" uniquement";
		case NOTFOUND_ONLY: return "Ecraser les concepts \"rouges\" uniquement";
		case FOUND_BUT_NOT_BY_RED: return "Ecraser tout, mais ne pas écraser les concerts \"verts\" par des \"rouges\".";
		case NO_ERASE: return "Ne rien ecraser! (ne mettre à jour que les concepts \"jaunes\")";
		default: return "Type inconnu...";
		}
	}
	
}
