package fr.theoszanto.webserver.routing;

import fr.theoszanto.webserver.handler.HandlersContainer;
import fr.theoszanto.webserver.api.HttpMethod;
import fr.theoszanto.webserver.handler.HandlingEndException;
import fr.theoszanto.webserver.handler.IntermediateHandler;
import fr.theoszanto.webserver.handler.IntermediateHandlersContainer;
import fr.theoszanto.webserver.handler.RequestHandler;
import fr.theoszanto.webserver.utils.Checks;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RouteBuilder {
	private @NotNull String route = "/";
	private @Nullable HttpMethod method = null;
	private @NotNull RequestHandler handler = RequestHandler.NO_OP;
	private @NotNull IntermediateHandler intermediateHandler = IntermediateHandler.NO_OP;
	private boolean strict = false;
	private @Nullable String name = null;

	@Contract(" -> new")
	public @NotNull Route buildRoute() {
		Checks.notEmpty(this.route, "route");
		Checks.notNull(this.handler, "handler");
		return new Route(this.route, this.method, this.handler, this.strict, this.name);
	}

	@Contract(" -> new")
	public @NotNull IntermediateRoute buildIntermediateRoute() {
		Checks.notEmpty(this.route, "route");
		Checks.notNull(this.intermediateHandler, "intermediateHandler");
		return new IntermediateRoute(this.route, this.method, this.intermediateHandler, this.strict, this.name);
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull RouteBuilder setRoute(@NotNull String route) {
		Checks.notEmpty(route, "route");
		this.route = route;
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull RouteBuilder setMethod(@Nullable HttpMethod method) {
		this.method = method;
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull RouteBuilder setHandler(@NotNull RequestHandler handler) {
		Checks.notNull(handler, "handler");
		this.handler = handler;
		return this;
	}

	@Contract(value = "_, _ -> this", mutates = "this")
	public @NotNull RouteBuilder setHandler(@NotNull Method handler, @Nullable HandlersContainer container) {
		this.handler = handlerMethod(handler, container);
		if (this.name == null)
			this.name = handler.getName();
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull RouteBuilder setIntermediateHandler(@NotNull IntermediateHandler intermediateHandler) {
		Checks.notNull(intermediateHandler, "intermediateHandler");
		this.intermediateHandler = intermediateHandler;
		return this;
	}

	@Contract(value = "_, _ -> this", mutates = "this")
	public @NotNull RouteBuilder setIntermediateHandler(@NotNull Method handler, @Nullable IntermediateHandlersContainer container) {
		this.intermediateHandler = handlerMethod(handler, container);
		if (this.name == null)
			this.name = handler.getName();
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull RouteBuilder setStrict(boolean strict) {
		this.strict = strict;
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull RouteBuilder setName(@Nullable String name) {
		this.name = name;
		return this;
	}

	@Contract("_, _ -> new")
	public static @NotNull RequestHandler handlerMethod(@NotNull Method handler, @Nullable HandlersContainer container) {
		Checks.validHandlerMethod(handler, container);
		handler.setAccessible(true);
		return (request, response) -> {
			try {
				handler.invoke(container, request, response);
			} catch (IllegalAccessException | InvocationTargetException e) {
				if (e.getCause() instanceof HandlingEndException)
					throw (HandlingEndException) e.getCause();
				throw new IOException("An exception occured during request handling", e);
			}
		};
	}

	@Contract("_, _ -> new")
	public static @NotNull IntermediateHandler handlerMethod(@NotNull Method handler, @Nullable IntermediateHandlersContainer container) {
		Checks.validIntermediateHandlerMethod(handler, container);
		handler.setAccessible(true);
		return (request, response) -> {
			try {
				return (boolean) handler.invoke(container, request, response);
			} catch (IllegalAccessException | InvocationTargetException e) {
				if (e.getCause() instanceof HandlingEndException)
					throw (HandlingEndException) e.getCause();
				throw new IOException("An exception occured during request intermediate handling", e);
			}
		};
	}
}
