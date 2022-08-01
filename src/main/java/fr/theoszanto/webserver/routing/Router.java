package fr.theoszanto.webserver.routing;

import fr.theoszanto.webserver.api.HttpMethod;
import fr.theoszanto.webserver.api.HttpRequest;
import fr.theoszanto.webserver.api.HttpResponse;
import fr.theoszanto.webserver.handling.ErrorsHandler;
import fr.theoszanto.webserver.handling.GetHandler;
import fr.theoszanto.webserver.handling.HandlersContainer;
import fr.theoszanto.webserver.handling.HandlingPrefix;
import fr.theoszanto.webserver.handling.HeadersBeforeSendHandler;
import fr.theoszanto.webserver.handling.HttpMethodHandler;
import fr.theoszanto.webserver.handling.PostHandler;
import fr.theoszanto.webserver.handling.RequestHandler;
import fr.theoszanto.webserver.utils.Checks;
import fr.theoszanto.webserver.utils.MiscUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Router {
	/**
	 * System logger for Router class.
	 */
	private static final @NotNull Logger LOGGER = Logger.getLogger(Router.class.getName());

	/**
	 * All registered routes.
	 */
	private final @NotNull Set<Route> routes = new HashSet<>();

	/**
	 * All registered intermediate routes.
	 */
	private final @NotNull SortedSet<IntermediateRoute> intermediateRoutes = new TreeSet<>();

	/**
	 * All handlers to be invoked before cookies are sent.
	 */
	private final @NotNull List<HeadersBeforeSendHandler> headersBeforeSendHandlers = new ArrayList<>();

	private @Nullable RequestHandler defaultHandler = null;

	private @NotNull ErrorsHandler errorsHandler = ErrorsHandler.LOG;

	/**
	 * Return the most complete route corresponding to the
	 * given request with the given method.
	 *
	 * @param request
	 * 			The request of the handler.
	 * @param method
	 * 			The method of the handler.
	 * @return	The matching route if it exists,
	 * 			{@code null} otherwise.
	 */
	@Contract(pure = true)
	public @Nullable Route route(@NotNull String request, @NotNull HttpMethod method) {
		Checks.notNull(request, "request");
		Checks.notNull(method, "method");
		Route bestMatch = null;
		int bestMatchLength = 0;
		for (Route route : this.routes)
			if (route.match(request, method) && route.length() >= bestMatchLength)
				bestMatchLength = (bestMatch = route).length();
		return bestMatch;
	}

	/**
	 * Register handlers contained in the HandlersContainer instance you
	 * gave.
	 *
	 * @param handlers
	 * 			The instance of the {@link HandlersContainer handlers container}
	 * 			to register.
	 * @return	Itself, to allow chained calls.
	 */
	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull Router registerHandlers(@NotNull HandlersContainer handlers) {
		Checks.notNull(handlers, "handlers");
		return this.registerHandlers(handlers, "");
	}

	/**
	 * Register handlers contained in the HandlersContainer instance you
	 * gave.
	 *
	 * @param handlers
	 * 			The instance of the {@link HandlersContainer handlers container}
	 * 			to register.
	 * @param prefix
	 * 			The route prefix for handlers.
	 * @return	Itself, to allow chained calls.
	 */
	@Contract(value = "_, _ -> this", mutates = "this")
	public @NotNull Router registerHandlers(@NotNull HandlersContainer handlers, @NotNull String prefix) {
		Checks.notNull(handlers, "handlers");
		return this.registerHandlers(handlers.getClass(), handlers, prefix + handlingPrefix(handlers.getClass()));
	}

	/**
	 * Register static handlers contained in the HandlersContainer class.
	 *
	 * @param handlersClass
	 * 			The class where the handlers are.
	 * @return	Itself, to allow chained calls.
	 */
	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull Router registerHandlers(@NotNull Class<? extends HandlersContainer> handlersClass) {
		Checks.notNull(handlersClass, "handlersClass");
		return this.registerHandlers(handlersClass, "");
	}

	/**
	 * Register static handlers contained in the HandlersContainer class.
	 *
	 * @param handlersClass
	 * 			The class where the handlers are.
	 * @param prefix
	 * 			The route prefix for handlers.
	 * @return	Itself, to allow chained calls.
	 */
	@Contract(value = "_, _ -> this", mutates = "this")
	public @NotNull Router registerHandlers(@NotNull Class<? extends HandlersContainer> handlersClass, @NotNull String prefix) {
		Checks.notNull(handlersClass, "handlersClass");
		return this.registerHandlers(handlersClass, null, prefix + handlingPrefix(handlersClass));
	}

	/**
	 * Register handlers contained in the HandlersContainer class.
	 *
	 * @param handlersClass
	 * 			The class where the handlers are.
	 * @param handlersContainer
	 * 			The instance of the {@link HandlersContainer handlers container}
	 * 			to register.
	 * @param prefix
	 * 			The route prefix for handlers.
	 * @return	Itself, to allow chained calls.
	 */
	@Contract(value = "_, _, _ -> this", mutates = "this")
	private @NotNull Router registerHandlers(@NotNull Class<? extends HandlersContainer> handlersClass, @Nullable HandlersContainer handlersContainer, @NotNull String prefix) {
		Checks.notNull(handlersClass, "handlersClass");
		for (Method m : handlersClass.getDeclaredMethods()) {
			if (m.isAnnotationPresent(GetHandler.class) || m.isAnnotationPresent(PostHandler.class) || m.isAnnotationPresent(HttpMethodHandler.class)) {
				RouteBuilder builder = new RouteBuilder();
				this.registerRoutesFromHandler(builder, m, handlersContainer, prefix);
			}
		}
		return this;
	}

	private static @NotNull String handlingPrefix(@NotNull Class<?> handlersClass) {
		return MiscUtils.ifNotNull(handlersClass.getDeclaringClass(), Router::handlingPrefix, "") +
				MiscUtils.ifNotNull(handlersClass.getAnnotation(HandlingPrefix.class), HandlingPrefix::value, "");
	}

	/**
	 * Register the given route.
	 *
	 * <p>Registered routes are called after intermediate
	 * routes, to handle request.</p>
	 *
	 * <p>Note: If a similar route was already registered,
	 * this one will override it.</p>
	 *
	 * @param route
	 * 			The route to register.
	 * @return	Itself, to allow chained calls.
	 */
	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull Router registerRoute(@NotNull Route route) {
		Checks.notNull(route, "route");
		if (this.routes.remove(route))
			LOGGER.warning("Overriding previously registered route with " + route);
		this.routes.add(route);
		return this;
	}

	/**
	 * Register the given intermediate route.
	 *
	 * <p>Registered intermediate routes are called before
	 * final request handling.</p>
	 *
	 * @param intermediateRoute
	 * 			The intermediate route to register.
	 * @return	Itself, to allow chained calls.
	 */
	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull Router registerIntermediateRoute(@NotNull IntermediateRoute intermediateRoute) {
		Checks.notNull(intermediateRoute, "intermediateRoute");
		this.intermediateRoutes.add(intermediateRoute);
		return this;
	}

	@Contract(mutates = "this, param1")
	private void registerRoutesFromHandler(@NotNull RouteBuilder builder, @NotNull Method method, @Nullable HandlersContainer handlersContainer, @NotNull String prefix) {
		GetHandler[] getHandlers = method.getAnnotationsByType(GetHandler.class);
		for (GetHandler getHandler : getHandlers)
			this.register(builder, method, handlersContainer, prefix + getHandler.value(), HttpMethod.GET, getHandler.strict(), getHandler.intermediate());

		PostHandler[] postHandlers = method.getAnnotationsByType(PostHandler.class);
		for (PostHandler postHandler : postHandlers)
			this.register(builder, method, handlersContainer, prefix + postHandler.value(), HttpMethod.POST, postHandler.strict(), postHandler.intermediate());

		HttpMethodHandler[] allHandlers = method.getAnnotationsByType(HttpMethodHandler.class);
		for (HttpMethodHandler allHandler : allHandlers)
			for (HttpMethod m : allHandler.methods())
				this.register(builder, method, handlersContainer, prefix + allHandler.route(), m, allHandler.strict(), allHandler.intermediate());
	}

	@Contract(mutates = "this, param1")
	private void register(@NotNull RouteBuilder builder, @NotNull Method handler, @Nullable HandlersContainer handlersContainer,
	                      @NotNull String route, @NotNull HttpMethod method, boolean strict, boolean intermediate) {
		builder.setRoute(route).setMethod(method).setStrict(strict);
		if (intermediate)
			this.registerIntermediateRoute(builder.setIntermediateHandler(handler, handlersContainer).buildIntermediateRoute());
		else
			this.registerRoute(builder.setHandler(handler, handlersContainer).buildRoute());
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull Router addHeadersBeforeSendHandler(@NotNull HeadersBeforeSendHandler handler) {
		this.headersBeforeSendHandlers.add(handler);
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull Router setDefaultHandler(@NotNull RequestHandler handler) {
		this.defaultHandler = handler;
		return this;
	}

	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull Router setErrorsHandler(@NotNull ErrorsHandler handler) {
		Checks.notNull(handler, "errors handler");
		this.errorsHandler = handler;
		return this;
	}

	@Contract(mutates = "param1, param2")
	public void handle(@NotNull HttpRequest request, @NotNull HttpResponse response) throws Throwable {
		String requestPath = request.getURI().getPath();
		HttpMethod requestMethod = request.getMethod();

		for (IntermediateRoute route : this.intermediateRoutes) {
			if (route.match(requestPath, requestMethod)) {
				request.setRoute(route);
				if (!route.getHandler().handle(request, response))
					return;
			}
		}

		Route route = this.route(requestPath, requestMethod);
		if (route != null) {
			request.setRoute(route);
			route.getHandler().handle(request, response);
		}

		if (this.defaultHandler != null)
			this.defaultHandler.handle(request, response);
	}

	@Contract(mutates = "param1")
	public void handleBeforeHeadersSend(@NotNull HttpResponse response) throws IOException {
		for (HeadersBeforeSendHandler handler : this.headersBeforeSendHandlers)
			handler.beforeHeadersSend(response);
	}

	@Contract(pure = true)
	public @NotNull ErrorsHandler getErrorsHandler() {
		return this.errorsHandler;
	}

	/**
	 * Log router information using {@link Level#INFO}.
	 */
	public void logDebugInfo() {
		this.logDebugInfo(Level.INFO);
	}

	/**
	 *  Log router information using the given {@link Level level}.
	 *
	 * @param level
	 * 			The Level used to log information.
	 */
	public void logDebugInfo(@NotNull Level level) {
		Checks.notNull(level, "level");
		LOGGER.log(level, "Debug caller: " + MiscUtils.caller());
		LOGGER.log(level, "v ===== Debug information for router ===== v");
		LOGGER.log(level, "Registered routes: (" + this.routes.size() + ")");
		this.routes.forEach(route -> LOGGER.log(level, "\t" + route));
		LOGGER.log(level, "Registered intermediate routes: (" + this.intermediateRoutes.size() + ")");
		this.intermediateRoutes.forEach(route -> LOGGER.log(level, "\t" + route));
		LOGGER.log(level, "^ ===== Debug information for router ===== ^");
	}
}
