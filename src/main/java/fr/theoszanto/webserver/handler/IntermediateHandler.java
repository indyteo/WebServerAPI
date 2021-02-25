package fr.theoszanto.webserver.handler;

import fr.theoszanto.webserver.api.HttpRequest;
import fr.theoszanto.webserver.api.HttpResponse;
import fr.theoszanto.webserver.api.HttpStatus;
import fr.theoszanto.webserver.routing.IntermediateRoute;
import fr.theoszanto.webserver.routing.Router;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * An intermediate handler for client requests that can be
 * assigned to an intermediate route.
 *
 * @author	indyteo
 * @see		Router#registerIntermediateRoute(IntermediateRoute)
 */
@FunctionalInterface
public interface IntermediateHandler {
	/**
	 * Do nothing and continue request handling.
	 */
	IntermediateHandler NO_OP = (request, response) -> true;
	/**
	 * End with a {@link HttpStatus#FORBIDDEN 403 Forbidden} status.
	 */
	IntermediateHandler FORBIDDEN = endingWithStatus(HttpStatus.FORBIDDEN);
	/**
	 * End with a {@link HttpStatus#NOT_FOUND 404 Not Found} status.
	 */
	IntermediateHandler DEFAULT = endingWithStatus(HttpStatus.NOT_FOUND);
	/**
	 * Log request informations and continue request handling.
	 */
	IntermediateHandler LOG = (request, response) -> {
		request.logDebugInfo();
		return true;
	};

	/**
	 * Basic handler that end with the given status.
	 *
	 * @param status
	 * 			The status to end with.
	 * @return	The intermediate ending handler.
	 */
	@Contract(value = "_ -> new", pure = true)
	static @NotNull IntermediateHandler endingWithStatus(@NotNull HttpStatus status) {
		if (status.needResponseBody()) {
			return (request, response) -> {
				response.setStatus(status).end();
				return false;
			};
		} else {
			return (request, response) -> {
				response.setStatus(status).endWithoutBody();
				return false;
			};
		}
	}

	/**
	 * Handle the request and prepare a response for the client.
	 *
	 * @param request
	 * 			The client request.
	 * @param response
	 * 			The server response.
	 * @return	Whether to continue request handling or not.
	 * @throws IOException
	 * 			If an error occurs during the process.
	 */
	boolean handle(@NotNull HttpRequest request, @NotNull HttpResponse response) throws IOException;
}
