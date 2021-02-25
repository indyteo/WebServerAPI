package fr.theoszanto.webserver.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.net.httpserver.Headers;

import com.sun.net.httpserver.HttpExchange;
import fr.theoszanto.webserver.WebServer;
import fr.theoszanto.webserver.handler.Handler;
import fr.theoszanto.webserver.utils.Checks;
import fr.theoszanto.webserver.utils.MiscUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
	 * Check whether or not a terminal operation
	 * have been called for this exchange.
	 * 
	 * <p>If this field is {@code false} when the Handle
	 * terminate, the {@link Handler super-handler} will send
	 * a default {@link HttpStatus#NOT_FOUND 404 Not Found}.</p>
	 */
	private boolean terminated;

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
		return exchange.getResponseHeaders();
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
		Headers headers = this.getHeaders();
		headers.set(name, value);
		for (String moreValue : moreValues)
			headers.add(name, moreValue);
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
			this.response.append(response.toString());
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
	 * @see		HttpResponse.FileResponse
	 */
	public void sendFile(@NotNull FileResponse fileResponse) throws IOException {
		Checks.notNull(fileResponse, "file");

		// Verify the file exists
		if (!fileResponse.file.exists() || !fileResponse.file.isFile()) {
			this.setStatus(HttpStatus.NOT_FOUND).end();
			return;
		}

		// Ensure a valid status is set
		if (this.status == null)
			this.status = HttpStatus.OK;

		// Retrieve the MIME type and deny access if unknown extension
		HttpMIMEType mime = MiscUtils.ifNullGet(fileResponse.type, () -> HttpMIMEType.fromExtension(fileResponse.file));
		if (mime == null) {
			if (fileResponse.unsafe)
				mime = HttpMIMEType.DEFAULT;
			else {
				this.setStatus(HttpStatus.FORBIDDEN).end();
				return;
			}
		}
		// Then, set the Content-type header
		this.contentType(mime);

		// Add Content-disposition if needed to download
		if (fileResponse.download)
			this.header("Content-disposition", "attachment; filename=\"" + fileResponse.file.getName() + "\"");

		try {
			// Send file in response to client
			this.exchange.sendResponseHeaders(this.status.getCode(), 0);
			FileInputStream fs = new FileInputStream(fileResponse.file);
			OutputStream responseBody = this.exchange.getResponseBody();
			int byteRead;
			while ((byteRead = fs.read()) != -1)
				responseBody.write(byteRead);
			responseBody.flush();
			responseBody.close();
			fs.close();
			this.terminated = true;
		} catch (FileNotFoundException e) {
			// Or a 404 Not Found error
			this.setStatus(HttpStatus.NOT_FOUND).contentType(HttpMIMEType.HTML).end();
		}
	}

	/**
	 * Represent a file as a response to client.
	 *
	 * @see		HttpResponse#sendFile(FileResponse)
	 * @see		HttpResponse.FileResponseBuilder
	 */
	public static class FileResponse {
		/**
		 * The file to send to the client.
		 */
		private final @NotNull File file;
		/**
		 * Whether or not the file should be downloaded by
		 * the client.
		 */
		private final boolean download;
		/**
		 * Whether to allow unsafe (unknown) file extensions
		 * to be sent to the client or not.
		 */
		private final boolean unsafe;
		/**
		 * The MIME type to force. Otherwise, MIME is
		 * {@link HttpMIMEType#fromExtension(File) retrieved from the extension}.
		 */
		private final @Nullable HttpMIMEType type;

		/**
		 * Create a new file response object.
		 *
		 * @param file
		 * 			The file to send to the client.
		 * @param download
		 * 			Whether or not the file should be downloaded by
		 * 			the client.
		 * @param unsafe
		 * 			Whether to allow unsafe (unknown) file extensions
		 * 			to be sent to the client or not.
		 * @param type
		 * 			The MIME type to force. Otherwise, MIME is
		 * 			{@link HttpMIMEType#fromExtension(File) retrieved from the extension}.
		 * @see		HttpResponse.FileResponseBuilder
		 */
		public FileResponse(@NotNull File file, boolean download, boolean unsafe, @Nullable HttpMIMEType type) {
			Checks.notNull(file, "file");
			this.file = file;
			this.download = download;
			this.unsafe = unsafe;
			this.type = type;
		}
	}

	/**
	 * Builder to create {@link FileResponse file response}
	 */
	public static class FileResponseBuilder {
		/**
		 * Response for which the file response will be created.
		 */
		private final @NotNull HttpResponse response;

		/**
		 * The file to send to the client.
		 */
		private @Nullable File file = null;
		/**
		 * Whether or not the file should be downloaded by
		 * the client.
		 */
		private boolean download = false;
		/**
		 * Whether to allow unsafe (unknown) file extensions
		 * to be sent to the client or not.
		 */
		private boolean unsafe = false;
		/**
		 * The MIME type to force. Otherwise, MIME is
		 * {@link HttpMIMEType#fromExtension(File) retrieved from the extension}.
		 */
		private @Nullable HttpMIMEType type = null;

		/**
		 * Create a new builder bound to the given response.
		 *
		 * @param response
		 * 			Response for which the file response
		 * 			will be created.
		 */
		public FileResponseBuilder(@NotNull HttpResponse response) {
			this.response = response;
		}

		/**
		 * Create a new file response using the builder's
		 * current properties.
		 *
		 * @return	A new file response from the current state.
		 */
		@Contract(value = " -> new", pure = true)
		public @NotNull FileResponse build() {
			Checks.notNull(this.file, "file");
			return new FileResponse(this.file, this.download, this.unsafe, this.type);
		}

		/**
		 * Set the file to send to the client.
		 *
		 * @param file
		 * 			The file to send to the client.
		 * @return	Itself, to allow chained calls.
		 */
		@Contract(value = "_ -> this", mutates = "this")
		public @NotNull FileResponseBuilder setFile(@NotNull File file) {
			Checks.notNull(file, "file");
			this.file = file;
			return this;
		}

		/**
		 * Set the file to send to the client from the given
		 * path.
		 *
		 * @param path
		 * 			The path of the file to send to the client.
		 * @return	Itself, to allow chained calls.
		 */
		@Contract(value = "_ -> this", mutates = "this")
		public @NotNull FileResponseBuilder setFile(@NotNull String path) {
			Checks.notNull(path, "path");
			this.file = Paths.get(this.response.server.getRoot(), path).toFile();
			return this;
		}

		/**
		 * Set whether or not the file should be downloaded
		 * by the client.
		 *
		 * @param download
		 * 			Whether or not the file should be downloaded by
		 * 			the client.
		 * @return	Itself, to allow chained calls.
		 */
		@Contract(value = "_ -> this", mutates = "this")
		public @NotNull FileResponseBuilder setDownload(boolean download) {
			this.download = download;
			return this;
		}

		/**
		 * Set whether to allow unsafe (unknown) file extensions
		 * to be sent to the client or not.
		 *
		 * @param unsafe
		 * 			Whether to allow unsafe (unknown) file extensions
		 * 			to be sent to the client or not.
		 * @return	Itself, to allow chained calls.
		 */
		@Contract(value = "_ -> this", mutates = "this")
		public @NotNull FileResponseBuilder setUnsafe(boolean unsafe) {
			this.unsafe = unsafe;
			return this;
		}

		/**
		 * Set the MIME type to force. If not set, the MIME is
		 * {@link HttpMIMEType#fromExtension(File) retrieved from the extension}.
		 *
		 * @param type
		 * 			The MIME type to force.
		 * @return	Itself, to allow chained calls.
		 */
		@Contract(value = "_ -> this", mutates = "this")
		public @NotNull FileResponseBuilder setType(@Nullable HttpMIMEType type) {
			this.type = type;
			return this;
		}
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
	public void redirect(@NotNull String location) throws IOException {
		Checks.notNull(location, "location");
		this.getHeaders().add("Location", location);
		if (this.status == null)
			this.status = HttpStatus.FOUND;
		this.end();
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
	public void end() throws IOException {
		if (this.status == null)
			this.status = HttpStatus.OK;
		if (this.response.length() == 0)
			this.send("<h1>" + this.status.getStatus() + "</h1>");

		byte[] responseByte = this.response.toString().getBytes();
		this.exchange.sendResponseHeaders(this.status.getCode(), responseByte.length);
		OutputStream responseBody = this.exchange.getResponseBody();
		responseBody.write(responseByte);
		responseBody.flush();
		responseBody.close();
		this.terminated = true;
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
	public void endWithoutBody() throws IOException {
		if (this.status == null)
			this.status = HttpStatus.NO_CONTENT;
		this.exchange.sendResponseHeaders(status.getCode(), -1);
		this.terminated = true;
	}

	/**
	 * Check whether or not a response have been sent.
	 * 
	 * @return	{@code true} if a response have been sent,
	 * 			{@code false} otherwise.
	 */
	public boolean isTerminated() {
		return this.terminated;
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
