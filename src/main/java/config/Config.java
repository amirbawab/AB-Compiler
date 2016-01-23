package config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Singleton class to configure the application
 * This class will initialize the lexical, operators, punctuation and reserved words
 * @author Amir El Bawab
 */
public class Config {
	
	private static Config instance = new Config();
	
	// Logger
	Logger l = LogManager.getFormatterLogger(getClass());
	
	/**
	 * Constructor
	 */
	private Config() {
		// Nothing for now
	}
	
	/**
	 * Get instance
	 * @return instance
	 */
	public static Config getInstance() {
		return instance;
	}
}
