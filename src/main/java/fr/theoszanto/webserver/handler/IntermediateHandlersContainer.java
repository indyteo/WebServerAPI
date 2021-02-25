package fr.theoszanto.webserver.handler;

import fr.theoszanto.webserver.routing.Router;

/**
 * Used to tell a class contains IntermediateHandler and can be registered
 * with {@link Router#registerIntermediateHandlers(IntermediateHandlersContainer)}.
 * 
 * @author	indyteo
 * @see		Router#registerIntermediateHandlers(IntermediateHandlersContainer)
 */
public interface IntermediateHandlersContainer {}
