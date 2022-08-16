package fr.theoszanto.webserver.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiscUtils {
	public static final @NotNull DateFormat HTTP_DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH);
	public static final @NotNull Pattern HTML_SPECIAL_CHARS = Pattern.compile("[&<>\"']");
	private static final @NotNull Map<Character, String> HTML_ESCAPE_MAP = new HashMap<Character, String>() {{
		put('&', "&amp;");
		put('<', "&lt;");
		put('>', "&gt;");
		put('"', "&quot;");
		put('\'', "&#039;");
	}};
	private static final @NotNull List<String> IGNORED_CALLS = Arrays.asList("logDebugInfo", "getStackTrace", "caller");
	private static final char[] ALLOWED_RANDOM_CHARS = {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_', '+', '$', '.',
			'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
			'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
	};

	static {
		HTTP_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	private MiscUtils() {}

	@Contract(value = "!null, _ -> param1; null, _ -> param2", pure = true)
	public static <T> @Nullable T ifNull(@Nullable T value, @Nullable T nullDefault) {
		return value == null ? nullDefault : value;
	}

	@Contract(value = "!null, _ -> param1", pure = true)
	public static <T> @Nullable T ifNullGet(@Nullable T value, @NotNull Supplier<T> nullDefault) {
		return value == null ? nullDefault.get() : value;
	}

	@Contract(value = "null, _ -> null", pure = true)
	public static <T, U> @Nullable U ifNotNull(@Nullable T value, @NotNull Function<T, U> function) {
		return ifNotNull(value, function, null);
	}

	@Contract(value = "null, _, _ -> param3", pure = true)
	public static <T, U> @Nullable U ifNotNull(@Nullable T value, @NotNull Function<T, U> function, @Nullable U nullDefault) {
		return value == null ? nullDefault : function.apply(value);
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

	@Contract(value = "null -> false", pure = true)
	public static boolean nonEmpty(@Nullable String string) {
		return !isEmpty(string);
	}

	@Contract(value = "null, _ -> true; _, null -> true", pure = true)
	public static boolean nullOrEquals(@Nullable Object o1, @Nullable Object o2) {
		return o1 == null || o2 == null || o1.equals(o2);
	}

	@Contract(value = "null -> null; !null -> !null", pure = true)
	public static @Nullable String escapeHTML(@Nullable String rawHTML) {
		if (rawHTML == null)
			return null;

		Matcher charsMatcher = HTML_SPECIAL_CHARS.matcher(rawHTML);
		StringBuffer buffer = new StringBuffer();
		while (charsMatcher.find()) {
			String replace = HTML_ESCAPE_MAP.get(charsMatcher.group().charAt(0));
			charsMatcher.appendReplacement(buffer, replace);
		}
		charsMatcher.appendTail(buffer);

		return buffer.toString();
	}

	@Contract(value = " -> new", pure = true)
	public static @NotNull String caller() {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		for (StackTraceElement element : stack)
			if (!IGNORED_CALLS.contains(element.getMethodName()))
				return element.toString();
		return "Unknown Caller";
	}

	@Contract(value = "_ -> new", pure = true)
	public static @NotNull String randomString(@Range(from = 0, to = Integer.MAX_VALUE) int length) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < length; i++)
			builder.append(randomChar());
		return builder.toString();
	}

	public static char randomChar() {
		return ALLOWED_RANDOM_CHARS[(int) Math.floor(Math.random() * ALLOWED_RANDOM_CHARS.length)];
	}
}
