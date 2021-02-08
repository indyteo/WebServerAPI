package fr.theoszanto.webserver.api;

import fr.theoszanto.webserver.WebServer;

/**
 * Used to tell a class contains Handler and can be registered
 * with {@link WebServer#registerHandlers(HandlersContainer)}.
 * 
 * @author	indyteo
 * @see		WebServer#registerHandlers(HandlersContainer)
 */
public interface HandlersContainer {}