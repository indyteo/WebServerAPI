package fr.theoszanto.webserver.start;

import java.nio.file.Paths;
import java.util.Scanner;

import fr.theoszanto.webserver.WebServer;
import fr.theoszanto.webserver.handler.GlobalHandler;

/**
 * Start an example web server with only the
 * {@link GlobalHandler} enabled.
 * 
 * @author	indyteo
 */
public class Start {
	public static void main(String[] args) {
		WebServer ws = new WebServer(8080, Paths.get(System.getProperty("user.dir"), "files").toString());
		Scanner sc = new Scanner(System.in);
		
		GlobalHandler.enable(ws);
		
		sc.nextLine();
		ws.close();
		
		sc.close();
	}
}