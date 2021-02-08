package fr.theoszanto.webserver.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import fr.theoszanto.webserver.WebServer;

/**
 * Shortcut annotation used to mark methods from
 * {@link HandlersContainer handlers containers} which
 * should handle {@link HttpMethod#POST POST requests}.
 * 
 * @author	indyteo
 * @see		WebServer#registerHandlers(HandlersContainer)
 * @see		HttpMethod#POST
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PostHandler {
	/**
	 * The base route defining which routes handle.
	 * 
	 * <p>Note that if multiple handlers matches a route,
	 * only the most complete is called.
	 * 
	 * @return	The base route for this handler method.
	 */
	public String value() default "/";
}