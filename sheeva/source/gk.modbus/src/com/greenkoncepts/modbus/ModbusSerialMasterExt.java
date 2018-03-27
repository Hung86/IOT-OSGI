package com.greenkoncepts.modbus;

import com.ghgande.j2mod.modbus.ModbusCoupler;
import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.msg.ReadCoilsRequest;
import com.ghgande.j2mod.modbus.msg.ReadCoilsResponse;
import com.ghgande.j2mod.modbus.msg.ReadInputDiscretesRequest;
import com.ghgande.j2mod.modbus.msg.ReadInputDiscretesResponse;
import com.ghgande.j2mod.modbus.msg.ReadInputRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadInputRegistersResponse;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
import com.ghgande.j2mod.modbus.msg.WriteCoilRequest;
import com.ghgande.j2mod.modbus.msg.WriteCoilResponse;
import com.ghgande.j2mod.modbus.msg.WriteMultipleCoilsRequest;
import com.ghgande.j2mod.modbus.msg.WriteMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.msg.WriteSingleRegisterRequest;
import com.ghgande.j2mod.modbus.net.SerialConnection;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.util.BitVector;
import com.ghgande.j2mod.modbus.util.SerialParameters;


/**
 * Serial Master Extension facade.
 *
 * @author Phu Tran
 * @version 1.0.0
 */
public class ModbusSerialMasterExt {
	private SerialParameters m_CommParameters;
	private SerialConnection m_Connection;
	private ModbusSerialTransactionExt m_Transaction;
	private ReadCoilsRequest m_ReadCoilsRequest;
	private ReadInputDiscretesRequest m_ReadInputDiscretesRequest;
	private WriteCoilRequest m_WriteCoilRequest;
	private WriteMultipleCoilsRequest m_WriteMultipleCoilsRequest;
	private ReadInputRegistersRequest m_ReadInputRegistersRequest;
	private ReadMultipleRegistersRequest m_ReadMultipleRegistersRequest;
	private WriteSingleRegisterRequest m_WriteSingleRegisterRequest;
	private WriteMultipleRegistersRequest m_WriteMultipleRegistersRequest;
	
	private int m_Retries = -1;
	private int m_TransDelayMS = -1;
	private int m_TimeOut = -1;
	private int m_ReceiveThreshold = -1;

	  /**
	   * Constructs a new master facade instance for communication
	   * with a given slave.
	   *
	   * @param param SerialParameters specifies the serial port parameters to use
	   *              to communicate with the slave device network.
	   */
	  public ModbusSerialMasterExt(SerialParameters param) {
	    try {
	      m_CommParameters = param;
	      m_Connection = new SerialConnection(m_CommParameters);
	      m_ReadCoilsRequest = new ReadCoilsRequest();
	      m_ReadInputDiscretesRequest = new ReadInputDiscretesRequest();
	      m_WriteCoilRequest = new WriteCoilRequest();
	      m_WriteMultipleCoilsRequest = new WriteMultipleCoilsRequest();
	      m_ReadInputRegistersRequest = new ReadInputRegistersRequest();
	      m_ReadMultipleRegistersRequest = new ReadMultipleRegistersRequest();
	      m_WriteSingleRegisterRequest = new WriteSingleRegisterRequest();
	      m_WriteMultipleRegistersRequest = new WriteMultipleRegistersRequest();
	    } catch (Exception e) {
	      throw new RuntimeException(e.getMessage());
	    }
	  }//constructor

	  /**
	   * Sets the unit identifier of this <tt>ModbusSerialMaster</tt>.
	   *
	   * @param unitid the unit identifier.
	   */
	  public void setUnitIdentifier(int unitid) {
	    ModbusCoupler.getReference().setUnitID(unitid);
	  }//setUnitIdentifier

	  /**
	   * Returns the unit identifier of this <tt>ModbusSerialMaster</tt>.
	   *
	   * @return the unit identifier.
	   */
	  public int getUnitIdentifier() {
	    return ModbusCoupler.getReference().getUnitID();
	  }//setUnitIdentifier

	  /**
	   * Connects this <tt>ModbusSerialMaster</tt> with the slave.
	   *
	   * @throws Exception if the connection cannot be established.
	   */
	  public void connect()
	      throws Exception {
	    if (m_Connection != null && !m_Connection.isOpen()) {
	      m_Connection.open();
	      m_Transaction = new ModbusSerialTransactionExt(m_Connection);
	      if(m_Retries >= 0){
	    	  m_Transaction.setRetries(m_Retries);
	      }
	      if(m_TransDelayMS >= 0){
	    	  m_Transaction.setTransDelayMS(m_TransDelayMS);
	      }
	      if(m_TimeOut >= 0){
	    	  m_Transaction.setReceiveTimeout(m_TimeOut);
	      }
	      if(m_ReceiveThreshold >= 0){
	    	  m_Transaction.setReceiveThreshold(m_ReceiveThreshold);
	      }
	    }
	  }//connect

