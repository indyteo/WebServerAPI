package fr.theoszanto.webserver;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.sun.net.httpserver.HttpServer;

import fr.theoszanto.webserver.api.GetHandler;
import fr.theoszanto.webserver.api.Handler;
import fr.theoszanto.webserver.api.HandlersContainer;
import fr.theoszanto.webserver.api.HttpMethod;
import fr.theoszanto.webserver.api.HttpMethodHandler;
import fr.theoszanto.webserver.api.PostHandler;

/**
 * The web server class, managing the requests and handlers.
 * 
 * @author	indyteo
 * @see		WebServer#registerHandlers(HandlersContainer)
 */
public class WebServer {
	/**
	 * The HttpServer instance powering up the server.
	 */
	private HttpServer server;
	
	/**
	 * The port the server is currently listening.
	 */
	private int port;
	
	/**
	 * The handlers defined to response to requests.
	 */
	private Map<String, Map<HttpMethod, Method>> handlers;
	
	/**
	 * The root of the server.
	 */
	private String root;
	
	/**
	 * The instance containing the handlers.
	 */
	private HandlersContainer handlersContainer;
	
	/**
	 * Create a new WebServer, listening on the port {@code port},
	 * with the file root {@code root}.
	 * 
	 * @param port
	 * 			The port on which the server is listening.
	 * @param root
	 * 			The root of the server.
	 */
	public WebServer(int port, String root) {
		this.port = port;
		this.handlers = new HashMap<String, Map<HttpMethod, Method>>();
		this.root = root;
		this.handlersContainer = null;
		try {
			System.out.println("Starting server...");
			System.out.println("Listening port: " + this.port);
			
			System.out.println("Root directory: " + this.root);
			File f = new File(this.root);
			if (! f.isDirectory()) {
				f.mkdirs();
				System.out.println("Created \"" + this.root + "\" directory.");
			}
			
			this.server = HttpServer.create(new InetSocketAddress(this.port), 0);
			
			Handler.setServer(this);
			this.server.createContext("/", Handler.getInstance());
			
			this.server.start();
			
			System.out.println("Server started!");
		} catch (IOException e) {
			System.err.println("Unable to start server.");
			this.close();
			e.printStackTrace();
		}
	}
	
	/**
	 * Create a new WebServer, listening on the port {@code port},
	 * with the current working directory as file root.
	 * 
	 * @param port
	 * 			The port on which the server is listening.
	 * @see		WebServer#WebServer(int, String)
	 */
	public WebServer(int port) {
		this(port, System.getProperty("user.dir", ""));
	}
	
	/**
	 * Close this WebServer, making it no more listening requests.
	 * 
	 * <p>It's impossible to re-open a closed WebServer.
	 */
	public void close() {
		if (this.server != null)
			this.server.stop(0);
		System.out.println("Server closed!");
	}
	
	/**
	 * Register handlers contained in the HandlersContainer instance you
	 * gave.
	 * 
	 * <p>You cannot register more that one instance of HandlersContainer.
	 * 
	 * <p>Registering handlers with an instance with this method will clear
	 * the previously non-static registered handlers.
	 * 
	 * @param handlers
	 * 			The instance of the {@link HandlersContainer handlers container}
	 * 			to register.
	 */
	public void registerHandlers(HandlersContainer handlers) {
		this.handlersContainer = handlers;
		Iterator<String> i = this.handlers.keySet().iterator();
		while (i.hasNext())
			this.handlers.get(i.next()).entrySet().removeIf(h -> ! Modifier.isStatic(h.getValue().getModifiers()));
		
		this.registerHandlers(handlers.getClass(), true);
	}
	
	/**
	 * Register static handlers contained in the HandlersContainer class.
	 * 
	 * @param handlers
	 * 			The class where the handlers are.
	 */
	public void registerHandlers(Class<? extends HandlersContainer> handlers) {
		this.registerHandlers(handlers, false);
	}
	
	/**
	 * Register handlers contained in the HandlersContainer class.
	 * 
	 * @param handlers
	 * 			The class where the handlers are.
	 * @param all
	 * 			If the method should register all annoted methods
	 * 			or only static.
	 */
	private void registerHandlers(Class<? extends HandlersContainer> handlers, boolean all) {
		for (Method m : handlers.getDeclaredMethods()) {
			
			if (all ? true : Modifier.isStatic(m.getModifiers())) {
				if (m.isAnnotationPresent(GetHandler.class))
					this.registerHandler(m.getAnnotation(GetHandler.class).value(), m, HttpMethod.GET);
				else if (m.isAnnotationPresent(PostHandler.class))
					this.registerHandler(m.getAnnotation(PostHandler.class).value(), m, HttpMethod.POST);
				else if (m.isAnnotationPresent(HttpMethodHandler.class))
					this.registerHandler(m.getAnnotation(HttpMethodHandler.class).route(), m, m.getAnnotation(HttpMethodHandler.class).method());
			}
		}
	}
	
	/**
	 * Register the handling method {@code handler} for the route
	 * {@code route} and the methods {@code methods}.
	 * 
	 * @param route
	 * 			The route to bind the handler.
	 * @param handler
	 * 			The handler to bind to this route.
	 * @param methods
	 * 			The methods that should trigger the handler.
	 */
	private void registerHandler(String route, Method handler, HttpMethod... methods) {
		Map<HttpMethod, Method> h = this.handlers.get(route);
		// Add route if there were no handlers for it
		if (h == null) {
			h = new HashMap<HttpMethod, Method>();
			this.handlers.put(route, h);
		}
		
		for (HttpMethod m : methods)
			h.put(m, handler);
	}
	
	/**
	 * Return the most complete handler corresponding to the
	 * given route with the given method.
	 * 
	 * @param route
	 * 			The route of the handler.
	 * @param method
	 * 			The method of the handler.
	 * @return	The handler method if it exists,
	 * 			{@code null} otherwise.
	 */
	public Method getHandler(String route, HttpMethod method) {
		Set<String> keys = this.handlers.keySet();
		String matchedKey = "";
		for (String key : keys) {
			if (route.toLowerCase().startsWith(key.toLowerCase()) && key.length() > matchedKey.length())
				matchedKey = key;
		}
		
		Map<HttpMethod, Method> h = this.handlers.get(matchedKey);
		return h == null ? null : h.get(method);
	}
	
	/**
	 * Return the file root of the server.
	 * 
	 * @return	The root the of the server.
	 */
	public String getRoot() {
		return this.root;
	}
	
	/**
	 * Return the instance of the handlersContainer, used
	 * to call to handlers.
	 * 
	 * @return	The last registered instance of HandlersContainer.
	 */
	public HandlersContainer getHandlerContainer() {
		return this.handlersContainer;
	}
}