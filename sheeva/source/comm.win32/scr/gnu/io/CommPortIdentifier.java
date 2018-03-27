/*-------------------------------------------------------------------------
|   rxtx is a native interface to serial ports in java.
|   Copyright 1997-2004 by Trent Jarvi taj@www.linux.org.uk
|
|   This library is free software; you can redistribute it and/or
|   modify it under the terms of the GNU Library General Public
|   License as published by the Free Software Foundation; either
|   version 2 of the License, or (at your option) any later version.
|
|   This library is distributed in the hope that it will be useful,
|   but WITHOUT ANY WARRANTY; without even the implied warranty of
|   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
|   Library General Public License for more details.
|
|   You should have received a copy of the GNU Library General Public
|   License along with this library; if not, write to the Free
|   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
--------------------------------------------------------------------------*/
package gnu.io;

import java.io.FileDescriptor;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;


/**
* @author Trent Jarvi
* @version %I%, %G%
* @since JDK1.0
*/



public class CommPortIdentifier
{
  public static final int PORT_SERIAL = 1;
  public static final int PORT_PARALLEL = 2;
  public static final int PORT_I2C = 3;
  public static final int PORT_RS485 = 4;
  public static final int PORT_RAW = 5;
  private String PortName;
  private boolean Available = true;
  private String Owner;
  private CommPort commport;
  private CommDriver RXTXDriver;
  static CommPortIdentifier CommPortIndex;
  CommPortIdentifier next;
  private int PortType;
  private static final boolean verbose = Boolean.getBoolean("gnu.io.verbose");
  private static final boolean debug = Boolean.getBoolean("gnu.io.cpi.debug");
  private static final boolean trace = Boolean.getBoolean("gnu.io.trace");

  static Object Sync;
  Vector ownershipListener;
  public static CommDriver theDriver = null;
  private boolean HideOwnerEvents;
  private static SerialPortService serialPortService;
  
/*------------------------------------------------------------------------------
	static {}   aka initialization
	accept:       -
	perform:      load the rxtx driver
	return:       -
	exceptions:   Throwable
	comments:     static block to initialize the class
------------------------------------------------------------------------------*/
	// initialization only done once....
	static
	{
		if (debug) {
			System.out.println("CommPortIdentifier:static initialization()");
		}
		Sync = new Object();
    
		CommPortIndex = null;
		try
		{
			theDriver = (CommDriver)Class.forName("gnu.io.RXTXCommDriver").newInstance();
			theDriver.initialize();
		}
		catch (Throwable e)
		{
			System.err.println(e + " thrown while loading " + "gnu.io.RXTXCommDriver");
			e.printStackTrace();
		}
		String OS = System.getProperty("os.name");
		if (OS.toLowerCase().indexOf("linux") == -1) {
			if (debug) {
				System.out.println("Have not implemented native_psmisc_report_owner(PortName)); in CommPortIdentifier");
			}
		}
		System.loadLibrary("rxtxSerial");
		if (debug) {
			System.out.println("CommPortIdentifier:static initialization() finished");
		}
	}
  
