package fr.onagui.gui;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import fr.onagui.alignment.Alignment;

public class Messages {
	private static final String BUNDLE_NAME = "messages"; //$NON-NLS-1$

	// private static ResourceBundle RESOURCE_BUNDLE = null;
	private static ResourceBundle RESOURCE_BUNDLE = null;
	
	static 
	{
		String force_language = System.getenv("ONAGUI_FORCE_LANGUAGE");
		if(force_language != null)	{
			Locale.setDefault(new Locale("en"));
		}
		RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
		
	} 
	
	private Messages() {
		
	}

	public static String getString(String key) {
		try {
			ResourceBundle.clearCache();
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
	
	public static void changeLanguage(String langue){
		System.out.println("Changing language: "+langue);
		ResourceBundle.clearCache();
		Locale loc=new Locale(langue);
		Locale.setDefault(loc);
		RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
		
	}
}
