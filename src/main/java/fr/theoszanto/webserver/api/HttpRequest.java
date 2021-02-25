package fr.theoszanto.webserver.api;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import fr.theoszanto.webserver.WebServer;
import fr.theoszanto.webserver.utils.Checks;
import fr.theoszanto.webserver.utils.MiscUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The object representing the client request.
 *
 * @author	indyteo
 */
public final class HttpRequest {
	/**
	 * System logger for HttpRequest class.
	 */
	private static final @NotNull Logger LOGGER = Logger.getLogger(HttpRequest.class.getName());

	/**
	 * The server containing useful data such as the root or the router.
	 *
	 * @see		WebServer
	 */
	private final @NotNull WebServer server;

	/**
	 * The exchange containing client request data and server response
	 * methods.
	 *
	 * @see		HttpExchange
	 */
	private final @NotNull HttpExchange exchange;

	/**
	 * The parameters of the request, either send in through
	 * the request body, or in the request URL, according to
	 * the request method.
	 *
	 * @see		HttpMethod
	 * @see		HttpRequest#getParams()
	 */
	private final @NotNull Map<String, String> params;

	/**
	 * The parameters of the requested route, if any.
	 *
	 * @see		HttpRequest#getRouteParams()
	 */
	private @Nullable Map<String, String> routeParams;

	/**
	 * Construct a new request from the client.
	 *
	 * @param server
	 * 			The WebServer to bind this request to.
	 * @param exchange
	 * 			The exchange of the request.
	 * @throws IOException
	 * 			If an I/O exception occurs during the body-reading
	 * 			process.
	 */
	public HttpRequest(@NotNull WebServer server, @NotNull HttpExchange exchange) throws IOException {
		Checks.notNull(server, "server");
		Checks.notNull(exchange, "exchange");
		this.server = server;
		this.exchange = exchange;
		this.params = new HashMap<>();

		InputStream is = exchange.getRequestBody();
		StringBuilder params = new StringBuilder();
		int byteRead;
		while ((byteRead = is.read()) != -1)
			params.append((char) byteRead);
		this.parseParams(params.length() == 0 ? this.getURI().getQuery() : params.toString());
	}

	/**
	 * The method parse the String {@code params} which contains
	 * HTTP request parameters, formatted as the standard.
	 * 
	 * <p>This input:</p>
	 * <blockquote><pre>
	 * key1=value1&amp;key2=value+with+space+2
	 * </pre></blockquote>
	 * <p>Should produce the following:</p>
	 * <blockquote><pre>
	 * params.put("key1", "value1");
	 * params.put("key2", "value with space 2");
	 * </pre></blockquote>
	 * 
	 * <p>Any malformed input part will be ignored.</p>
	 * 
	 * @param params
	 * 			The String representation of the params
	 */
	@Contract(mutates = "this")
	private void parseParams(@Nullable String params) {
		if (params == null)
			return;
		String[] pairs = params.replace('+', ' ').split("&");
		for (String pair : pairs) {
			String[] val = pair.split("=");
			try {
				this.params.put(val[0], val[1]);
			} catch (ArrayIndexOutOfBoundsException ignored) {}
		}
	}

	/**
	 * Set the current route parameters.
	 *
	 * @param routeParams
	 * 			The map containing route parameters.
	 */
	@Contract(mutates = "this")
	public void setRouteParams(@Nullable Map<String, String> routeParams) {
		this.routeParams = routeParams;
	}

	/**
	 * Return the request parameters.
	 *
	 * @return	The request body params.
	 * @see		HttpRequest#params
	 */
	@UnmodifiableView
	@Contract(value = " -> new", pure = true)
	public @NotNull Map<String, String> getParams() {
		return Collections.unmodifiableMap(this.params);
	}

