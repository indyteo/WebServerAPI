package fr.theoszanto.webserver.routing;

import fr.theoszanto.webserver.api.HttpRequest;
import fr.theoszanto.webserver.api.HttpResponse;
import fr.theoszanto.webserver.handler.GetHandler;
import fr.theoszanto.webserver.handler.HandlersContainer;
import fr.theoszanto.webserver.api.HttpMethod;
import fr.theoszanto.webserver.handler.HttpMethodHandler;
import fr.theoszanto.webserver.handler.IntermediateHandlersContainer;
import fr.theoszanto.webserver.handler.PostHandler;
import fr.theoszanto.webserver.utils.Checks;
import fr.theoszanto.webserver.utils.MiscUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.HashSet;
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
		return this.registerHandlers(handlers.getClass(), handlers);
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
		return this.registerHandlers(handlersClass, null);
	}

	/**
	 * Register handlers contained in the HandlersContainer class.
	 *
	 * @param handlersClass
	 * 			The class where the handlers are.
	 * @param handlersContainer
	 * 			The instance of the {@link HandlersContainer handlers container}
	 * 			to register.
	 * @return	Itself, to allow chained calls.
	 */
	@Contract(value = "_, _ -> this", mutates = "this")
	private @NotNull Router registerHandlers(@NotNull Class<? extends HandlersContainer> handlersClass, @Nullable HandlersContainer handlersContainer) {
		Checks.notNull(handlersClass, "handlersClass");
		for (Method m : handlersClass.getDeclaredMethods()) {
			if (m.isAnnotationPresent(GetHandler.class) || m.isAnnotationPresent(PostHandler.class) || m.isAnnotationPresent(HttpMethodHandler.class)) {
				RouteBuilder builder = new RouteBuilder().setHandler(m, handlersContainer);
				registerRoutesFromHandler(builder, m, () -> this.registerRoute(builder.buildRoute()));
			}
		}
		return this;
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
	 * Register intermediate handlers contained in the
	 * IntermediateHandlersContainer instance you gave.
	 *
	 * @param intermediateHandlers
	 * 			The instance of the {@link IntermediateHandlersContainer intermediate handlers container}
	 * 			to register.
	 * @return	Itself, to allow chained calls.
	 */
	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull Router registerIntermediateHandlers(@NotNull IntermediateHandlersContainer intermediateHandlers) {
		Checks.notNull(intermediateHandlers, "intermediateHandlers");
		return this.registerIntermediateHandlers(intermediateHandlers.getClass(), intermediateHandlers);
	}

	/**
	 * Register static intermediate handlers contained in the
	 * IntermediateHandlersContainer class.
	 *
	 * @param intermediateHandlersClass
	 * 			The class where the intermediate handlers are.
	 * @return	Itself, to allow chained calls.
	 */
	@Contract(value = "_ -> this", mutates = "this")
	public @NotNull Router registerIntermediateHandlers(@NotNull Class<? extends IntermediateHandlersContainer> intermediateHandlersClass) {
		Checks.notNull(intermediateHandlersClass, "intermediateHandlersClass");
		return this.registerIntermediateHandlers(intermediateHandlersClass, null);
	}

	/**
	 * Register intermediate handlers contained in the
	 * IntermediateHandlersContainer class.
	 *
	 * @param intermediateHandlersClass
	 * 			The class where the handlers are.
	 * @param intermediateHandlersContainer
	 * 			The instance of the {@link IntermediateHandlersContainer intermediate handlers container}
	 * 			to register.
	 * @return	Itself, to allow chained calls.
	 */
	@Contract(value = "_, _ -> this", mutates = "this")
	private @NotNull Router registerIntermediateHandlers(@NotNull Class<? extends IntermediateHandlersContainer> intermediateHandlersClass, @Nullable IntermediateHandlersContainer intermediateHandlersContainer) {
		Checks.notNull(intermediateHandlersClass, "intermediateHandlersClass");
		for (Method m : intermediateHandlersClass.getDeclaredMethods()) {
			RouteBuilder builder = new RouteBuilder().setIntermediateHandler(m, intermediateHandlersContainer);
			registerRoutesFromHandler(builder, m, () -> this.registerIntermediateRoute(builder.buildIntermediateRoute()));
		}
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

	@Contract(mutates = "param1")
	private static void registerRoutesFromHandler(@NotNull RouteBuilder builder, @NotNull Method method, @NotNull Runnable build) {
		GetHandler[] getHandlers = method.getAnnotationsByType(GetHandler.class);
		for (GetHandler getHandler : getHandlers) {
			builder.setRoute(getHandler.value()).setMethod(HttpMethod.GET).setStrict(getHandler.strict());
			build.run();
		}

		PostHandler[] postHandlers = method.getAnnotationsByType(PostHandler.class);
		for (PostHandler postHandler : postHandlers) {
			builder.setRoute(postHandler.value()).setMethod(HttpMethod.POST).setStrict(postHandler.strict());
			build.run();
		}

		HttpMethodHandler[] allHandlers = method.getAnnotationsByType(HttpMethodHandler.class);
		for (HttpMethodHandler allHandler : allHandlers) {
			builder.setRoute(allHandler.route()).setStrict(allHandler.strict());
			for (HttpMethod m : allHandler.methods()) {
				builder.setMethod(m);
				build.run();
			}
		}
	}

	@Contract(mutates = "param1, param2")
	public void handle(@NotNull HttpRequest request, @NotNull HttpResponse response) throws Exception {
		String requestPath = request.getURI().getPath();
		HttpMethod requestMethod = request.getMethod();

		for (IntermediateRoute route : this.intermediateRoutes) {
			if (route.match(requestPath, requestMethod)) {
				request.setRouteParams(route.params(requestPath));
				if (!route.getHandler().handle(request, response))
					return;
			}
		}

		Route route = this.route(requestPath, requestMethod);
		if (route != null) {
			request.setRouteParams(route.params(requestPath));
			route.getHandler().handle(request, response);
		}
	}

	/**
	 * Log router informations using {@link Level#INFO}.
	 */
	public void logDebugInfo() {
		this.logDebugInfo(Level.INFO);
	}

	/**
	 *  Log router informations using the given {@link Level level}.
	 *
	 * @param level
	 * 			The Level used to log informations.
	 */
	public void logDebugInfo(@NotNull Level level) {
		Checks.notNull(level, "level");
		LOGGER.log(level, "Debug caller: " + MiscUtils.caller());
		LOGGER.log(level, "v ===== Debug informations for router ===== v");
		LOGGER.log(level, "Registered routes: (" + this.routes.size() + ")");
		this.routes.forEach(route -> LOGGER.log(level, "\t" + route));
		LOGGER.log(level, "Registered intermediate routes: (" + this.intermediateRoutes.size() + ")");
		this.intermediateRoutes.forEach(route -> LOGGER.log(level, "\t" + route));
		LOGGER.log(level, "^ ===== Debug informations for router ===== ^");
	}
}
