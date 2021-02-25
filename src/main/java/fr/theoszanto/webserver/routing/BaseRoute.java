package fr.theoszanto.webserver.routing;

import fr.theoszanto.webserver.api.HttpMethod;
import fr.theoszanto.webserver.utils.Checks;
import fr.theoszanto.webserver.utils.MiscUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseRoute {
	private final @NotNull String route;
	private final @Nullable HttpMethod method;
	private final boolean strict;
	private final @NotNull String name;

	private final int id;
	private final @NotNull String strippedRoute;
	private final @NotNull Pattern internalPattern;
	private final @NotNull List<String> groups = new ArrayList<>();

	private static int ids = 0;

	public static final @NotNull Pattern routeEscape = Pattern.compile("[\\\\.\\[\\](){}<>*+\\-=!?^$|]");
	public static final @NotNull Pattern paramEscape = Pattern.compile("\\\\\\{\\\\\\\\([^\\\\.\\[\\](){}<>*+\\-=!?^$|])");
	public static final @NotNull Pattern paramsPattern = Pattern.compile("\\\\\\{(?<d1>\\\\\\{)?(?<name>[a-zA-Z][a-zA-Z0-9]*?)(?<d2>\\\\})?\\\\}");

	BaseRoute(@NotNull String route, @Nullable HttpMethod method, boolean strict, @Nullable String name) {
		Checks.notEmpty(route, "route");
		this.id = ++ids;
		this.route = route;
		this.method = method;
		this.strict = strict;
		this.name = MiscUtils.ifEmpty(name, "Unnamed route");

		String escapedRoute = routeEscape.matcher(route).replaceAll("\\\\$0");
		Matcher paramsMatcher = paramsPattern.matcher(escapedRoute);
		StringBuilder regexBuilder = new StringBuilder("^");
		while (paramsMatcher.find()) {
			this.groups.add(paramsMatcher.group("name"));
			boolean full = "\\{".equals(paramsMatcher.group("d1")) && "\\}".equals(paramsMatcher.group("d2"));
			paramsMatcher.appendReplacement(regexBuilder, "(?<${name}>" + (full ? "." : "[^/]") + "+)");
		}
		paramsMatcher.appendTail(regexBuilder);
		if (strict)
			regexBuilder.append("$");
		else if (regexBuilder.charAt(regexBuilder.length() - 1) != '/')
			regexBuilder.append("(?:/|$)");
		String regex = regexBuilder.toString();
		this.internalPattern = Pattern.compile(paramEscape.matcher(regex).replaceAll("\\\\{$1"), Pattern.CASE_INSENSITIVE);
		this.strippedRoute = paramsMatcher.replaceAll("{}");
	}

	@Contract(pure = true)
	public int getId() {
		return this.id;
	}

	@Contract(pure = true)
	public @NotNull String getRoute() {
		return this.route;
	}

	@Contract(pure = true)
	public @Nullable HttpMethod getMethod() {
		return this.method;
	}

	@Contract(pure = true)
	public boolean isStrict() {
		return this.strict;
	}

	@Contract(pure = true)
	public @NotNull String getName() {
		return this.name;
	}

	@Contract(pure = true)
	public boolean match(@NotNull String request, @NotNull HttpMethod method) {
		Checks.notEmpty(request, "request");
		Checks.notNull(method, "method");
		return (this.method == null || this.method == method) && this.internalPattern.matcher(request).find();
	}

	@Contract(value = "_ -> new", pure = true)
	public @NotNull Map<String, String> params(@NotNull String request) {
		Checks.notEmpty(request, "request");
		Matcher matcher = this.internalPattern.matcher(request);
		if (!matcher.find())
			throw new IllegalArgumentException("Request did not match the route. Did you tried Route.match(String request, HttpMethod method)?");
		Map<String, String> params = new HashMap<>();
		for (String group : this.groups)
			params.put(group, matcher.group(group));
		return params;
	}

	@Contract(pure = true)
	public @NotNull String getStrippedRoute() {
		return this.strippedRoute;
	}

	@Contract(pure = true)
	public int length() {
		return this.strippedRoute.length();
	}

	@Override
	@Contract(pure = true)
	public int hashCode() {
		return Objects.hash(this.id);
	}

	@Override
	@Contract(value = "null -> false", pure = true)
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		BaseRoute baseRoute = (BaseRoute) o;
		return this.id == baseRoute.id;
	}

	@Override
	@Contract(value = " -> new", pure = true)
	public @NotNull String toString() {
		return MiscUtils.ifNull(this.method, "[*]") + " " + this.route + " (" + this.id + ": " + this.name + ")";
	}
}