	  /**
	   * Disconnects this <tt>ModbusSerialMaster</tt> from the slave.
	   */
	  public void disconnect() {
	    if (m_Connection != null && m_Connection.isOpen()) {
	      m_Connection.close();
	      m_Transaction = null;
	    }
	  }//disconnect
	  
	  /**
	   * Check this <tt>ModbusSerialMaster</tt> connection.
	   */
	  public boolean checkConnect() {
		  if (m_Connection == null || m_Transaction == null || (m_Connection != null && !m_Connection.isOpen())) {
			  return false;
		  }
		  return true;
	  }//disconnect

	  /**
	   * Reads a given number of coil states from the slave.
	   * <p/>
	   * Note that the number of bits in the bit vector will be
	   * forced to the number originally requested.
	   *
	   * @param unitid (IN) the slave unit id.
	   * @param ref    the offset of the coil to start reading from.
	   * @param count  the number of coil states to be read.
	   * @return a <tt>BitVector</tt> instance holding the
	   *         received coil states.
	   * @throws ModbusException if an I/O error, a slave exception or
	   *                         a transaction error occurs.
	   */
	  public synchronized BitVector readCoils(int unitid, int ref, int count)
	      throws ModbusException {
	    m_ReadCoilsRequest.setUnitID(unitid);
	    m_ReadCoilsRequest.setReference(ref);
	    m_ReadCoilsRequest.setBitCount(count);
	    m_Transaction.setRequest(m_ReadCoilsRequest);
	    m_Transaction.execute();
	    BitVector bv = ((ReadCoilsResponse) m_Transaction.getResponse()).getCoils();
	    bv.forceSize(count);
	    return bv;
	  }//readCoils
	  
	  /**
	   * Reads a given number of coil states from the slave.
	   * <p/>
	   * Note that the number of bits in the bit vector will be
	   * forced to the number originally requested.
	   *
	   * @param unitid (IN) the slave unit id.
	   * @param ref    the offset of the coil to start reading from.
	   * @param count  the number of coil states to be read.
	   * @return a <tt>BitVector</tt> instance holding the
	   *         received coil states.
	   * @throws ModbusException if an I/O error, a slave exception or
	   *                         a transaction error occurs.
	   */
	  public synchronized byte[] readCoils2Bytes(int unitid, int ref, int count)
	      throws ModbusException {
	    m_ReadCoilsRequest.setUnitID(unitid);
	    m_ReadCoilsRequest.setReference(ref);
	    m_ReadCoilsRequest.setBitCount(count);
	    m_Transaction.setRequest(m_ReadCoilsRequest);
	    m_Transaction.execute();
	    return ((ReadCoilsResponse) m_Transaction.getResponse()).getMessage();
	  }//readCoils

	  /**
	   * Writes a coil state to the slave.
	   *
	   * @param unitid the slave unit id.
	   * @param ref    the offset of the coil to be written.
	   * @param state  the coil state to be written.
	   * @return the state of the coil as returned from the slave.
	   * @throws ModbusException if an I/O error, a slave exception or
	   *                         a transaction error occurs.
	   */
	  public synchronized boolean writeCoil(int unitid, int ref, boolean state)
	      throws ModbusException {
	    m_WriteCoilRequest.setUnitID(unitid);
	    m_WriteCoilRequest.setReference(ref);
	    m_WriteCoilRequest.setCoil(state);
	    m_Transaction.setRequest(m_WriteCoilRequest);
	    m_Transaction.execute();
	    return ((WriteCoilResponse) m_Transaction.getResponse()).getCoil();
	  }//writeCoil

	  /**
	   * Writes a given number of coil states to the slave.
	   * <p/>
	   * Note that the number of coils to be written is given
	   * implicitly, through {@link BitVector#size()}.
	   *
	   * @param unitid the slave unit id.
	   * @param ref    the offset of the coil to start writing to.
	   * @param coils  a <tt>BitVector</tt> which holds the coil states to be written.
	   * @throws ModbusException if an I/O error, a slave exception or
	   *                         a transaction error occurs.
	   */
	  public synchronized void writeMultipleCoils(int unitid, int ref, BitVector coils)
	      throws ModbusException {
	    m_WriteMultipleCoilsRequest.setUnitID(unitid);
	    m_WriteMultipleCoilsRequest.setReference(ref);
	    m_WriteMultipleCoilsRequest.setCoils(coils);
	    m_Transaction.setRequest(m_WriteMultipleCoilsRequest);
	    m_Transaction.execute();
	  }//writeMultipleCoils

