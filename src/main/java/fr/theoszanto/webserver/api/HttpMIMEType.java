package fr.theoszanto.webserver.api;

import fr.theoszanto.webserver.utils.Checks;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * List of common MIME types.
 * 
 * <p>You can add your MIME type with the
 * {@link HttpMIMEType#addMIME(String, String...)}
 * method, and bypass default.</p>
 * 
 * <p>To disable a default MIME type, remove it using
 * {@link HttpMIMEType#removeMIME(String)}.</p>
 * 
 * @author	indyteo
 */
public class HttpMIMEType {
	/**
	 * The list of all existing MIME types.
	 */
	private static final @NotNull Set<HttpMIMEType> registry = new HashSet<>();

	/**
	 * Any kind of binary data.
	 */
	public static final HttpMIMEType BIN = new HttpMIMEType("application/octet-stream", "bin", "");
	/**
	 * Cascading Style Sheets (CSS)
	 */
	public static final HttpMIMEType CSS = new HttpMIMEType("text/css", "css");
	/**
	 * Comma-separated values (CSV)
	 */
	public static final HttpMIMEType CSV = new HttpMIMEType("text/csv", "csv");
	/**
	 * MS Embedded OpenType fonts
	 */
	public static final HttpMIMEType EOT = new HttpMIMEType("application/vnd.ms-fontobject", "eot");
	/**
	 * Graphics Interchange Format (GIF)
	 */
	public static final HttpMIMEType GIF = new HttpMIMEType("image/gif", "gif");
	/**
	 * HyperText Markup Language (HTML)
	 */
	public static final HttpMIMEType HTML = new HttpMIMEType("text/html", "html", "htm");
	/**
	 * Icon format
	 */
	public static final HttpMIMEType ICO = new HttpMIMEType("image/x-icon", "ico");
	/**
	 * Java Archive (JAR)
	 */
	public static final HttpMIMEType JAR = new HttpMIMEType("application/java-archive", "jar");
	/**
	 * JPEG images
	 */
	public static final HttpMIMEType JPEG = new HttpMIMEType("image/jpeg", "jpeg", "jpg");
	/**
	 * JavaScript (module)
	 */
	public static final HttpMIMEType JS = new HttpMIMEType("application/javascript", "js", "mjs");
	/**
	 * JSON format
	 */
	public static final HttpMIMEType JSON = new HttpMIMEType("application/json", "json");
	/**
	 * MP3 audio
	 */
	public static final HttpMIMEType MP3 = new HttpMIMEType("audio/mpeg", "mp3");
	/**
	 * MPEG Video
	 */
	public static final HttpMIMEType MPEG = new HttpMIMEType("video/mpeg", "mpeg");
	/**
	 * OGG audio
	 */
	public static final HttpMIMEType OGA = new HttpMIMEType("audio/ogg", "oga");
	/**
	 * OGG video
	 */
	public static final HttpMIMEType OGV = new HttpMIMEType("video/ogg", "ogv");
	/**
	 * OGG
	 */
	public static final HttpMIMEType OGG = new HttpMIMEType("application/ogg", "ogg", "ogx");
	/**
	 * Opus audio
	 */
	public static final HttpMIMEType OPUS = new HttpMIMEType("audio/opus", "opus");
	/**
	 * OpenType font
	 */
	public static final HttpMIMEType OTF = new HttpMIMEType("font/otf", "otf");
	/**
	 * Portable Network Graphics
	 */
	public static final HttpMIMEType PNG = new HttpMIMEType("image/png", "png");
	/**
	 * Adobe Portable Document Format (PDF)
	 */
	public static final HttpMIMEType PDF = new HttpMIMEType("application/pdf", "pdf");
	/**
	 * Hypertext Preprocessor (Personal Home Page)
	 */
	public static final HttpMIMEType PHP = new HttpMIMEType("application/x-httpd-php", "php");
	/**
	 * RAR archive
	 */
	public static final HttpMIMEType RAR = new HttpMIMEType("application/x-rar-compressed", "rar");
	/**
	 * Rich Text Format (RTF)
	 */
	public static final HttpMIMEType RTF = new HttpMIMEType("application/rtf", "rtf");
	/**
	 * Scalable Vector Graphics (SVG)
	 */
	public static final HttpMIMEType SVG = new HttpMIMEType("image/svg+xml", "svg");
	/**
	 * Tape Archive (TAR)
	 */
	public static final HttpMIMEType TAR = new HttpMIMEType("application/x-tar", "tar");
	/**
	 * TypeScript
	 */
	public static final HttpMIMEType TS = new HttpMIMEType("application/typescript", "ts");
	/**
	 * TrueType Font
	 */
	public static final HttpMIMEType TTF = new HttpMIMEType("font/ttf", "ttf");
	/**
	 * Text
	 */
	public static final HttpMIMEType TXT = new HttpMIMEType("text/plain", "txt");
	/**
	 * Waveform Audio Format
	 */
	public static final HttpMIMEType WAV = new HttpMIMEType("audio/wav", "wav");
	/**
	 * WEBM audio
	 */
	public static final HttpMIMEType WEBA = new HttpMIMEType("audio/webm", "weba");
	/**
	 * WEBM video
	 */
	public static final HttpMIMEType WEBM = new HttpMIMEType("video/webm", "webm");
	/**
	 * WEBP image
	 */
	public static final HttpMIMEType WEBP = new HttpMIMEType("image/webp", "webp");
	/**
	 * Web Open Font Format (WOFF)
	 */
	public static final HttpMIMEType WOFF = new HttpMIMEType("font/woff", "woff");
	/**
	 * Web Open Font Format (WOFF)
	 */
	public static final HttpMIMEType WOFF2 = new HttpMIMEType("font/woff2", "woff2");
	/**
	 * XML
	 */
	public static final HttpMIMEType XML = new HttpMIMEType("application/xml", "xml");
	/**
	 * ZIP archive
	 */
	public static final HttpMIMEType ZIP = new HttpMIMEType("application/zipp", "zip");

	/**
	 * Default MIME type to BIN format (download needed).
	 *
	 * <p>{@code application/octet-stream} is the default
	 * value for all other cases. An unknown file type should
	 * use this type. Browsers pay a particular care when
	 * manipulating these files, attempting to safeguard
	 * the user to prevent dangerous behaviors.</p>
	 */
	public static final HttpMIMEType DEFAULT = BIN;

	/**
	 * The extension to associate the MIME string with.
	 */
	private final @NotNull Set<String> extentions = new HashSet<>();

	/**
	 * The MIME string associated with the extension.
	 */
	private final @NotNull String mime;

	/**
	 * Private constructor to create a new MIME type.
	 *
	 * @param mime
	 * 			The MIME string for this MIME type.
	 */
	private HttpMIMEType(@NotNull String mime, @NotNull String... extentions) {
		Checks.notEmpty(mime, "mime");
		Checks.notEmpty(extentions, "extentions");
		this.mime = mime;
		this.extentions.addAll(Arrays.asList(extentions));
		registry.add(this);
	}

	/**
	 * Return the extensions of this MIME type.
	 * 
	 * @return	The extention that match this MIME type.
	 */
	@Contract(pure = true)
	public @NotNull Set<String> getExtentions() {
		return this.extentions;
	}

	/**
	 * Return the string MIME of this MIME type.
	 *
	 * @return	The string MIME that should be sent in the
	 * 			{@code Content-type} header field.
	 */
	@Contract(pure = true)
	public @NotNull String getMime() {
		return this.mime;
	}

	/**
	 * Return the MIME corresponding to the extension {@code extention}.
	 * 
	 * @param extention
	 * 			The extension of the file to get the MIME.
	 * @return	The MIME type corresponding to the extension.
	 */
	@Contract(pure = true)
	public static @Nullable HttpMIMEType fromExtension(@Nullable String extention) {
		// Check if extentions is default
		if (extention == null || extention.isEmpty())
			return DEFAULT;

		// And the native MIME types
		for (HttpMIMEType type : registry)
			if (type.getExtentions().contains(extention))
				return type;

		// Finally
		return null;
	}

	/**
	 * Return the MIME corresponding to the file {@code f}.
	 * 
	 * @param f
	 * 			The file to get the MIME.
	 * @return	The MIME type corresponding to the file.
	 */
	@Contract(value = "null -> null", pure = true)
	public static @Nullable HttpMIMEType fromExtension(@Nullable File f) {
		if (f == null || !f.exists())
			return null;
		String fn = f.getName();
		int lastDotIndex = fn.lastIndexOf('.');
		if (lastDotIndex != -1)
			return fromExtension(fn.substring(lastDotIndex + 1));
		return DEFAULT;
	}

	/**
	 * Return the MIME corresponding to the string MIME {@code mime}.
	 *
	 * @param mime
	 * 			The string MIME to get the MIME type.
	 * @return	The MIME type corresponding to the string MIME.
	 */
	@Contract(pure = true)
	public static @Nullable HttpMIMEType fromMIME(@NotNull String mime) {
		for (HttpMIMEType type : registry)
			if (mime.equals(type.mime))
				return type;
		return null;
	}

	/**
	 * Add a MIME or bypass a default.
	 *
	 * @param mime
	 * 			The string MIME, usable in {@code Content-Type}
	 * 			header.
	 * @param extentions
	 * 			The extensions to MIME is corresponding to.
	 * @return	The added, or modified if already present, string MIME.
	 */
	public static @NotNull HttpMIMEType addMIME(@NotNull String mime, @NotNull String... extentions) {
		Checks.notEmpty(mime, "mime");
		Checks.notEmpty(extentions, "extentions");
		HttpMIMEType mimeType = fromMIME(mime);
		if (mimeType == null)
			mimeType = new HttpMIMEType(mime, extentions);
		else
			mimeType.extentions.addAll(Arrays.asList(extentions));
		return mimeType;
	}

	/**
	 * Remove a MIME from the registry.
	 *
	 * @param mime
	 * 			The string MIME to remove.
	 * @return	The removed string MIME, or {@code null} if absent.
	 */
	public static @Nullable HttpMIMEType removeMIME(@NotNull String mime) {
		Checks.notEmpty(mime, "mime");
		HttpMIMEType mimeType = fromMIME(mime);
		if (mimeType == null)
			return null;
		registry.remove(mimeType);
		return mimeType;
	}

	@Override
	@Contract(pure = true)
	public int hashCode() {
		return Objects.hash(mime);
	}

	@Override
	@Contract(pure = true)
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		HttpMIMEType that = (HttpMIMEType) o;
		return mime.equals(that.mime);
	}

	/**
	 * Return the extensions and the corresponding MIME string.
	 *
	 * <p>For example, {@link #HTML}{@code .toString()}
	 * produce the following:</p>
	 * <blockquote><pre>
	 * [html, htm]: text/html
	 * </pre></blockquote>
	 * 
	 * @return	The extensions and the string MIME.
	 */
	@Override
	@Contract(value = " -> new", pure = true)
	public @NotNull String toString() {
		return this.extentions + ": " + this.mime;
	}
}
