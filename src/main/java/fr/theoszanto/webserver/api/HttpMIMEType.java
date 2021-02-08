package fr.theoszanto.webserver.api;

import java.io.File;
import java.util.HashMap;

/**
 * List of common MIME types.
 * 
 * <p>You can add your MIME type with the {@link HttpMIMEType#addMIME(String, String)}
 * method, and bypass default.
 * 
 * <p>To disable a default MIME type, just override it with {@code null}.
 * 
 * @author	indyteo
 */
public enum HttpMIMEType {
	DEFAULT("application/octet-stream"),
	TXT("text/plain"),
	BIN("application/octet-stream"),
	CSS("text/css"),
	CSV("text/csv"),
	EOT("application/vnd.ms-fontobject"),
	GIF("image/gif"),
	HTM("text/html"),
	HTML("text/html"),
	ICO("image/x-icon"),
	JPG("image/jpeg"),
	JPEG("image/jpeg"),
	JS("application/javascript"),
	JSON("application/json"),
	OGG("audio/ogg"),
	OTF("font/otf"),
	PNG("image/png"),
	PDF("application/pdf"),
	RAR("application/x-rar-compressed"),
	RTF("application/rtf"),
	SVG("image/svg+xml"),
	TAR("application/x-tar"),
	TS("application/typescript"),
	TTF("font/ttf"),
	WOFF("font/woff"),
	WOFF2("font/woff2"),
	XML("application/xml"),
	ZIP("application/zipp");
	
	/**
	 * The MIME string associated with the extension.
	 */
	private String mime;
	
	/**
	 * Additional MIME defined by the user.
	 */
	private static HashMap<String, String> additionalMIME = new HashMap<String, String>();
	
	/**
	 * Private constructor.
	 * 
	 * @param mime
	 * 			The MIME string for this MIME type.
	 */
	private HttpMIMEType(String mime) {
		this.mime = mime;
	}
	
	/**
	 * Return the file extension this MIME is corresponding to.
	 * 
	 * @return	The file extension, without the dot.
	 */
	public String getExt() {
		if (this == DEFAULT)
			return null;
		return this.name().toLowerCase();
	}
	
	/**
	 * Return the String MIME of this MIME type.
	 * 
	 * @return	The string MIME that should be sent in the
	 * 			{@code Content-Type} header field.
	 */
	public String getMime() {
		return this.mime;
	}
	
	/**
	 * Return the MIME corresponding to the extension {@code ext}.
	 * 
	 * @param ext
	 * 			The extension of the file to get the MIME.
	 * @return	The string MIME corresponding to the extension.
	 */
	public static String getMime(String ext) {
		// Check if ext is default
		if (ext == null || ext.isEmpty())
			return DEFAULT.getMime();
		
		// Then the user defined MIME types first
		if (additionalMIME.containsKey(ext))
			return additionalMIME.get(ext);
		
		// And the native MIME types
		for (HttpMIMEType v : values())
			if (ext.equalsIgnoreCase(v.getExt()))
				return v.getMime();
		
		// Finally
		return null;
	}
	
	/**
	 * Return the MIME corresponding to the file {@code f}.
	 * 
	 * @param f
	 * 			The file to get the MIME.
	 * @return	The string MIME corresponding to the file.
	 */
	public static String getMime(File f) {
		String fn = f.getName();
		int lastDotIndex = fn.lastIndexOf('.');
		if (lastDotIndex != -1)
			return getMime(fn.substring(lastDotIndex + 1));
		return DEFAULT.getMime();
	}
	
	/**
	 * Add a MIME or bypass a default.
	 * 
	 * @param ext
	 * 			The extension to MIME is corresponding to.
	 * @param mime
	 * 			The string MIME, usable in {@code Content-Type}
	 * 			header.
	 */
	public static void addMIME(String ext, String mime) {
		additionalMIME.put(ext, mime);
	}
	
	/**
	 * Return the extension, prefixed by a dot.
	 * 
	 * @return	The extension, as following: {@code .extension}.
	 */
	@Override
	public String toString() {
		return "." + this.getExt();
	}
}