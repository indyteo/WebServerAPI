package fr.theoszanto.webserver.api;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import fr.theoszanto.webserver.WebServer;

/**
 * The HttpHandler which link the server to the API.
 * 
 * <p>This class is a singleton.
 * 
 * @author	indyteo
 * @see		Handler#getInstance()
 * @see		Handler#req
 * @see		Handler#res
 */
public class Handler implements HttpHandler {
	/**
	 * The HttpRequest instance representing the client request.
	 * 
	 * <p>You can also retrieve this with:
	 * <blockquote><pre>
	 * HttpRequest.{@link HttpRequest#getInstance() getInstance}();
	 * </pre></blockquote>
	 * 
	 * @see		HttpRequest
	 */
	public static final HttpRequest req = HttpRequest.getInstance();
	
	/**
	 * The HttpResponse instance representing the server response.
	 * 
	 * <p>You can also retrieve this with:
	 * <blockquote><pre>
	 * HttpResponse.{@link HttpResponse#getInstance() getInstance}();
	 * </pre></blockquote>
	 * 
	 * @see		HttpResponse
	 */
	public static final HttpResponse res = HttpResponse.getInstance();
	
	/**
	 * The exchange containing client request data and server response
	 * methods.
	 * 
	 * @see		HttpExchange
	 */
	private static HttpExchange exchange = null;
	
	/**
	 * The server containing useful data such as the root or the handlers.
	 * 
	 * @see		WebServer
	 */
	private static WebServer server = null;
	
	/**
	 * The unique instance of this class.
	 * 
	 * @see		Handler#getInstance()
	 */
	private static Handler instance = null;
	
	/**
	 * Private constructor to prevent other instanciation.
	 */
	private Handler() {}
	
	/**
	 * Return the unique instance of this singleton class.
	 * 
	 * <p>The first call to this method will create the instance.
	 * 
	 * @return	The instance of the class.
	 */
	public static Handler getInstance() {
		if (instance == null)
			instance = new Handler();
		return instance;
	}
	
	/**
	 * Set the server which use this handler.
	 * 
	 * @param server
	 * 			The WebServer that use this handler.
	 */
	public static void setServer(WebServer server) {
		Handler.server = server;
	}
	
	/**
	 * Method called by the system to handler request using HttpExchange.
	 * 
	 * <p>This method parse the HttpExchange and build two objects
	 * (HttpRequest &amp; HttpResponse), search for the handler registered
	 * for the requested route with the requested method, and call it.
	 * At the end, it close the exchange.
	 * 
	 * @param exchange
	 * 			The HttpExchange received from the system call.
	 * @throws IOException
	 * 			When an I/O exception is thrown by one of the
	 * 			{@code HttpExchange}'s method.
	 * @see		WebServer#registerHandlers(HandlersContainer)
	 */
	@Override
	public final void handle(HttpExchange exchange) throws IOException {
		Handler.exchange = exchange;
		req.init();
		res.init();
		
		Method handler = server.getHandler(req.getURI().getPath(), req.getMethod());
		if (handler != null) {
			handler.setAccessible(true);
			try {
				handler.invoke(server.getHandlerContainer(), new Object[0]);
			}
			catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				System.err.print("An exception occured during request handling: ");
				e.printStackTrace();
			}
		}
		
		if (! res.isStatusSent()) {
			if (req.getMethod().needResponseBody())
				res.end(HttpStatus.NOT_FOUND);
			else
				res.endWithoutBody(HttpStatus.NOT_FOUND);
		}
		
		Handler.exchange = null;
		req.close();
		res.close();
	}
	
	/**
	 * Return the static field {@link Handler#exchange}.
	 * 
	 * @return	The HttpExchange, used by HttpRequest and
	 * 			HttpResponse objects.
	 */
	static HttpExchange getExchange() {
		return exchange;
	}
	
	/**
	 * Return the static field {@link Handler#server}.
	 * 
	 * @return	The WebServer, used by HttpRequest and
	 * 			HttpResponse objects.
	 */
	static WebServer getServer() {
		return server;
	}
}