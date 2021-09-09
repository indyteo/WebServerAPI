package fr.theoszanto.webserver.api;

import fr.theoszanto.webserver.utils.Checks;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Paths;

/**
 * Represent a file as a response to client.
 *
 * @see		HttpResponse#sendFile(FileResponse)
 * @see		Builder
 */
public class FileResponse {
	/**
	 * The file to send to the client.
	 */
	private final @NotNull File file;
	/**
	 * Whether or not the file should be downloaded by
	 * the client.
	 */
	private final boolean download;
	/**
	 * Whether to allow unsafe (unknown) file extensions
	 * to be sent to the client or not.
	 */
	private final boolean unsafe;
	/**
	 * The MIME type to force. Otherwise, MIME is
	 * {@link HttpMIMEType#fromExtension(File) retrieved from the extension}.
	 */
	private final @Nullable HttpMIMEType type;

	/**
	 * Create a new file response object.
	 *
	 * @param file
	 * 			The file to send to the client.
	 * @param download
	 * 			Whether or not the file should be downloaded by
	 * 			the client.
	 * @param unsafe
	 * 			Whether to allow unsafe (unknown) file extensions
	 * 			to be sent to the client or not.
	 * @param type
	 * 			The MIME type to force. Otherwise, MIME is
	 * 			{@link HttpMIMEType#fromExtension(File) retrieved from the extension}.
     * @see		Builder
	 */
	public FileResponse(@NotNull File file, boolean download, boolean unsafe, @Nullable HttpMIMEType type) {
		Checks.notNull(file, "file");
		this.file = file;
		this.download = download;
		this.unsafe = unsafe;
		this.type = type;
	}

	public @NotNull File getFile() {
		return this.file;
	}

	public boolean isDownload() {
		return this.download;
	}

	public boolean isUnsafe() {
		return this.unsafe;
	}

	public @Nullable HttpMIMEType getType() {
		return this.type;
	}

	/**
	 * Builder to create {@link FileResponse file response}
	 */
	public static class Builder {
		/**
		 * Response for which the file response will be created.
		 */
		private final @NotNull HttpResponse response;

		/**
		 * The file to send to the client.
		 */
		private @Nullable File file = null;
		/**
		 * Whether or not the file should be downloaded by
		 * the client.
		 */
		private boolean download = false;
		/**
		 * Whether to allow unsafe (unknown) file extensions
		 * to be sent to the client or not.
		 */
		private boolean unsafe = false;
		/**
		 * The MIME type to force. Otherwise, MIME is
		 * {@link HttpMIMEType#fromExtension(File) retrieved from the extension}.
		 */
		private @Nullable HttpMIMEType type = null;

		/**
		 * Create a new builder bound to the given response.
		 *
		 * @param response
		 * 			Response for which the file response
		 * 			will be created.
		 */
		public Builder(@NotNull HttpResponse response) {
			this.response = response;
		}

		/**
		 * Create a new file response using the builder's
		 * current properties.
		 *
		 * @return	A new file response from the current state.
		 */
		@Contract(value = " -> new", pure = true)
		public @NotNull FileResponse build() {
			Checks.notNull(this.file, "file");
			return new FileResponse(this.file, this.download, this.unsafe, this.type);
		}

		/**
		 * Set the file to send to the client.
		 *
		 * @param file
		 * 			The file to send to the client.
		 * @return	Itself, to allow chained calls.
		 */
		@Contract(value = "_ -> this", mutates = "this")
		public @NotNull Builder setFile(@NotNull File file) {
			Checks.notNull(file, "file");
			this.file = file;
			return this;
		}

		/**
		 * Set the file to send to the client from the given
		 * path.
		 *
		 * @param path
		 * 			The path of the file to send to the client.
		 * @return	Itself, to allow chained calls.
		 */
		@Contract(value = "_ -> this", mutates = "this")
		public @NotNull Builder setFile(@NotNull String path) {
			Checks.notNull(path, "path");
			this.file = Paths.get(this.response.getServer().getRoot(), path).toFile();
			return this;
		}

		/**
		 * Set whether or not the file should be downloaded
		 * by the client.
		 *
		 * @param download
		 * 			Whether or not the file should be downloaded by
		 * 			the client.
		 * @return	Itself, to allow chained calls.
		 */
		@Contract(value = "_ -> this", mutates = "this")
		public @NotNull Builder setDownload(boolean download) {
			this.download = download;
			return this;
		}

		/**
		 * Set whether to allow unsafe (unknown) file extensions
		 * to be sent to the client or not.
		 *
		 * @param unsafe
		 * 			Whether to allow unsafe (unknown) file extensions
		 * 			to be sent to the client or not.
		 * @return	Itself, to allow chained calls.
		 */
		@Contract(value = "_ -> this", mutates = "this")
		public @NotNull Builder setUnsafe(boolean unsafe) {
			this.unsafe = unsafe;
			return this;
		}

		/**
		 * Set the MIME type to force. If not set, the MIME is
		 * {@link HttpMIMEType#fromExtension(File) retrieved from the extension}.
		 *
		 * @param type
		 * 			The MIME type to force.
		 * @return	Itself, to allow chained calls.
		 */
		@Contract(value = "_ -> this", mutates = "this")
		public @NotNull Builder setType(@Nullable HttpMIMEType type) {
			this.type = type;
			return this;
		}
	}
}