	  /**
	   * Reads a given number of input discrete states from the slave.
	   * <p/>
	   * Note that the number of bits in the bit vector will be
	   * forced to the number originally requested.
	   *
	   * @param unitid the slave unit id.
	   * @param ref    the offset of the input discrete to start reading from.
	   * @param count  the number of input discrete states to be read.
	   * @return a <tt>BitVector</tt> instance holding the received input discrete
	   *         states.
	   * @throws ModbusException if an I/O error, a slave exception or
	   *                         a transaction error occurs.
	   */
	  public synchronized BitVector readInputDiscretes(int unitid, int ref, int count)
	      throws ModbusException {
	    m_ReadInputDiscretesRequest.setUnitID(unitid);
	    m_ReadInputDiscretesRequest.setReference(ref);
	    m_ReadInputDiscretesRequest.setBitCount(count);
	    m_Transaction.setRequest(m_ReadInputDiscretesRequest);
	    m_Transaction.execute();
	    BitVector bv = ((ReadInputDiscretesResponse) m_Transaction.getResponse()).getDiscretes();
	    bv.forceSize(count);
	    return bv;
	  }//readInputDiscretes
	  
	  /**
	   * Reads a given number of input discrete states from the slave.
	   * <p/>
	   * Note that the number of bits in the bit vector will be
	   * forced to the number originally requested.
	   *
	   * @param unitid the slave unit id.
	   * @param ref    the offset of the input discrete to start reading from.
	   * @param count  the number of input discrete states to be read.
	   * @return a <tt>byte[]</tt> instance holding the received input discrete
	   *         states.
	   * @throws ModbusException if an I/O error, a slave exception or
	   *                         a transaction error occurs.
	   */
	  public synchronized byte[] readInputDiscretes2Bytes(int unitid, int ref, int count)
	      throws ModbusException {
	    m_ReadInputDiscretesRequest.setUnitID(unitid);
	    m_ReadInputDiscretesRequest.setReference(ref);
	    m_ReadInputDiscretesRequest.setBitCount(count);
	    m_Transaction.setRequest(m_ReadInputDiscretesRequest);
	    m_Transaction.execute();
	    return ((ReadInputDiscretesResponse) m_Transaction.getResponse()).getMessage();
	  }//readInputDiscretes


	  /**
	   * Reads a given number of input registers from the slave.
	   * <p/>
	   * Note that the number of input registers returned (i.e. array length)
	   * will be according to the number received in the slave response.
	   *
	   * @param unitid the slave unit id.
	   * @param ref    the offset of the input register to start reading from.
	   * @param count  the number of input registers to be read.
	   * @return a <tt>InputRegister[]</tt> with the received input registers.
	   * @throws ModbusException if an I/O error, a slave exception or
	   *                         a transaction error occurs.
	   */
	  public synchronized InputRegister[] readInputRegisters(int unitid, int ref, int count)
	      throws ModbusException {
	    m_ReadInputRegistersRequest.setUnitID(unitid);
	    m_ReadInputRegistersRequest.setReference(ref);
	    m_ReadInputRegistersRequest.setWordCount(count);
	    m_Transaction.setRequest(m_ReadInputRegistersRequest);
	    m_Transaction.execute();
	    return ((ReadInputRegistersResponse) m_Transaction.getResponse()).getRegisters();
	  }//readInputRegisters
	  
	  /**
	   * Reads a given number of input registers from the slave.
	   * <p/>
	   * Note that the number of input registers returned (i.e. array length)
	   * will be according to the number received in the slave response.
	   *
	   * @param unitid the slave unit id.
	   * @param ref    the offset of the input register to start reading from.
	   * @param count  the number of input registers to be read.
	   * @return a <tt>InputRegister[]</tt> with the received input registers.
	   * @throws ModbusException if an I/O error, a slave exception or
	   *                         a transaction error occurs.
	   */
	  public synchronized byte[] readInputRegisters2Bytes(int unitid, int ref, int count)
	      throws ModbusException {
	    m_ReadInputRegistersRequest.setUnitID(unitid);
	    m_ReadInputRegistersRequest.setReference(ref);
	    m_ReadInputRegistersRequest.setWordCount(count);
	    m_Transaction.setRequest(m_ReadInputRegistersRequest);
	    m_Transaction.execute();
	    return ((ReadInputRegistersResponse) m_Transaction.getResponse()).getMessage();
	  }//readInputRegisters

