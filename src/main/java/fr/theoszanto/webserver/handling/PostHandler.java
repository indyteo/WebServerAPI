package fr.theoszanto.webserver.handling;

import fr.theoszanto.webserver.api.HttpMethod;
import fr.theoszanto.webserver.routing.Router;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Shortcut annotation used to mark methods from
 * {@link HandlersContainer handlers containers} which
 * should handle {@link HttpMethod#POST POST requests}.
 * 
 * @author	indyteo
 * @see		Router#registerHandlers(HandlersContainer)
 * @see		HttpMethod#POST
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(PostHandler.Repeated.class)
public @interface PostHandler {
	/**
	 * The base route defining which routes handle.
	 * 
	 * <p>Note that if multiple handlers matches a route,
	 * only the most complete is called.</p>
	 * 
	 * @return	The base route for this handler method.
	 */
	@NotNull String value() default "/";

	/**
	 * Whether the request may be longer or not.
	 *
	 * @return  {@code true} if the route need to completely
	 *          match the requested path, {@code false} otherwise.
	 */
	boolean strict() default true;

	/**
	 * Whether the handler is intermediate or not.
	 *
	 * @return  {@code true} if the handler is intermediate,
	 *          {@code false} otherwise.
	 */
	boolean intermediate() default false;

	/**
	 * Repeatable annotation container
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@interface Repeated {
		@NotNull PostHandler @NotNull[] value();
	}
}
