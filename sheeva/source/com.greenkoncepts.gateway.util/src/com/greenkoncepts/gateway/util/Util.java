package com.greenkoncepts.gateway.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class Util {

	static String directory = "script/";
//	String ValidIpAddressRegex = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
//	String ValidHostnameRegex = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9])$";
	//Network
	private static final Pattern IP_V4_PATTERN = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
	//private static final Pattern SERVER_NAME = Pattern.compile("^(?=.{1,255}$)[0-9A-Za-z](?:(?:[0-9A-Za-z]|-){0,61}[0-9A-Za-z])?(?:\\.[0-9A-Za-z](?:(?:[0-9A-Za-z]|-){0,61}[0-9A-Za-z])?)*\\.?$");
	/**
	 * ����Double�����
	 * @param v1
	 * @param v2
	 * @return Double
	 */
	public static double sub(double v1,double v2){
	    BigDecimal b1 = new BigDecimal(v1);
	    BigDecimal b2 = new BigDecimal(v2);
	    return b1.subtract(b2).doubleValue();
	}
	
	// ����һ���ֽڵ�ʮ�������ַ���
	  static String hexByte(byte b) {
	    // String s = "000000" + Integer.toHexString(b);
	    // return s.substring(s.length() - 2);
	    return String.format("%02x",b);
	  }
	
	// Get the first available Mac Address
	public static String getMac() {
		byte[] mac = { 0 };
		try {
			if (osName().equals("window")) {
				Enumeration<NetworkInterface> win = NetworkInterface.getNetworkInterfaces();
				mac = win.nextElement().getHardwareAddress();
			} else {
				NetworkInterface eth0 = NetworkInterface.getByName("eth0");
				if (eth0 == null) {
					eth0 = NetworkInterface.getByName("enp1s0");
				}
				if(eth0 != null) {
					mac = eth0.getHardwareAddress();
				}

			}
		} catch (SocketException e) {
			e.printStackTrace();
		}

		StringBuilder builder = new StringBuilder();
		for (byte b : mac) {
			builder.append(hexByte(b));
		}
		return builder.toString().toUpperCase();

	}
	
	

	/**
	 * Get MAC address by parsing returned data from script bash(Windows/Linux)
	 * @return MAC address in string
	 * @throws IOException
	 */
	public static String getMacByScript() throws IOException {
	     String os = System.getProperty("os.name");

	     try {
	          if(os.startsWith("Windows")) {
	               return windowsParseMacAddress(windowsRunIpConfigCommand());
	          } else if(os.startsWith("Linux")) {
	               return linuxParseMacAddress(linuxRunIfConfigCommand());
	          } else {
	               throw new IOException("unknown operating system: " + os);
	          }
	     } catch(ParseException ex) {
	          ex.printStackTrace();
	          throw new IOException(ex.getMessage());
	     }
	}

	
	public static String osName() {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win")) {
			return "window";
		} else if (os.contains("linux")) {
			return "linux";
		}
		return "unknown os";
	}

	/*
	 * Linux stuff
	 */
	private final static String linuxParseMacAddress(String ipConfigResponse) throws ParseException {
	     String localHost = null;
	     try {
	          localHost = InetAddress.getLocalHost().getHostAddress();
	     } catch(java.net.UnknownHostException ex) {
	          ex.printStackTrace();
	          throw new ParseException(ex.getMessage(), 0);
	     }

	     StringTokenizer tokenizer = new StringTokenizer(ipConfigResponse, "\n");
	     String lastMacAddress = null;

	     while(tokenizer.hasMoreTokens()) {
	          String line = tokenizer.nextToken().trim();
	          boolean containsLocalHost = line.indexOf(localHost) >= 0;

	          // see if line contains IP address
	          if(containsLocalHost && lastMacAddress != null) {
	        	  lastMacAddress = RemoveCharacters(lastMacAddress,':');
	        	  lastMacAddress = lastMacAddress.toUpperCase();
	        	  return lastMacAddress;
	          }

	          // see if line contains MAC address
	          int macAddressPosition = line.indexOf("HWaddr");
	          if(macAddressPosition <= 0) continue;

	          String macAddressCandidate = line.substring(macAddressPosition + 6).trim();
	          if(linuxIsMacAddress(macAddressCandidate)) {
	               lastMacAddress = macAddressCandidate;
	               continue;
	          }
	     }

	     ParseException ex = new ParseException
	          ("cannot read MAC address for " + localHost + " from [" + ipConfigResponse + "]", 0);
	     ex.printStackTrace();
	     throw ex;
	}


	private final static boolean linuxIsMacAddress(String macAddressCandidate) {
	     // TODO: use a smart regular expression
	     if(macAddressCandidate.length() != 17) return false;
	     return true;
	}


	private final static String linuxRunIfConfigCommand() throws IOException {
	     Process p = Runtime.getRuntime().exec("ifconfig");
	     InputStream stdoutStream = new BufferedInputStream(p.getInputStream());

	     StringBuffer buffer= new StringBuffer();
	     for (;;) {
	          int c = stdoutStream.read();
	          if (c == -1) break;
	          buffer.append((char)c);
	     }
	     String outputText = buffer.toString();

	     stdoutStream.close();

	     return outputText;
	}



	/*
	 * Windows stuff
	 */
	private final static String windowsParseMacAddress(String ipConfigResponse) throws ParseException {
	     String localHost = null;
	     try {
	          localHost = InetAddress.getLocalHost().getHostAddress();
	     } catch(java.net.UnknownHostException ex) {
	          ex.printStackTrace();
	          throw new ParseException(ex.getMessage(), 0);
	     }

	     StringTokenizer tokenizer = new StringTokenizer(ipConfigResponse, "\n");
	     String lastMacAddress = null;

	     while(tokenizer.hasMoreTokens()) {
	          String line = tokenizer.nextToken().trim();

	          // see if line contains IP address
	          if(line.endsWith(localHost) && lastMacAddress != null) {
	               return lastMacAddress;
	          }

	          // see if line contains MAC address
	          int macAddressPosition = line.indexOf(":");
	          if(macAddressPosition <= 0) continue;

	          String macAddressCandidate = line.substring(macAddressPosition + 1).trim();
	          if(windowsIsMacAddress(macAddressCandidate)) {
	               lastMacAddress = macAddressCandidate;
	               continue;
	          }
	     }

	     ParseException ex = new ParseException("cannot read MAC address from [" + ipConfigResponse + "]", 0);
	     ex.printStackTrace();
	     throw ex;
	}


	private final static boolean windowsIsMacAddress(String macAddressCandidate) {     
	     if(macAddressCandidate.length() != 17) return false;
	     return true;
	}


	private final static String windowsRunIpConfigCommand() throws IOException {
	     Process p = Runtime.getRuntime().exec("ipconfig /all");
	     InputStream stdoutStream = new BufferedInputStream(p.getInputStream());

	     StringBuffer buffer= new StringBuffer();
	     for (;;) {
	          int c = stdoutStream.read();
	          if (c == -1) break;
	          buffer.append((char)c);
	     }
	     String outputText = buffer.toString();

	     stdoutStream.close();

	     return outputText;
	}
	
	/**
	 * Remove character in string
	 * @param str: input string
	 * @param rpl: remove character
	 * @return
	 */
	public static String RemoveCharacters(String str,char rpl) {
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<str.length();i++) {
			char ch = str.charAt(i);
			if (ch != rpl)
				sb.append(ch);
	    }
	    return sb.toString();
	}
	
	/**
	 * Reboot Greenkoncepts Java Application
	 */
	public static void RebootJVM(){
		System.out.println("Reboot JVM");
		try{
			Runtime r = Runtime.getRuntime();
			Process p = r.exec("sudo " + directory + "rebootJVM.sh");
			p.waitFor();
			//BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
			//String line = "";
	
			//while ((line = b.readLine()) != null) {
				//log(GWLogger.INFO, line);
			  //System.out.println(line);
			//}
		}catch(IOException e){
			System.out.println("Fail to reboot JVM");
		}catch(InterruptedException e){
			System.out.println("Fail to reboot JVM");
		}catch(Exception e){
			System.out.println("Fail to reboot JVM");
		}
	}
	
	public static void RebootOs(){
		System.out.println("Reboot OS");
		String os = System.getProperty("os.name").toLowerCase();
		if(os.indexOf("win") >= 0) {
			System.out.println("not handle reboot OS on Windows");
		} else if((os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") > 0 )) {
			try{
				Runtime r = Runtime.getRuntime();
				Process p = r.exec("sudo reboot");
				p.waitFor();
				BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = "";
				while ((line = b.readLine()) != null) {
				  //System.out.println(line);
				}
			}catch(IOException e){
				System.out.println("Fail to reboot OS");
			}catch(InterruptedException e){
				System.out.println("Fail to reboot OS");
			}catch(Exception e){
				System.out.println("Fail to reboot OS");
			}
		}
	}
	
	public static boolean changeStaticSettings(Map<String, String> netInfo) {
		System.out.println("Change static IP");
		try {
			String ip = netInfo.get("ip");
			String subnet = netInfo.get("subnet");
			String gateway = netInfo.get("gateway");
			String dns= netInfo.get("dns1");
			if ("".equals(dns)) {
				dns= netInfo.get("dns2");
			} else {
				if (!"".equals(netInfo.get("dns2"))) {
					dns = dns + "," + netInfo.get("dns2");
				}
			}
			Runtime r = Runtime.getRuntime();
			System.out.println("----------------dns = " + dns);
			Process p = r.exec("sudo " + directory + "network.sh change " + ip + " " + subnet + " " + gateway + " " + dns);
			p.waitFor();
			BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";

			while ((line = b.readLine()) != null) {
				System.out.println(line);
			}
			DateFormat dateformat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss.SSS");
			String d = dateformat.format(new Date());
			System.out.println("Changed static IP at:" + d);
			return true;
		} catch (IOException e) {
			System.out.println(e);
		} catch (InterruptedException e) {
			System.out.println(e);
		} catch (Exception e) {
			System.out.println(e);
		}
		return false;
	}

	public static boolean configNetwork(String mode){
		System.out.println("Config Network");
		try{
			Runtime r = Runtime.getRuntime();
			Process p = r.exec("sudo "+directory+"network.sh config "+ mode);
			p.waitFor();
			BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			while ((line = b.readLine()) != null) {
				System.out.println(line);
			}
			DateFormat dateformat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss.SSS");
			String d = dateformat.format(new Date());
			System.out.println("Change to:"+mode+" at:"+d);
			return true;
		}catch(IOException e){
			System.out.println(e);
		}catch(InterruptedException e){
			System.out.println(e);
		}catch(Exception e){
			System.out.println(e);
		}
		return false;
	}
	
	public static boolean restartNetwork(){
		System.out.println("Config Network");
		try{
			Runtime r = Runtime.getRuntime();
			Process p = r.exec("sudo "+directory+"network.sh restart");
			p.waitFor();
			BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			while ((line = b.readLine()) != null) {
				System.out.println(line);
			}
			DateFormat dateformat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss.SSS");
			String d = dateformat.format(new Date());
			System.out.println("finished restart networking");
			return true;
		}catch(IOException e){
			System.out.println(e);
		}catch(InterruptedException e){
			System.out.println(e);
		}catch(Exception e){
			System.out.println(e);
		}
		return false;
	}
	
	public static boolean setNtpServer(String server1, String server2) {
		try{
			String ntpServer = server1;
			if ("".equals(server1)) {
				ntpServer = server2;
			} else {
				if (!"".equals(server2)) {
					ntpServer = ntpServer + "," + server2;
				}
			}
			
			String cmdline = "sudo " + directory + "setntp.sh" + " " + ntpServer;
			System.out.println(cmdline);
			Process result = Runtime.getRuntime().exec(cmdline);
			result.waitFor();
			BufferedReader br = new BufferedReader(new InputStreamReader(result.getInputStream()));
			String output = "", line = "";
			while((line = br.readLine()) != null) {
				output += line;
			}
			System.out.println(output);
	        return true;
		}catch(IOException e){
			System.out.println(e);
		}catch(Exception e){
			System.out.println(e);
		}
		return false;		
	}
	
	public static String findLocalIP(){
		String ip = "";
	    try {
	        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
	        while (interfaces.hasMoreElements()) {
	            NetworkInterface iface = interfaces.nextElement();
	            // filters out 127.0.0.1 and inactive interfaces
	            if (iface.isLoopback() || !iface.isUp())
	                continue;

	            Enumeration<InetAddress> addresses = iface.getInetAddresses();
	            while(addresses.hasMoreElements()) {
	                InetAddress addr = addresses.nextElement();
	                ip = addr.getHostAddress();
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    System.out.println("IP:"+ip);
	    return ip;
	}
	
	public static String  findGateway(){
		System.out.println("find default gateway");
		try{
			Process result = Runtime.getRuntime().exec(directory+"getdefaultgateway.sh");
	        BufferedReader br = new BufferedReader(new InputStreamReader(result.getInputStream()));
	        String output = "", line = "";
	        while((line = br.readLine()) != null) {
	            output += line;
	        }
	        System.out.println(output);
	        return output;
		}catch(IOException e){
			System.out.println(e);
		}catch(Exception e){
			System.out.println(e);
		}
		return null;
	}
	
	public static String findSubnet(){
		try{
			Process result = Runtime.getRuntime().exec(directory+"getsubnet.sh");
	        BufferedReader br = new BufferedReader(new InputStreamReader(result.getInputStream()));
	        String output = "", line = "";
	        while((line = br.readLine()) != null) {
	            output += line;
	        }
	        System.out.println(output);
	        return output;
		}catch(IOException e){
			System.out.println(e);
		}catch(Exception e){
			System.out.println(e);
		}
		return null;
	}

	public static ArrayList<String> findDns(){
        ArrayList<String> output = new ArrayList<String>();
		try{
			Process result = Runtime.getRuntime().exec(directory+"getdns.sh");
	        BufferedReader br = new BufferedReader(new InputStreamReader(result.getInputStream()));
	        String line = "";
	        while((line = br.readLine()) != null) {
	        	output.add(line);
	        }
		}catch(IOException e){
			System.out.println(e);
		}catch(Exception e){
			System.out.println(e);
		}
		return output;
	}

	public static String findNetworkMode(){
		try{
			Process result = Runtime.getRuntime().exec(directory+"checknetworkmode.sh");
	        BufferedReader br = new BufferedReader(new InputStreamReader(result.getInputStream()));
	        String output = "", line = "";
	        while((line = br.readLine()) != null) {
	            output += line;
	        }
	        System.out.println(output);
	        return output;
		}catch(IOException e){
			System.out.println(e);
		}catch(Exception e){
			System.out.println(e);
		}
		return null;
	}
	
	public static void setPermissions() throws Exception {
		Process procChmod = null;
		//Process procDos = null;
		
		try {
			procChmod = Runtime.getRuntime().exec("chmod 700 -R" + directory);
			procChmod.waitFor();
			
			//procDos = Runtime.getRuntime().exec("dos2unix " + m_scriptFile.toString());
			//procDos.waitFor();
		} catch (Exception e) {
			throw e;
		} finally {
			//ProcessUtil.destroy(procDos);
			ProcessUtil.destroy(procChmod);
		}
		
	}

	public static ArrayList<String> findNtpServer() {
        ArrayList<String> output = new ArrayList<String>();
		try{
			Process result = Runtime.getRuntime().exec(directory + "getntp.sh");
	        BufferedReader br = new BufferedReader(new InputStreamReader(result.getInputStream()));
			String line = "";
	        while((line = br.readLine()) != null) {
	        	if (!line.contains(">")) {
	        		output.add(line);
	        	}
	        }
		}catch(IOException e){
			System.out.println(e);
		}catch(Exception e){
			System.out.println(e);
		}
		return output;
	}
	
	public static Hashtable<String, String> getInfoHardware(String parameter){
		try{
			Process result = Runtime.getRuntime().exec(directory+"getInfoHW.sh " + parameter);
	        BufferedReader br = new BufferedReader(new InputStreamReader(result.getInputStream()));
	        String line = "";
	        String[] item ;
	        Hashtable<String, String> output = new Hashtable<String, String>();
	        while((line = br.readLine()) != null) {
	        	System.out.println("--------------------------line = " + line);
	        	item = line.split(":");
	        	if ((item != null) && (item.length == 2)) { 
	        		output.put(item[0].trim(), item[1].trim().replace(",", "."));
	        	}
	        }
	        return output;
		}catch(IOException e){
			System.out.println(e);
		}catch(Exception e){
			System.out.println(e);
		}
		return null;
	}
	
	public static boolean checkExternalStoreAlive(String pathName){
		pathName = pathName.endsWith("/") ? pathName : pathName+"/";
		File tmpPath = new File(pathName + "testExternalStore");
		if (tmpPath.mkdir()) {
    		tmpPath.delete();
    		return true;
    	}
		return false;
	}
	
	public static Properties getPropertiesFile(File _configFile) {
		Properties _props = new Properties();
		FileInputStream in = null;
		try {
			in = new FileInputStream(_configFile);
			_props.load(in);
		} catch (FileNotFoundException e) {
			System.out.println("Can't find configuration file: " + _configFile );
		} catch (IOException e) {
			System.out.println("Read failure for configuration file: " + _configFile);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
		
		return _props;
	}

	
	public static Properties getPropertiesFile(String _configFile) {
		Properties _props = new Properties();
		FileInputStream in = null;
		try {
			in = new FileInputStream(_configFile);
			_props.load(in);
		} catch (FileNotFoundException e) {
			System.out.println("Can't find configuration file: " + _configFile );
		} catch (IOException e) {
			System.out.println("Read failure for configuration file: " + _configFile);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return _props;
	}
	
	public static boolean setPropertiesFile(String _configFile, Properties _properties) {
		FileOutputStream fos = null;
		boolean ok = false;
		try {
			File file = new File(_configFile);
			file.createNewFile();
			file.setReadable(true, false);
			file.setWritable(true, false);
			fos = new FileOutputStream(file);
			_properties.store(fos, "Configuration file");
			ok = true;
		} catch (Exception e1) {
			System.out.println("Fail to create a new configuration file: " + _configFile);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return ok;
	}

	public static double getDoubleValueOf(String s) {
		try {
			return Double.parseDouble(s);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public static int getIntValueOf(String s) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public static boolean syncClock()
	{
		try{
			Process result = Runtime.getRuntime().exec(directory+"syncServerClock.sh");
			result.waitFor();
	        BufferedReader br = new BufferedReader(new InputStreamReader(result.getInputStream()));
	        String output = "", line = "";
	        while((line = br.readLine()) != null) {
	            output += line;
	        }
	        System.out.println("Synchronized clock : output = " + output);
	        return true;
		}catch(IOException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
}

