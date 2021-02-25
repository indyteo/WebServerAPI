package fr.theoszanto.webserver.api;

/**
 * An exception thrown by the web server.
 */
public class WebServerException extends RuntimeException {
	/**
	 * Construct a new exception.
	 *
	 * @param message
	 * 			The message of the exception.
	 * @param cause
	 * 			The initial exception.
	 */
	public WebServerException(String message, Throwable cause) {
		super(message, cause);
	}
}
