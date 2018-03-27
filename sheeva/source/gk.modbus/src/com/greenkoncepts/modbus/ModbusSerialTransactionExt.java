package com.greenkoncepts.modbus;

import java.io.IOException;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.ModbusSlaveException;
import com.ghgande.j2mod.modbus.io.ModbusSerialTransport;
import com.ghgande.j2mod.modbus.io.ModbusTransaction;
import com.ghgande.j2mod.modbus.msg.ExceptionResponse;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.net.SerialConnection;


/**
 * Class implementing the <tt>ModbusTransaction</tt>
 * interface.
 *
 * @author Phu Tran
 * @version 1.0.0
 */
public class ModbusSerialTransactionExt implements ModbusTransaction{

	//class attributes
	  private static int c_TransactionID =
	      Modbus.DEFAULT_TRANSACTION_ID;


	  //instance attributes and associations
	  private ModbusSerialTransport m_IO;
	  private ModbusRequest m_Request;
	  private ModbusResponse m_Response;
	  private boolean m_ValidityCheck =
	      Modbus.DEFAULT_VALIDITYCHECK;
	  private int m_Retries = Modbus.DEFAULT_RETRIES;
	  private int m_TransDelayMS = Modbus.DEFAULT_TRANSMIT_DELAY;
	  private SerialConnection m_SerialCon;
	  
	  //private boolean isError = false;
	  private int errorCode = NO_ERROR;

	  /**
	   * Defines code
	   * for <tt>no error</tt>.
	   */
	  public static final int NO_ERROR = 0;

	  /**
	   * Defines code
	   * for <tt>assertion error</tt>.
	   */
	  public static final int ASSERTION_ERROR = 1;
	  
	  /**
	   * Defines code
	   * for <tt>write error</tt>.
	   */
	  public static final int WRITE_ERROR = 2;
	  
	  /**
	   * Defines code
	   * for <tt>read error</tt>.
	   */
	  public static final int READ_ERROR = 3;

	  /**
	   * Defines code
	   * for <tt>crc check fail error</tt>.
	   */
	  public static final int READ_CRC_ERROR = 4;

	  /**
	   * Defines code
	   * for <tt>response error</tt>.
	   */
	  public static final int READ_RESPONSE_ERROR = 5;
	  
	  /**
	   * Defines code
	   * for <tt>slave error</tt>.
	   */
	  public static final int SLAVE_ERROR = 6;

	  /**
	   * Constructs a new <tt>ModbusSerialTransaction</tt>
	   * instance.
	   */
	  public ModbusSerialTransactionExt() {
	  }//constructor

	  /**
	   * Constructs a new <tt>ModbusSerialTransaction</tt>
	   * instance with a given <tt>ModbusRequest</tt> to
	   * be send when the transaction is executed.
	   * <p>
	   * @param request a <tt>ModbusRequest</tt> instance.
	   */
	  public ModbusSerialTransactionExt(ModbusRequest request) {
	    setRequest(request);
	  }//constructor

	  /**
	   * Constructs a new <tt>ModbusSerialTransaction</tt>
	   * instance with a given <tt>ModbusRequest</tt> to
	   * be send when the transaction is executed.
	   * <p>
	   * @param con a <tt>TCPMasterConnection</tt> instance.
	   */
	  public ModbusSerialTransactionExt(SerialConnection con) {
	    setSerialConnection(con);
	  }//constructor

	  /**
	   * Sets the port on which this <tt>ModbusTransaction</tt>
	   * should be executed.<p>
	   * <p>
	   * @param con a <tt>SerialConnection</tt>.
	   */
	  public void setSerialConnection(SerialConnection con) {
	    m_SerialCon = con;
	    m_IO = (ModbusSerialTransport) m_SerialCon.getModbusTransport();
	  }//setConnection
	  
	  public void setTransport(ModbusSerialTransport transport) {
		  m_IO = transport;
	  }

	  public int getTransactionID() {
	    return c_TransactionID;
	  }//getTransactionID


	  public void setRequest(ModbusRequest req) {
	    m_Request = req;
	    //m_Response = req.getResponse();
	  }//setRequest

	  public ModbusRequest getRequest() {
	    return m_Request;
	  }//getRequest

	  public ModbusResponse getResponse() {
	    return m_Response;
	  }//getResponse

	  public void setCheckingValidity(boolean b) {
	    m_ValidityCheck = b;
	  }//setCheckingValidity

	  public boolean isCheckingValidity() {
	    return m_ValidityCheck;
	  }//isCheckingValidity

	  public int getRetries() {
	    return m_Retries;
	  }//getRetries

	  public void setRetries(int num) {
	    m_Retries = num;
	  }//setRetries

