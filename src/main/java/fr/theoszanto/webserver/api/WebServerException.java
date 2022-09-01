package fr.theoszanto.webserver.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An exception thrown by the web server.
 */
public class WebServerException extends RuntimeException {
	/**
	 * Construct a new exception.
	 *
	 * @param message
	 * 			The message of the exception.
	 */
	public WebServerException(@NotNull String message) {
		super(message);
	}

	/**
	 * Construct a new exception.
	 *
	 * @param message
	 * 			The message of the exception.
	 * @param cause
	 * 			The initial exception.
	 */
	public WebServerException(@NotNull String message, @Nullable Throwable cause) {
		super(message, cause);
	}
}