	protected CommPortIdentifier(String pn, CommPort cp, int pt, CommDriver driver)
	{
		this.PortName = pn;
		this.commport = cp;
		this.PortType = pt;
		this.next = null;
		this.RXTXDriver = driver;
		if ((verbose) && (debug)) {
			System.out.println("new CommPortIdentifier:CommPortIdentifier('" + pn + "'," + cp + "," + pt + "," + driver + ")");
		}
	}
  
/*------------------------------------------------------------------------------
	addPortName()
	accept:         Name of the port s, Port type, 
                        reverence to RXTXCommDriver.
	perform:        place a new CommPortIdentifier in the linked list
	return: 	none.
	exceptions:     none.
	comments:
------------------------------------------------------------------------------*/
	public static void addPortName(String s, int type, CommDriver c)
	{
		if (debug) {
			System.out.println("CommPortIdentifier:addPortName('" + s + "'," + type + "," + c + ")");
		}
		CommPortIdentifier res = null;
		res = new CommPortIdentifier(s, null, type, c);
		AddIdentifierToList(res);
	}

/*------------------------------------------------------------------------------
	AddIdentifierToList()
	accept:        The cpi to add to the list. 
	perform:        
	return: 	
	exceptions:    
	comments:
------------------------------------------------------------------------------*/
	private static void AddIdentifierToList(CommPortIdentifier cpi)
	{
		if ((verbose) && (debug)) {
			System.out.println("CommPortIdentifier:AddIdentifierToList(" + cpi.getName() + ")");
		}
		synchronized (Sync)
		{
			if (CommPortIndex == null)
			{
				CommPortIndex = cpi;
				if ((verbose) && (debug)) {
					System.out.println("CommPortIdentifier:AddIdentifierToList() null");
				}
			}
			else
			{
				CommPortIdentifier index = CommPortIndex;
				while (index.next != null)
				{

					index = index.next;



					if(debug) System.out.println("CommPortIdentifier:AddIdentifierToList() index.next");
				}
				index.next = cpi;
			}
		}
	}
	
/*------------------------------------------------------------------------------
	addPortOwnershipListener()
	accept:
	perform:
	return:
	exceptions:
	comments:	 
------------------------------------------------------------------------------*/
	public void addPortOwnershipListener(CommPortOwnershipListener c)
	{
		if (debug) {
			System.out.println("CommPortIdentifier:addPortOwnershipListener(" + c + ")");
		}



		if (this.ownershipListener == null) {

			this.ownershipListener = new Vector();
		}



		if (!this.ownershipListener.contains(c)) {

			this.ownershipListener.addElement(c);
		}
	}
	
/*------------------------------------------------------------------------------
	getCurrentOwner()
	accept:
	perform:
	return:
	exceptions:
	comments:		
------------------------------------------------------------------------------*/

	public String getCurrentOwner()
	{
		if (debug) {
			System.out.println("CommPortIdentifier:getCurrentOwner() -> " + this.Owner);
		}
		return this.Owner;
	}
	
/*------------------------------------------------------------------------------
	getName()
	accept:
	perform:
	return:
	exceptions:
	comments:
------------------------------------------------------------------------------*/
	public String getName()
	{
		if (debug) {
			System.out.println("CommPortIdentifier:getName()->'" + this.PortName + "'");
		}
		return this.PortName;
	}
	
/*------------------------------------------------------------------------------
	getPortIdentifier()
	accept:
	perform:
	return:
	exceptions:
	comments:	 
------------------------------------------------------------------------------*/
	public static CommPortIdentifier getPortIdentifier(String s)
		throws NoSuchPortException
	{
		if (debug) {
			System.out.println("CommPortIdentifier:getPortIdentifier(" + s + ")");
		}
		CommPortIdentifier index = CommPortIndex;

		synchronized (Sync)
		{
			while (index != null)
			{
				if (index.PortName.equals(s)) {
					break;
				}
				index = index.next;
			}
		}
		if (index != null) {
			return index;
		}


		if (debug) {
			System.out.println("not found!" + s);
		}
		throw new NoSuchPortException();
	}
	
/*------------------------------------------------------------------------------
	getPortIdentifier()
	accept:
	perform:
	return:
	exceptions:
	comments:		
------------------------------------------------------------------------------*/
	public static CommPortIdentifier getPortIdentifier(CommPort p)
		throws NoSuchPortException
	{
		if (debug) {
			System.out.println("CommPortIdentifier:getPortIdentifier(" + p.getName() + ")");
		}
		CommPortIdentifier c = CommPortIndex;
		synchronized (Sync)
		{
			while ((c != null) && (c.commport != p)) {
				c = c.next;
			}
		}
		if (c != null) {
			return c;
		}



		if (debug) {
			System.out.println("not found!" + p.getName());
		}
		throw new NoSuchPortException();
	}
/*------------------------------------------------------------------------------
	getPortIdentifiers()
	accept:
	perform:
	return:
	exceptions:
	comments:
------------------------------------------------------------------------------*/
	public static Enumeration getPortIdentifiers()
	{
		if (debug) {
			System.out.println("static CommPortIdentifier:getPortIdentifiers() index=" + CommPortIndex);





		}




		return new CommPortEnumerator();
	}
	
/*------------------------------------------------------------------------------
	getPortType()
	accept:
	perform:
	return:
	exceptions:
	comments:
------------------------------------------------------------------------------*/
	public int getPortType()
	{
		if (debug) {
			System.out.println("CommPortIdentifier:getPortType()->" + this.PortType);
		}
		return this.PortType;
	}
	
/*------------------------------------------------------------------------------
	isCurrentlyOwned()
	accept:
	perform:
	return:
	exceptions:
	comments:		
------------------------------------------------------------------------------*/