	/**
	 * Get the TransDelayMS value.
	 * @return the TransDelayMS value.
	 */
	  public int getTransDelayMS() {
	    return m_TransDelayMS;
	  }

	/**
	 * Set the TransDelayMS value.
	 * @param newTransDelayMS The new TransDelayMS value.
	 */
	  public void setTransDelayMS(int newTransDelayMS) {
	    this.m_TransDelayMS = newTransDelayMS;
	  }

	  public void execute() throws ModbusIOException,
	      ModbusSlaveException,
	      ModbusException {
	    //1. assert executeability
	    assertExecutable();

	    //3. write request, and read response,
	    //   while holding the lock on the IO object
	    synchronized (m_IO) {
	      int tries = 0;
	      boolean finished = false;
	      do {
	        try {
	          if (m_TransDelayMS > 0) {
	            try {
	              Thread.sleep(m_TransDelayMS);
	            } catch (InterruptedException ex) {
	            	if (Modbus.debug)
	            		System.err.println("InterruptedException: " +
	            				ex.getMessage());
	            }
	          }
	          //write request message
	          try{
	        	  m_IO.writeMessage(m_Request);
	          }catch(ModbusIOException e){
	        	  //isError = true;
	        	  errorCode = WRITE_ERROR;
	        	  throw e;
	          }
	          
	          //read response message
	          try{
	        	  m_Response = m_IO.readResponse();
	          }catch(Exception e){
	        	  //isError = true;
	        	  if (e instanceof IOException) {
	        		  if(e.getMessage().startsWith("CRC")){
	        			  errorCode = READ_CRC_ERROR;
	        		  }else if(e.getMessage().startsWith("Error")){
	        			  errorCode = READ_RESPONSE_ERROR;
	        		  }
	        	  }else if(e instanceof ModbusIOException){
	        		  errorCode = READ_ERROR;
	        	  }
	        	  throw (ModbusIOException)e;
	          }
	          finished = true;
	        } catch (ModbusIOException e) {
	          if (++tries >= m_Retries) {
	            throw e;
	          }
	          if (Modbus.debug)
	        	  System.err.println("Execute try " + tries + " error: " +
	        			  e.getMessage());
	        }
	      } while (!finished);
	    }

	    //4. deal with exceptions
	    if (m_Response instanceof ExceptionResponse) {
	    	//isError = true;
	    	errorCode = SLAVE_ERROR;
	      throw new ModbusSlaveException(
	          ((ExceptionResponse) m_Response).getExceptionCode()
	      );
	    }

	    if (isCheckingValidity()) {
	      checkValidity();
	    }
	    //toggle the id
	    toggleTransactionID();
	    
	    //isError = false;
	    errorCode = NO_ERROR;
	  }//execute

	  /**
	   * Asserts if this <tt>ModbusTCPTransaction</tt> is
	   * executable.
	   *
	   * @throws ModbusException if the transaction cannot be asserted.
	   */
	  private void assertExecutable()
	      throws ModbusException {
	    if (m_Request == null ||
	        m_IO == null) {
	    	//isError = true;
	    	errorCode = ASSERTION_ERROR;
	      throw new ModbusException(
	          "Assertion failed, transaction not executable"
	      );
	    }
	  }//assertExecuteable

	  /**
	   * Checks the validity of the transaction, by
	   * checking if the values of the response correspond
	   * to the values of the request.
	   *
	   * @throws ModbusException if the transaction is not valid.
	   */
	  private void checkValidity() throws ModbusException {

	  }//checkValidity

	  /**
	   * Toggles the transaction identifier, to ensure
	   * that each transaction has a distinctive
	   * identifier.<br>
	   * When the maximum value of 65535 has been reached,
	   * the identifiers will start from zero again.
	   */
	  private void toggleTransactionID() {
	    if (isCheckingValidity()) {
	      if (c_TransactionID == (Short.MAX_VALUE * 2)) {
	        c_TransactionID = 0;
	      } else {
	        c_TransactionID++;
	      }
	    }
	    m_Request.setTransactionID(getTransactionID());
	  }//toggleTransactionID

	  /**
	   * Describe <code>setReceiveThreshold</code> method here.
	   *
	   * @param th an <code>int</code> value
	   */
	  public void setReceiveThreshold(int th) {
		m_IO.setReceiveThreshold(th);
	  }
	  
	  /**
	   * Describe <code>setReceiveTimeout</code> method here.
	   *
	   * @param ms an <code>int</code> value
	   */
	  public void setReceiveTimeout(int ms) {
		  m_IO.setReceiveTimeout(ms);
	  }
	  
	  //public boolean isError(){
	  //	return isError;
	  //}
	  
	  public int getErrorCode(){
		  return errorCode;
	  }
}
