package gk.web.console.plugin.kem.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;


public class FileMonitor implements Runnable {

	private static final Logger LOG = Logger.getLogger(FileMonitor.class.getName());
	private String NAMELOG;
	private Hashtable<Integer,FileTail> logObservers = new Hashtable<Integer,FileTail>();
	private File file;
	private RandomAccessFile raf;


	/**
     * 
     */
	FileMonitor(final String fileName) {
		if (fileName == null) {
			throw new IllegalArgumentException("File name is null");
		}
		this.file = new File(fileName);
		if (file.exists() && file.isDirectory()) {
			throw new IllegalArgumentException("File " + file.getAbsolutePath() + " is directory!");
		}
		NAMELOG = fileName;
		raf = null;	
	}


	/**
	 * @return the file
	 * @throws IOException
	 */
	public String getFileName() {
		String filename = null;
		try {
			filename = this.file.getCanonicalPath();
		} catch (IOException e) {
			LOG.log(Level.WARNING, "Failed to get canonical name!", e);
		}
		return filename;
	}


	synchronized public int createNewObserver() {
		FileTail logObserver;
		int hashCode = -1;
		try {
			logObserver = new FileTail(NAMELOG);
			hashCode = logObserver.hashCode();
			logObservers.put(Integer.valueOf(hashCode), logObserver);
			if (raf == null) {
				raf = new RandomAccessFile(file, "r");
				raf.seek(file.length());
			}
			notify();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hashCode;
	}


	synchronized public FileTail removeObserver(int hashCode) {
		return logObservers.remove(Integer.valueOf(hashCode));
	}
	
	synchronized public String readLineFromObserver(int hashCode) {
		String line = null;
		if (logObservers.containsKey(Integer.valueOf(hashCode))) {
			line = logObservers.get(Integer.valueOf(hashCode)).readLine();	
		}
		return line;
	}
	
	synchronized public void updateStartTime(int hashCode) {
		if (logObservers.containsKey(Integer.valueOf(hashCode))) {
			logObservers.get(hashCode).setStartTime(System.currentTimeMillis());
		}
	}
	
	private void sleep(final long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			// wake up
		}
	}


	synchronized private boolean haveObservers() {
		try {
			if (logObservers.isEmpty()) {
				System.out.println("-----haveObservers : logObservers is empty");
				raf = null;
				wait();	
			}
			return true;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
	}


	synchronized private void cleanObserverTimeOut() {
		long currentTime = System.currentTimeMillis();
		for (Integer hashCode : logObservers.keySet()) {
			if ((currentTime - logObservers.get(hashCode).getStartTime()) > 15000) {
				System.out.println("-----cleanObserverTimeOut : delete hashcode = " + hashCode);
				logObservers.remove(hashCode);
			}
		}
	}
	synchronized private void readAndUpdateObservers() {
		try {
			long fileLength = file.length();
			if (fileLength > raf.getFilePointer()) {
				String line;
				int count = 0; //only update max 150 lines per very time
				while (((line = raf.readLine()) != null) && (count < 150)) {
					for (Integer hashCode : logObservers.keySet()) {	
						logObservers.get(hashCode).addLine(line);	
					}
					count++;
				}
			} else if (fileLength < raf.getFilePointer()) {
				raf.seek(fileLength);
			}
		} catch (IOException e) {
			LOG.log(Level.WARNING, "File not found", e);
			return;
		}
	}


	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while (!file.exists()) {
			sleep(100);
		}

		while (!Thread.interrupted() && haveObservers()) {
			cleanObserverTimeOut();
			readAndUpdateObservers();
			sleep(100);
		}
	}
}
