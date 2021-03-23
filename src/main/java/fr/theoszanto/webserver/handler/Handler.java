package fr.theoszanto.webserver.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import fr.theoszanto.webserver.WebServer;
import fr.theoszanto.webserver.api.HttpRequest;
import fr.theoszanto.webserver.api.HttpResponse;
import fr.theoszanto.webserver.api.HttpStatus;
import fr.theoszanto.webserver.api.WebServerException;
import fr.theoszanto.webserver.routing.Router;
import fr.theoszanto.webserver.utils.Checks;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The HttpHandler which link the server to the API.
 *
 * @author	indyteo
 */
public class Handler implements HttpHandler {
	/**
	 * System logger for Handler class.
	 */
	private static final @NotNull Logger LOGGER = Logger.getLogger(Handler.class.getName());

	/**
	 * The server containing useful data such as the root or the handlers.
	 *
	 * @see		WebServer
	 */
	private final @NotNull WebServer server;

	/**
	 * Construct a new handler bound to the given WebServer.
	 *
	 * @param server
	 * 			The WebServer to bind this handler to.
	 */
	public Handler(@NotNull WebServer server) {
		Checks.notNull(server, "server");
		this.server = server;
	}

	/**
	 * Method called by the system to handler request using HttpExchange.
	 * 
	 * <p>This method parse the HttpExchange and build two objects
	 * (HttpRequest &amp; HttpResponse), search for the handler registered
	 * for the requested route with the requested method, and call it.
	 * At the end, it close the exchange.</p>
	 * 
	 * @param exchange
	 * 			The HttpExchange received from the system call.
	 * @throws IOException
	 * 			When an I/O exception is thrown by one of the
	 * 			{@code HttpExchange}'s method.
	 * @see		Router#registerHandlers(HandlersContainer)
	 * @see		Router#registerIntermediateHandlers(IntermediateHandlersContainer)
	 */
	@Override
	public final void handle(@NotNull HttpExchange exchange) throws IOException {
		Checks.notNull(exchange, "exchange");
		HttpRequest request = new HttpRequest(this.server, exchange);
		HttpResponse response = new HttpResponse(this.server, exchange);

		try {
			this.server.getRouter().handle(request, response);

			// Executed only if handler did not terminate
			if (response.getStatus() == null)
				response.setStatus(HttpStatus.NOT_FOUND);
			if (request.getMethod().needResponseBody())
				response.end();
			else
				response.endWithoutBody();
		} catch (HandlingEndException ignored) {
		} catch (Throwable e) {
			LOGGER.log(Level.SEVERE, "Uncatched error!", e);
			request.logDebugInfo();
			response.logDebugInfo();
			throw new WebServerException("An error occured while handling a request.", e);
		}
	}
}
