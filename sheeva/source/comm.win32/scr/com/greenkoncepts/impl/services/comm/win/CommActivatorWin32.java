package com.greenkoncepts.impl.services.comm.win;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPortService;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.StringTokenizer;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class CommActivatorWin32
  implements BundleActivator, ServiceTrackerCustomizer
{
  private static final String COMM_DEVICES_WIN32 = "COM1,COM2,COM3,COM4,LPT1,LPT2";
  private static boolean DEBUG = false;
  private static CommPortIdentifier cpi = null;
  private ServiceTracker smartcommTracker;
  private BundleContext bc;
  
  private boolean hasPortIdentifier(Enumeration e, String portName)
  {
    while (e.hasMoreElements())
    {
      CommPortIdentifier cpi = (CommPortIdentifier)e.nextElement();
      if ((cpi != null) && (cpi.getName().equals(portName))) {
        return true;
      }
    }
    return false;
  }
  
  private String getPortIdentifiers()
  {
    StringBuffer sb = new StringBuffer(30);
    for (Enumeration e = CommPortIdentifier.getPortIdentifiers(); e.hasMoreElements();)
    {
      CommPortIdentifier cpi = (CommPortIdentifier)e.nextElement();
      sb.append(cpi.getName());
      sb.append(' ');
    }
    return sb.toString();
  }
  
  public void start(BundleContext bundlecontext)
    throws Exception
  {
    this.bc = bundlecontext;
    DEBUG = "true".equalsIgnoreCase(PropertyUtils.getProperty(bundlecontext, "mbs.comm.debug", "false"));
    if (DEBUG) {
      System.out.println("[comm-rxtx] start : initial portIDs [ " + getPortIdentifiers() + "]");
    }
    String devices = PropertyUtils.getProperty(bundlecontext, "mbs.comm.devices", PropertyUtils.getProperty(bundlecontext, "comm.devices", null));
    if (devices == null)
    {
      if (DEBUG) {
        System.out.println("[comm-rxtx] start : Adding COM1..255 ('mbs.comm.devices' is empty)");
      }
      StringBuffer sb = new StringBuffer(2000);
      for (int i = 1; i < 256; i++)
      {
        sb.append("COM" + i);
        if (i < 255) {
          sb.append(',');
        }
      }
      devices = sb.toString();
    }
    if (DEBUG) {
      System.out.println("[comm-rxtx] start : Registering dynamic ports [ " + devices + " ]");
    }
    String sep = "," + PropertyUtils.getProperty(bundlecontext, "path.separator", ";");
    StringTokenizer stringtokenizer = new StringTokenizer(devices, sep);
    while (stringtokenizer.hasMoreTokens())
    {
      String s = stringtokenizer.nextToken().trim();
      Enumeration e = CommPortIdentifier.getPortIdentifiers();
      if (!hasPortIdentifier(e, s)) {
        if (s.startsWith("COM"))
        {
          if (DEBUG) {
            System.out.println("[comm-rxtx] start : adding unavailable serial port " + s);
          }
          CommPortIdentifier.addPortName(s, 1, CommPortIdentifier.theDriver);
        }
        else if (s.startsWith("LPT"))
        {
          if (DEBUG) {
            System.out.println("[comm-rxtx] start : adding unavailable parallel port " + s);
          }
          CommPortIdentifier.addPortName(s, 2, null);
        }
      }
    }
    if (DEBUG) {
      System.out.println("[comm-rxtx] start : custom portIDs [ " + getPortIdentifiers() + "]");
    }
    this.smartcommTracker = new ServiceTracker(bundlecontext, SerialPortService.class.getName(), this);
    this.smartcommTracker.open();
  }
  
  public void stop(BundleContext bundlecontext)
  {
    if (DEBUG)
    {
      System.out.println("[comm-rxtx] stop : left portIDs [ " + getPortIdentifiers() + "]");
      if (Boolean.getBoolean("gnu.io.trace")) {
        Thread.dumpStack();
      }
    }
    this.smartcommTracker.close();
    this.smartcommTracker = null;
    
    cpi = null;
    System.gc();
    System.gc();
    System.gc();
  }
  
  public Object addingService(ServiceReference reference)
  {
    SerialPortService serialPortService = (SerialPortService)this.bc.getService(reference);
    if (DEBUG) {
      System.out.println("[comm-rxtx] addingService(" + serialPortService + ")");
    }
    CommPortIdentifier.setSerialPortService(serialPortService);
    return serialPortService;
  }
  
  public void modifiedService(ServiceReference reference, Object service) {}
  
  public void removedService(ServiceReference reference, Object service)
  {
    if (DEBUG) {
      System.out.println("[comm-rxtx] removedService(" + service + ")");
    }
    CommPortIdentifier.setSerialPortService(null);
  }
}
