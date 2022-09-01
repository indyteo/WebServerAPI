package fr.theoszanto.webserver.api;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Functional interface to write an HTTP response
 * body through an {@link OutputStream}.
 * @see		HttpResponse#sendCustom(HttpMIMEType, ResponseBodyProvider)
 */
@FunctionalInterface
public interface ResponseBodyProvider {
	/**
	 * Write the response body into the output stream.
	 * @param responseBody
	 * 			The output stream bound to
	 * 			the response body
	 * @throws IOException
	 * 			If an I/O exception occurs, for example, if the
	 * 			response was already send.
	 */
	void writeResponseBody(@NotNull OutputStream responseBody) throws IOException;
}
