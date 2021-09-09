package fr.theoszanto.webserver.api;

import fr.theoszanto.webserver.handling.HandlersContainer;
import fr.theoszanto.webserver.handling.HttpMethodHandler;
import fr.theoszanto.webserver.utils.Checks;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CorsManager implements HandlersContainer {
	private final @NotNull List<@NotNull String> allowedOrigins;

	public CorsManager() {
		this("*");
	}

	public CorsManager(@NotNull String allowedOrigin) {
		Checks.notEmpty(allowedOrigin, "allowedOrigin");
		this.allowedOrigins = Collections.singletonList(allowedOrigin);
	}

	public CorsManager(@NotNull String @NotNull... allowedOrigins) {
		Checks.notEmpty(allowedOrigins, "allowedOrigins");
		Checks.noneEmpty(allowedOrigins, "allowedOrigin");
		this.allowedOrigins = Arrays.asList(allowedOrigins);
	}

	public CorsManager(@NotNull List<@NotNull String> allowedOrigins) {
		Checks.notEmpty(allowedOrigins, "allowedOrigins");
		Checks.noneEmpty(allowedOrigins, "allowedOrigin");
		this.allowedOrigins = allowedOrigins;
	}

	@HttpMethodHandler(intermediate = true)
	private boolean globalCorsHeaders(HttpRequest request, HttpResponse response) {
		String origin;
		if (this.allowedOrigins.size() == 1)
			origin = this.allowedOrigins.get(0);
		else {
			String requestOrigin = request.getOrigin();
			if (this.allowedOrigins.contains(requestOrigin))
				origin = requestOrigin;
			else
				origin = this.allowedOrigins.get(0);
		}
		response.header("Vary", "Origin")
				.header("Access-control-allow-origin", origin)
				.header("Access-control-allow-credentials", "true");
		return true;
	}

	@HttpMethodHandler(methods = HttpMethod.OPTIONS)
	private void allowedMethodsHeader(HttpRequest request, HttpResponse response) throws IOException {
		response.header("Access-control-allow-methods", "*").setStatus(HttpStatus.OK).end();
	}
}
