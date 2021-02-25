package fr.theoszanto.webserver.utils;

import fr.theoszanto.webserver.handler.HandlersContainer;
import fr.theoszanto.webserver.api.HttpRequest;
import fr.theoszanto.webserver.api.HttpResponse;
import fr.theoszanto.webserver.handler.IntermediateHandlersContainer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;

public class Checks {
	private Checks() {}

	@Contract("false, _, _ -> fail")
	private static void check(boolean condition, @NotNull String message, @NotNull Type type) throws InvalidCheckException {
		if (!condition)
			throw new InvalidCheckException(type, message);
	}

	@Contract("true, _, _ -> fail")
	private static void not(boolean condition, @NotNull String message, @NotNull Type type) throws InvalidCheckException {
		check(!condition, message, type);
	}

	@Contract("false, _ -> fail")
	public static void check(boolean condition, @NotNull String message) throws InvalidCheckException {
		check(condition, message, Type.BOOLEAN);
	}

	@Contract("true, _ -> fail")
	public static void not(boolean condition, @NotNull String message) throws InvalidCheckException {
		check(!condition, message, Type.BOOLEAN);
	}

	@Contract("null, _ -> fail")
	public static void notNull(@Nullable Object argument, @NotNull String name) throws InvalidCheckException {
		if (argument == null)
			throw new InvalidCheckException(Type.NULL, name + " cannot be null");
	}

	@Contract("null, _ -> fail")
	public static void notEmpty(@Nullable Collection<?> argument, @NotNull String name) throws InvalidCheckException {
		notNull(argument, name);
		if (argument.isEmpty())
			throw new InvalidCheckException(Type.EMPTY, name + " cannot be empty");
	}

	@Contract("null, _ -> fail")
	public static void notEmpty(@Nullable Object[] argument, @NotNull String name) throws InvalidCheckException {
		notNull(argument, name);
		if (argument.length == 0)
			throw new InvalidCheckException(Type.EMPTY, name + " cannot be empty");
	}

	@Contract("null, _ -> fail")
	public static void notEmpty(@Nullable CharSequence argument, @NotNull String name) throws InvalidCheckException {
		notNull(argument, name);
		if (argument.length() == 0)
			throw new InvalidCheckException(Type.EMPTY, name + " cannot be empty");
	}

	@Contract("null, _ -> fail")
	public static void noneNull(@Nullable Collection<?> argument, @NotNull String name) throws InvalidCheckException {
		notNull(argument, name);
		for (Object o : argument)
			if (o == null)
				throw new InvalidCheckException(Type.NULL, name + " cannot contains null");
	}

	@Contract("null, _ -> fail")
	public static void noneNull(@Nullable Object[] argument, @NotNull String name) throws InvalidCheckException {
		notNull(argument, name);
		for (Object o : argument)
			if (o == null)
				throw new InvalidCheckException(Type.NULL, name + " cannot contains null");
	}

	@Contract("null, _ -> fail")
	public static void validHandlerMethod(@Nullable Method handler, @Nullable HandlersContainer container) throws InvalidCheckException {
		notNull(handler, "handler");
		Class<?>[] parameterTypes = handler.getParameterTypes();
		check(parameterTypes.length == 2 && HttpRequest.class.equals(parameterTypes[0]) && HttpResponse.class.equals(parameterTypes[1]),
				"Handler method must receive " + HttpRequest.class.getName() + " and " + HttpResponse.class.getName() + " as parameters", Type.HANDLER);
		check(handler.getReturnType().equals(void.class), "Handler method cannot return value", Type.HANDLER);
		if ((handler.getModifiers() & Modifier.STATIC) == 0)
			notNull(container, "container (of non-static handler)");
	}

	@Contract("null, _ -> fail")
	public static void validIntermediateHandlerMethod(@Nullable Method handler, @Nullable IntermediateHandlersContainer container) throws InvalidCheckException {
		notNull(handler, "handler");
		Class<?>[] parameterTypes = handler.getParameterTypes();
		check(parameterTypes.length == 2 && HttpRequest.class.equals(parameterTypes[0]) && HttpResponse.class.equals(parameterTypes[1]),
				"Intermediate handler method must receive " + HttpRequest.class.getName() + " and " + HttpResponse.class.getName() + " as parameters", Type.HANDLER);
		check(handler.getReturnType().equals(boolean.class), "Intermediate handler method must return boolean", Type.HANDLER);
		if ((handler.getModifiers() & Modifier.STATIC) == 0)
			notNull(container, "container (of non-static intermediate handler)");
	}

	public static class InvalidCheckException extends RuntimeException {
		private final @NotNull Type type;

		private InvalidCheckException(@NotNull Type type, @NotNull String message) {
			super("[" + type + "] " + message);
			this.type = type;
		}

		@Contract(pure = true)
		public @NotNull Type getType() {
			return this.type;
		}
	}

	public enum Type {
		NULL, EMPTY, BOOLEAN, HANDLER
	}
}
