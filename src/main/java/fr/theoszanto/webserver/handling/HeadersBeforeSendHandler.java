package fr.theoszanto.webserver.handling;

import fr.theoszanto.webserver.api.HttpResponse;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@FunctionalInterface
public interface HeadersBeforeSendHandler {
	void beforeHeadersSend(@NotNull HttpResponse response) throws IOException;
}
