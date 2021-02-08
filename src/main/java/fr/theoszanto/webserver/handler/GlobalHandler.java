package fr.theoszanto.webserver.handler;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

import fr.theoszanto.webserver.WebServer;
import fr.theoszanto.webserver.api.Handler;
import fr.theoszanto.webserver.api.HandlersContainer;
import fr.theoszanto.webserver.api.HttpMIMEType;
import fr.theoszanto.webserver.api.HttpMethod;
import fr.theoszanto.webserver.api.HttpStatus;
import fr.theoszanto.webserver.api.HttpMethodHandler;

/**
 * A global handler, which handle a basic server serving
 * static HTML files.
 * 
 * @author	indyteo
 */
public class GlobalHandler implements HandlersContainer {
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
	public static void enable(WebServer server) {
		server.registerHandlers(GlobalHandler.class);
	}
	
	/**
	 * The handling method.
	 * 
	 * @throws IOException
	 * 			If a I/O exception occurs.
	 */
	@HttpMethodHandler
	public static void globalHandler() throws IOException {
		System.out.println("Starting handling main route (\"/\")...");
		
		HttpMethod requestMethod = Handler.req.getMethod();
		URI requestURI = Handler.req.getURI();
		System.out.println("Request method: " + requestMethod);
		System.out.println("Request URI: " + requestURI);
		System.out.println("Request headers:");
		Handler.req.forEachHeaders((key, valList) -> {
			System.out.print("\t" + key + ": ");
			for (int i = 0; i < valList.size(); i++) {
				if (i != 0)
					System.out.print(", ");
				System.out.print(valList.get(i));
			}
			System.out.println();
		});
		System.out.println("Request params:");
		Handler.req.getParamKeys().forEach(key -> System.out.println(key + ": " + Handler.req.getParam(key)));
		
		
		File f = Handler.req.getRequestedFile();
		if (f.isDirectory()) {
			if (requestURI.toString().endsWith("/"))
				f = Paths.get(f.getCanonicalPath(), "index.html").toFile();
			else {
				Handler.res.redirect(requestURI.toString() + "/");
				System.out.println("Redirecting request on correct syntax route!");
				return;
			}
		}
		System.out.println("Request file: " + f.getCanonicalPath());
		String mime = HttpMIMEType.getMime(f);
		if (mime != null)
			Handler.res.sendFile(f);
		else {
			Handler.res.end(HttpStatus.FORBIDDEN);
			System.err.println("Request file \"" + f.getCanonicalPath() + "\" forbidden!");
		}
		
		System.out.println("Main route (\"/\") handling ended!");
	}
}