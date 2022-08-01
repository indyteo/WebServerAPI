package fr.theoszanto.webserver.api;

import fr.theoszanto.webserver.WebServer;
import fr.theoszanto.webserver.handling.HandlersContainer;
import fr.theoszanto.webserver.handling.HttpMethodHandler;
import fr.theoszanto.webserver.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CorsManager implements HandlersContainer {
	private @Nullable List<@NotNull String> allowedOrigins = null;
	private @Nullable List<@NotNull String> allowedHeaders = null;
	private @Nullable List<@NotNull HttpMethod> allowedMethods = null;
	private boolean allowCredentials = true;
	private @Nullable List<@NotNull String> exposedHeaders = null;

	public static final @NotNull String ALLOW_ORIGIN = "Access-control-allow-origin";
	public static final @NotNull String ALLOW_HEADERS = "Access-control-allow-headers";
	public static final @NotNull String ALLOW_METHODS = "Access-control-allow-methods";
	public static final @NotNull String ALLOW_CREDENTIALS = "Access-control-allow-credentials";

	public static final @NotNull String EXPOSE_HEADERS = "Access-control-expose-headers";

	public static final @NotNull String REQUEST_ORIGIN = "Origin";
	public static final @NotNull String REQUEST_HEADERS = "Access-control-request-headers";
	public static final @NotNull String REQUEST_METHOD = "Access-control-request-method";

	public static final @NotNull List<@NotNull String> HIDDEN_HEADERS = Arrays.asList("cookie", "set-cookie");

	public CorsManager(@NotNull WebServer server) {
		server.getRouter().addHeadersBeforeSendHandler(this::beforeHeadersSend);
	}

	public @NotNull CorsManager allowAllOrigins() {
		this.allowedOrigins = null;
		return this;
	}

	public @NotNull CorsManager allowOrigins(@NotNull String @NotNull... allowedOrigins) {
		return this.allowOrigins(Arrays.asList(allowedOrigins));
	}

	public @NotNull CorsManager allowOrigins(@NotNull List<@NotNull String> allowedOrigins) {
		Checks.notEmpty(allowedOrigins, "allowedOrigins");
		Checks.noneEmpty(allowedOrigins, "allowedOrigin");
		if (this.allowedOrigins == null)
			this.allowedOrigins = new ArrayList<>();
		this.allowedOrigins.addAll(allowedOrigins);
		return this;
	}

	public @NotNull CorsManager allowAllHeaders() {
		this.allowedHeaders = null;
		return this;
	}

	public @NotNull CorsManager allowHeaders(@NotNull String @NotNull... allowedHeaders) {
		return this.allowOrigins(Arrays.asList(allowedHeaders));
	}

	public @NotNull CorsManager allowHeaders(@NotNull List<@NotNull String> allowedHeaders) {
		Checks.notEmpty(allowedHeaders, "allowedHeaders");
		Checks.noneEmpty(allowedHeaders, "allowedHeader");
		if (this.allowedHeaders == null)
			this.allowedHeaders = new ArrayList<>();
		this.allowedHeaders.addAll(allowedHeaders);
		return this;
	}

	public @NotNull CorsManager allowAllMethods() {
		this.allowedMethods = null;
		return this;
	}

	public @NotNull CorsManager allowMethods(@NotNull HttpMethod @NotNull... allowedMethods) {
		return this.allowMethods(Arrays.asList(allowedMethods));
	}

	public @NotNull CorsManager allowMethods(@NotNull List<@NotNull HttpMethod> allowedMethods) {
		Checks.notEmpty(allowedMethods, "allowedMethods");
		Checks.noneNull(allowedMethods, "allowedMethod");
		if (this.allowedMethods == null)
			this.allowedMethods = new ArrayList<>();
		this.allowedMethods.addAll(allowedMethods);
		return this;
	}

	public @NotNull CorsManager allowCredentials(boolean allowCredentials) {
		this.allowCredentials = allowCredentials;
		return this;
	}

	private @NotNull String bestAllowedOrigin(@Nullable String requestOrigin) {
		boolean isAcceptingAll = this.allowedOrigins == null;
		if (requestOrigin == null)
			return isAcceptingAll ? "*" : this.allowedOrigins.get(0);
		if (isAcceptingAll || this.allowedOrigins.contains(requestOrigin))
			return requestOrigin;
		return this.allowedOrigins.get(0);
	}

	@HttpMethodHandler(intermediate = true)
	private boolean globalCorsHeaders(HttpRequest request, HttpResponse response) {
		response.header("Vary", "Origin")
				.header(ALLOW_ORIGIN, this.bestAllowedOrigin(request.getOrigin()))
				.header(ALLOW_CREDENTIALS, Boolean.toString(this.allowCredentials));
		return true;
	}

	@HttpMethodHandler(methods = HttpMethod.OPTIONS)
	private void optionsCorsHeaders(HttpRequest request, HttpResponse response) throws IOException {
		if (this.allowedOrigins == null) {
			// Allow every origin
			String origin = request.header(REQUEST_ORIGIN);
			if (origin != null)
				response.header(ALLOW_ORIGIN, origin);
		}

		if (this.allowedHeaders == null) {
			// Allow every header
			String headers = request.header(REQUEST_HEADERS);
			if (headers != null)
				response.header(ALLOW_HEADERS, headers);
		} else
			response.header(ALLOW_HEADERS, String.join(", ", this.allowedHeaders));

		if (this.allowedMethods == null) {
			// Allow every method
			String method = request.header(REQUEST_METHOD);
			if (method != null)
				response.header(ALLOW_METHODS, method);
		} else
			response.header(ALLOW_METHODS, this.allowedMethods.stream().map(HttpMethod::toString).collect(Collectors.joining(", ")));

		response.setStatus(HttpStatus.OK).end();
	}

	private static boolean headerNotHidden(@NotNull String header) {
		return !HIDDEN_HEADERS.contains(header);
	}

	private @NotNull String getHeadersToExpose(@NotNull HttpResponse response) {
		if (this.exposedHeaders == null)
			return response.getHeaders().keySet().stream()
					.map(String::toLowerCase)
					.filter(CorsManager::headerNotHidden)
					.collect(Collectors.joining(", "));
		return String.join(", ", this.exposedHeaders);
	}

	private void beforeHeadersSend(@NotNull HttpResponse response) {
		response.header(EXPOSE_HEADERS, this.getHeadersToExpose(response));
	}
}
