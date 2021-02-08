package fr.theoszanto.webserver.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;

import com.sun.net.httpserver.Headers;

import fr.theoszanto.webserver.WebServer;

/**
 * The object representing the server response.
 * 
 * <p>This class is a singleton and should be initialized
 * before handling each exchange, and close after.
 * 
 * @author	indyteo
 * @see		HttpResponse#getInstance()
 * @see		HttpResponse#init()
 * @see		HttpResponse#close()
 */
public final class HttpResponse {
	/**
	 * The response that will be send to the client before
	 * ending handling with {@link HttpResponse#end()}.
	 */
	private String response;
	
	/**
	 * The status to be send if {@link HttpResponse#end()} is
	 * called without specifying one.
	 */
	private HttpStatus status;
	
	/**
	 * Check whether or not the method {@link HttpResponse#end()}
	 * have been called for this exchange.
	 * 
	 * <p>If this field is {@code false} when the Handle
	 * terminate, the {@link Handler super-handler} will send
	 * a default {@link HttpStatus#NOT_FOUND 404 Not Found}.
	 */
	private boolean statusSent;

	/**
	 * Check whether or not the {@link HttpResponse#init()}
	 * method have been called for this exchange.
	 * 
	 * <p>The {@link HttpResponse#close()} method reset this
	 * value to {@code false}.
	 * 
	 * @see		HttpResponse#init()
	 * @see		HttpResponse#close()
	 */
	private boolean isInit;
	
	/**
	 * The unique instance of this class.
	 * 
	 * @see		HttpResponse#getInstance()
	 */
	private static HttpResponse instance;
	
	/**
	 * Private constructor to prevent other instanciation.
	 */
	private HttpResponse() {
		this.init();
	}
	
	/**
	 * Return the unique instance of this singleton class.
	 * 
	 * <p>The first call to this method will create the instance.
	 * 
	 * @return	The instance of the class.
	 */
	public static HttpResponse getInstance() {
		if (instance == null)
			instance = new HttpResponse();
		
		return instance;
	}

	/**
	 * Launch the init process with the current HttpExchange
	 * static field of the {@link Handler super-handler}.
	 * 
	 * <p>This should normally be call once by exchange.
	 */
	void init() {
		if (this.isInit)
			return;
		
		this.response = "";
		this.status = null;
		this.statusSent = false;
		
		this.isInit = true;
	}
	
	/**
	 * Return the response headers, used to give informations
	 * about our response.
	 * 
	 * @return	The response {@link Headers headers}
	 */
	public Headers getHeaders() {
		return Handler.getExchange().getResponseHeaders();
	}
	
	/**
	 * End this exchange with a {@link HttpStatus#NOT_FOUND 404 Not Found}
	 * error code.
	 * 
	 * <p>This is a terminal operation.
	 * 
	 * @throws IOException
	 * 			If an I/O exception occurs, for example, if the
	 * 			response was already send.
	 */
	public void send404() throws IOException {
		this.end(HttpStatus.NOT_FOUND);
	}
	
	/**
	 * Add the response to a buffer until the method
	 * {@link HttpResponse#end()} is call.
	 * 
	 * @param response
	 * 			The character to be buffered.
	 * @return	Itself, to allow chained calls.
	 * @see		HttpResponse#send(String)
	 */
	public HttpResponse send(char response) {
		return this.send(Character.toString(response));
	}
	
	/**
	 * Add the response to a buffer until the method
	 * {@link HttpResponse#end()} is call.
	 * 
	 * @param response
	 * 			The String to be buffered.
	 * @return	Itself, to allow chained calls.
	 */
	public HttpResponse send(String response) {
		this.response += response;
		return this;
	}
	
	/**
	 * Define the HttpStatus to be send if not specified
	 * when calling {@link HttpResponse#end()}.
	 * 
	 * @param status
	 * 			The new HttpStatus.
	 * @return	Itself, to allow chained calls.
	 * @see		HttpResponse#getCurrentStatus()
	 */
	public HttpResponse setStatus(HttpStatus status) {
		this.status = status;
		return this;
	}
	
	/**
	 * Return the actual defined status.
	 * 
	 * @return	The status, if defined, {@code null} otherwise.
	 * @see		HttpResponse#setStatus(HttpStatus)
	 */
	public HttpStatus getCurrentStatus() {
		return this.status;
	}
	
	/**
	 * End the handling by sending the file located at path.
	 * 
	 * <p>A {@link HttpStatus#NOT_FOUND 404 Not Found} error
	 * code will be send if the file does not exists.
	 * 
	 * <p>A default {@link HttpStatus#OK 200 OK} status code
	 * is send.
	 * 
	 * <p>This is a terminal operation.
	 * 
	 * @param path
	 * 			The location of the file to send. It must be
	 * 			relative to the {@link WebServer#getRoot() Web server root}.
	 * @throws IOException
	 * 			If an I/O exception occurs, for example, if the
	 * 			response was already send.
	 * @see		HttpResponse#sendFile(File)
	 * @see		HttpResponse#sendFile(String, boolean)
	 */
	public void sendFile(String path) throws IOException {
		this.sendFile(path, false);
	}
	
