package se.cbb.jprime.apps;

/**
 * Interface for JPrIME apps. Considered using annotations too, but that seemed less practical.
 * We assume empty constructors for apps at the moment.
 * 
 * @author Joel Sjöstrand.
 */
public interface JPrIMEApp {
	
	/**
	 * Returns the app's name.
	 * @return the name.
	 */
	public String getAppName();
	
	/**
	 * Starts the application.
	 * @param args the application arguments.
	 */
	public void main(String[] args) throws Exception;
	
}
