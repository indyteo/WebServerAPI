package fr.theoszanto.webserver.api;

import fr.theoszanto.webserver.WebServer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Represent an HTML file with placeholders
 * dynamically replaced with runtime values.
 * @see FileTemplate @FileTemplate("my-file.tmpl")
 */
public class HtmlTemplate {
	/**
	 * The file this template represent.
	 */
	private final @NotNull File file;
	/**
	 * The placeholders registered to be replaced in the next
	 * {@link #send(OutputStream) .send(...)} or
	 * {@link #get() .get()} call, then cleared.
	 */
	private final @NotNull Map<@NotNull String, @Nullable Object> placeholders = new HashMap<>();

	/**
	 * Loaded templates cache.
	 */
	private static final @NotNull Map<@NotNull String, @NotNull HtmlTemplate> loadedTemplates = new HashMap<>();

	/**
	 * Construct a new template with the given file.
	 * @param file The file to bind this template to.
	 * @throws WebServerException If the file does
	 *                            not exist or is
	 *                            not readable.
	 */
	protected HtmlTemplate(@NotNull File file) throws WebServerException {
		if (!file.canRead())
			throw new WebServerException(file.getAbsolutePath());
		this.file = file;
	}

	/**
	 * Register a placeholder to be replaced in the next
	 * {@link #send(OutputStream) .send(...)} or
	 * {@link #get() .get()} call.
	 * @param name The name of the placeholder.
	 * @param value The value to substitute.
	 * @return {@code this}, to allow chained calls.
	 */
	@Contract(value = "_, _ -> this", mutates = "this")
	public @NotNull HtmlTemplate placeholder(@NotNull String name, @Nullable Object value) {
		this.placeholders.put(name, value);
		return this;
	}

	/**
	 * Get this template as a string.
	 * Placeholders are substituted,
	 * then cleared.
	 * @return The formatted template.
	 * @throws IOException If an I/O exception
	 *                     occurs when reading
	 *                     the file template.
	 */
	@Contract(value = " -> new", mutates = "this")
	public @NotNull String get() throws IOException {
		StringBuilder builder = new StringBuilder();
		this.output(new BuilderOutput(builder));
		return builder.toString();
	}

	/**
	 * Send this template to an output stream.
	 * Placeholders are substituted,
	 * then cleared.
	 * @param sendTo The stream to write to.
	 * @throws IOException If an I/O exception
	 *                     occurs when reading
	 *                     the file template.
	 */
	@Contract(mutates = "this, param1")
	public void send(@NotNull OutputStream sendTo) throws IOException {
		this.output(new StreamOutput(sendTo));
	}

	/**
	 * Output this template to the given output.
	 * Placeholders are substituted,
	 * then cleared.
	 * @param sendTo The output to send to.
	 * @throws IOException If an I/O exception
	 *                     occurs when reading
	 *                     the file template.
	 */
	@Contract(mutates = "this, param1")
	private void output(@NotNull Output sendTo) throws IOException {
		try (InputStream is = Files.newInputStream(this.file.toPath())) {
			int read;
			boolean escaped = false, placeholder = false;
			StringBuilder name = new StringBuilder();
			while ((read = is.read()) != -1) {
				char c = (char) read;
				if (escaped) {
					if (placeholder)
						name.append(c);
					else
						sendTo.send(c);
					escaped = false;
				} else if (c == '\\')
					escaped = true;
				else if (placeholder) {
					if (c == '{')
						throw new IllegalStateException("Unescaped placeholder opening mark in placeholder name! Template: " + this.file);
					if (c == '}') {
						placeholder = false;
						Object value = this.placeholders.get(name.toString());
						if (value instanceof HtmlTemplate)
							((HtmlTemplate) value).output(sendTo);
						else if (value != null)
							sendTo.send(value.toString());
						name = new StringBuilder();
					} else
						name.append(c);
				} else if (c == '{')
					placeholder = true;
				else
					sendTo.send(c);
			}
		}
		this.reset();
	}

	/**
	 * Reset the template placeholders.
	 */
	@Contract(mutates = "this")
	public void reset() {
		this.placeholders.clear();
	}

