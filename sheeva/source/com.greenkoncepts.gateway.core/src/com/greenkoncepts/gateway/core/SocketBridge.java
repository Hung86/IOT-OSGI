package com.greenkoncepts.gateway.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.greenkoncepts.gateway.api.adapter.Adapter;
import com.greenkoncepts.gateway.watchdog.DeadlockDetector;

public class SocketBridge extends Bridge {

	private boolean isFirstConnect = true;

	ScheduledExecutorService processServerThread = Executors.newSingleThreadScheduledExecutor();
	ScheduledExecutorService socketThreads = Executors.newScheduledThreadPool(1);

	private Socket socketClient;
	private BufferedReader bufferReader;
	private PrintWriter pwriter;

	int totaldevice = 0;

	SocketBridge(ArrayList<Adapter> adapters,
			ArrayList<DeadlockDetector> deadlockDetector) {
		super(adapters, deadlockDetector, SOCKET);
		remoteHost = "test.greenkoncepts.com";
		remotePort = 4548;
	}

	// Thread threadSocket;
	@Override
	public void start() {
		final Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		delay_start_query = delay_start_query + (60 - calendar.get(Calendar.SECOND));
		mLogger.info("------delay_start_query = " + delay_start_query);
		mLogger.info("------querying will start at time : minute=" + calendar.get(Calendar.MINUTE) + "; second=" + calendar.get(Calendar.SECOND));

		socketThreads.scheduleAtFixedRate(new Runnable(){
			@Override
			public void run() {
				doQueryingDevice();
			}		
		}
		, delay_start_query, tickPeriod_query, TimeUnit.SECONDS);
		
		socketThreads.scheduleAtFixedRate(new Runnable(){
			@Override
			public void run() {
				doBufferingData();
				doCheckingStatus();
			}		
		}
		, delay_start_internet, tickPeriod_status, TimeUnit.SECONDS);
		
		processServerThread.execute(new Runnable(){
			@Override
			public void run() {
				dataSocketHandler();
			}		
		});
		
		mLogger.info("SocketBridge is started !");
	}

	@Override
	public synchronized void stop() {
		socketThreads.shutdownNow();
		processServerThread.shutdownNow();
		closeSocket();
		mLogger.info("SocketBridge is stop !");
	}


	public void initSettings(Map<String, String> settings) {
		try {
			super.initSettings(settings);
			customerId = settings.get("customer_id").trim();
			gatewayId = settings.get("gateway_id").trim();
			remoteHost = settings.get("remote_host").trim();
			remotePort = Integer.parseInt(settings.get("remote_port").trim());
			protocolVer = settings.get("protocol_version").trim();
			tickPeriod_query = Integer.parseInt(settings.get("check_period_query").trim());
			//tickPeriod_internet = Integer.parseInt(settings.get("check_period_internet").trim());
			delay_start_query = Integer.parseInt(settings.get("delay_start_query").trim());
			//delay_start_internet = Integer.parseInt(settings.get("delay_start_internet").trim());
			bridgeErrorMax = Integer.parseInt(settings.get("socket_err_max").trim());
			bufferSendLimit = Integer.parseInt(settings.get("buffer_send_limit").trim());
		} catch (Exception e) {
			mLogger.error("Exception", e);
		}

	}

	private boolean createSocket() {
		mLogger.info("create Socket to " + remoteHost + ", port " + remotePort);
		if (socketClient != null) {
			mLogger.warn("Socket already connects");
			return true;
		}
		try {
			socketClient = new Socket(remoteHost, remotePort);
			pwriter = new PrintWriter(socketClient.getOutputStream(), true);
			bufferReader = new BufferedReader(new InputStreamReader(socketClient.getInputStream(), "UTF8"), 100*1024);
			mLogger.info("[createSocket] ... Notify Socket Client Thread !");
			this.notify();
			return true;
		} catch (IOException e) {
			mLogger.error("IOException", e);
		} catch (Exception e) {
			mLogger.error("Exception", e);
		}

		mLogger.error("Fail to connect to " + remoteHost + ":" + remotePort);
		return false;

	}

