package fr.theoszanto.webserver.routing;

import fr.theoszanto.webserver.api.HttpMethod;
import fr.theoszanto.webserver.handler.RequestHandler;
import fr.theoszanto.webserver.utils.Checks;
import fr.theoszanto.webserver.utils.MiscUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Route extends BaseRoute {
	private final RequestHandler handler;

	Route(@NotNull String route, @Nullable HttpMethod method, @NotNull RequestHandler handler, boolean strict, @Nullable String name) {
		super(route, method, strict, name);
		Checks.notNull(handler, "handler");
		this.handler = handler;
	}

	@Contract(pure = true)
	public @NotNull RequestHandler getHandler() {
		return this.handler;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.getMethod(), this.getStrippedRoute());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Route route = (Route) o;
		return MiscUtils.nullOrEquals(this.getMethod(), route.getMethod())
				&& this.getStrippedRoute().equals(route.getStrippedRoute());
	}
}
