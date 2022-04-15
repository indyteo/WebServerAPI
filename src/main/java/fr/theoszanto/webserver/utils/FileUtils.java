package fr.theoszanto.webserver.utils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class FileUtils {
	public static @NotNull String normalizePath(@NotNull String path) {
		return new File(path).toPath().normalize().toString();
	}

	public static @NotNull URI appendURI(@NotNull URI uri, @NotNull String @NotNull... paths) {
		for (String path : paths)
			for (String append : path.split("[/\\\\]"))
				uri = uri.resolve(append + "/");
		return uri;
	}

	public static void extractResources(@NotNull Class<?> clazz, @NotNull String resourcesPath, @NotNull File destination) throws IllegalStateException {
		extractResources(clazz, resourcesPath, destination, false);
	}

	public static void extractResources(@NotNull Class<?> clazz, @NotNull String resourcesPath, @NotNull File destination, boolean replaceExisting) throws IllegalStateException {
		String normalizedPath = normalizePath(resourcesPath);
		URL location = clazz.getProtectionDomain().getCodeSource().getLocation();
		CopyOption[] options = replaceExisting ? new CopyOption[] { StandardCopyOption.REPLACE_EXISTING } : new CopyOption[0];
		if (location.getFile().endsWith(".jar")) {
			ClassLoader loader = clazz.getClassLoader();
			int prefix = normalizedPath.length();
			try {
				forEachJarEntry(location, entry -> {
					if (entry.isDirectory())
						return;
					String name = entry.getName();
					if (!name.startsWith(normalizedPath))
						return;
					InputStream resource = loader.getResourceAsStream(name);
					if (resource == null)
						return;
					File target = new File(destination, name.substring(prefix));
					ensureDirectoryExists(target.getParentFile());
					Files.copy(resource, target.toPath(), options);
				});
			} catch (IOException e) {
				throw new IllegalStateException("Unable to extract resources from JAR", e);
			}
		} else {
			try {
				copy(new File(appendURI(location.toURI(), normalizedPath)), destination, options);
			} catch (URISyntaxException | IOException e) {
				throw new IllegalStateException("Unable to extract resources from Class-Path", e);
			}
		}
	}

	public static void forEachJarEntry(@NotNull URL jarLocation, @NotNull JarEntryConsumer action) throws IOException {
		try (JarInputStream jar = new JarInputStream(jarLocation.openStream())) {
			JarEntry entry;
			while ((entry = jar.getNextJarEntry()) != null)
				action.consume(entry);
		}
	}

	public static void copy(@NotNull File from, @NotNull File to, @NotNull CopyOption @NotNull... options) throws IOException {
		if (from.isDirectory()) {
			String[] files = from.list();
			if (files == null)
				throw new IOException("Unable to copy " + from.getName() + " into " + to.getName());
			ensureDirectoryExists(to);
			for (String file : files)
				copy(new File(from, file), new File(to, file), options);
		} else
			Files.copy(from.toPath(), to.toPath(), options);
	}

	public static void ensureDirectoryExists(@NotNull File directory) throws IOException {
		if (!directory.exists() && !directory.mkdirs())
			throw new IOException("Unable to create folders: " + directory);
	}

	@FunctionalInterface
	public interface JarEntryConsumer {
		void consume(@NotNull JarEntry entry) throws IOException;
	}
}
