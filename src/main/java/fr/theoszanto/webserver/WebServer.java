package fr.theoszanto.webserver;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpServer;

import fr.theoszanto.webserver.handler.Handler;
import fr.theoszanto.webserver.api.HttpMethod;
import fr.theoszanto.webserver.api.HttpStatus;
import fr.theoszanto.webserver.handler.HandlersContainer;
import fr.theoszanto.webserver.api.WebServerException;
import fr.theoszanto.webserver.handler.IntermediateHandler;
import fr.theoszanto.webserver.routing.RouteBuilder;
import fr.theoszanto.webserver.routing.Router;
import fr.theoszanto.webserver.utils.Checks;
import fr.theoszanto.webserver.utils.MiscUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * The web server class, managing the HTTP server.
 * 
 * @author	indyteo
 * @see		WebServer#getRouter()
 * @see		Router#registerHandlers(HandlersContainer)
 */
public class WebServer {
	/**
	 * System logger for WebServer class.
	 */
	private static final @NotNull Logger LOGGER = Logger.getLogger(WebServer.class.getName());

	/**
	 * The HttpServer instance powering up the server.
	 */
	private final @NotNull HttpServer server;

	/**
	 * The port the server is currently listening.
	 */
	private final int port;

	/**
	 * The router defined to map requests to handlers.
	 */
	private final @NotNull Router router;

	/**
	 * The root of the server.
	 */
	private final @NotNull String root;

	/**
	 * The super-handler for all requests.
	 */
	private final @NotNull Handler handler;

	/**
	 * Indicate whether the server is running or not.
	 */
	private boolean running;

	/**
	 * Create a new WebServer, listening on the port {@code port},
	 * with the file root {@code root}.
	 * 
	 * @param port
	 * 			The port on which the server is listening.
	 * @param root
	 * 			The root of the server.
	 */
	public WebServer(int port, @NotNull String root) throws WebServerException {
		Checks.notNull(root, "root");
		this.port = port;
		this.root = root;
		this.router = new Router();

		try {
			LOGGER.info("Starting server...");
			LOGGER.config("Listening port: " + this.port);
			LOGGER.config("Root directory: " + this.root);
			File f = new File(this.root);
			if (!f.isDirectory()) {
				if (f.mkdirs())
					LOGGER.info("Created \"" + this.root + "\" directory.");
				else
					throw new IOException("Unable to create the root directory \"" + this.root + "\".");
			}

			this.server = HttpServer.create(new InetSocketAddress(this.port), 0);

			this.handler = new Handler(this);
			this.server.createContext("/", this.handler);

			this.router.registerIntermediateRoute(new RouteBuilder()
					.setName("Unknown methods filter")
					.setRoute("/")
					.setMethod(HttpMethod.UNKNOWN)
					.setIntermediateHandler(IntermediateHandler.endingWithStatus(HttpStatus.METHOD_NOT_ALLOWED))
					.buildIntermediateRoute());

			this.server.start();
			this.running = true;
			LOGGER.info("Server started!");
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Unable to start server.", e);
			throw new WebServerException("A fatal error occured while starting server.", e);
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
	 * Immediately close this WebServer, making it no more
	 * listening requests, without any delay.
	 *
	 * <p>If any, current handlers will be stopped.</p>
	 * 
	 * <p>Note: It's impossible to re-open a closed WebServer.</p>
	 */
	public void close() {
		this.close(0);
	}

	/**
	 * Close this WebServer, making it no more listening requests.
	 *
	 * <p>Note: It's impossible to re-open a closed WebServer.</p>
	 *
	 * @param maximumDelay
	 * 			The maximum number of seconds to wait before closing
	 * 			current handlers.
	 */
	public void close(int maximumDelay) {
		this.server.stop(maximumDelay);
		this.running = false;
		LOGGER.info("Server closed!");
	}

	/**
	 * Return the router.
	 *
	 * @return  The router.
	 */
	@Contract(pure = true)
	public @NotNull Router getRouter() {
		return this.router;
	}

	/**
	 * Return the file root of the server.
	 * 
	 * @return	The root the of the server.
	 */
	@Contract(pure = true)
	public @NotNull String getRoot() {
		return this.root;
	}

	/**
	 * Return the port the current server is listening to.
	 *
	 * @return	The port the server is listening.
	 */
	@Contract(pure = true)
	public int getPort() {
		return this.port;
	}

	/**
	 * Return the super-handler the server is currently using.
	 *
	 * @return	The super-handler used by the server to route request.
	 */
	@Contract(pure = true)
	public @NotNull Handler getHandler() {
		return this.handler;
	}

	/**
	 * Log server informations using {@link Level#INFO}.
	 */
	public void logDebugInfo() {
		this.logDebugInfo(Level.INFO);
	}

	/**
	 *  Log server informations using the given {@link Level level}.
	 *
	 * @param level
	 * 			The Level used to log informations.
	 */
	public void logDebugInfo(@NotNull Level level) {
		Checks.notNull(level, "level");
		LOGGER.log(level, "Debug caller: " + MiscUtils.caller());
		LOGGER.log(level, "v ===== Debug informations for server ===== v");
		LOGGER.log(level, "Listening port: " + this.getPort());
		LOGGER.log(level, "Root directory: " + this.getRoot());
		LOGGER.log(level, "Server address: " + this.server.getAddress());
		LOGGER.log(level, "Server status: " + (this.running ? "Running" : "Closed"));
		LOGGER.log(level, "^ ===== Debug informations for server ===== ^");
	}

	@Override
	@Contract(value = " -> new", pure = true)
	public @NotNull String toString() {
		return this.running ? "WebServer running at " + this.getPort() : "Closed WebServer";
	}
}
