package fr.theoszanto.webserver.api;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * List of HTTP Methods used in HTTP requests.
 * 
 * <p>HTTP defines methods (sometimes referred to as verbs, but
 * nowhere in the specification does it mention verb, nor is
 * OPTIONS or HEAD a verb) to indicate the desired action to
 * be performed on the identified resource.</p>
 * 
 * <p>You can use the {@code static} method
 * {@link HttpMethod#parse(String)} to get the HTTP Method
 * corresponding to a String.</p>
 * 
 * @author	indyteo
 * @see		HttpMethod#parse(String)
 * @see		HttpMethod#hasRequestBody()
 * @see		HttpMethod#needResponseBody()
 * @see		HttpMethod#isSafe()
 */
public enum HttpMethod {
	/**
	 * The GET method requests a representation of the specified
	 * resource. Requests using GET should only retrieve data
	 * and should have no other effect. (This is also true of
	 * some other HTTP methods.) The W3C has published guidance
	 * principles on this distinction, saying, "Web application
	 * design should be informed by the above principles, but
	 * also by the relevant limitations." See safe methods below.
	 */
	GET,
	/**
	 * The HEAD method asks for a response identical to that of a
	 * GET request, but without the response body. This is useful
	 * for retrieving meta-information written in response
	 * headers, without having to transport the entire content.
	 */
	HEAD,
	/**
	 * The POST method requests that the server accept the entity
	 * enclosed in the request as a new subordinate of the web
	 * resource identified by the URI. The data POSTed might be,
	 * for example, an annotation for existing resources; a
	 * message for a bulletin board, newsgroup, mailing list,
	 * or comment thread; a block of data that is the result of
	 * submitting a web form to a data-handling process; or an
	 * item to add to a database.
	 */
	POST,
	/**
	 * The PUT method requests that the enclosed entity be stored
	 * under the supplied URI. If the URI refers to an already
	 * existing resource, it is modified; if the URI does not
	 * point to an existing resource, then the server can create
	 * the resource with that URI.
	 */
	PUT,
	/**
	 * The DELETE method deletes the specified resource.
	 */
	DELETE,
	/**
	 * The CONNECT method converts the request connection to
	 * a transparent TCP/IP tunnel, usually to facilitate
	 * SSL-encrypted communication (HTTPS) through an
	 * unencrypted HTTP proxy. See HTTP CONNECT method.
	 */
	CONNECT,
	/**
	 * The OPTIONS method returns the HTTP methods that the
	 * server supports for the specified URL. This can be used
	 * to check the functionality of a web server by requesting
	 * '*' instead of a specific resource.
	 */
	OPTIONS,
	/**
	 * The TRACE method echoes the received request so that a
	 * client can see what (if any) changes or additions have
	 * been made by intermediate servers.
	 */
	TRACE,
	/**
	 * The PATCH method applies partial modifications to a
	 * resource.
	 */
	PATCH,
	/**
	 * Unknown or malformed HTTP method.
	 */
	UNKNOWN;

	/**
	 * Special value to handle all methods.
	 */
	public static final @Nullable HttpMethod ALL_METHODS = null;

	/**
	 * All valid HTTP methods.
	 */
	public static final @NotNull HttpMethod[] KNOWN_METHODS = {
			HttpMethod.CONNECT,
			HttpMethod.DELETE,
			HttpMethod.GET,
			HttpMethod.HEAD,
			HttpMethod.OPTIONS,
			HttpMethod.PATCH,
			HttpMethod.POST,
			HttpMethod.PUT,
			HttpMethod.TRACE
	};

	/**
	 * Return the HTTP Method corresponding to the String
	 * representation of the method given in param.
	 * 
	 * @param method
	 * 			The String representation of the HTTP Method
	 * 			to get.
	 * @return	The HTTP Method corresponding to the String
	 * 			if it exists, {@code null} otherwise.
	 */
	@Contract(pure = true)
	public static @NotNull HttpMethod parse(@Nullable String method) {
		if (method == null)
			return UNKNOWN;
		try {
			return valueOf(method.toUpperCase());
		} catch (IllegalArgumentException e) {
			return UNKNOWN;
		}
	}

	/**
	 * Return {@code 1} if a request using this method has a
	 * body, {@code -1} if it hasn't and {@code 0} if the
	 * body is optional.
	 * 
	 * @return	{@code 1}, {@code 0} or {@code -1} depending
	 * 			of the method used.
	 */
	@Contract(pure = true)
	public int hasRequestBody() {
		switch (this) {
		case POST:
		case PUT:
		case PATCH:
			return 1;
		case TRACE:
			return -1;
		default:
			return 0;
		}
	}

	/**
	 * Return {@code true} if a response to a request using
	 * this method needs a body.
	 *
	 * @return	{@code true} if the response needs to have
	 * 			a body, {@code false} otherwise.
	 */
	@Contract(pure = true)
	public boolean needResponseBody() {
		return this != HEAD;
	}

	/**
	 * Return {@code true} if the method is considered as
	 * "safe".
	 * 
	 * @return	{@code true} if the method is safe,
	 * 			{@code false} otherwise.
	 * @see		<a href="https://en.wikipedia.org/wiki/Hypertext_Transfer_Protocol#Safe_methods">Safe Methods</a>
	 */
	@Contract(pure = true)
	public boolean isSafe() {
		switch (this) {
		case GET:
		case HEAD:
		case OPTIONS:
		case TRACE:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Return {@code true} if the method is considered as
	 * "idempotent".
	 * 
	 * @return	{@code true} if the method is idempotent,
	 * 			{@code false} otherwise.
	 * @see		<a href="https://en.wikipedia.org/wiki/Hypertext_Transfer_Protocol#Idempotent_methods_and_web_applications">Idempotent Methods</a>
	 */
	@Contract(pure = true)
	public boolean isIdempotent() {
		switch (this) {
		case POST:
		case CONNECT:
		case PATCH:
			return false;
		default:
			return true;
		}
	}

	/**
	 * Return {@code true} if the method can be store in
	 * the cache.
	 * 
	 * @return	{@code true} if the method is cacheable,
	 * 			{@code false} otherwise.
	 */
	@Contract(pure = true)
	public boolean isCacheable() {
		switch (this) {
		case GET:
		case HEAD:
		case POST:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Return the String representation of this HTTP Method.
	 * 
	 * @return	The String representation of this HTTP
	 * 			Method.
	 */
	@Override
	@Contract(value = " -> new", pure = true)
	public @NotNull String toString() {
		return this.name();
	}
}
