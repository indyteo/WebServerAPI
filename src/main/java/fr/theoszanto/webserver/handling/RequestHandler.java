package fr.theoszanto.webserver.handling;

import fr.theoszanto.webserver.api.HttpRequest;
import fr.theoszanto.webserver.api.HttpResponse;
import fr.theoszanto.webserver.api.HttpStatus;
import fr.theoszanto.webserver.routing.Route;
import fr.theoszanto.webserver.routing.Router;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * A final handler for client requests that can be
 * assigned to a route.
 *
 * @author	indyteo
 * @see		Router#registerRoute(Route)
 */
@FunctionalInterface
public interface RequestHandler {
	/**
	 * End with a {@link HttpStatus#NO_CONTENT 204 No Content} status.
	 */
	RequestHandler NO_OP = endingWithStatus(HttpStatus.NO_CONTENT);
	/**
	 * End with a {@link HttpStatus#FORBIDDEN 403 Forbidden} status.
	 */
	RequestHandler FORBIDDEN = endingWithStatus(HttpStatus.FORBIDDEN);
	/**
	 * End with a {@link HttpStatus#NOT_FOUND 404 Not Found} status.
	 */
	RequestHandler DEFAULT = endingWithStatus(HttpStatus.NOT_FOUND);
	/**
	 * Log request informations then call {@link RequestHandler#NO_OP}.
	 */
	RequestHandler LOG = (request, response) -> {
		request.logDebugInfo();
		NO_OP.handle(request, response);
	};

	/**
	 * Basic handler that end with the given status.
	 *
	 * @param status
	 * 			The status to end with.
	 * @return	The handler.
	 */
	@Contract(value = "_ -> new", pure = true)
	static @NotNull RequestHandler endingWithStatus(@NotNull HttpStatus status) {
		if (status.needResponseBody())
			return (request, response) -> response.setStatus(status).end();
		else
			return (request, response) -> response.setStatus(status).endWithoutBody();
	}

	/**
	 * Handle the request and produce a response for the client.
	 *
	 * @param request
	 * 			The client request.
	 * @param response
	 * 			The server response.
	 * @throws IOException
	 * 			If an error occurs during the process.
	 */
	void handle(@NotNull HttpRequest request, @NotNull HttpResponse response) throws IOException;
}
