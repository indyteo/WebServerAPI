package fr.theoszanto.webserver.handling;

import fr.theoszanto.webserver.api.HttpRequest;
import fr.theoszanto.webserver.api.HttpResponse;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@FunctionalInterface
public interface ErrorsHandler {
	@NotNull Logger LOGGER = Logger.getLogger(ErrorsHandler.class.getName());

	@NotNull ErrorsHandler LOG = (request, response, error) -> {
		LOGGER.log(Level.SEVERE, "An error occurred while handling a request", error);
		request.logDebugInfo();
		response.logDebugInfo();
	};

	void handleError(@NotNull HttpRequest request, @NotNull HttpResponse response, @NotNull Throwable error) throws IOException;
}
