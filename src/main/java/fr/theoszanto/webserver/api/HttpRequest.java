package fr.theoszanto.webserver.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * The object representing the client request.
 * 
 * <p>This class is a singleton and should be initialized
 * before handling each exchange, and close after.
 * 
 * @author	indyteo
 * @see		HttpRequest#getInstance()
 * @see		HttpRequest#init()
 * @see		HttpRequest#close()
 */
public final class HttpRequest {
	/**
	 * The parameters of the request, either send in through
	 * the request body, or in the request URL, according to
	 * the request method.
	 * 
	 * <p>You can get the params with the key with the method
	 * {@link HttpRequest#getParam(String)}. You can check if
	 * a key is present with {@link HttpRequest#isParamKey(String)}
	 * and get the key list with {@link HttpRequest#getParamKeys()}.
	 * 
	 * <p>Alternatively, you can execute a code for each param
	 * with {@link HttpRequest#forEachParam(BiConsumer)}.
	 * 
	 * @see		HttpMethod
	 * @see		HttpRequest#getParam(String)
	 * @see		HttpRequest#isParamKey(String)
	 * @see		HttpRequest#getParamKeys()
	 * @see		HttpRequest#forEachParam(BiConsumer)
	 */
	private HashMap<String, String> params;
	
	/**
	 * Check whether or not the {@link HttpRequest#init()}
	 * method have been called for this exchange.
	 * 
	 * <p>The {@link HttpRequest#close()} method reset this
	 * value to {@code false}.
	 * 
	 * @see		HttpRequest#init()
	 * @see		HttpRequest#close()
	 */
	private boolean isInit;
	
	/**
	 * The unique instance of this class.
	 * 
	 * @see		HttpRequest#getInstance()
	 */
	private static HttpRequest instance = null;
	
	/**
	 * Private constructor to prevent other instanciation.
	 */
	private HttpRequest() {
		this.params = new HashMap<String, String>();
		this.isInit = false;
	}
	
	/**
	 * Return the unique instance of this singleton class.
	 * 
	 * <p>The first call to this method will create the instance.
	 * 
	 * @return	The instance of the class.
	 */
	public static HttpRequest getInstance() {
		if (instance == null)
			instance = new HttpRequest();
		return instance;
	}
	
	/**
	 * Launch the init process with the current HttpExchange
	 * static field of the {@link Handler super-handler}.
	 * 
	 * <p>This should normally be call once by exchange.
	 * 
	 * @throws IOException
	 * 			If an I/O exception occurs during the body-reading
	 * 			process.
	 */
	void init() throws IOException {
		if (this.isInit)
			return;
		
		InputStream is = Handler.getExchange().getRequestBody();
		String params = "";
		int byteRead;
		while ((byteRead = is.read()) != -1)
			params += (char) byteRead;
		if (params.isEmpty())
			params = Handler.getExchange().getRequestURI().getQuery();
		this.parseParams(params);
		
		this.isInit = true;
	}
	
	/**
	 * The method parse the String {@code params} which contains
	 * HTTP request parameters, formatted as the standard.
	 * 
	 * <p>This input:
	 * <blockquote><pre>
	 * key1=value1&amp;key2=value+with+space+2
	 * </pre></blockquote>
	 * should produce the following:
	 * <blockquote><pre>
	 * params.put("key1", "value1");
	 * params.put("key2", "value with space 2");
	 * </pre></blockquote>
	 * 
	 * <p>Any malformed input part will be ignored.
	 * 
	 * @param params
	 * 			The String representation of the params
	 */
	private void parseParams(String params) {
		if (params == null)
			return;
		String[] pairs = params.replace('+', ' ').split("&");
		for (String pair : pairs) {
			String[] val = pair.split("=");
			try {
				this.params.put(val[0], val[1]);
			}
			catch (ArrayIndexOutOfBoundsException e) {}
		}
	}
	
	/**
	 * Return the parameter associated to this key.
	 * 
	 * @param key
	 * 			The key of the param to retrieve.
	 * @return	The request param value for this key.
	 * @see		HttpRequest#params
	 */
	public String getParam(String key) {
		return this.params.get(key);
	}
	
	/**
	 * Return {@code true} if a param value is associated
	 * to this key.
	 * 
	 * @param key
	 * 			The key that will be check
	 * @return	{@code true} if a param value is associated
	 * 			to the key, {@code false} otherwise.
	 */
	public boolean isParamKey(String key) {
		return this.params.containsKey(key);
	}
	
	/**
	 * Return a {@link Set} of String containing the param keys.
	 * 
	 * @return	A Set with all keys that have param associated with.
	 */
	public Set<String> getParamKeys() {
		return this.params.keySet();
	}
	
	/**
	 * Execute the given {@code action} for each parameters.
	 * 
	 * <p>The action will be called with the key and the value.
	 * 
	 * @param action
	 * 			The {@link BiConsumer} to execute.
	 */
	public void forEachParam(BiConsumer<String, String> action) {
		this.params.forEach(action);
	}
	
	/**
	 * Return the requested {@link URI}.
	 * 
	 * @return	The URI of the request.
	 */
	public URI getURI() {
		return Handler.getExchange().getRequestURI();
	}
	
	/**
	 * Return the {@link File} object corresponding to the
	 * requested URI and the server root.
	 * 
	 * @return	The File requested by the client, that might
	 * 			not exist.
	 */
	public File getRequestedFile() {
		return Paths.get(Handler.getServer().getRoot(), this.getURI().toString()).toFile();
	}
	
	/**
	 * Return the method used in the request.
	 * 
	 * @return	The {@link HttpMethod} representing the method
	 * 			used by the client in the request.
	 */
	public HttpMethod getMethod() {
		return HttpMethod.parse(Handler.getExchange().getRequestMethod());
	}
	
	/**
	 * Return the first header value of the field received in
	 * parameter of this method.
	 * 
	 * <p>The HTTP headers may have several values for a single
	 * header field.
	 * 
	 * @param header
	 * 			The field name (case insensitive) of the value to
	 * 			retrieve.
	 * @return	The header value corresponding to the field name.
	 */
	public String getHeader(String header) {
		return Handler.getExchange().getRequestHeaders().getFirst(header);
	}
	
	/**
	 * Return the {@link List} of values associated to the field received
	 * in parameter of this method.
	 * 
	 * @param header
	 * 			The field name (case insensitive) of the values to
	 * 			retrieve.
	 * @return	A List of values corresponding to the field name.
	 */
	public List<String> getHeaders(String header) {
		return Handler.getExchange().getRequestHeaders().get(header);
	}
	
	/**
	 * Execute the given {@code action} for each header fields.
	 * 
	 * <p>The action will be called with the field and the values.
	 * 
	 * @param action
	 * 			The {@link BiConsumer} to execute.
	 */
	public void forEachHeaders(BiConsumer<String, List<String>> action) {
		Handler.getExchange().getRequestHeaders().forEach(action);
	}
	
	/**
	 * Close this request.
	 * 
	 * <p>This method should be called at the end of an exchange.
	 */
	void close() {
		this.params.clear();
		this.isInit = false;
	}
}