	/**
	 * End the handling by sending the file located at path.
	 * 
	 * <p>A {@link HttpStatus#NOT_FOUND 404 Not Found} error
	 * code will be send if the file does not exists.
	 * 
	 * <p>If set, the {@link HttpResponse#status actual status}
	 * will be send, otherwise a default {@link HttpStatus#OK 200 OK}
	 * status code is send.
	 * 
	 * <p>If the download parameter is set to {@code true}, the
	 * file with be suggested as download for the client, instead
	 * of displaying it, if possible (such as text files).
	 * 
	 * <p>This is a terminal operation.
	 * 
	 * @param path
	 * 			The location of the file to send. It must be
	 * 			relative to the {@link WebServer#getRoot() Web server root}.
	 * @param download
	 * 			Whether or not the file should be downloaded by
	 * 			the client.
	 * @throws IOException
	 * 			If an I/O exception occurs, for example, if the
	 * 			response was already send.
	 * @see		HttpResponse#sendFile(File, boolean)
	 * @see		HttpResponse#sendFile(HttpStatus, String, boolean)
	 */
	public void sendFile(String path, boolean download) throws IOException {
		HttpStatus status = this.status;
		if (status == null)
			status = HttpStatus.OK;
		this.sendFile(status, path, download);
	}
	
	/**
	 * End the handling by sending the file located at path.
	 * 
	 * <p>A {@link HttpStatus#NOT_FOUND 404 Not Found} error
	 * code will be send if the file does not exists.
	 * 
	 * <p>The status specified will be send with the file.
	 * 
	 * <p>This is a terminal operation.
	 * 
	 * @param status
	 * 			The {@link HttpStatus} to be send with the file.
	 * @param path
	 * 			The location of the file to send. It must be
	 * 			relative to the {@link WebServer#getRoot() Web server root}.
	 * @throws IOException
	 * 			If an I/O exception occurs, for example, if the
	 * 			response was already send.
	 * @see		HttpResponse#sendFile(HttpStatus, File)
	 * @see		HttpResponse#sendFile(HttpStatus, String, boolean)
	 */
	public void sendFile(HttpStatus status, String path) throws IOException {
		this.sendFile(status, path, false);
	}
	
	/**
	 * End the handling by sending the file located at path.
	 * 
	 * <p>A {@link HttpStatus#NOT_FOUND 404 Not Found} error
	 * code will be send if the file does not exists.
	 * 
	 * <p>The status specified will be send with the file.
	 * 
	 * <p>If the download parameter is set to {@code true}, the
	 * file with be suggested as download for the client, instead
	 * of displaying it, if possible (such as text files).
	 * 
	 * <p>This is a terminal operation.
	 * 
	 * @param status
	 * 			The {@link HttpStatus} to be send with the file.
	 * @param path
	 * 			The location of the file to send. It must be
	 * 			relative to the {@link WebServer#getRoot() Web server root}.
	 * @param download
	 * 			Whether or not the file should be downloaded by
	 * 			the client.
	 * @throws IOException
	 * 			If an I/O exception occurs, for example, if the
	 * 			response was already send.
	 * @see		HttpResponse#sendFile(HttpStatus, File, boolean)
	 */
	public void sendFile(HttpStatus status, String path, boolean download) throws IOException {
		this.sendFile(status, new File(Paths.get(Handler.getServer().getRoot(), path).toString()), download);
	}
	
	/**
	 * End the handling by sending the specified file.
	 * 
	 * <p>A {@link HttpStatus#NOT_FOUND 404 Not Found} error
	 * code will be send if the file does not exists.
	 * 
	 * <p>A default {@link HttpStatus#OK 200 OK} status code
	 * is send.
	 * 
	 * <p>This is a terminal operation.
	 * 
	 * @param file
	 * 			The file to be send.
	 * @throws IOException
	 * 			If an I/O exception occurs, for example, if the
	 * 			response was already send.
	 * @see		HttpResponse#sendFile(String)
	 * @see		HttpResponse#sendFile(File, boolean)
	 */
	public void sendFile(File file) throws IOException {
		this.sendFile(file, false);
	}
	
