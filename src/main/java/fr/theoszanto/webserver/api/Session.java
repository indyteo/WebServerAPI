package fr.theoszanto.webserver.api;

import fr.theoszanto.webserver.utils.Checks;
import fr.theoszanto.webserver.utils.JsonUtils;
import fr.theoszanto.webserver.utils.MiscUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class Session {
	private @Nullable String id;
	private @Nullable File file;
	private @Nullable SessionData data;

	private final @NotNull HttpResponse response;

	public static final @NotNull String SESSION_ID_COOKIE_NAME = "WS_SESSION_ID";
	public static final long DEFAULT_SESSION_TIMEOUT = 3600;

	public Session(@NotNull HttpRequest request, @NotNull HttpResponse response) throws IOException {
		Checks.notNull(request, "request");
		Checks.notNull(response, "response");
		this.response = response;

		this.id = request.getCookie(SESSION_ID_COOKIE_NAME);
		if (this.id != null) {
			this.file = this.file();
			if (this.file.exists()) {
				this.data = JsonUtils.fromFile(this.file, SessionData.class);
				if (this.data.expiration < Instant.now().getEpochSecond())
					this.destroy();
			}
		}

		request.setSession(this);
	}

	@Contract(pure = true)
	private @NotNull File file() {
		return Paths.get(this.response.getServer().getSessionsDir(), this.id + ".json").toFile();
	}

	@Contract(pure = true)
	public @Nullable String getId() {
		return this.id;
	}

	@Contract(pure = true)
	public boolean isInit() {
		return this.id != null;
	}

	@Contract(pure = true)
	public @Nullable String get(@NotNull String name) {
		return MiscUtils.ifNotNull(this.data, data -> data.values.get(name));
	}

	@Contract(mutates = "this")
	public void set(@NotNull String name, @Nullable String value) throws IOException {
		this.init();
		assert this.data != null; // Data cannot be null after init call
		this.data.values.put(name, value);
		this.saveData();
	}

	@Contract(mutates = "this")
	public void saveData() throws IOException {
		this.init();
		assert this.file != null; // File cannot be null after init call
		JsonUtils.toFile(this.file, this.data);
	}

	@Contract(mutates = "this")
	public void init() throws IOException {
		this.init(DEFAULT_SESSION_TIMEOUT);
	}

	@Contract(mutates = "this")
	public void init(@Range(from = 0, to = Long.MAX_VALUE) long timeout) throws IOException {
		if (this.isInit())
			return;

		this.id = MiscUtils.randomString(32);
		this.file = this.file();
		if (!this.file.createNewFile())
			throw new IOException("Unable to initialize session " + this.id);
		this.data = new SessionData(timeout);
		this.response.cookie(new Cookie.Builder()
				.setName(SESSION_ID_COOKIE_NAME)
				.setValue(this.id)
				.setHttpOnly(true)
				.setMaxAge(timeout)
				.setSameSite(Cookie.SameSitePolicy.LAX)
				.build());
		JsonUtils.toFile(this.file, this.data);
	}

	@Contract(mutates = "this")
	public void destroy() throws IOException {
		this.destroy(true);
	}

	@Contract(mutates = "this")
	private void destroy(boolean deleteCookie) throws IOException {
		if (this.file != null && !this.file.delete())
			throw new IOException("Unable to destroy session " + this.id);
		this.id = null;
		if (deleteCookie)
			this.response.deleteCookie(SESSION_ID_COOKIE_NAME);
	}

	@Contract(mutates = "this")
	public void regenerateId() throws IOException {
		SessionData old = this.data;
		this.destroy(false);
		if (old != null) {
			this.init(old.timeout);
			assert this.data != null; // Data cannot be null after init call
			this.data.values.putAll(old.values);
		}
		this.saveData();
	}

	private static class SessionData {
		private final long timeout;
		private final long expiration;
		private final @NotNull Map<String, String> values;

		private SessionData(long timeout) {
			this.timeout = timeout;
			this.expiration = Instant.now().getEpochSecond() + timeout;
			this.values = new HashMap<>();
		}
	}
}
