package com.greenkoncepts.impl.services.comm.win;

//import java.util.Hashtable;
import java.util.Properties;
import org.osgi.framework.BundleContext;

public class PropertyUtils
{
  public static String getProperty(BundleContext bc, String propertyName)
  {
    return getProperty(bc, propertyName, null);
  }
  
  public static String getProperty(BundleContext bc, String propertyName, String defaultValue)
  {
    String result = null;
    if (bc == null) {
      result = System.getProperty(propertyName);
    } else {
      result = bc.getProperty(propertyName);
    }
    if (result == null) {
      result = defaultValue;
    }
    return result;
  }
  
  public static boolean getBoolean(BundleContext bc, String property)
  {
    String prop = null;
    if (bc == null) {
      prop = System.getProperty(property);
    } else {
      prop = bc.getProperty(property);
    }
    return (prop != null) && (prop.equalsIgnoreCase("true"));
  }
  
  public static Integer getInteger(BundleContext bc, String property, int defaultValue)
  {
    String v = null;
    if (bc == null) {
      v = System.getProperty(property);
    } else {
      v = bc.getProperty(property);
    }
    if (v != null) {
      try
      {
        return Integer.decode(v);
      }
      catch (NumberFormatException e) {}
    }
    return new Integer(defaultValue);
  }
  
  public static Integer getInteger(BundleContext bc, String property, Integer defaultValue)
  {
    String v = null;
    if (bc == null) {
      v = System.getProperty(property);
    } else {
      v = bc.getProperty(property);
    }
    if (v != null) {
      try
      {
        return Integer.decode(v);
      }
      catch (NumberFormatException e) {}
    }
    return defaultValue;
  }
  
  public static Long getLong(BundleContext bc, String property, long defaultValue)
  {
    String v = null;
    if (bc == null) {
      v = System.getProperty(property);
    } else {
      v = bc.getProperty(property);
    }
    if (v != null) {
      try
      {
        return Long.decode(v);
      }
      catch (NumberFormatException e) {}
    }
    return new Long(defaultValue);
  }
  
  public static Long getLong(BundleContext bc, String property, Long defaultValue)
  {
    String v = null;
    if (bc == null) {
      v = System.getProperty(property);
    } else {
      v = bc.getProperty(property);
    }
    if (v != null) {
      try
      {
        return Long.decode(v);
      }
      catch (NumberFormatException e) {}
    }
    return defaultValue;
  }
  
  public static Properties getProperties(BundleContext bc)
  {
    Properties props = null;
    


    props = System.getProperties();
    
    return props;
  }
  
  public static void setProperty(String key, String value)
  {
    System.setProperty(key, value);
  }
  
  public static void setProperties(Properties props)
  {
    System.setProperties(props);
  }
  
  public static void removeProperty(String key)
  {
    System.getProperties().remove(key);
  }
}
