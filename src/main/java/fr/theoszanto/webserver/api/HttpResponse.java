package fr.theoszanto.webserver.api;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import fr.theoszanto.webserver.WebServer;
import fr.theoszanto.webserver.handling.HandlingEndException;
import fr.theoszanto.webserver.utils.Checks;
import fr.theoszanto.webserver.utils.JsonUtils;
import fr.theoszanto.webserver.utils.MiscUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The object representing the server response.
 *
 * @author	indyteo
 */
public final class HttpResponse {
	/**
	 * System logger for HttpRequest class.
	 */
	private static final @NotNull Logger LOGGER = Logger.getLogger(HttpResponse.class.getName());

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

	private final @NotNull Headers headers;

	private final @NotNull Set<@NotNull Cookie> cookies;

	/**
	 * The response that will be send to the client before
	 * ending handling with {@link HttpResponse#end()}.
	 */
	private final @NotNull StringBuilder response = new StringBuilder();

	/**
	 * The status to be send when calling {@link HttpResponse#end()}
	 */
	private @Nullable HttpStatus status;

	/**
	 * Construct a new response from the server.
	 *
	 * @param server
	 * 			The WebServer to bind this response to.
	 * @param exchange
	 * 			The exchange of the response.
	 */
	public HttpResponse(@NotNull WebServer server, @NotNull HttpExchange exchange) {
		Checks.notNull(server, "server");
		Checks.notNull(exchange, "exchange");
		this.server = server;
		this.exchange = exchange;
		this.headers = exchange.getResponseHeaders();
		this.cookies = new HashSet<>();
		this.header("Content-type", HttpMIMEType.HTML.getMime());
	}

	/**
	 * Return the response headers, used to give informations
	 * about our response.
	 * 
	 * @return	The response {@link Headers headers}
	 */
	@Contract(pure = true)
	public @NotNull Headers getHeaders() {
		return this.headers;
	}

	/**
	 * Set the current header to the given value(s).
	 *
	 * @param name
	 * 			The name of the header to set.
	 * @param value
	 * 			The first value to set the header to.
	 * @param moreValues
	 *			Other values to add to the header.
	 * @return	Itself, to allow chained calls.
	 * @see		HttpResponse#getHeaders()
	 */
	@Contract(value = "_, _, _ -> this", mutates = "this")
	public @NotNull HttpResponse header(@NotNull String name, @NotNull String value, @NotNull String... moreValues) {
		Checks.notEmpty(name, "name");
		Checks.notEmpty(value, "value");
		Checks.notNull(moreValues, "moreValues");
		this.headers.set(name, value);
		for (String moreValue : moreValues)
			this.headers.add(name, moreValue);
		return this;
	}

	@Contract(value = "_, _ -> this", mutates = "this")
	public @NotNull HttpResponse headerAppend(@NotNull String name, @NotNull String... values) {
		Checks.notEmpty(name, "name");
		Checks.notEmpty(values, "values");
		for (String value : values)
			this.headers.add(name, value);
		return this;
	}

	/**
	 * Add the response to a buffer until the method
	 * {@link HttpResponse#end()} is call.
	 *
	 * <p>If {@code response} is not {@code null},
	 * calls {@code response.toString()} to
	 * transform the element into a string value.
	 * Otherwise, {@code null} is not displayed.</p>
	 *
	 * @param response
	 * 			The element to be buffered.
	 * @return	Itself, to allow chained calls.
	 * @see		HttpResponse#sendEscaped(Object)
	 */
	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull HttpResponse send(@Nullable Object response) {
		if (response != null)
			this.response.append(response);
		return this;
	}

	/**
	 * Escape HTML characters of the response and add it to
	 * a buffer until the method {@link HttpResponse#end()}
	 * is call.
	 *
	 * <p>If {@code response} is not {@code null},
	 * calls {@code response.toString()} to
	 * transform the element into a string value.
	 * Otherwise, {@code null} is not displayed.</p>
	 *
	 * @param response
	 * 			The element to be escaped then buffered.
	 * @return	Itself, to allow chained calls.
	 * @see		HttpResponse#send(Object)
	 */
	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull HttpResponse sendEscaped(@Nullable Object response) {
		if (response != null)
			this.response.append(MiscUtils.escapeHTML(response.toString()));
		return this;
	}

	/**
	 * Load a JSON file from the server.
	 *
	 * @param path
	 * 			The path of the JSON file.
	 * @param type
	 * 			The type or class of the JSON data.
	 * @param <T>
	 * 			The type of the JSON data.
	 * @return	The loaded JSON data.
	 * @throws IOException
	 * 			If an I/O exception occurs, for example,
	 * 			if the file doesn't exists.
	 */
	@Contract(value = "null, _ -> null; !null, _ -> !null", pure = true)
	public <T> @Nullable T loadJson(@Nullable String path, @NotNull Type type) throws IOException {
		return JsonUtils.fromFile(this.server, path, type);
	}