	/**
	 * End the handling by sending the specified file.
	 * 
	 * <p>A {@link HttpStatus#NOT_FOUND 404 Not Found} error
	 * code will be send if the file does not exists.
	 * 
	 * <p>If set, the {@link HttpResponse#status actual status}
	 * will be send, otherwise a default {@link HttpStatus#OK 200 OK}
	 * status code is send.
	 * 
	 * <p>If the download parameter is set to {@code true}, the
	 * file with be suggested as download for the client, instead
	 * of displaying it, if possible (such as text files).
	 * 
	 * <p>This is a terminal operation.
	 * 
	 * @param file
	 * 			The file to be send.
	 * @param download
	 * 			Whether or not the file should be downloaded by
	 * 			the client.
	 * @throws IOException
	 * 			If an I/O exception occurs, for example, if the
	 * 			response was already send.
	 * @see		HttpResponse#sendFile(String, boolean)
	 * @see		HttpResponse#sendFile(HttpStatus, File, boolean)
	 */
	public void sendFile(File file, boolean download) throws IOException {
		HttpStatus status = this.status;
		if (status == null)
			status = HttpStatus.OK;
		this.sendFile(status, file, download);
	}
	
	/**
	 * End the handling by sending the specified file.
	 * 
	 * <p>A {@link HttpStatus#NOT_FOUND 404 Not Found} error
	 * code will be send if the file does not exists.
	 * 
	 * <p>The status specified will be send with the file.
	 * 
	 * <p>This is a terminal operation.
	 * 
	 * @param status
	 * 			The {@link HttpStatus} to be send with the file.
	 * @param file
	 * 			The file to be send.
	 * @throws IOException
	 * 			If an I/O exception occurs, for example, if the
	 * 			response was already send.
	 * @see		HttpResponse#sendFile(HttpStatus, String)
	 * @see		HttpResponse#sendFile(HttpStatus, File, boolean)
	 */
	public void sendFile(HttpStatus status, File file) throws IOException {
		this.sendFile(status, file, false);
	}
	
