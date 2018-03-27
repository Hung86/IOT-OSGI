////License
///***
// * Java Modbus Library (jamod)
// * Copyright (c) 2002-2004, jamod development team
// * All rights reserved.
// *
// * Redistribution and use in source and binary forms, with or without
// * modification, are permitted provided that the following conditions are
// * met:
// *
// * Redistributions of source code must retain the above copyright notice,
// * this list of conditions and the following disclaimer.
// *
// * Redistributions in binary form must reproduce the above copyright notice,
// * this list of conditions and the following disclaimer in the documentation
// * and/or other materials provided with the distribution.
// *
// * Neither the name of the author nor the names of its contributors
// * may be used to endorse or promote products derived from this software
// * without specific prior written permission.
// *
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS ``AS
// * IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
// * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
// * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE
// * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// * POSSIBILITY OF SUCH DAMAGE.
// ***/
//package com.ghgande.j2mod.modbus.cmd;
//
//import com.ghgande.j2mod.modbus.ModbusException;
//import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
//import com.ghgande.j2mod.modbus.procimg.Register;
//
///**
// * <p>
// * Class that implements a simple commandline tool which demonstrates how a
// * analog input can be bound with a analog output.
// * 
// * <p>
// * Note that if you write to a remote I/O with a Modbus protocol stack, it will
// * most likely expect that the communication is <i>kept alive</i> after the
// * first write message.
// * 
// * <p>
// * This can be achieved either by sending any kind of message, or by repeating
// * the write message within a given period of time.
// * 
// * <p>
// * If the time period is exceeded, then the device might react by turning out
// * all signals of the I/O modules. After this timeout, the device might require
// * a reset message.
// * 
// * @author Dieter Wimberger
// * @version 1.2rc1 (09/11/2004)
// */
//public class AIAOTest {
//
//	public static void main(String[] args) {
//		System.out.println("-------------------- START");
//		ModbusTCPMaster master = new ModbusTCPMaster("192.168.1.243", 502);
//		Register[] re = null;
//		try {
//			master.connect();
//			//master.
//			re = master.readMultipleRegisters(5, 2);
//
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println("-------------------- FINISH : re " + re[1].getValue());
//	}
//
//
//}