	  /**
	   * Reads a given number of registers from the slave.
	   * <p/>
	   * Note that the number of registers returned (i.e. array length)
	   * will be according to the number received in the slave response.
	   *
	   * @param unitid the slave unit id.
	   * @param ref    the offset of the register to start reading from.
	   * @param count  the number of registers to be read.
	   * @return a <tt>Register[]</tt> holding the received registers.
	   * @throws ModbusException if an I/O error, a slave exception or
	   *                         a transaction error occurs.
	   */
	  public synchronized Register[] readMultipleRegisters(int unitid, int ref, int count)
	      throws ModbusException {
	    m_ReadMultipleRegistersRequest.setUnitID(unitid);
	    m_ReadMultipleRegistersRequest.setReference(ref);
	    m_ReadMultipleRegistersRequest.setWordCount(count);
	    m_Transaction.setRequest(m_ReadMultipleRegistersRequest);
	    m_Transaction.execute();
	    return ((ReadMultipleRegistersResponse) m_Transaction.getResponse()).getRegisters();
	  }//readMultipleRegisters
	  
	  /**
	   * Reads a given number of registers from the slave.
	   * <p/>
	   * Note that the number of registers returned (i.e. array length)
	   * will be according to the number received in the slave response.
	   *
	   * @param unitid the slave unit id.
	   * @param ref    the offset of the register to start reading from.
	   * @param count  the number of registers to be read.
	   * @return a <tt>byte[]</tt> holding the received registers in bytes.
	   * @throws ModbusException if an I/O error, a slave exception or
	   *                         a transaction error occurs.
	   */
	  public synchronized byte[] readMultipleRegisters2Bytes(int unitid, int ref, int count)
	      throws ModbusException {
	    m_ReadMultipleRegistersRequest.setUnitID(unitid);
	    m_ReadMultipleRegistersRequest.setReference(ref);
	    m_ReadMultipleRegistersRequest.setWordCount(count);
	    m_Transaction.setRequest(m_ReadMultipleRegistersRequest);
	    m_Transaction.execute();
	    return ((ReadMultipleRegistersResponse) m_Transaction.getResponse()).getMessage();
	  }//readMultipleRegisters

	  /**
	   * Writes a single register to the slave.
	   *
	   * @param unitid   the slave unit id.
	   * @param ref      the offset of the register to be written.
	   * @param register a <tt>Register</tt> holding the value of the register
	   *                 to be written.
	   * @throws ModbusException if an I/O error, a slave exception or
	   *                         a transaction error occurs.
	   */
	  public synchronized void writeSingleRegister(int unitid, int ref, Register register)
	      throws ModbusException {
	    m_WriteSingleRegisterRequest.setUnitID(unitid);
	    m_WriteSingleRegisterRequest.setReference(ref);
	    m_WriteSingleRegisterRequest.setRegister(register);
	    m_Transaction.setRequest(m_WriteSingleRegisterRequest);
	    m_Transaction.execute();
	  }//writeSingleRegister

	  /**
	   * Writes a number of registers to the slave.
	   *
	   * @param unitid    the slave unit id.
	   * @param ref       the offset of the register to start writing to.
	   * @param registers a <tt>Register[]</tt> holding the values of
	   *                  the registers to be written.
	   * @throws ModbusException if an I/O error, a slave exception or
	   *                         a transaction error occurs.
	   */
	  public synchronized void writeMultipleRegisters(int unitid, int ref, Register[] registers)
	      throws ModbusException {
	    m_WriteMultipleRegistersRequest.setUnitID(unitid);
	    m_WriteMultipleRegistersRequest.setReference(ref);
	    m_WriteMultipleRegistersRequest.setRegisters(registers);
	    m_Transaction.setRequest(m_WriteMultipleRegistersRequest);
	    m_Transaction.execute();
	  }//writeMultipleRegisters
	  
	  /**
	   * <code>setReceiveThreshold</code> set ReceiveThreshold for SerialPort.Only call this function after calling connect().
	   *
	   * @param th an <code>int</code> value
	   */
	  public void setReceiveThreshold(int th) {
		  m_ReceiveThreshold = th;
	  }
	  
	  /**
	   * Describe <code>setReceiveTimeout</code> set Receive Time Out for SerialPort.
	   *
	   * @param ms an <code>int</code> value
	   */
	  public void setReceiveTimeout(int ms) {
		  //m_Transaction.setReceiveTimeout(ms);
		  m_TimeOut = ms;
	  }
	  
	  /**
	   * <code>getErrorCode</code>.
	   * Get error code after executing request
	   * @return an <code>error code</code> value
	   */
	  public int getErrorCode() {
		  if(m_Transaction == null){
			  return -1;
		  }
		  return m_Transaction.getErrorCode();
	  }
	  
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
}
