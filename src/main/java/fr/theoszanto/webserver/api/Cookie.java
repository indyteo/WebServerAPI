package fr.theoszanto.webserver.api;

import fr.theoszanto.webserver.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class Cookie {
	private final @NotNull String name;
	private final @NotNull String value;
	private final @Nullable Date expires;
	private final long maxAge;
	private final @Nullable String domain;
	private final @Nullable String path;
	private final boolean secure;
	private final boolean httpOnly;
	private final @Nullable SameSitePolicy sameSite;

	private static final DateFormat gmtFormat = DateFormat.getInstance();

	static {
		gmtFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	public Cookie(@NotNull String name,
	              @NotNull String value,
	              @Nullable Date expires,
	              long maxAge,
	              @Nullable String domain,
	              @Nullable String path,
	              boolean secure,
	              boolean httpOnly,
	              @Nullable SameSitePolicy sameSite) {
		Checks.notEmpty(name, "name");
		Checks.notNull(value, "value");
		this.name = name;
		this.value = value;
		this.expires = expires;
		this.maxAge = maxAge;
		this.domain = domain;
		this.path = path;
		this.secure = secure;
		this.httpOnly = httpOnly;
		this.sameSite = sameSite;
	}

	@Override
	public String toString() {
		List<String> parts = new ArrayList<>();
		parts.add(this.name + "=" + this.value);
		if (this.expires != null)
			parts.add("Expires=" + gmtFormat.format(this.expires));
		if (this.maxAge != -1)
			parts.add("Max-Age=" + this.maxAge);
		if (this.domain != null)
			parts.add("Domain=" + this.domain);
		if (this.path != null)
			parts.add("Path=" + this.path);
		if (this.secure)
			parts.add("Secure");
		if (this.httpOnly)
			parts.add("HttpOnly");
		if (this.sameSite != null)
			parts.add("SameSite=" + this.sameSite);
		return String.join("; ", parts);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Cookie cookie = (Cookie) o;

		return name.equals(cookie.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	public enum SameSitePolicy {
		STRICT, LAX, NONE;

		@Override
		public String toString() {
			String name = this.name();
			return name.charAt(0) + name.substring(1).toLowerCase();
		}
	}

	public static class Builder {
		private @Nullable String name;
		private @Nullable String value;
		private @Nullable Date expires = null;
		private long maxAge = -1;
		private @Nullable String domain = null;
		private @Nullable String path = null;
		private boolean secure = false;
		private boolean httpOnly = false;
		private @Nullable Cookie.SameSitePolicy sameSite = null;

		public @NotNull Cookie build() {
			Checks.notEmpty(this.name, "name");
			if (this.value == null)
				this.setValue("");
			return new Cookie(
					this.name,
					this.value,
					this.expires,
					this.maxAge,
					this.domain,
					this.path,
					this.secure,
					this.httpOnly,
					this.sameSite
			);
		}

		public @NotNull Builder setName(@NotNull String name) {
			Checks.notEmpty(name, "name");
			this.name = name;
			return this;
		}

		public @NotNull Builder setValue(@Nullable String value) {
			this.value = value;
			return this;
		}

		public @NotNull Builder setExpires(@Nullable Date expires) {
			this.expires = expires;
			return this;
		}

		public @NotNull Builder setMaxAge(long maxAge) {
			this.maxAge = maxAge;
			return this;
		}

		public @NotNull Builder setDomain(@Nullable String domain) {
			this.domain = domain;
			return this;
		}

		public @NotNull Builder setPath(@Nullable String path) {
			this.path = path;
			return this;
		}

		public @NotNull Builder setSecure(boolean secure) {
			this.secure = secure;
			return this;
		}

		public @NotNull Builder setHttpOnly(boolean httpOnly) {
			this.httpOnly = httpOnly;
			return this;
		}

		public @NotNull Builder setSameSite(@Nullable SameSitePolicy sameSite) {
			this.sameSite = sameSite;
			return this;
		}
	}
}
