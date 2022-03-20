package fr.theoszanto.webserver;

import com.sun.net.httpserver.HttpServer;
import fr.theoszanto.webserver.api.HtmlTemplate;
import fr.theoszanto.webserver.api.HttpMethod;
import fr.theoszanto.webserver.api.HttpStatus;
import fr.theoszanto.webserver.api.WebServerException;
import fr.theoszanto.webserver.handling.Handler;
import fr.theoszanto.webserver.handling.HandlersContainer;
import fr.theoszanto.webserver.handling.IntermediateHandler;
import fr.theoszanto.webserver.routing.RouteBuilder;
import fr.theoszanto.webserver.routing.Router;
import fr.theoszanto.webserver.utils.Checks;
import fr.theoszanto.webserver.utils.FileUtils;
import fr.theoszanto.webserver.utils.MiscUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	 * The root directory of the clients sessions.
	 */
	private final @NotNull String sessionsDir;

	/**
	 * The super-handler for all requests.
	 */
	private final @NotNull Handler handler;

	/**
	 * Indicate whether the server is running or not.
	 */
	private boolean running;

	/**
	 * The thread registered as a shutdown hook.
	 */
	private @Nullable Thread closeTask = null;

	/**
	 * Create a new WebServer, listening on the port {@code port},
	 * with the file root {@code root}.
	 * 
	 * @param port
	 * 			The port on which the server is listening.
	 * @param root
	 * 			The root of the server.
	 * @param sessionsDir
	 * 			The root directory of the clients sessions.
	 */
	public WebServer(int port, @NotNull String root, @NotNull String sessionsDir) throws WebServerException {
		Checks.notNull(root, "root");
		this.port = port;
		this.root = root;
		this.sessionsDir = sessionsDir;
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
			LOGGER.config("Sessions root directory: " + this.sessionsDir);
			f = new File(this.sessionsDir);
			if (!f.isDirectory()) {
				if (f.mkdirs())
					LOGGER.info("Created \"" + this.sessionsDir + "\" directory.");
				else
					throw new IOException("Unable to create the sessions root directory \"" + this.sessionsDir + "\".");
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
	 * @see		WebServer#WebServer(int, String, String)
	 */
	public WebServer(int port) {
		this(port, System.getProperty("user.dir", ""), "./sessions");
	}

	/**
	 * Schedule server close when JVM ends.
	 *
	 * @see		WebServer#cancelCloseOnShutdown()
	 * @see		WebServer#close()
	 */
	public void closeOnShutdown() {
		if (!this.running || this.closeTask != null)
			return;
		Runtime.getRuntime().addShutdownHook(this.closeTask = new Thread(() -> {
			this.closeTask = null;
			this.close();
		}));
	}

	/**
	 * Cancel scheduled server close when JVM ends.
	 *
	 * @see		WebServer#closeOnShutdown()
	 * @see		WebServer#close()
	 */
	public void cancelCloseOnShutdown() {
		if (!this.running || this.closeTask == null)
			return;
		Runtime.getRuntime().removeShutdownHook(this.closeTask);
		this.closeTask = null;
	}

	/**
	 * Immediately close this WebServer, making it no more
	 * listening requests, without any delay.
	 *
	 * <p>If any, current handlers will be stopped.</p>
	 * 
	 * <p>Note: It's impossible to re-open a closed WebServer.</p>
	 *
	 * @see		WebServer#close(int)
	 */
	@Contract(mutates = "this")
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
	 * @see		WebServer#close()
	 */
	@Contract(mutates = "this")
	public void close(int maximumDelay) {
		if (this.closeTask != null)
			this.cancelCloseOnShutdown();
		this.server.stop(maximumDelay);
		this.running = false;
		LOGGER.info("Server closed!");
	}

	/**
	 * Load the given templates for the current server.
	 *
	 * @param templates
	 * 			The templates to load.
	 * @return	Itself, to allow chained calls.
	 */
	@Contract(value = "_ -> this", mutates = "this")
	public WebServer loadTemplates(String... templates) {
		HtmlTemplate.loadTemplates(this, templates);
		return this;
	}

	/**
	 * Load the given templates for the current server.
	 *
	 * @param clazz
	 * 			The class containing templates fields to load.
	 * @return	Itself, to allow chained calls.
	 * @see		fr.theoszanto.webserver.api.FileTemplate @FileTemplate
	 */
	@Contract(value = "_ -> this", mutates = "this")
	public WebServer loadTemplates(Class<?> clazz) {
		HtmlTemplate.loadTemplates(this, clazz);
		return this;
	}

	/**
	 * Extract the root resources to the server file root.
	 *
	 * @param clazz
	 * 			Any {@link Class} located where to find root resources.
	 * @param replaceExisting
	 * 			Whether existing resources should be replaced or not.
	 * @return	Itself, to allow chained calls.
	 */
	@Contract(value = "_, _ -> this")
	public WebServer extractRootResources(@NotNull Class<?> clazz, boolean replaceExisting) {
		FileUtils.extractResources(clazz, this.root, new File(this.root), replaceExisting);
		return this;
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
	 * Return the root directory of the clients sessions.
	 *
	 * @return	The root directory of the clients sessions.
	 */
	@Contract(pure = true)
	public @NotNull String getSessionsDir() {
		return this.sessionsDir;
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
	 * Return the running status of the server.
	 *
	 * @return	The running status of the server.
	 * @see		WebServer#close()
	 */
	@Contract(pure = true)
	public boolean isRunning() {
		return this.running;
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