	/**
	 * Load the given templates. A template named {@code my-template}
	 * uses the file {@code my-template.html} that must exists.
	 * @param server The WebServer to search the files from.
	 * @param templates The names of the templates to load.
	 * @throws WebServerException If a template is not found.
	 */
	public static void loadTemplates(@NotNull WebServer server, @NotNull String @NotNull... templates) throws WebServerException {
		for (String template : templates)
			loadTemplate(server, template);
	}

	/**
	 * Automatically load and inject the templates from the
	 * fields annotated by {@link FileTemplate @FileTemplate("name")}.
	 * @param server The WebServer to search the files from.
	 * @param clazz The class to list fields from.
	 * @throws WebServerException If a template is not found
	 *                            or the field is invalid.
	 * @see #loadTemplates(WebServer, String...)
	 */
	public static void loadTemplates(@NotNull WebServer server, @NotNull Class<?> clazz) throws WebServerException {
		for (Field field : clazz.getDeclaredFields()) {
			FileTemplate fileTemplate = field.getAnnotation(FileTemplate.class);
			if (fileTemplate != null
					&& Modifier.isStatic(field.getModifiers())
					&& !Modifier.isFinal(field.getModifiers())
					&& field.getType().isAssignableFrom(HtmlTemplate.class)) {
				try {
					field.setAccessible(true);
					field.set(null, loadTemplate(server, fileTemplate.value()));
				} catch (IllegalAccessException e) {
					throw new WebServerException("Failed to inject template in the field", e);
				}
			}
		}
	}

	/**
	 * Load the given template.
	 * @param server The WebServer to search the file from.
	 * @param template The name of the template to load.
	 * @return The loaded template.
	 * @throws WebServerException If a template is not found.
	 */
	@Contract("_, _ -> new")
	private static @NotNull HtmlTemplate loadTemplate(@NotNull WebServer server, @NotNull String template) throws WebServerException {
		HtmlTemplate htmlTemplate = new HtmlTemplate(new File(server.getRoot(), template + ".html"));
		loadedTemplates.put(template, htmlTemplate);
		return htmlTemplate;
	}

	/**
	 * Get the loaded template with the given name.
	 * @param template The name of the template to get.
	 * @return The loaded template.
	 * @throws IllegalArgumentException If the template is
	 *                                  unknown or unloaded.
	 */
	@Contract(pure = true)
	public static @NotNull HtmlTemplate getTemplate(@NotNull String template) {
		if (loadedTemplates.containsKey(template))
			return loadedTemplates.get(template);
		throw new IllegalArgumentException("Unknown (or unloaded) template: " + template);
	}

	/**
	 * Represent an output for the template.
	 */
	private interface Output {
		/**
		 * Send a string to the output.
		 * @param str The string to send.
		 * @throws IOException If an I/O exception occurs.
		 */
		@Contract(mutates = "this")
		void send(@NotNull String str) throws IOException;

		/**
		 * Send a character to the output.
		 * @param c The char to send.
		 * @throws IOException If an I/O exception occurs.
		 */
		@Contract(mutates = "this")
		void send(char c) throws IOException;
	}

	/**
	 * An output bound to an {@link OutputStream}.
	 */
	private static class StreamOutput implements Output {
		/**
		 * The backing output stream.
		 */
		private final @NotNull OutputStream stream;

		/**
		 * Construct a new output on the given stream.
		 * @param stream The stream to wrap.
		 */
		private StreamOutput(@NotNull OutputStream stream) {
			this.stream = stream;
		}

		@Override
		@Contract(mutates = "this")
		public void send(@NotNull String str) throws IOException {
			this.stream.write(str.getBytes(StandardCharsets.UTF_8));
		}

		@Override
		@Contract(mutates = "this")
		public void send(char c) throws IOException {
			this.stream.write(c);
		}
	}

	/**
	 * An output bound to a {@link StringBuilder}.
	 */
	private static class BuilderOutput implements Output {
		/**
		 * The backing string builder.
		 */
		private final @NotNull StringBuilder builder;

		/**
		 * Construct a new output on the given builder.
		 * @param builder The builder to wrap.
		 */
		private BuilderOutput(@NotNull StringBuilder builder) {
			this.builder = builder;
		}

		@Override
		@Contract(mutates = "this")
		public void send(@NotNull String str) {
			this.builder.append(str);
		}

		@Override
		@Contract(mutates = "this")
		public void send(char c) {
			this.builder.append(c);
		}
	}
}
