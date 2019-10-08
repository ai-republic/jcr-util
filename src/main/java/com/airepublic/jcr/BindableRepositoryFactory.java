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


import java.util.Hashtable;

import javax.jcr.Repository;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.spi.InitialContextFactory;

import org.apache.jackrabbit.core.jndi.RegistryHelper;
import org.apache.jackrabbit.core.jndi.provider.DummyInitialContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for loading and exposing JCR repositories on JNDI.<br>
 * <br>
 * (c) Copyright 2014 by ai-republic GmbH, Germany
 * 
 * @author Torsten.Oltmanns@ai-republic.com
 */
public class BindableRepositoryFactory
{
	private static final Logger LOG = LoggerFactory.getLogger(BindableRepositoryFactory.class);

	private static final String DEFAULT_PROVIDERURL = "localhost";
	private static final boolean DEFAULT_OVERWRITE = true;

	private String repositoryName;
	private String configFile;
	private String homeDir;
	private boolean overwrite = DEFAULT_OVERWRITE;
	private String providerUrl = DEFAULT_PROVIDERURL;
	private Class<? extends InitialContextFactory> initialContextFactoryClass = DummyInitialContextFactory.class;
	private Repository repository;
	
	/**
	 * register the repository
	 * 
	 * @throws Exception
	 */
	private void register() throws Exception
	{
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactoryClass.getName());
		env.put(Context.PROVIDER_URL, providerUrl);
		InitialContext ctx = new InitialContext(env);
		// always create instance by using BindableRepositoryFactory
		RegistryHelper.registerRepository(ctx, repositoryName, configFile, homeDir, overwrite);
		repository = (Repository) ctx.lookup(repositoryName); 
		LOG.info("register repository");
	}

	/**
	 * unregister the repository
	 * 
	 * @throws Exception
	 */
	public void unregister() throws Exception
	{
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactoryClass.getName());
		env.put(Context.PROVIDER_URL, providerUrl);
		InitialContext ctx = new InitialContext(env);
		RegistryHelper.unregisterRepository(ctx, repositoryName);
	}

	public Repository getRepository() throws Exception {
		if (repository == null) {
			register();
		}
		
		return repository;
	}

	public void destroy() throws Exception
	{
		unregister();
		LOG.info("unregister repository");
	}

	public void setRepositoryName(String repositoryName)
	{
		this.repositoryName = repositoryName;
	}

	public void setConfigFile(String configFile)
	{
		this.configFile = configFile;
	}

	public void setHomeDir(String homeDir)
	{
		this.homeDir = homeDir;
	}

	public void setOverwrite(boolean overwrite)
	{
		this.overwrite = overwrite;
	}

	public void setInitialContextFactoryClass(Class<? extends InitialContextFactory> initialContextFactoryClass)
	{
		this.initialContextFactoryClass = initialContextFactoryClass;
	}
}
