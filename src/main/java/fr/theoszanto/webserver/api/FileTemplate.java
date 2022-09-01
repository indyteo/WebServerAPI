package fr.theoszanto.webserver.api;

import fr.theoszanto.webserver.WebServer;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a field for injection with a template.
 * Only {@code static non-final} fields of type
 * {@link HtmlTemplate} may be annotated with this.
 * The method {@link HtmlTemplate#loadTemplates(WebServer, Class)}
 * must be called with the class containing the
 * annotated field for the value to be injected.
 * @see HtmlTemplate#loadTemplates(WebServer, String...)
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FileTemplate {
	/**
	 * The name of the template as if you called
	 * {@link HtmlTemplate#loadTemplates(WebServer, String...)}.
	 * @return The name of the template to inject
	 */
	@NotNull String value();
}
