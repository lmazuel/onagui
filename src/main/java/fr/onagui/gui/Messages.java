package fr.onagui.gui;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
	private static final String BUNDLE_NAME = "fr.onagui.gui.messages"; //$NON-NLS-1$

	private static ResourceBundle RESOURCE_BUNDLE = null;
	
	static 
	{
		String force_language = System.getenv("ONAGUI_FORCE_LANGUAGE");
		if(force_language != null)	{
			Locale.setDefault(new Locale(force_language));
		}
		RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
	}
	
	private Messages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