	public synchronized boolean isCurrentlyOwned()
	{
		if (debug) {
			System.out.println("CommPortIdentifier:isCurrentlyOwned() -> " + (!this.Available));
		}
		return !this.Available;
	}
/*------------------------------------------------------------------------------
	open()
	accept:
	perform:
	return:
	exceptions:
	comments:
------------------------------------------------------------------------------*/
	public synchronized CommPort open(FileDescriptor f)
		throws UnsupportedCommOperationException
	{
		if (debug) {
			System.out.println("CommPortIdentifier:open(FileDescriptor)");
		}
		throw new UnsupportedCommOperationException();
	}
/*------------------------------------------------------------------------------
	open()
	accept:      application makeing the call and milliseconds to block
                     during open.
	perform:     open the port if possible
	return:      CommPort if successfull
	exceptions:  PortInUseException if in use.
	comments:
------------------------------------------------------------------------------*/
	public synchronized CommPort open(String TheOwner, int i)
		throws PortInUseException
	{
		if (debug)

		{
			System.out.println("CommPortIdentifier:open('" + TheOwner + "', " + i + ") Available=" + this.Available + ", RXTXDriver=" + this.RXTXDriver);
			if (trace) {
				Thread.dumpStack();
			}
		}
		if (serialPortService != null) {
			return serialPortService.open(this.RXTXDriver, TheOwner, getName(), i);
		}
		if (!this.Available) {
			synchronized (Sync)
			{
				fireOwnershipEvent(3);
				try
				{
					wait(i);
				}
				catch (InterruptedException e) {}
			}
		}
		if (!this.Available) {
			throw new PortInUseException("Used by " + getCurrentOwner());
		}
		if (this.commport == null) {
			this.commport = theDriver.getCommPort(this.PortName, this.PortType);
		}
		if (this.commport != null)
		{
			this.Owner = TheOwner;
			this.Available = false;
			fireOwnershipEvent(1);
			return this.commport;
		}
		throw new PortInUseException("Used by unknown");
	}
/*------------------------------------------------------------------------------
	removePortOwnership()
	accept:
	perform:
	return:
	exceptions:
	comments:
------------------------------------------------------------------------------*/
	public void removePortOwnershipListener(CommPortOwnershipListener c)

	{
		if (debug) {
			System.out.println("CommPortIdentifier:removePortOwnershipListener()");
		}
		if (this.ownershipListener != null) {
			this.ownershipListener.removeElement(c);
		}
	}
	
/*------------------------------------------------------------------------------
	internalClosePort()
	accept:			 None
	perform:			clean up the Ownership information and send the event
	return:			 None
	exceptions:	 None
	comments:		 None
------------------------------------------------------------------------------*/
	synchronized void internalClosePort()
	{
		if (debug) {
			System.out.println("CommPortIdentifier:internalClosePort() " + this.commport);
		}
		this.Owner = null;
		this.Available = true;
		this.commport = null;
		notifyAll();
		fireOwnershipEvent(2);
	}
	
/*------------------------------------------------------------------------------
	fireOwnershipEvent()
	accept:
	perform:
	return:
	exceptions:
	comments:
------------------------------------------------------------------------------*/
	void fireOwnershipEvent(int eventType)
	{
		if (debug) {
			System.out.println("CommPortIdentifier:fireOwnershipEvent( " + eventType + " )");
		}
		if (this.ownershipListener != null)
		{
			Enumeration e = this.ownershipListener.elements();
			CommPortOwnershipListener c;
			for (; e.hasMoreElements(); c.ownershipChange(eventType)) {
				c = (CommPortOwnershipListener)e.nextElement();
			}
		}
	}
	
	public static void setSerialPortService(SerialPortService serialPortService)
	{
		if (debug) {
			System.out.println("CommPortIdentifier:setSerialPortService( " + serialPortService + " )");
		}
		serialPortService = serialPortService;
	}
	
	private native String native_psmisc_report_owner(String paramString);
}