	/**
	 * End the handling by sending the specified file.
	 * 
	 * <p>A {@link HttpStatus#NOT_FOUND 404 Not Found} error
	 * code will be send if the file does not exists.
	 * 
	 * <p>The status specified will be send with the file.
	 * 
	 * <p>If the download parameter is set to {@code true}, the
	 * file with be suggested as download for the client, instead
	 * of displaying it, if possible (such as text files).
	 * 
	 * <p>This is a terminal operation.
	 * 
	 * @param status
	 * 			The {@link HttpStatus} to be send with the file.
	 * @param file
	 * 			The file to be send.
	 * @param download
	 * 			Whether or not the file should be downloaded by
	 * 			the client.
	 * @throws IOException
	 * 			If an I/O exception occurs, for example, if the
	 * 			response was already send.
	 * @see		HttpResponse#sendFile(HttpStatus, String, boolean)
	 */
	public void sendFile(HttpStatus status, File file, boolean download) throws IOException {
		String mime = HttpMIMEType.getMime(file);
		if (mime == null)
			mime = HttpMIMEType.DEFAULT.getMime();
		this.getHeaders().add("Content-Type", mime);
		
		if (download)
			this.getHeaders().add("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
		try {
			FileInputStream fs = new FileInputStream(file);
			int byteRead;
			Handler.getExchange().sendResponseHeaders(status.getCode(), 0);
			OutputStream responseBody = Handler.getExchange().getResponseBody();
			while ((byteRead = fs.read()) != -1)
				responseBody.write((char) byteRead);
			responseBody.flush();
			responseBody.close();
			fs.close();
			this.statusSent = true;
		}
		catch (FileNotFoundException e) {
			this.getHeaders().set("Content-Type", HttpMIMEType.HTML.getMime());
			this.send404();
		}
	}
	
	/**
	 * End the handling by redirecting the client to location.
	 * 
	 * <p>If set, the {@link HttpResponse#status actual status}
	 * will be send, otherwise a default {@link HttpStatus#FOUND 302 Found}
	 * status code is send.
	 * 
	 * <p>This is a terminal operation.
	 * 
	 * @param location
	 * 			The location where the client should be redirected.
	 * @throws IOException
	 * 			If an I/O exception occurs, for example, if the
	 * 			response was already send.
	 * @see		HttpResponse#redirect(String, HttpStatus)
	 */
	public void redirect(String location) throws IOException {
		HttpStatus status = this.status;
		if (status == null)
			status = HttpStatus.FOUND;
		this.redirect(location, status);
	}
	
	/**
	 * End the handling by redirecting the client to location.
	 * 
	 * <p>The status specified will be send.
	 * 
	 * <p>This is a terminal operation.
	 * 
	 * @param location
	 * 			The location where the client should be redirected.
	 * @param status
	 * 			The {@link HttpStatus} to be send.
	 * @throws IOException
	 * 			If an I/O exception occurs, for example, if the
	 * 			response was already send.
	 */
	public void redirect(String location, HttpStatus status) throws IOException {
		this.getHeaders().add("Location", location);
		this.end(status);
	}
	
	/**
	 * End this handling.
	 * 
	 * <p>If set, the {@link HttpResponse#status actual status}
	 * will be send, otherwise a default {@link HttpStatus#OK 200 OK}
	 * status code is send.
	 * 
	 * <p>If no response exists when this method is called,
	 * the String status will be send as response.
	 * 
	 * <p>This is a terminal operation.
	 * 
	 * @throws IOException
	 * 			If an I/O exception occurs, for example, if the
	 * 			response was already send.
	 * @see		HttpResponse#end(HttpStatus)
	 */
	public void end() throws IOException {
		HttpStatus status = this.status;
		if (status == null)
			status = HttpStatus.OK;
		this.end(status);
	}
	
	/**
	 * End this handling.
	 * 
	 * <p>The status specified will be send.
	 * 
	 * <p>If no response exists when this method is called,
	 * the String status will be send as response.
	 * 
	 * <p>This is a terminal operation.
	 * 
	 * @param status
	 * 			The {@link HttpStatus} to be send.
	 * @throws IOException
	 * 			If an I/O exception occurs, for example, if the
	 * 			response was already send.
	 * @see		HttpResponse#end(HttpStatus, String)
	 */
	public void end(HttpStatus status) throws IOException {
		String response = "";
		if (this.response.isEmpty())
			response = "<h1>" + status.getStatus() + "</h1>";
		this.end(status, response);
	}
	
	/**
	 * End this handling.
	 * 
	 * <p>If set, the {@link HttpResponse#status actual status}
	 * will be send, otherwise a default {@link HttpStatus#OK 200 OK}
	 * status code is send.
	 * 
	 * <p>The response will be buffered before ending the
	 * handling.
	 * 
	 * <p>This is a terminal operation.
	 * 
	 * @param response
	 * 			The response to add to the buffer before ending
	 * 			the handling.
	 * @throws IOException
	 * 			If an I/O exception occurs, for example, if the
	 * 			response was already send.
	 * @see		HttpResponse#end(HttpStatus, String)
	 */
	public void end(String response) throws IOException {
		HttpStatus status = this.status;
		if (status == null)
			status = HttpStatus.OK;
		this.end(status, response);
	}
	
	/**
	 * End this handling.
	 * 
	 * <p>The status specified will be send.
	 * 
	 * <p>The response will be buffered before ending the
	 * handling.
	 * 
	 * <p>This is a terminal operation.
	 * 
	 * @param status
	 * 			The {@link HttpStatus} to be send.
	 * @param response
	 * 			The response to add to the buffer before ending
	 * 			the handling.
	 * @throws IOException
	 * 			If an I/O exception occurs, for example, if the
	 * 			response was already send.
	 */
	public void end(HttpStatus status, String response) throws IOException {
		this.send(response);
		byte[] responseByte = this.response.getBytes();
		Handler.getExchange().sendResponseHeaders(status.getCode(), responseByte.length);
		OutputStream responseBody = Handler.getExchange().getResponseBody();
		responseBody.write(responseByte);
		responseBody.flush();
		responseBody.close();
		this.statusSent = true;
	}
	
	/**
	 * End this handling without sending any response body.
	 * 
	 * <p>The status specified will be send.
	 * 
	 * <p>This is a terminal operation.
	 * 
	 * @param status
	 * 			The {@link HttpStatus} to be send.
	 * @throws IOException
	 * 			If an I/O exception occurs, for example, if the
	 * 			response was already send.
	 * @see		HttpResponse#endWithoutBody()
	 */
	public void endWithoutBody(HttpStatus status) throws IOException {
		this.setStatus(status).endWithoutBody();
	}
	
	/**
	 * End this handling without sending any response body.
	 * 
	 * <p>If set, the {@link HttpResponse#status actual status}
	 * will be send, otherwise a default {@link HttpStatus#OK 200 OK}
	 * status code is send.
	 * 
	 * <p>This is a terminal operation.
	 * 
	 * @throws IOException
	 * 			If an I/O exception occurs, for example, if the
	 * 			response was already send.
	 */
	public void endWithoutBody() throws IOException {
		HttpStatus status = this.getCurrentStatus();
		if (status == null)
			status = HttpStatus.OK;
		Handler.getExchange().sendResponseHeaders(this.status.getCode(), -1);
	}
	
	/**
	 * Close this response
	 * 
	 * <p>This method should be called at the end of an exchange.
	 */
	void close() {
		this.isInit = false;
	}
	
	/**
	 * Check whether or not a response have been sent.
	 * 
	 * @return	{@code true} if a response have been sent,
	 * 			{@code false} otherwise.
	 */
	boolean isStatusSent() {
		return this.statusSent;
	}
}