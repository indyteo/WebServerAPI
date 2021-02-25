package fr.theoszanto.webserver.handler;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import fr.theoszanto.webserver.api.HttpMethod;
import fr.theoszanto.webserver.routing.Router;
import org.jetbrains.annotations.NotNull;

/**
 * Annotation used to mark methods from
 * {@link HandlersContainer handlers containers} which
 * should handle requests from single or multiple
 * {@link HttpMethod method(s)}.
 * 
 * <p>There are two shortcut annotations {@link GetHandler} and {@link PostHandler}
 * for the two most common HttpMethods.</p>
 * 
 * @author	indyteo
 * @see		Router#registerHandlers(HandlersContainer)
 * @see		HttpMethod
 * @see		GetHandler
 * @see		PostHandler
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(HttpMethodHandler.Repeated.class)
public @interface HttpMethodHandler {
	/**
	 * The base route defining which routes handle.
	 * 
	 * <p>Note that if multiple handlers matches a route,
	 * only the most complete is called.</p>
	 * 
	 * @return	The base route for this handler method.
	 */
	@NotNull String route() default "/";

	/**
	 * The {@link HttpMethod}(s) that should be handled by this
	 * handler method.
	 * 
	 * @return	All methods handled by this handler method.
	 */
	@NotNull HttpMethod[] methods() default {
			HttpMethod.CONNECT,
			HttpMethod.DELETE,
			HttpMethod.GET,
			HttpMethod.HEAD,
			HttpMethod.OPTIONS,
			HttpMethod.PATCH,
			HttpMethod.POST,
			HttpMethod.PUT,
			HttpMethod.TRACE
	};

	/**
	 * Whether the request may be longer or not.
	 *
	 * @return  {@code true} if the route need to completely
	 *          match the requested path, {@code false} otherwise.
	 */
	boolean strict() default false;

	/**
	 * Repeatable annotation container
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@interface Repeated {
		@NotNull HttpMethodHandler[] value();
	}
}
