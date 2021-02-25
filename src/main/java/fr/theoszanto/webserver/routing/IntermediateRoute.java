package fr.theoszanto.webserver.routing;

import fr.theoszanto.webserver.api.HttpMethod;
import fr.theoszanto.webserver.handler.IntermediateHandler;
import fr.theoszanto.webserver.utils.Checks;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntermediateRoute extends BaseRoute implements Comparable<IntermediateRoute> {
	private final IntermediateHandler handler;

	IntermediateRoute(@NotNull String route, @Nullable HttpMethod method, @NotNull IntermediateHandler handler, boolean strict, @Nullable String name) {
		super(route, method, strict, name);
		Checks.notNull(handler, "handler");
		this.handler = handler;
	}

	@Contract(pure = true)
	public @NotNull IntermediateHandler getHandler() {
		return this.handler;
	}

	@Override
	@Contract(pure = true)
	public int compareTo(@NotNull IntermediateRoute o) {
		int res = this.length() - o.length();
		return res == 0 ? this.getId() - o.getId() : res;
	}

	@Override
	@Contract(value = " -> new", pure = true)
	public @NotNull String toString() {
		return super.toString() + " [+]";
	}
}