	/**
	 * Save a JSON file to the server.
	 *
	 * @param path
	 * 			The path of the JSON file.
	 * @param object
	 * 			The JSON data to save.
	 * @throws IOException
	 * 			If an I/O exception occurs, for example,
	 * 			if the file can't be created.
	 */
	public void saveJson(@NotNull String path, @Nullable Object object) throws IOException {
		JsonUtils.toFile(this.server, path, object);
	}

	/**
	 * End the handling by sending the specified object
	 * in a JSON format.
	 *
	 * @param response
	 * 			The element to be translated into JSON
	 * 			then sent to the client.
	 * @throws IOException
	 * 			If an I/O exception occurs, for example,
	 * 			if the response was already send.
	 */
	@Contract(value = "_ -> fail", mutates = "this")
	public void sendJson(@Nullable Object response) throws IOException, HandlingEndException {
		this.response.append(JsonUtils.GSON.toJson(response));
		this.contentType(HttpMIMEType.JSON).end();
	}

	/**
	 * Define the HttpStatus to be send when
	 * calling {@link HttpResponse#end()}.
	 * 
	 * @param status
	 * 			The new HttpStatus.
	 * @return	Itself, to allow chained calls.
	 * @see		HttpResponse#getStatus()
	 */
	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull HttpResponse setStatus(@Nullable HttpStatus status) {
		this.status = status;
		return this;
	}

	/**
	 * Return the actual defined status.
	 * 
	 * @return	The status, if defined, {@code null} otherwise.
	 * @see		HttpResponse#setStatus(HttpStatus)
	 */
	@Contract(pure = true)
	public @Nullable HttpStatus getStatus() {
		return this.status;
	}

