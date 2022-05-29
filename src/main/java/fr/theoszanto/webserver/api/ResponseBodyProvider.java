package fr.theoszanto.webserver.api;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;

@FunctionalInterface
public interface ResponseBodyProvider {
	void fillOutputStream(@NotNull OutputStream responseBody) throws IOException;
}
