package fr.theoszanto.webserver.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class MiscUtils {
	public static final Pattern HTML_SPECIAL_CHARS = Pattern.compile("[&<>\"']");
	private static final Map<Character, String> HTML_ESCAPE_MAP = new HashMap<>() {{
		put('&', "&amp;");
		put('<', "&lt;");
		put('>', "&gt;");
		put('"', "&quot;");
		put('\'', "&#039;");
	}};
	private static final List<String> IGNORED_CALLS = Arrays.asList("logDebugInfo", "getStackTrace", "caller");

	private MiscUtils() {}

	@Contract(value = "!null, _ -> param1; null, _ -> param2", pure = true)
	public static <T> @Nullable T ifNull(@Nullable T value, @Nullable T nullDefault) {
		return value == null ? nullDefault : value;
	}

	@Contract(value = "!null, _ -> param1", pure = true)
	public static <T> @Nullable T ifNullGet(@Nullable T value, @NotNull Supplier<T> nullDefault) {
		return value == null ? nullDefault.get() : value;
	}

	@Contract(value = "null, _ -> param2; _, !null -> !null", pure = true)
	public static @Nullable String ifEmpty(@Nullable String string, @Nullable String emptyDefault) {
		return isEmpty(string) ? emptyDefault : string;
	}

	@Contract(value = "null, _, _ -> param3", pure = true)
	public static <K, V> @Nullable V getOr(@Nullable Map<K, V> map, @Nullable K key, @Nullable V def) {
		return map == null ? def : map.getOrDefault(key, def);
	}

	@Contract(value = "null, _ -> null", pure = true)
	public static <K, V> @Nullable V get(@Nullable Map<K, V> map, @Nullable K key) {
		return getOr(map, key, null);
	}

	@Contract(value = "null -> true", pure = true)
	public static boolean isEmpty(@Nullable String string) {
		return string == null || string.isEmpty();
	}

	@Contract(value = "null, _ -> true; _, null -> true", pure = true)
	public static boolean nullOrEquals(@Nullable Object o1, @Nullable Object o2) {
		return o1 == null || o2 == null || o1.equals(o2);
	}

	@Contract(value = "null -> null; !null -> !null", pure = true)
	public static @Nullable String escapeHTML(@Nullable String rawHTML) {
		if (rawHTML == null)
			return null;
		return HTML_SPECIAL_CHARS.matcher(rawHTML).replaceAll(result -> HTML_ESCAPE_MAP.get(result.group().charAt(0)));
	}

	@Contract(value = " -> new", pure = true)
	public static @NotNull String caller() {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		for (StackTraceElement element : stack)
			if (!IGNORED_CALLS.contains(element.getMethodName()))
				return element.toString();
		return "Unknown Caller";
	}
}