	/**
	 * Define the {@code Content-type} response header.
	 *
	 * @param type
	 * 			The content type to set.
	 * @return	Itself, to allow chained calls.
	 * @see		HttpResponse#getHeaders()
	 */
	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull HttpResponse contentType(@NotNull HttpMIMEType type) {
		return this.header("Content-type", type.getMime());
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull HttpResponse cookie(Cookie cookie) {
		if (this.cookies.remove(cookie))
			LOGGER.warning("Overriding previously set cookie with " + cookie);
		this.cookies.add(cookie);
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull HttpResponse deleteCookie(String name) {
		return this.cookie(new Cookie.Builder().setName(name).setMaxAge(0).setSameSite(Cookie.SameSitePolicy.LAX).build());
	}

	/**
	 * End the handling by sending the specified file.
	 *
	 * <p>A {@link HttpStatus#NOT_FOUND 404 Not Found} error
	 * code will be send if the file does not exists.</p>
	 *
	 * <p>If set, the {@link HttpResponse#status actual status}
	 * will be send, otherwise a default {@link HttpStatus#OK 200 OK}
	 * status code is send.</p>
	 *
	 * <p>If the download parameter is set to {@code true}, the
	 * file will be suggested as download for the client, instead
	 * of displaying it, if possible (such as text files).</p>
	 *
	 * <p>This is a terminal operation.</p>
	 *
	 * @param fileResponse
	 * 			The file response object containing information
	 * 			about file to send.
	 * @throws IOException
	 * 			If an I/O exception occurs, for example, if the
	 * 			response was already send.
	 * @see		FileResponse
	 */
	@Contract(value = "_ -> fail", mutates = "this")
	public void sendFile(@NotNull FileResponse fileResponse) throws IOException, HandlingEndException {
		Checks.notNull(fileResponse, "file");

		// Verify the file exists
		if (!fileResponse.getFile().exists() || !fileResponse.getFile().isFile()) {
			this.setStatus(HttpStatus.NOT_FOUND).end();
			return;
		}

		// Ensure a valid status is set
		if (this.status == null)
			this.status = HttpStatus.OK;

		// Retrieve the MIME type and deny access if unknown extension
		HttpMIMEType mime = MiscUtils.ifNullGet(fileResponse.getType(), () -> HttpMIMEType.fromExtension(fileResponse.getFile()));
		if (mime == null) {
			if (fileResponse.isUnsafe())
				mime = HttpMIMEType.DEFAULT;
			else {
				this.setStatus(HttpStatus.FORBIDDEN).end();
				return;
			}
		}
		// Then, set the Content-type header
		this.contentType(mime);

		// Add Content-disposition if needed to download
		if (fileResponse.isDownload())
			this.header("Content-disposition", "attachment; filename=\"" + fileResponse.getFile().getName() + "\"");

		try {
			// Send file in response to client
			this.exchange.sendResponseHeaders(this.status.getCode(), 0);
			OutputStream responseBody = this.exchange.getResponseBody();
			// Standard one byte at the time copy is too slow
			Files.copy(fileResponse.getFile().toPath(), responseBody);
			responseBody.flush();
			responseBody.close();
			throw new HandlingEndException();
		} catch (FileNotFoundException e) {
			// Or a 404 Not Found error
			this.setStatus(HttpStatus.NOT_FOUND).contentType(HttpMIMEType.HTML).end();
		}
	}

	@Contract(value = "_ -> fail", mutates = "this")
	public void sendTemplate(HtmlTemplate template) throws IOException, HandlingEndException {
		if (this.status == null)
			this.status = HttpStatus.OK;
		this.contentType(HttpMIMEType.HTML);
		this.exchange.sendResponseHeaders(this.status.getCode(), 0);
		OutputStream responseBody = this.exchange.getResponseBody();
		template.send(responseBody);
		responseBody.flush();
		responseBody.close();
		throw new HandlingEndException();
	}

	/**
	 * End the handling by redirecting the client to location.
	 * 
	 * <p>If set, the {@link HttpResponse#status actual status}
	 * will be send, otherwise a default {@link HttpStatus#FOUND 302 Found}
	 * status code is send.</p>
	 * 
	 * <p>This is a terminal operation.</p>
	 * 
	 * @param location
	 * 			The location where the client should be redirected.
	 * @throws IOException
	 * 			If an I/O exception occurs, for example, if the
	 * 			response was already send.
	 */
	@Contract(value = "_ -> fail", mutates = "this")
	public void redirect(@NotNull String location) throws IOException, HandlingEndException {
		Checks.notNull(location, "location");
		this.getHeaders().add("Location", location);
		if (this.status == null)
			this.status = HttpStatus.FOUND;
		this.end();
	}

	private void setCookieHeaders() {
		for (Cookie cookie : this.cookies)
			this.headerAppend("Set-cookie", cookie.toString());
	}

	/**
	 * End this handling by calling the appropriate ending method
	 * according the the {@code withResponseBody} parameter.
	 *
	 * <p>This is a terminal operation.</p>
	 *
	 * @param withResponseBody
	 * 			Whether to include response body or not.
	 * @throws IOException
	 * 			If an I/O exception occurs, for example, if the
	 * 			response was already send.
	 * @see		HttpResponse#end()
	 * @see		HttpResponse#endWithoutBody()
	 */
	@Contract(value = "_ -> fail", mutates = "this")
	public void end(boolean withResponseBody) throws IOException, HandlingEndException {
		if (withResponseBody)
			this.end();
		else
			this.endWithoutBody();
	}

	/**
	 * End this handling.
	 * 
	 * <p>If set, the {@link HttpResponse#status actual status}
	 * will be send, otherwise a default {@link HttpStatus#OK 200 OK}
	 * status code is send.</p>
	 * 
	 * <p>If no response exists when this method is called,
	 * the String status will be send as response.</p>
	 * 
	 * <p>This is a terminal operation.</p>
	 *
	 * @throws IOException
	 * 			If an I/O exception occurs, for example, if the
	 * 			response was already send.
	 */
	@Contract(value = " -> fail", mutates = "this")
	public void end() throws IOException, HandlingEndException {
		if (this.status == null)
			this.status = HttpStatus.OK;
		if (this.response.length() == 0)
			this.send("<h1>" + this.status.getStatus() + "</h1>");
		this.setCookieHeaders();

		byte[] responseByte = this.response.toString().getBytes(StandardCharsets.UTF_8);
		this.exchange.sendResponseHeaders(this.status.getCode(), responseByte.length);
		OutputStream responseBody = this.exchange.getResponseBody();
		responseBody.write(responseByte);
		responseBody.flush();
		responseBody.close();
		throw new HandlingEndException();
	}

	/**
	 * End this handling without sending any response body.
	 *
	 * <p>If set, the {@link HttpResponse#status actual status}
	 * will be send, otherwise a default {@link HttpStatus#NO_CONTENT 204 No Content}
	 * status code is send.</p>
	 *
	 * <p>This is a terminal operation.</p>
	 *
	 * @throws IOException
	 * 			If an I/O exception occurs, for example, if the
	 * 			response was already send.
	 */
	@Contract(value = " -> fail", mutates = "this")
	public void endWithoutBody() throws IOException, HandlingEndException {
		if (this.status == null)
			this.status = HttpStatus.NO_CONTENT;
		this.setCookieHeaders();
		this.exchange.sendResponseHeaders(status.getCode(), -1);
		throw new HandlingEndException();
	}

	public @NotNull WebServer getServer() {
		return this.server;
	}

	/**
	 * Log response informations using {@link Level#INFO}.
	 */
	public void logDebugInfo() {
		this.logDebugInfo(Level.INFO);
	}

	/**
	 *  Log response informations using the given {@link Level level}.
	 *
	 * @param level
	 * 			The Level used to log informations.
	 */
	public void logDebugInfo(@NotNull Level level) {
		Checks.notNull(level, "level");
		LOGGER.log(level, "Debug caller: " + MiscUtils.caller());
		LOGGER.log(level, "v ===== Debug informations for response ===== v");
		LOGGER.log(level, "Response status: " + this.getStatus());
		LOGGER.log(level, "Response body: " + this.response);
		Headers headers = this.getHeaders();
		LOGGER.log(level, "Response headers: (" + headers.size() + ")");
		headers.forEach((key, valList) -> LOGGER.log(level, "\t" + key + ": " + String.join(", ", valList)));
		LOGGER.log(level, "Server informations: " + this.server);
		LOGGER.log(level, "^ ===== Debug informations for response ===== ^");
	}
}
