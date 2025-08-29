/**
 * 
 */
package net.sf.recipetools.javarecipes.model;

/**
 * @author Frank
 *
 */
public class MailAccount {
	String	fullname;
	String	email;
	String	smtpServer;
	int 	port;
	String	username;
	String	password;
	boolean enableTLS;

	/**
	 * @return the fullname
	 */
	public String getFullname() {
		return fullname;
	}
	/**
	 * @param fullname the fullname to set
	 */
	public void setFullname(String fullname) {
		this.fullname = fullname;
	}
	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	/**
	 * @return the smtpServer
	 */
	public String getSmtpServer() {
		return smtpServer;
	}
	/**
	 * @param smtpServer the smtpServer to set
	 */
	public void setSmtpServer(String smtpServer) {
		this.smtpServer = smtpServer;
	}
	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}
	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	/**
	 * @return the enableTLS
	 */
	public boolean isEnableTLS() {
		return enableTLS;
	}
	/**
	 * @param enableTLS the enableTLS to set
	 */
	public void setEnableTLS(boolean enableTLS) {
		this.enableTLS = enableTLS;
	}
	
	
}
