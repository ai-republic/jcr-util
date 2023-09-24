/**
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Helper class to read/write properties on a node depending on its {@link PropertyType}.
 * 
 * @author Torsten Oltmanns
 * <br>(c) Copyright ai-republic GmbH, Germany (2014)
 */
public class JCRHelper
{
	/**
	 * Gets the properties of the specified node using the specified prefix to identify the properties.
	 * 
	 * @param node the node
	 * @param prefix the property prefix
	 * @return the map of property names (without the prefix) and their value
	 * 
	 * @throws RepositoryException if access to the repository or node failed
	 */
	public static Map<String, Comparable<?>> getProperties(Node node, String prefix) throws RepositoryException 
	{
		Map<String, Comparable<?>> properties = new LinkedHashMap<String, Comparable<?>>();
		
		PropertyIterator it = node.getProperties(prefix + "*");
		
		while (it.hasNext())
		{
			Property p = it.nextProperty();
			String name = p.getName().substring(prefix.length());
			properties.put(name, getProperty(node, p.getName()));
		}

		return properties;
	}
	
	/**
	 * Sets the properties of the specified node using the specified prefix to prepend to the property names.
	 * 
	 * @param node the node
	 * @param prefix the property prefix
	 * @return the map of property names (will be prepended with the prefix) and their value
	 * 
	 * @throws RepositoryException if access to the repository or node failed
	 */
	public static void setProperties(Node node, String prefix, Map<String, Comparable<?>> properties) throws Exception
	{
		//remove all old properties
		PropertyIterator it = node.getProperties(prefix + "*");
		
		while (it.hasNext())
		{
			it.nextProperty().remove();
		}
		
		//add all new properties
		Iterator<String> pIt = properties.keySet().iterator();
		
		while (pIt.hasNext())
		{
			String name = pIt.next();
			setProperty(node, prefix + name, properties.get(name));
		}
		
		node.getSession().save();
	}
	
	/**
	 * Gets the property with the specified name from the specified node. Non-Standard properties will be converted to an object.
	 * 
	 * @param node the node
	 * @param name the property name
	 * @return the value
	 * @throws RepositoryException if access to the repository or node failed
	 */
	public static Comparable<?> getProperty(Node node, String name) throws RepositoryException
	{
		Property p = node.getProperty(name);

		switch (p.getType())
		{
		case PropertyType.STRING:
			return p.getString();
		case PropertyType.NAME:
			return p.getName();
		case PropertyType.PATH:
			return p.getPath();
		case PropertyType.URI:
			return p.getString();
		case PropertyType.LONG:
			return p.getLong();
		case PropertyType.DOUBLE:
			return p.getDouble();
		case PropertyType.BOOLEAN:
			return p.getBoolean();
		case PropertyType.DECIMAL:
			return p.getDecimal();
		case PropertyType.DATE:
			return p.getDate();
		default:
			try {
				return (Comparable<?>) JCRHelper.createObject(p.getBinary());
			} catch (IOException e) {
				throw new RepositoryException("Error creating object from binary property '" + name + "' from node: " + node, e);
			}
		}
	}
	
	/**
	 * Sets the property with the specified name from the specified node. Non-Standard properties will be converted to a binary type object.
	 * 
	 * @param node the node
	 * @param name the property name
	 * @param value the value
	 * @throws RepositoryException if access to the repository or node failed
	 */
	public static void setProperty(Node node, String name, Comparable<?> value) throws RepositoryException
	{
		if (value instanceof String)
		{
			node.setProperty(name, (String) value);
		}
		else if ((value != null && value.getClass() == int.class) || value instanceof Integer)
		{
			node.setProperty(name, (Integer)value);
		}
		else if ((value != null && value.getClass() == long.class) || value instanceof Long)
		{
			node.setProperty(name, (Long)value);
		}
		else if ((value != null && value.getClass() == float.class) || value instanceof Float)
		{
			node.setProperty(name, (Float)value);
		}
		else if ((value != null && value.getClass() == double.class) || value instanceof Double)
		{
			node.setProperty(name, (Double)value);
		}
		else if (value instanceof BigDecimal)
		{
			node.setProperty(name, (BigDecimal)value);
		}
		else if ((value != null && value.getClass() == boolean.class) || value instanceof Boolean)
		{
			node.setProperty(name, (Boolean) value);
		}
		else if (value instanceof Date)
		{
			Calendar cal = new GregorianCalendar();
			cal.setTime((Date) value);
			node.setProperty(name, cal);
		}
		else if (value instanceof Calendar)
		{
			node.setProperty(name, (Calendar) value);
		}
		else if (value != null)
		{
			try {
				node.setProperty(name, JCRHelper.createBinary(node.getSession(), (Serializable) value));
			} catch (IOException e) {
				throw new RepositoryException("Error creating binary property '" + name + "' from object value '" + value + "' on node: " + node, e);
			}
		}
		else
		{
			node.setProperty(name, (String)value);
		}
	}
	
	/**
	 * Creates a binary property from the specified value. 
	 * 
	 * @param session the current JCR connection session
	 * @param value the value
	 * @return the binary property
	 * @throws IOException if an error occurs converting the value
	 */
	public static Binary createBinary(Session session, Serializable value) throws IOException
	{
		PipedInputStream pis = new PipedInputStream();
		ObjectOutputStream oos = null;
		
		try
		{
			oos = new ObjectOutputStream(new PipedOutputStream(pis));
			oos.writeObject(value);
			oos.close();
			return session.getValueFactory().createBinary(pis);
		}
		catch (Exception e)
		{
			throw new IOException(e);
		}
		finally 
		{
			if (oos != null)
			{
				try
				{
					oos.close();
				}
				catch (Exception e2)
				{
				}
			}
			
			try
			{
				pis.close();
			}
			catch (Exception e2)
			{
			}
		}

	}

	/**
	 * Creates an object from the specified binary property. 
	 * 
	 * @param value the value
	 * @return the object value
	 * @throws IOException if an error occurs converting the value
	 */
	public static Object createObject(Binary value) throws IOException
	{
		Object obj = null;
		ObjectInputStream ois = null;
		
		try
		{
			ois = new ObjectInputStream(value.getStream());
			obj = ois.readObject();
		}
		catch (Exception e)
		{
			throw new IOException(e);
		}
		finally
		{
			ois.close();
		}
		
		return obj;
	}
}
