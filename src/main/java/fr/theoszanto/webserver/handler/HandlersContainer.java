package fr.theoszanto.webserver.handler;

import fr.theoszanto.webserver.routing.Router;

/**
 * Used to tell a class contains Handler and can be registered
 * with {@link Router#registerHandlers(HandlersContainer)}.
 * 
 * @author	indyteo
 * @see		Router#registerHandlers(HandlersContainer)
 */
public interface HandlersContainer {}
