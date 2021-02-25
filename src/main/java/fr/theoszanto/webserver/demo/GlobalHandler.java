package fr.theoszanto.webserver.demo;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.theoszanto.webserver.WebServer;
import fr.theoszanto.webserver.api.HttpMIMEType;
import fr.theoszanto.webserver.api.HttpRequest;
import fr.theoszanto.webserver.api.HttpResponse;
import fr.theoszanto.webserver.api.HttpStatus;
import fr.theoszanto.webserver.handler.HandlersContainer;
import fr.theoszanto.webserver.handler.HttpMethodHandler;
import org.jetbrains.annotations.NotNull;

/**
 * A global handler, which handle a basic server serving
 * static HTML files.
 * 
 * @author	indyteo
 */
public class GlobalHandler implements HandlersContainer {
	/**
	 * System logger for GlobalHandler class.
	 */
	private static final Logger LOGGER = Logger.getLogger(GlobalHandler.class.getName());

	/**
	 * Private constructor.
	 */
	private GlobalHandler() {}

	/**
	 * Enable this handler for the given server.
	 * 
	 * @param server
	 * 			The server where the handler should be enabled.
	 */
	public static void enable(@NotNull WebServer server) {
		server.getRouter().registerHandlers(GlobalHandler.class);
	}

	/**
	 * The handling method.
	 *
	 * @param request
	 * 			The request from the client.
	 * @param response
	 * 			The response of the server.
	 * @throws IOException
	 * 			If a I/O exception occurs.
	 */
	@HttpMethodHandler
	private static void globalHandler(@NotNull HttpRequest request, @NotNull HttpResponse response) throws IOException {
		LOGGER.fine("Starting handling main route (\"/\")...");

		request.logDebugInfo(Level.FINE);

		URI requestURI = request.getURI();
		File f = request.getRequestedFile();
		if (f.isDirectory()) {
			if (requestURI.toString().endsWith("/"))
				f = Paths.get(f.getCanonicalPath(), "index.html").toFile();
			else {
				response.redirect(requestURI.toString() + "/");
				LOGGER.fine("Redirecting request on correct syntax route!");
				return;
			}
		}
		LOGGER.fine("Request file: " + f.getCanonicalPath());
		HttpMIMEType mime = HttpMIMEType.fromExtension(f);
		if (mime != null) {
			response.sendFile(new HttpResponse.FileResponseBuilder(response)
					.setFile(f)
					.setType(mime)
					.build());
		} else {
			response.setStatus(HttpStatus.FORBIDDEN).end();
			LOGGER.warning("Request file \"" + f.getCanonicalPath() + "\" forbidden!");
		}

		LOGGER.fine("Main route (\"/\") handling ended!");
	}
}
