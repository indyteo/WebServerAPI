package fr.theoszanto.webserver.utils;

import com.google.gson.Gson;
import fr.theoszanto.webserver.WebServer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Paths;

public class JsonUtils {
	public static final Gson GSON = new Gson();

	private JsonUtils() {}

	@Contract(value = "_, null, _ -> null; _, !null, _ -> !null", pure = true)
	public static <T> @Nullable T fromFile(@NotNull WebServer server, @Nullable String path, @NotNull Type type) throws FileNotFoundException {
		Checks.notNull(server, "response");
		Checks.notNull(type, "type");
		if (path == null)
			return null;
		return fromFile(Paths.get(server.getRoot(), path).toFile(), type);
	}

	@Contract(value = "null, _ -> null; !null, _ -> !null", pure = true)
	public static <T> @Nullable T fromFile(@Nullable File file, @NotNull Type type) throws FileNotFoundException {
		Checks.notNull(type, "type");
		if (file == null)
			return null;
		return GSON.fromJson(new FileReader(file), type);
	}

	public static void toFile(@NotNull WebServer server, @NotNull String path, @Nullable Object object) throws IOException {
		Checks.notNull(server, "response");
		Checks.notNull(path, "path");
		toFile(Paths.get(server.getRoot(), path).toFile(), object);
	}

	public static void toFile(@NotNull File file, @Nullable Object object) throws IOException {
		Checks.notNull(file, "file");
		if (!file.exists() && !file.createNewFile())
			throw new IOException("Unable to create file: " + file);
		FileWriter writer = new FileWriter(file);
		GSON.toJson(object, writer);
		writer.close();
	}
}
