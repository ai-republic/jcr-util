/**
   Copyright 2015 Torsten Oltmanns, ai-republic GmbH, Germany

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.airepublic.jcr;

import java.util.Map;

/**
 * Configuration to identify a JCR repository and user credentials.
 * The repository can be configured as follows:
 * <ul>
 * <li>user => the username
 * <li>password => the password
 * <li>encrypted => true/false if the username and password are encrypted
 * (optional)
 * </ul>
 * 
 * For a local repository the configuration needs to contain the following
 * properties:
 * <ul>
 * <li>configFile => path to repository.xml
 * <li>homeDir => path to repository home
 * <li>repositoryName => name of the repository to access
 * </ul>
 * For a remote repository the configuration needs to contain the following
 * properties:
 * <ul>
 * <li>url => the url to the repository server
 * </ul>
 * 
 * To use a CND-file add the following property:<br>
 * <ul>
 * <li>cndFile => the path to the CND-file
 * </ul>
 * (c) Copyright 2014 by ai-republic GmbH, Germany<br>
 *  <br>
 * @author Torsten.Oltmanns@ai-republic.com
 */
public class JCRConfiguration {
	private String configFile;
	private String homeDir;
	private String repositoryName;
	private String user;
	private String password;
	private boolean encrypted;
	private String url;
	private String cndFile;

	/**
	 * Default constructor.
	 */
	public JCRConfiguration() {
	}
	
	/**
	 * Constructor.
	 * 
	 * @param configFile the path to the repository.xml file
	 * @param homeDir the repository home path
	 * @param repositoryName the name of the repository
	 * @param user the user to log in with
	 * @param password the corresponding password
	 * @param encrypted flag whether the password is encrypted
	 * @param cndFile the path to the CND configuration file
	 */
	public JCRConfiguration(String configFile, String homeDir, String repositoryName, String user, String password, boolean encrypted, String cndFile) {
		this.configFile = configFile;
		this.homeDir = homeDir;
		this.repositoryName = repositoryName;
		this.user = user;
		this.password = password;
		this.encrypted = encrypted;
		this.cndFile = cndFile;
	}
	
	/**
	 * Constructor.
	 * 
	 * @param url to the repository server
	 * @param user the user to log in with
	 * @param password the corresponding password
	 * @param encrypted flag whether the password is encrypted
	 * @param cndFile the path to the CND configuration file
	 */
	public JCRConfiguration(String url, String user, String password, boolean encrypted, String cndFile) {
		this.url = url;
		this.user = user;
		this.password = password;
		this.encrypted = encrypted;
		this.cndFile = cndFile;
	}
	
	/**
	 * Constructor.
	 * 
	 * @param config the map containing the following keys:
	 * <ul>
	 *  <li><code>url</code> -> the url to the remote repository server <br>
	 *  <b>or (for local repositories)</b>
	 *  <li><code>configFile</code> -> the path to the repository.xml file
	 *  <li><code>homeDir</code> -> the repository home path
	 *  <li><code>repositoryName</code> -> the name of the repository<br>
	 *  <b>and</b>
	 *  <li><code>user</code> -> the user to log in with
	 *  <li><code>password</code> -> the corresponding password
	 *  <li><code>encrypted</code> -> flag whether the password is encrypted
	 *  <li><code>cndFile</code> -> the path to an optional CND configuration file
	 * </ul>
	 */
	public JCRConfiguration(Map<String, String> config) {
		this.configFile = config.get("configFile");
		this.homeDir = config.get("homeDir");
		this.repositoryName = config.get("repositoryName");
		this.user = config.get("user");
		this.password = config.get("password");
		this.encrypted = Boolean.parseBoolean(config.get("encrypted"));
		this.url = config.get("url");
		this.cndFile = config.get("cndFile");
	}

	/**
	 * @return the configFile
	 */
	public String getConfigFile() {
		return configFile;
	}

	/**
	 * @param configFile the configFile to set
	 */
	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	/**
	 * @return the homeDir
	 */
	public String getHomeDir() {
		return homeDir;
	}

	/**
	 * @param homeDir the homeDir to set
	 */
	public void setHomeDir(String homeDir) {
		this.homeDir = homeDir;
	}

	/**
	 * @return the repositoryName
	 */
	public String getRepositoryName() {
		return repositoryName;
	}

	/**
	 * @param repositoryName the repositoryName to set
	 */
	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
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
	 * @return the encrypted
	 */
	public boolean isEncrypted() {
		return encrypted;
	}

	/**
	 * @param encrypted the encrypted to set
	 */
	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	
	/**
	 * @return the cndFile
	 */
	public String getCndFile() {
		return cndFile;
	}

	/**
	 * @param cndFile the cndFile to set
	 */
	public void setCndFile(String cndFile) {
		this.cndFile = cndFile;
	}
}