	/**
	 * Return the value of the given request param.
	 *
	 * @param key
	 * 			The name of the request param to retrieve.
	 * @return	The request param value corresponding to the
	 * 			key if it exists, {@code null} otherwise.
	 * @see		HttpRequest#getParams()
	 */
	@Contract(pure = true)
	public @Nullable String getParam(@NotNull String key) {
		Checks.notNull(key, "key");
		return this.params.get(key);
	}

	/**
	 * Return the route parameters.
	 *
	 * @return	The request route params.
	 * @see		HttpRequest#routeParams
	 */
	@UnmodifiableView
	@Contract(value = " -> new", pure = true)
	public @NotNull Map<String, String> getRouteParams() {
		return this.routeParams == null ? Collections.emptyMap() : Collections.unmodifiableMap(this.routeParams);
	}

	/**
	 * Return the value of the given route param.
	 *
	 * @param key
	 * 			The name of the route param to retrieve.
	 * @return	The route param value corresponding to the
	 * 			key if it exists, {@code null} otherwise.
	 * @see		HttpRequest#getRouteParams()
	 */
	@Contract(pure = true)
	public @Nullable String getRouteParam(@NotNull String key) {
		Checks.notNull(key, "key");
		return MiscUtils.get(this.routeParams, key);
	}

	/**
	 * Return the requested {@link URI}.
	 * 
	 * @return	The URI of the request.
	 */
	@Contract(pure = true)
	public @NotNull URI getURI() {
		return this.exchange.getRequestURI();
	}

	/**
	 * Return the {@link File} object corresponding to the
	 * requested URI and the server root.
	 * 
	 * @return	The File requested by the client, that might
	 * 			not exist.
	 */
	@Contract(value = " -> new", pure = true)
	public @NotNull File getRequestedFile() {
		return Paths.get(this.server.getRoot(), this.getURI().toString()).toFile();
	}

	/**
	 * Return the method used in the request.
	 * 
	 * @return	The {@link HttpMethod} representing the method
	 * 			used by the client in the request.
	 */
	@Contract(pure = true)
	public @NotNull HttpMethod getMethod() {
		return HttpMethod.parse(this.exchange.getRequestMethod());
	}

	/**
	 * Return the headers of the request.
	 *
	 * @return	The request {@link Headers headers}.
	 */
	@UnmodifiableView
	@Contract(pure = true)
	public @NotNull Headers getHeaders() {
		return this.exchange.getRequestHeaders();
	}

	/**
	 * Log request informations using {@link Level#INFO}.
	 */
	public void logDebugInfo() {
		this.logDebugInfo(Level.INFO);
	}

	/**
	 *  Log request informations using the given {@link Level level}.
	 *
	 * @param level
	 * 			The Level used to log informations.
	 */
	public void logDebugInfo(@NotNull Level level) {
		Checks.notNull(level, "level");
		LOGGER.log(level, "Debug caller: " + MiscUtils.caller());
		LOGGER.log(level, "v ===== Debug informations for request ===== v");
		LOGGER.log(level, "Request method: " + this.getMethod());
		LOGGER.log(level, "Request URI: " + this.getURI());
		Headers headers = this.getHeaders();
		LOGGER.log(level, "Request headers: (" + headers.size() + ")");
		headers.forEach((key, valList) -> {
			StringJoiner joiner = new StringJoiner(", ", "\t" + key + ": ", "");
			for (String val : valList)
				joiner.add(val);
			LOGGER.log(level, joiner.toString());
		});
		LOGGER.log(level, "Request params: (" + this.params.size() + ")");
		this.params.forEach((key, value) -> LOGGER.log(level, "\t" + key + ": " + value));
		if (this.routeParams == null)
			LOGGER.log(level, "No route params");
		else {
			LOGGER.log(level, "Route params: (" + this.routeParams.size() + ")");
			this.routeParams.forEach((key, value) -> LOGGER.log(level, "\t" + key + ": " + value));
		}
		LOGGER.log(level, "Server informations: " + this.server);
		LOGGER.log(level, "^ ===== Debug informations for request ===== ^");
	}
}
