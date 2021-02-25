package fr.theoszanto.webserver.demo;

import java.nio.file.Paths;
import java.util.Scanner;

import fr.theoszanto.webserver.WebServer;
import fr.theoszanto.webserver.api.HttpMethod;
import fr.theoszanto.webserver.handler.IntermediateHandler;
import fr.theoszanto.webserver.routing.RouteBuilder;

/**
 * Start an example web server.
 * 
 * @author	indyteo
 */
public class Start {
	public static void main(String[] args) {
		WebServer ws = new WebServer(8080, Paths.get(System.getProperty("user.dir"), "files").toString());
		Scanner sc = new Scanner(System.in);

		GlobalHandler.enable(ws);
		ws.getRouter()
				.registerRoute(new RouteBuilder()
						.setName("Search")
						.setRoute("/search/{type}/{{search}}")
						.setMethod(HttpMethod.GET)
						.setHandler((request, response) -> response.sendEscaped("<h1 title=\"Hey\">'Coucou' &copy;</h1>").end())
						.setStrict(true)
						.buildRoute())
				.registerIntermediateRoute(new RouteBuilder()
						.setName("Logger")
						.setIntermediateHandler(IntermediateHandler.LOG)
						.buildIntermediateRoute());

		ws.logDebugInfo();
		ws.getRouter().logDebugInfo();

		System.out.println("Press enter to stop the server...");
		sc.nextLine();
		ws.close();
		sc.close();
	}
}
