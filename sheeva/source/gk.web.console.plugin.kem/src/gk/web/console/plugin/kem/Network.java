package gk.web.console.plugin.kem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import com.greenkoncepts.gateway.util.FuncUtil;

public class Network {
	public static boolean changeStaticIP(String ip,String submask,String gateway){
		System.out.println("Change static IP");
		try{
			Runtime r = Runtime.getRuntime();
			Process p = r.exec("sudo /home/gkadmin/network change "+ip+" "+submask+" "+gateway);
			p.waitFor();
			BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
	
			while ((line = b.readLine()) != null) {
				System.out.println(line);
			}
			DateFormat dateformat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss.SSS");
			String d = dateformat.format(new Date());
			System.out.print("Change static IP at:"+d);
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
	
	public static boolean configNetwork(boolean isStatic){
		System.out.println("Config Network");
		try{
			Runtime r = Runtime.getRuntime();
			int config = 0;
			if(isStatic){
				config = 1;
			}
			Process p = r.exec("sudo /home/gkadmin/network config "+config);
			p.waitFor();
			BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
	
			while ((line = b.readLine()) != null) {
				System.out.println(line);
			}
			DateFormat dateformat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss.SSS");
			String d = dateformat.format(new Date());
			String tmp = "dhcp";
			if(isStatic){
				tmp = "static";
			}
			System.out.print("Change to:"+tmp+" at:"+d);
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
	
	public static String findLocalIP(){
		String ip = null;
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
	    } catch (SocketException e) {
	        throw new RuntimeException(e);
	    }
	    System.out.println("IP:"+ip);
	    return ip;
	}
	
	public static String  findGateway(){
		System.out.println("find default gateway");
		try{
			Process result = Runtime.getRuntime().exec("/home/gkadmin/getdefaultgateway.sh");
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
			Process result = Runtime.getRuntime().exec("/home/gkadmin/getsubnet.sh");
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
	
	public static String findNetworkMode(){
		try{
			Process result = Runtime.getRuntime().exec("/home/gkadmin/checknetworkmode.sh");
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
	
	public static String en_XORCrypt(String str, String key) {
		if ((str != null) && (key != null)) {
			try {
				byte byteOfstr[] = str.getBytes("UTF-8");
				byte byteOfkey[] = key.getBytes("UTF-8");
				int n = byteOfstr.length;
				int m = byteOfkey.length;
				byte byteOfstrEncrypted[] = new byte[n];
				for (int i = 0; i < n; i++) {
					byteOfstrEncrypted[i] = (byte) (byteOfstr[i] ^ byteOfkey[i%m]);
									
				}
				
				StringBuffer hexString = new StringBuffer(byteOfstrEncrypted.length * 2);
				
				for (int i = 0; i < byteOfstrEncrypted.length; i++) {
					if (((int) byteOfstrEncrypted[i] & 0xff) < 0x10) {
						hexString.append("0");
					}
					hexString.append(Long.toString((int) byteOfstrEncrypted[i] & 0xff, 16));
				}
				
				return hexString.toString();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return "00";
	}
	
	public static String de_XORCrypt(String str, String key) {
		if ((str != null) && (key != null)) {
			try {
				int length = str.length();
				if ((length % 2) == 0) {
					byte byteOfkey[] = key.getBytes("UTF-8");
					int n = length/2;
					int m = byteOfkey.length;
					byte[] byteArray  = new byte[n];
					for (int i = 0; i < n; i++) {
						byteArray[i] = (byte) (Byte.parseByte(str.substring(2*i, 2*i + 2), 16) ^ byteOfkey[i % m]);
						
					}
					return new String(byteArray);

				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return "";
	}

}