	private void closeSocket() {
		mLogger.info("close Socket !");
		try {
			if (pwriter != null) {
				pwriter.close();
				pwriter = null;
			}
			if (bufferReader != null) {
				bufferReader.close();
				bufferReader = null;
			}
			if (socketClient != null) {
				socketClient.close();
				socketClient = null;
			}
		} catch (IOException e) {
			mLogger.error("IOException", e);
		} catch (Exception e) {
			mLogger.error("Exception", e);
		}
		mLogger.info("Finish closeSocket");

	}

	public boolean sendMessage(String msg) {
		try {
			pwriter.print(msg);
			if (pwriter.checkError())
			{
				mLogger.warn("Can't send message,increase error to #" + bridgeError);
			} else {
				mLogger.info("Sent message: " + msg);
				bridgeError = 0;
				return true;
			}
		} catch (Exception e) {
			mLogger.warn("Can't send message #" + bridgeError);
			mLogger.error("Exception", e);
		}
		bridgeError++;
		bridgeErrorPerMinute++;
		return false;

	}


	private void dataSocketHandler() {
		mLogger.info("...start dataSocketHandler thread");
		String str;
		long sleepingTime = 1000;
		while (true) {
			try {
				synchronized (this) {
					if ((socketClient == null) || (!socketClient.isConnected())) {
						mLogger.info("[dataSocketHandler] ... waiting Socket Client !");
						this.wait();
					}
				}
				mLogger.info("... read socket ");
				if ((str = bufferReader.readLine()) != null) {
					bridgeError = 0;
					processMessage(str);
					sleepingTime = 100;
				} else {
					bridgeError++;
					sleepingTime = 2000;
				}
				mLogger.info("... read socket : read request from server is "
						+ str);

			} catch (IOException e) {
				mLogger.error("run--readLine()--IOException", e);
			} catch (Exception e) {
				mLogger.error("run--readLine()--Exception", e);
			}

			try {
				Thread.sleep(sleepingTime);
			} catch (InterruptedException e) {
				mLogger.error("InterruptedException", e);
			}
		}
	}

	void processMessage(String msg) {
		mLogger.info("processMessage");
		// Metadata request
		if (msg.equals("M")) {
			mLogger.debug("Metadata request from server. ");
			doSendingMetadata();
		}
		// Data request
		else if (msg.equals("P")) {
			mLogger.debug("Data request from server. ");
			doSendingData();
		}
		// Upgrade request
		else if (msg.equals("U")) {
			mLogger.debug("Upgrade request from server. ");
		}
		// Heart beat request
		else if (msg.equals("*")) {
			mLogger.debug("Heart beat request from server. ");
			// response heart beat request from the server
			sendMessage("*\n");
		}
		// Time adjust request
		else if (msg.equals("T")) {
			mLogger.debug("Time calibrate request from server. ");
		}
		else {
			mLogger.debug("Unknown message (" + msg + ") from server. ");
		}
	}
	

	@Override
	public synchronized void checkBridgeStatus() {
		try {
			if (socketClient == null)
			{
				if (isFirstConnect) {
					mLogger.info("Checking Connection ... The first time to connect (" + remoteHost + ")");
					isFirstConnect = false;
				}
				else {
					mLogger.warn("Checking Connection ... not connected! Try to reconnect.");
					closeSocket();
				}
				createSocket();
			}
			else if (bridgeError >= bridgeErrorMax) {
				mLogger.warn("Checking Connection ... too many error! Try to reconnect. socketErr = " + bridgeError);
				closeSocket();
				createSocket();
			}
			else {
				mLogger.info("Checking Connection (" + remoteHost + ") ... " + (bridgeError == 0 ? "OK" : bridgeError));
				bridgeError++;
			}
		} catch (Exception e) {
			mLogger.error("Exception", e);
		}
	}
	
	@Override
	public boolean adapterSendDeviceState(String adapter, String data) {
		mLogger.info("[adapterSendStateChange] Socket doesn't support this method");
		return false;

	}

	@Override
	public boolean adapterSendMetaData(String Adapter, String Data) {
		mLogger.info("[adapterSendMetaData] Socket doesn't support this method");
		return false;

	}

	@Override
	public boolean adapterSendData(String Adapter, String Data) {
		mLogger.info("[adapterSendData] Socket doesn't support this method");
		return false;

	}
}
