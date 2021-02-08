package fr.theoszanto.webserver.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import fr.theoszanto.webserver.WebServer;

/**
 * Annotation used to mark methods from
 * {@link HandlersContainer handlers containers} which
 * should handle requests from single or multiple
 * {@link HttpMethod method(s)}.
 * 
 * <p>There are two shortcut annotations {@link GetHandler} and {@link PostHandler}
 * for the two most common HttpMethods.
 * 
 * @author	indyteo
 * @see		WebServer#registerHandlers(HandlersContainer)
 * @see		HttpMethod
 * @see		GetHandler
 * @see		PostHandler
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HttpMethodHandler {
	/**
	 * The base route defining which routes handle.
	 * 
	 * <p>Note that if multiple handlers matches a route,
	 * only the most complete is called.
	 * 
	 * @return	The base route for this handler method.
	 */
	public String route() default "/";
	
	/**
	 * The {@link HttpMethod}(s) that should be handled by this
	 * handler method.
	 * 
	 * @return	All methods handled by this handler method.
	 */
	public HttpMethod[] method() default {
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
}