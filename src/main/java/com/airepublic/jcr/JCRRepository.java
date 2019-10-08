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

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.jcr.Credentials;
import javax.jcr.GuestCredentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.nodetype.NodeType;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.commons.cnd.CndImporter;
import org.apache.jackrabbit.core.security.authentication.CryptedSimpleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to access a JCR repository. The repository can be a local or remove
 * repository.<br>
 * If no credentials are configured then GuestCredential are used to try to
 * access the repository.<br>
 * <br>
 * (c) Copyright 2014 by ai-republic GmbH, Germany
 *	 
 * @author Torsten.Oltmanns@ai-republic.com
 * 
 */
public class JCRRepository {
	private static final Logger LOG = LoggerFactory.getLogger(JCRRepository.class);
	@Inject
	private JCRConfiguration configuration;
	private Repository repository = null;
	private Session session;
	private Credentials credentials = new GuestCredentials();

	/**
	 * Constructor.
	 */
	public JCRRepository() {
	}
	
	/**
	 * Constructor.
	 * 
	 * @param configuration the configuration to create the repository.
	 */
	public JCRRepository(JCRConfiguration configuration) {
		this.configuration = configuration;
	}
	
	/**
	 * Gets the repository based on the configuration.
	 * 
	 * @return the repository
	 */
	public Repository getRepository() {
		if (repository == null) {
			createRepository();
		}
		return repository;
	}
	
	/**
	 * Creates the repository based on the configuration.
	 * 
	 * @return the created repository
	 */
	protected synchronized void createRepository() {
		if (repository == null) {
			try {
				if (getConfiguration().getUser() != null && getConfiguration().getPassword() != null) {

					// check whether they are treated as encrypted
					if (getConfiguration().isEncrypted()) {
						// create encrypted credentials
						credentials = new CryptedSimpleCredentials(getConfiguration().getUser(), getConfiguration().getPassword());
					} else {
						// create simple unencrypted credentials
						credentials = new SimpleCredentials(getConfiguration().getUser(), getConfiguration().getPassword().toCharArray());
					}
				} else {
					// per default use guest credentials
					credentials = new GuestCredentials();
				}

				if (getConfiguration().getUrl() != null) {
					// connect to a remote repository
					repository = JcrUtils.getRepository(getConfiguration().getUrl());
				} else if (getConfiguration().getConfigFile() != null && getConfiguration().getHomeDir() != null && getConfiguration().getRepositoryName() != null) {
					// connect to a local repository
					BindableRepositoryFactory factory = new BindableRepositoryFactory();
					factory.setConfigFile(getConfiguration().getConfigFile());
					factory.setHomeDir(getConfiguration().getHomeDir());
					factory.setRepositoryName(getConfiguration().getRepositoryName());
					factory.setOverwrite(false);
					repository = factory.getRepository();
				} else {
					throw new IllegalArgumentException("Repository configuration is not valid!");
				}

				// Login to the default workspace
				session = repository.login(credentials);

				// load an optionally configured CND file
				if (getConfiguration().getCndFile() != null) {
					FileReader reader = null;
					try {
						reader = new FileReader(getConfiguration().getCndFile());
						
						NodeType[] nodeTypes = CndImporter.registerNodeTypes(reader, session);

						for (NodeType nt : nodeTypes) {
							System.out.println("Registered: " + nt.getName());
						}
					} catch (Throwable t) {
						throw new RepositoryException(t);
					}
					finally {
						if (reader != null) {
							reader.close();
						}
					}
				}
			} catch (Exception e) {
				LOG.error("Error creating local repository!", e);
			}
		}
	}

	/**
	 * Gets the default session for the configured credentials.
	 * 
	 * @return the session
	 * @throws RepositoryException
	 *             if an error occurred
	 */
	public Session getDefaultSession() throws RepositoryException {
		//check if the repository and session have been created
		if (session == null) {
			createRepository();
		}
		
		//check if the session is still alive
		if (session.isLive()) {
			return session;
		}

		//otherwise create a new session with default credentials
		session = createSession(credentials);

		return session;
	}

	/**
	 * Creates a new session with the configured default credentials.
	 * 
	 * @return the new session
	 * @throws RepositoryException
	 *             if an error occurred
	 */
	public Session createSession() throws RepositoryException {
		//check if repository has been created
		if (repository == null) {
			createRepository();
		}
		
		return createSession(credentials);
	}

	/**
	 * Creates a new session with the specified credentials.
	 * 
	 * @return the new session
	 * @throws RepositoryException
	 *             if an error occurred
	 */
	public Session createSession(Credentials credentials) throws RepositoryException {
		return getRepository().login(credentials);
	}

	/**
	 * @return the configuration
	 */
	public JCRConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * @param configuration the configuration to set
	 */
	public void setConfiguration(JCRConfiguration configuration) {
		this.configuration = configuration;
	}

	public static void main(String[] args) throws Exception {
		
		Map<String, String> config = new HashMap<String, String>();
		config.put("configFile", "./src/test/resources/repository.xml");
		config.put("repositoryName", "test");
		config.put("homeDir", "./target/repository");
		config.put("user", "admin");
		config.put("password", "admin");
		config.put("cndFile", "./src/test/resources/nodetypes.cnd");
		
		JCRRepository rep = new JCRRepository(new JCRConfiguration(config));
		
		System.out.println(rep.getDefaultSession().getRootNode());
	}
}
