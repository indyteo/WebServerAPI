package fr.theoszanto.webserver.api;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import fr.theoszanto.webserver.WebServer;
import fr.theoszanto.webserver.routing.BaseRoute;
import fr.theoszanto.webserver.utils.Checks;
import fr.theoszanto.webserver.utils.JsonUtils;
import fr.theoszanto.webserver.utils.MiscUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
	 * The request context.
	 *
	 * @see		HttpRequest#getContext()
	 */
	private final @NotNull RequestContext context;

	/**
	 * The request headers.
	 *
	 * @see		HttpRequest#getHeaders()
	 */
	private final @NotNull Headers headers;

	/**
	 * The parameters of the request, either send in through
	 * the request body, or in the request URL, according to
	 * the request method.
	 *
	 * @see		HttpMethod
	 * @see		HttpRequest#getParams()
	 */
	private final @Nullable Map<String, String> params;

	/**
	 * The raw JSON body of the request.
	 *
	 * @see		HttpRequest#getJsonParams()
	 * @see		HttpRequest#getJsonParams(Class)
	 * @see		HttpRequest#getJsonParams(Class, Object)
	 * @see		HttpRequest#getJsonParams(Type)
	 * @see		HttpRequest#getJsonParams(Type, Object)
	 */
	private final @Nullable String rawJsonParams;

	/**
	 * The requested route that triggered the handler.
	 *
	 * @see		HttpRequest#getRoute()
	 */
	private @Nullable BaseRoute route;

	/**
	 * The parameters of the requested route, if any.
	 *
	 * @see		HttpRequest#getRouteParams()
	 */
	private @Nullable Map<String, String> routeParams;

	/**
	 * The cookies sent in the request.
	 *
	 * @see		HttpRequest#getCookies()
	 */
	private final @NotNull Map<String, String> cookies;

	private @Nullable Session session;

	/**
	 * Construct a new request from the client.
	 *
	 * <p>If the {@code Content-type} request header is set to
	 * {@link HttpMIMEType#JSON application/json}, the body is
	 * parsed as JSON.</p>
	 *
	 * <p>Otherwise, this input:</p>
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
	 * @param server
	 * 			The WebServer to bind this request to.
	 * @param exchange
	 * 			The exchange of the request.
	 * @param context
	 * 			The context associated with the request.
	 * @throws IOException
	 * 			If an I/O exception occurs during the body-reading
	 * 			process.
	 */
	public HttpRequest(@NotNull WebServer server, @NotNull HttpExchange exchange, @NotNull RequestContext context) throws IOException {
		Checks.notNull(server, "server");
		Checks.notNull(exchange, "exchange");
		this.server = server;
		this.exchange = exchange;
		this.context = context;
		this.headers = exchange.getRequestHeaders();
		this.cookies = new HashMap<>();

		InputStream is = exchange.getRequestBody();
		StringBuilder paramsReader = new StringBuilder();
		int byteRead;
		while ((byteRead = is.read()) != -1)
			paramsReader.append((char) byteRead);
		String params = paramsReader.length() == 0 ? this.getURI().getQuery() : paramsReader.toString();

		if (HttpMIMEType.fromMIME(this.header("Content-type")) == HttpMIMEType.JSON) {
			this.params = null;
			this.rawJsonParams = params;
		} else if (params != null) {
			this.params = new HashMap<>();
			this.rawJsonParams = null;
			String[] pairs = params.replace('+', ' ').split("&");
			for (String pair : pairs) {
				String[] val = pair.split("=");
				try {
					this.params.put(val[0], val[1]);
				} catch (ArrayIndexOutOfBoundsException ignored) {}
			}
		} else {
			this.params = null;
			this.rawJsonParams = null;
		}

		String cookieHeader = this.header("Cookie");
		if (cookieHeader != null) {
			String[] cookies = cookieHeader.trim().split(" *; *");
			for (String cookie : cookies) {
				String[] val = cookie.split("=");
				try {
					this.cookies.put(val[0], val[1]);
				} catch (ArrayIndexOutOfBoundsException ignored) {}
			}
		}
	}

	public @NotNull RequestContext getContext() {
		return this.context;
	}

	void setSession(@NotNull Session session) {
		Checks.notNull(session, "session");
		this.session = session;
	}

	@Contract(pure = true)
	public @NotNull Session getSession() {
		Checks.notNull(this.session, "session");
		return this.session;
	}

	/**
	 * Set the current route.
	 *
	 * @param route
	 * 			The route that triggered the handler.
	 */
	@Contract(mutates = "this")
	public void setRoute(@NotNull BaseRoute route) {
		Checks.notNull(route, "route");
		this.route = route;
		this.routeParams = route.params(this.getURI().getPath());
	}

	/**
	 * Get the current route.
	 *
	 * @return	The route that triggered the handler.
	 */
	@Contract(pure = true)
	public @NotNull BaseRoute getRoute() {
		Checks.notNull(this.route, "route");
		return this.route;
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
	public @Nullable Map<String, String> getParams() {
		return MiscUtils.ifNotNull(this.params, Collections::unmodifiableMap);
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
	public @Nullable String getOptionalParam(@NotNull String key) {
		Checks.notNull(key, "key");
		return MiscUtils.ifNotNull(this.params, params -> params.get(key));
	}

	/**
	 * Return the value of the given request param.
	 *
	 * @param key
	 * 			The name of the request param to retrieve.
	 * @return	The request param value corresponding to the
	 * 			key.
	 * @throws IllegalStateException
	 * 			If the request has no params.
	 * @throws IllegalArgumentException
	 * 			If the request param was not found.
	 * @see		HttpRequest#getParams()
	 * @see		HttpRequest#getOptionalParam(String)
	 */
	@Contract(pure = true)
	public @NotNull String getParam(@NotNull String key) {
		Checks.notNull(key, "key");
		if (this.params == null)
			throw new IllegalStateException("Cannot find request params");
		String param = this.params.get(key);
		if (param == null)
			throw new IllegalArgumentException("Cannot find request param with key " + key);
		return param;
	}

	/**
	 * Return the JSON parsed body.
	 *
	 * @return	The JSON parsed body.
	 */
	@Contract(pure = true)
	public @Nullable Object getJsonParams() {
		return this.getJsonParams(Object.class, null);
	}

	/**
	 * Return the JSON parsed body.
	 *
	 * @param <T>
	 * 			The type of data to retrieve
	 * @param type
	 * 			The Java Class to parse JSON.
	 * @return	The JSON parsed body.
	 */
	@Contract(pure = true)
	public <T> @Nullable T getJsonParams(@NotNull Class<T> type) {
		return this.getJsonParams(type, null);
	}

	/**
	 * Return the JSON parsed body.
	 *
	 * @param <T>
	 * 			The type of data to retrieve
	 * @param type
	 * 			The Java Class to parse JSON.
	 * @param def
	 * 			The default value if no JSON params where present.
	 * @return	The JSON parsed body.
	 */
	@Contract(value = "_, !null -> !null", pure = true)
	public <T> @Nullable T getJsonParams(@NotNull Class<T> type, @Nullable T def) {
		return this.rawJsonParams == null ? def : JsonUtils.GSON.fromJson(this.rawJsonParams, type);
	}

	/**
	 * Return the JSON parsed body.
	 *
	 * @param <T>
	 * 			The type of data to retrieve
	 * @param type
	 * 			The Java Type to parse JSON.
	 * @return	The JSON parsed body.
	 */
	@Contract(pure = true)
	public <T> @Nullable T getJsonParams(@NotNull Type type) {
		return this.getJsonParams(type, null);
	}

	/**
	 * Return the JSON parsed body.
	 *
	 * @param <T>
	 * 			The type of data to retrieve
	 * @param type
	 * 			The Java Type to parse JSON.
	 * @param def
	 * 			The default value if no JSON params where present.
	 * @return	The JSON parsed body.
	 */
	@Contract(value = "_, !null -> !null", pure = true)
	public <T> @Nullable T getJsonParams(@NotNull Type type, @Nullable T def) {
		return this.rawJsonParams == null ? def : JsonUtils.GSON.fromJson(this.rawJsonParams, type);
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
	public @Nullable String getOptionalRouteParam(@NotNull String key) {
		Checks.notNull(key, "key");
		return MiscUtils.get(this.routeParams, key);
	}

	/**
	 * Return the value of the given route param.
	 *
	 * @param key
	 * 			The name of the route param to retrieve.
	 * @return	The route param value corresponding to the
	 * 			key.
	 * @throws IllegalStateException
	 * 			If the request has no route params.
	 * @throws IllegalArgumentException
	 * 			If the route param was not found.
	 * @see		HttpRequest#getRouteParams()
	 * @see		HttpRequest#getOptionalRouteParam(String)
	 */
	@Contract(pure = true)
	public @NotNull String getRouteParam(@NotNull String key) {
		Checks.notNull(key, "key");
		if (this.routeParams == null)
			throw new IllegalStateException("Cannot find route params");
		String param = this.routeParams.get(key);
		if (param == null)
			throw new IllegalArgumentException("Cannot find route param with key " + key);
		return param;
	}

	/**
	 * Return the request cookies.
	 *
	 * @return	The request cookies.
	 * @see		HttpRequest#cookies
	 */
	@UnmodifiableView
	@Contract(value = " -> new", pure = true)
	public @NotNull Map<String, String> getCookies() {
		return Collections.unmodifiableMap(this.cookies);
	}

	/**
	 * Return the value of the given cookie.
	 *
	 * @param name
	 * 			The name of the cookie to retrieve.
	 * @return	The cookie value corresponding to the
	 * 			name if it exists, {@code null} otherwise.
	 * @see		HttpRequest#getCookies()
	 */
	@Contract(pure = true)
	public @Nullable String getCookie(@NotNull String name) {
		Checks.notNull(name, "name");
		return this.cookies.get(name);
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
	 * Return the request origin ({@code schem://host:port}).
	 *
	 * @return	The origin of the request, if provided by the client.
	 */
	@Contract(pure = true)
	public @Nullable String getOrigin() {
		return this.header("Origin");
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
		return this.getRequestedFile("", false);
	}

	/**
	 * Return the {@link File} object corresponding to the
	 * requested URI and the server root.
	 *
	 * @param trimRoute
	 * 			Whether to remove the route prefix or not.
	 * @return	The File requested by the client, that might
	 * 			not exist.
	 */
	@Contract(value = "_ -> new", pure = true)
	public @NotNull File getRequestedFile(boolean trimRoute) {
		return this.getRequestedFile("", trimRoute);
	}

	/**
	 * Return the {@link File} object corresponding to the
	 * requested URI and the server root.
	 *
	 * @param folder
	 * 			The sub-root folder to consider.
	 * @return	The File requested by the client, that might
	 * 			not exist.
	 */
	@Contract(value = "_ -> new", pure = true)
	public @NotNull File getRequestedFile(@NotNull String folder) {
		return this.getRequestedFile(folder, false);
	}

	/**
	 * Return the {@link File} object corresponding to the
	 * requested URI and the server root.
	 *
	 * @param folder
	 * 			The sub-root folder to consider.
	 * @param trimRoute
	 * 			Whether to remove the route prefix or not.
	 * @return	The File requested by the client, that might
	 * 			not exist.
	 */
	@Contract(value = "_, _ -> new", pure = true)
	public @NotNull File getRequestedFile(@NotNull String folder, boolean trimRoute) {
		String file = this.getURI().toString();
		if (trimRoute)
			file = file.replaceFirst("^" + this.getRoute().getRoute(), "");
		return Paths.get(this.server.getRoot(), folder, file).toFile();
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
		return this.headers;
	}

	/**
	 * Return the header values for the given name.
	 *
	 * @param name
	 * 			The name of the headers to retrieve.
	 * @return	The list of values for this header.
	 */
	@Contract(pure = true)
	public @Nullable List<String> headers(@NotNull String name) {
		return this.getHeaders().get(name);
	}

	/**
	 * Return the header value for the given name.
	 *
	 * @param name
	 * 			The name of the header to retrieve.
	 * @return	The value for this header.
	 */
	@Contract(pure = true)
	public @Nullable String header(@NotNull String name) {
		return this.getHeaders().getFirst(name);
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
		if (this.params == null) {
			if (this.rawJsonParams != null)
				LOGGER.log(level, "Request JSON body: " + this.rawJsonParams);
		} else {
			LOGGER.log(level, "Request params: (" + this.params.size() + ")");
			this.params.forEach((key, value) -> LOGGER.log(level, "\t" + key + ": " + value));
		}
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
