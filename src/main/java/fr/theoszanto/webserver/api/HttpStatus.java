package fr.theoszanto.webserver.api;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Complete list of HTTP status defined by RFCs.
 * 
 * <p>All HTTP response status codes are separated into five classes
 * or categories. The first digit of the status code defines the
 * class of response, while the last two digits do not have any
 * classifying or categorization role. There are five classes
 * defined by the standard:</p>
 * <ul>
 * <li>1xx informational response - the request was received,
 * continuing process</li>
 * <li>2xx successful - the request was successfully received,
 * understood, and accepted</li>
 * <li>3xx redirection - further action needs to be taken in order
 * to complete the request</li>
 * <li>4xx client error - the request contains bad syntax or cannot
 * be fulfilled</li>
 * <li>5xx server error - the server failed to fulfil an apparently
 * valid request</li>
 * </ul>
 * 
 * <p>You can use the static method {@link HttpStatus#get(int) HttpStatus.get(code)}
 * or {@link HttpStatus#get(String) HttpStatus.get(status)} to get
 * the HttpStatus corresponding to the code or status given in param.</p>
 * 
 * <p>You can use the two methods {@link HttpStatus#getCode()} and
 * {@link HttpStatus#getStatus()} to get the code or the status (String).</p>
 * 
 * <p>The {@link HttpStatus#toString()} method returns the code followed
 * by the status (String).</p>
 * 
 * @author	indyteo
 * @see		HttpStatus#getCode()
 * @see		HttpStatus#getStatus()
 * @see		HttpStatus#toString()
 */
public enum HttpStatus {
	// 1xx Informational response
	/**
	 * Code : 100
	 * 
	 * <p>The server has received the request headers and the client should
	 * proceed to send the request body (in the case of a request for
	 * which a body needs to be sent; for example, a POST request).
	 * Sending a large request body to a server after a request has
	 * been rejected for inappropriate headers would be inefficient.
	 * To have a server check the request's headers, a client must send
	 * <code>Expect: 100-continue</code> as a header in its initial
	 * request and receive a <code>100 Continue</code> status code
	 * in response before sending the body. If the client receives
	 * an error code such as 403 (Forbidden) or 405 (Method Not Allowed)
	 * then it shouldn't send the request's body. The response
	 * <code>417 Expectation Failed</code> indicates that the request
	 * should be repeated without the <code>Expect</code> header as
	 * it indicates that the server doesn't support expectations
	 * (this is the case, for example, of HTTP/1.0 servers).</p>
	 */
	CONTINUE(100),
	/**
	 * Code : 101
	 * 
	 * <p>The requester has asked the server to switch protocols and
	 * the server has agreed to do so.</p>
	 */
	SWITCHING_PROTOCOLS(101),
	/**
	 * Code : 102
	 * 
	 * <p>A WebDAV request may contain many sub-requests involving file
	 * operations, requiring a long time to complete the request.
	 * This code indicates that the server has received and is
	 * processing the request, but no response is available yet.
	 * This prevents the client from timing out and assuming the
	 * request was lost.</p>
	 */
	PROCESSING(102),
	/**
	 * Code : 103
	 * 
	 * <p>Used to return some response headers before final HTTP message.</p>
	 */
	EARLY_HINTS(103),

	// 2xx Success
	/**
	 * Code : 200
	 * 
	 * <p>Standard response for successful HTTP requests. The actual
	 * response will depend on the request method used. In a GET
	 * request, the response will contain an entity corresponding
	 * to the requested resource. In a POST request, the response
	 * will contain an entity describing or containing the result
	 * of the action.</p>
	 */
	OK(200),
	/**
	 * Code : 201
	 * 
	 * <p>The request has been fulfilled, resulting in the creation
	 * of a new resource.</p>
	 */
	CREATED(201),
	/**
	 * Code : 202
	 * 
	 * <p>The request has been accepted for processing, but the
	 * processing has not been completed. The request might or
	 * might not be eventually acted upon, and may be disallowed
	 * when processing occurs.</p>
	 */
	ACCEPTED(202),
	/**
	 * Code : 203
	 * 
	 * <p>The server is a transforming proxy (e.g. a Web accelerator)
	 * that received a 200 OK from its origin, but is returning a
	 * modified version of the origin's response.</p>
	 */
	NON_AUTHORITATIVE_INFORMATION(203),
	/**
	 * Code : 204
	 * 
	 * <p>The server successfully processed the request and is not
	 * returning any content.</p>
	 */
	NO_CONTENT(204),
	/**
	 * Code : 205
	 * 
	 * <p>The server successfully processed the request, but is not
	 * returning any content. Unlike a 204 response, this response
	 * requires that the requester reset the document view.</p>
	 */
	RESET_CONTENT(205),
	/**
	 * Code : 206
	 * 
	 * <p>The server is delivering only part of the resource (byte
	 * serving) due to a range header sent by the client. The
	 * range header is used by HTTP clients to enable resuming
	 * of interrupted downloads, or split a download into multiple
	 * simultaneous streams.</p>
	 */
	PARTIAL_CONTENT(206),
	/**
	 * Code : 207
	 * 
	 * <p>The message body that follows is by default an XML message
	 * and can contain a number of separate response codes,
	 * depending on how many sub-requests were made.</p>
	 */
	MULTI_STATUS(207),
	/**
	 * Code : 208
	 * 
	 * <p>The members of a DAV binding have already been enumerated
	 * in a preceding part of the (multistatus) response,
	 * and are not being included again.</p>
	 */
	ALREADY_REPORTED(208),
	/**
	 * Code : 226
	 * 
	 * <p>The server has fulfilled a request for the resource,
	 * and the response is a representation of the result of
	 * one or more instance-manipulations applied to the
	 * current instance.</p>
	 */
	IM_USED(226),

	// 3xx Redirection
	/**
	 * Code : 300
	 * 
	 * <p>Indicates multiple options for the resource from which the
	 * client may choose (via agent-driven content negotiation).
	 * For example, this code could be used to present multiple
	 * video format options, to list files with different filename
	 * extensions, or to suggest word-sense disambiguation.</p>
	 */
	MULTIPLE_CHOICES(300),
	/**
	 * Code : 301
	 * 
	 * <p>This and all future requests should be directed to the given URI.</p>
	 */
	MOVED_PERMANENTLY(301),
	/**
	 * Code : 302
	 * 
	 * <p>Tells the client to look at (browse to) another URL. 302
	 * has been superseded by 303 and 307. This is an example of
	 * industry practice contradicting the standard. The HTTP/1.0
	 * specification (RFC 1945) required the client to perform a
	 * temporary redirect (the original describing phrase was
	 * "Moved Temporarily"), but popular browsers implemented 302
	 * with the functionality of a 303 See Other. Therefore,
	 * HTTP/1.1 added status codes 303 and 307 to distinguish
	 * between the two behaviours. However, some Web applications
	 * and frameworks use the 302 status code as if it were the 303.</p>
	 */
	FOUND(302),
	/**
	 * Code : 303
	 * 
	 * <p>The response to the request can be found under another URI
	 * using the GET method. When received in response to a POST
	 * (or PUT/DELETE), the client should presume that the server
	 * has received the data and should issue a new GET request
	 * to the given URI.</p>
	 */
	SEE_OTHER(303),
	/**
	 * Code : 304
	 * 
	 * <p>Indicates that the resource has not been modified since
	 * the version specified by the request headers
	 * If-Modified-Since or If-None-Match. In such case, there
	 * is no need to retransmit the resource since the client
	 * still has a previously-downloaded copy.</p>
	 */
	NOT_MODIFIED(304),
	/**
	 * Code : 305
	 * 
	 * <p>The requested resource is available only through a proxy,
	 * the address for which is provided in the response. For
	 * security reasons, many HTTP clients (such as Mozilla
	 * Firefox and Internet Explorer) do not obey this status code.</p>
	 */
	USE_PROXY(305),
	/**
	 * Code : 306
	 * 
	 * <p>No longer used. Originally meant "Subsequent requests
	 * should use the specified proxy."</p>
	 */
	SWITCH_PROXY(306),
	/**
	 * Code : 307
	 * 
	 * <p>In this case, the request should be repeated with another
	 * URI; however, future requests should still use the original
	 * URI. In contrast to how 302 was historically implemented,
	 * the request method is not allowed to be changed when
	 * reissuing the original request. For example, a POST request
	 * should be repeated using another POST request.</p>
	 */
	TEMPORARY_REDIRECT(307),
	/**
	 * Code : 308
	 * 
	 * <p>The request and all future requests should be repeated
	 * using another URI. 307 and 308 parallel the behaviors of
	 * 302 and 301, but do not allow the HTTP method to change.
	 * So, for example, submitting a form to a permanently
	 * redirected resource may continue smoothly.</p>
	 */
	PERMANENT_REDIRECT(308),

	// 4xx Client errors
	/**
	 * Code : 400
	 * 
	 * <p>The server cannot or will not process the request due to
	 * an apparent client error (e.g., malformed request syntax,
	 * size too large, invalid request message framing, or
	 * deceptive request routing).</p>
	 */
	BAD_REQUEST(400),
	/**
	 * Code : 401
	 * 
	 * <p>Similar to 403 Forbidden, but specifically for use when
	 * authentication is required and has failed or has not yet
	 * been provided. The response must include a WWW-Authenticate
	 * header field containing a challenge applicable to the
	 * requested resource. See Basic access authentication and
	 * Digest access authentication. 401 semantically means
	 * "unauthorised", the user does not have valid authentication
	 * credentials for the target resource. Note: Some sites
	 * incorrectly issue HTTP 401 when an IP address is banned
	 * from the website (usually the website domain) and that
	 * specific address is refused permission to access a website.</p>
	 */
	UNAUTHORIZED(401),
	/**
	 * Code : 402
	 * 
	 * <p>Reserved for future use. The original intention was that
	 * this code might be used as part of some form of digital
	 * cash or micropayment scheme, as proposed, for example,
	 * by GNU Taler, but that has not yet happened, and this
	 * code is not usually used. Google Developers API uses
	 * this status if a particular developer has exceeded the
	 * daily limit on requests. Sipgate uses this code if an
	 * account does not have sufficient funds to start a call.
	 * Shopify uses this code when the store has not paid their
	 * fees and is temporarily disabled. Stripe uses this code
	 * for failed payments where parameters were correct, for
	 * example blocked fraudulent payments.</p>
	 */
	PAYMENT_REQUIRED(402),
	/**
	 * Code : 403
	 * 
	 * <p>The request contained valid data and was understood by
	 * the server, but the server is refusing action. This may
	 * be due to the user not having the necessary permissions
	 * for a resource or needing an account of some sort, or
	 * attempting a prohibited action (e.g. creating a duplicate
	 * record where only one is allowed). This code is also
	 * typically used if the request provided authentication
	 * via the WWW-Authenticate header field, but the server
	 * did not accept that authentication. The request should
	 * not be repeated.</p>
	 */
	FORBIDDEN(403),
	/**
	 * Code : 404
	 * 
	 * <p>The requested resource could not be found but may be
	 * available in the future. Subsequent requests by the
	 * client are permissible.</p>
	 */
	NOT_FOUND(404),
	/**
	 * Code : 405
	 * 
	 * <p>A request method is not supported for the requested
	 * resource; for example, a GET request on a form that
	 * requires data to be presented via POST, or a PUT
	 * request on a read-only resource.</p>
	 */
	METHOD_NOT_ALLOWED(405),
	/**
	 * Code : 406
	 * 
	 * <p>The requested resource is capable of generating only
	 * content not acceptable according to the Accept
	 * headers sent in the request. See Content negotiation.</p>
	 */
	NOT_ACCEPTABLE(406),
	/**
	 * Code : 407
	 * 
	 * <p>The client must first authenticate itself with the proxy.</p>
	 */
	PROXY_AUTHENTIFICATION_REQUIRED(407),
	/**
	 * Code : 408
	 * 
	 * <p>The server timed out waiting for the request. According
	 * to HTTP specifications: "The client did not produce a
	 * request within the time that the server was prepared
	 * to wait. The client MAY repeat the request without
	 * modifications at any later time."</p>
	 */
	REQUEST_TIMEOUT(408),
	/**
	 * Code : 409
	 * 
	 * <p>Indicates that the request could not be processed
	 * because of conflict in the current state of the
	 * resource, such as an edit conflict between multiple
	 * simultaneous updates.</p>
	 */
	CONFLICT(409),
	/**
	 * Code : 410
	 * 
	 * <p>Indicates that the resource requested is no longer
	 * available and will not be available again. This
	 * should be used when a resource has been intentionally
	 * removed and the resource should be purged. Upon
	 * receiving a 410 status code, the client should not
	 * request the resource in the future. Clients such as
	 * search engines should remove the resource from their
	 * indices. Most use cases do not require clients and
	 * search engines to purge the resource, and a "404
	 * Not Found" may be used instead.</p>
	 */
	GONE(410),
	/**
	 * Code : 411
	 * 
	 * <p>The request did not specify the length of its content,
	 * which is required by the requested resource.</p>
	 */
	LENGTH_REQUIRED(411),
	/**
	 * Code : 412
	 * 
	 * <p>The server does not meet one of the preconditions
	 * that the requester put on the request header fields.</p>
	 */
	PRECONDITION_FAILED(412),
	/**
	 * Code : 413
	 * 
	 * <p>The request is larger than the server is willing or
	 * able to process. Previously called "Request Entity
	 * Too Large".</p>
	 */
	PAYLOAD_TOO_LARGE(413),
	/**
	 * Code : 414
	 * 
	 * <p>The URI provided was too long for the server to
	 * process. Often the result of too much data being
	 * encoded as a query-string of a GET request, in
	 * which case it should be converted to a POST request.
	 * Called "Request-URI Too Long" previously.</p>
	 */
	URI_TOO_LONG(414),
	/**
	 * Code : 415
	 * 
	 * <p>The request entity has a media type which the server
	 * or resource does not support.  For example, the
	 * client uploads an image as image/svg+xml, but the
	 * server requires that images use a different format.</p>
	 */
	UNSUPPORTED_MEDIA_TYPE(415),
	/**
	 * Code : 416
	 * 
	 * <p>The client has asked for a portion of the file (byte
	 * serving), but the server cannot supply that portion.
	 * For example, if the client asked for a part of the
	 * file that lies beyond the end of the file. Called
	 * "Requested Range Not Satisfiable" previously.</p>
	 */
	RANGE_NOT_SATISFIABLE(416),
	/**
	 * Code : 417
	 * 
	 * <p>The server cannot meet the requirements of the
	 * Expect request-header field.</p>
	 */
	EXPECTATION_FAILED(417),
	/**
	 * Code : 418
	 * 
	 * <p>This code was defined in 1998 as one of the traditional
	 * IETF April Fools' jokes, in RFC 2324, Hyper Text Coffee
	 * Pot Control Protocol, and is not expected to be
	 * implemented by actual HTTP servers. The RFC specifies
	 * this code should be returned by teapots requested to
	 * brew coffee. This HTTP status is used as an Easter egg
	 * in some websites, including Google.com.</p>
	 */
	IM_A_TEAPOT(418),
	/**
	 * Code : 421
	 * 
	 * <p>The request was directed at a server that is not able
	 * to produce a response (for example because of
	 * connection reuse).</p>
	 */
	MISDIRECTED_REQUEST(421),
	/**
	 * Code : 422
	 * 
	 * <p>The request was well-formed but was unable to be
	 * followed due to semantic errors.</p>
	 */
	UNPROCESSABLE_ENTITY(422),
	/**
	 * Code : 423
	 * 
	 * <p>The resource that is being accessed is locked.</p>
	 */
	LOCKED(423),
	/**
	 * Code : 424
	 * 
	 * <p>The request failed because it depended on another
	 * request and that request failed (e.g., a PROPPATCH).</p>
	 */
	FAILED_DEPENDENCY(424),
	/**
	 * Code : 425
	 * 
	 * <p>Indicates that the server is unwilling to risk
	 * processing a request that might be replayed.</p>
	 */
	TOO_EARLY(425),
	/**
	 * Code : 426
	 * 
	 * <p>The client should switch to a different protocol
	 * such as TLS/1.0, given in the Upgrade header field.</p>
	 */
	UPGRADE_REQUIRED(426),
	/**
	 * Code : 428
	 * 
	 * <p>The origin server requires the request to be
	 * conditional. Intended to prevent the 'lost update'
	 * problem, where a client GETs a resource's state,
	 * modifies it, and PUTs it back to the server, when
	 * meanwhile a third party has modified the state on
	 * the server, leading to a conflict.</p>
	 */
	PRECONDITION_REQUIRED(428),
	/**
	 * Code : 429
	 * 
	 * <p>The user has sent too many requests in a given
	 * amount of time. Intended for use with rate-limiting
	 * schemes.</p>
	 */
	TOO_MANY_REQUESTS(429),
	/**
	 * Code : 431
	 * 
	 * <p>The server is unwilling to process the request
	 * because either an individual header field, or all
	 * the header fields collectively, are too large.</p>
	 */
	REQUEST_HEADER_FIELDS_TOO_LARGE(431),
	/**
	 * Code : 451
	 * 
	 * <p>A server operator has received a legal demand to
	 * deny access to a resource or to a set of resources
	 * that includes the requested resource. The code 451
	 * was chosen as a reference to the novel Fahrenheit
	 * 451 (see the Acknowledgements in the RFC).</p>
	 */
	UNAVAILABLE_FOR_LEGAL_REASONS(451),

	// 5xx Server errors
	/**
	 * Code : 500
	 * 
	 * <p>A generic error message, given when an unexpected
	 * condition was encountered and no more specific
	 * message is suitable.</p>
	 */
	INTERNAL_SERVER_ERROR(500),
	/**
	 * Code : 501
	 * 
	 * <p>The server either does not recognize the request
	 * method, or it lacks the ability to fulfil the
	 * request. Usually this implies future availability
	 * (e.g., a new feature of a web-service API).</p>
	 */
	NOT_IMPLEMENTED(501),
	/**
	 * Code : 502
	 * 
	 * <p>The server was acting as a gateway or proxy
	 * and received an invalid response from the
	 * upstream server.</p>
	 */
	BAD_GATEWAY(502),
	/**
	 * Code : 503
	 * 
	 * <p>The server cannot handle the request (because
	 * it is overloaded or down for maintenance).
	 * Generally, this is a temporary state.</p>
	 */
	SERVICE_UNAVAILABLE(503),
	/**
	 * Code : 504
	 * 
	 * <p>The server was acting as a gateway or proxy
	 * and did not receive a timely response from the
	 * upstream server.</p>
	 */
	GATEWAY_TIMEOUT(504),
	/**
	 * Code : 505
	 * 
	 * <p>The server does not support the HTTP protocol
	 * version used in the request.</p>
	 */
	HTTP_VERSION_NOT_SUPPORTED(505),
	/**
	 * Code : 506
	 * 
	 * <p>Transparent content negotiation for the
	 * request results in a circular reference.</p>
	 */
	VARIANT_ALSO_NEGOTIATES(506),
	/**
	 * Code : 507
	 * 
	 * <p>The server is unable to store the
	 * representation needed to complete the request.</p>
	 */
	INSUFFICIENT_STORAGE(507),
	/**
	 * Code : 508
	 * 
	 * <p>The server detected an infinite loop while
	 * processing the request (sent instead of 208
	 * Already Reported).</p>
	 */
	LOOP_DETECTED(508),
	/**
	 * Code : 510
	 * 
	 * <p>Further extensions to the request are
	 * required for the server to fulfil it.</p>
	 */
	NOT_EXTENDED(510),
	/**
	 * Code : 511
	 * 
	 * <p>The client needs to authenticate to gain
	 * network access. Intended for use by intercepting
	 * proxies used to control access to the network
	 * (e.g., "captive portals" used to require
	 * agreement to Terms of Service before granting
	 * full Internet access via a Wi-Fi hotspot).</p>
	 */
	NETWORK_AUTHENTIFICATION_REQUIRED(511);

	/**
	 * The HTTP response code of the status
	 */
	private final int code;

	/**
	 * Private constructor.
	 * 
	 * @param code
	 * 			The int code of this status.
	 */
	HttpStatus(int code) {
		this.code = code;
	}

	/**
	 * Returns the {@link HttpStatus} corresponding to
	 * the code given in param.
	 * 
	 * @param code
	 * 			The code of the HttpStatus to get.
	 * @return	The HttpStatus corresponding to code.
	 */
	@Contract(pure = true)
	public static @Nullable HttpStatus get(int code) {
		for (HttpStatus s : values())
			if (s.code == code)
				return s;
		return null;
	}

	/**
	 * Returns the {@link HttpStatus} corresponding to
	 * the status given in param.
	 *
	 * <p>Note: This method may be slow.</p>
	 * 
	 * @param status
	 * 			The status of the HttpStatus to get.
	 * @return	The HttpStatus corresponding to status.
	 */
	@Contract(value = "null -> null", pure = true)
	public static @Nullable HttpStatus get(@Nullable String status) {
		for (HttpStatus s : values())
			if (s.getStatus().equalsIgnoreCase(status))
				return s;
		return null;
	}

	/**
	 * Check if this HttpStatus is an informational
	 * response (class 1xx).
	 * 
	 * @return	{@code true} if the status is of class
	 * 			1xx, {@code false} otherwise.
	 */
	@Contract(pure = true)
	public boolean isInformationalResponse() {
		return this.code < 200;
	}

	/**
	 * Check if this HttpStatus is a success (class 2xx).
	 * 
	 * @return	{@code true} if the status is of class
	 * 			2xx, {@code false} otherwise.
	 */
	@Contract(pure = true)
	public boolean isSuccess() {
		return this.code >= 200 && this.code < 300;
	}

	/**
	 * Check if this HttpStatus is a redirection
	 * (class 3xx).
	 * 
	 * @return	{@code true} if the status is of class
	 * 			3xx, {@code false} otherwise.
	 */
	@Contract(pure = true)
	public boolean isRedirection() {
		return this.code >= 300 && this.code < 400;
	}

	/**
	 * Check if this HttpStatus is a client error
	 * (class 4xx).
	 * 
	 * @return	{@code true} if the status is of class
	 * 			4xx, {@code false} otherwise.
	 */
	@Contract(pure = true)
	public boolean isClientError() {
		return this.code >= 400 && this.code < 500;
	}

	/**
	 * Check if this HttpStatus is a server error
	 * (class 5xx).
	 * 
	 * @return	{@code true} if the status is of class
	 * 			5xx, {@code false} otherwise.
	 */
	@Contract(pure = true)
	public boolean isServerError() {
		return this.code >= 500;
	}

	/**
	 * Check if this HttpStatus is a normal response
	 * (class 1xx to 3xx).
	 * 
	 * @return	{@code true} if the status is of class
	 * 			1xx, 2xx or 3xx, {@code false} otherwise.
	 */
	@Contract(pure = true)
	public boolean isNormal() {
		return !this.isError();
	}

	/**
	 * Check if this HttpStatus is an error (class 4xx
	 * and 5xx).
	 * 
	 * @return	{@code true} if the status is of class
	 * 			4xx or 5xx, {@code false} otherwise.
	 */
	@Contract(pure = true)
	public boolean isError() {
		return this.isClientError() || this.isServerError();
	}

	/**
	 * Returns the code followed by the status.
	 * 
	 * <p>For example, for the {@link HttpStatus#NOT_FOUND}
	 * status, it returns:</p>
	 * <blockquote><pre>
	 * 404 Not Found
	 * </pre></blockquote>
	 * 
	 * @return	The String representation of this HttpStatus.
	 */
	@Override
	@Contract(value = " -> new", pure = true)
	public @NotNull String toString() {
		return this.code + " " + this.getStatus();
	}

	/**
	 * Returns the code of this HttpStatus.
	 * 
	 * @return	The HTTP response code corresponding to this
	 * 			HttpStatus.
	 */
	@Contract(pure = true)
	public int getCode() {
		return this.code;
	}

	/**
	 * Returns the String status name of the HttpStatus (without
	 * the code).
	 * 
	 * <p>For example, for the {@link HttpStatus#NOT_FOUND}
	 * status, it returns:</p>
	 * <blockquote><pre>
	 * Not Found
	 * </pre></blockquote>
	 * 
	 * @return	The String status name corresponding to this
	 * 			HttpStatus.
	 */
	@Contract(value = " -> new", pure = true)
	public @NotNull String getStatus() {
		char[] s = this.name().replace('_', ' ').toLowerCase().toCharArray();
		boolean space = true;
		for (int i = 0; i < s.length; i++) {
			if (space)
				s[i] += 'A' - 'a';
			space = s[i] == ' ';
		}
		return new String(s);
	}

	/**
	 * Return {@code true} if a response using this status
	 * needs a body.
	 *
	 * @return	{@code true} if the response needs to have
	 * 			a body, {@code false} otherwise.
	 */
	@Contract(pure = true)
	public boolean needResponseBody() {
		return !this.isInformationalResponse() && this != NO_CONTENT && this != NOT_MODIFIED;
	}
}
