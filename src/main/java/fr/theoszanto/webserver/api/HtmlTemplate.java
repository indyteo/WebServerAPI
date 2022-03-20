package fr.theoszanto.webserver.api;

import fr.theoszanto.webserver.WebServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class HtmlTemplate {
	private final File file;
	private final Map<String, Object> placeholders = new HashMap<>();

	private static final Map<String, HtmlTemplate> loadedTemplates = new HashMap<>();

	protected HtmlTemplate(File file) {
		this.file = file;
	}

	public HtmlTemplate placeholder(String name, Object value) {
		this.placeholders.put(name, value);
		return this;
	}

	public void send(OutputStream sendTo) throws IOException {
		InputStream is = new FileInputStream(this.file);
		int read;
		boolean escaped = false, placeholder = false;
		StringBuilder name = new StringBuilder();
		while ((read = is.read()) != -1) {
			char c = (char) read;
			if (escaped) {
				if (placeholder)
					name.append(c);
				else
					sendTo.write(c);
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
						((HtmlTemplate) value).send(sendTo);
					else if (value != null)
						sendTo.write(value.toString().getBytes());
					name = new StringBuilder();
				} else
					name.append(c);
			} else if (c == '{')
				placeholder = true;
			else
				sendTo.write(c);
		}

		this.placeholders.clear();
	}

	public static void loadTemplates(WebServer server, String... templates) {
		for (String template : templates)
			loadTemplate(server, template);
	}

	public static void loadTemplates(WebServer server, Class<?> clazz) {
		for (Field field : clazz.getDeclaredFields()) {
			FileTemplate fileTemplate = field.getAnnotation(FileTemplate.class);
			if (fileTemplate != null && Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
				try {
					field.setAccessible(true);
					field.set(null, loadTemplate(server, fileTemplate.value()));
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static HtmlTemplate loadTemplate(WebServer server, String template) {
		HtmlTemplate htmlTemplate = new HtmlTemplate(new File(server.getRoot(), template + ".html"));
		loadedTemplates.put(template, htmlTemplate);
		return htmlTemplate;
	}

	public static HtmlTemplate getTemplate(String template) {
		return loadedTemplates.get(template);
	}
}
