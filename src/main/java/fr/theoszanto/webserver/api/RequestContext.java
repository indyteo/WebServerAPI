package fr.theoszanto.webserver.api;

import fr.theoszanto.webserver.utils.Checks;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class RequestContext {
	private final @NotNull Map<@NotNull String, @NotNull Object> content;

	public RequestContext() {
		this.content = new HashMap<>();
	}

	public RequestContext(@NotNull Map<@NotNull String, @NotNull Object> content) {
		Checks.notNull(content, "content");
		this.content = content;
	}

	public @NotNull RequestContext set(@NotNull String key, @NotNull Object value) {
		Checks.notEmpty(key, "key");
		Checks.notNull(value, "value");
		this.content.put(key, value);
		return this;
	}

	@SuppressWarnings("unchecked")
	public <T> @NotNull T get(@NotNull String key) {
		if (this.has(key)) {
			Object value = this.content.get(key);
			try {
				return (T) value;
			} catch (ClassCastException e) {
				throw new IllegalStateException("Context value associated with key " + key + " has wrong type (" + value.getClass() + ")", e);
			}
		}
		throw new IllegalArgumentException("Cannot find context value with key " + key);
	}

	public boolean has(@NotNull String key) {
		Checks.notEmpty(key, "key");
		return this.content.containsKey(key);
	}

	public boolean has(@NotNull String key, @NotNull Class<?> type) {
		Checks.notNull(type, "type");
		return this.has(key) && type.isAssignableFrom(this.content.get(key).getClass());
	}

	public void remove(@NotNull String key) {
		Checks.notEmpty(key, "key");
		this.content.remove(key);
	}
}
