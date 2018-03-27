package gk.web.console.plugin.kem.file;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;


public class FileTail  {

	private String fileName;
	private int maxSize;
	private Queue<String> buffer = new LinkedList<String>();
	private long startTime;


	public FileTail(String fileName) throws IOException {
		this.fileName = new File(fileName).getCanonicalPath();
		maxSize = 1000;
		startTime = System.currentTimeMillis();
	}


	/**
	 * @return the fileName
	 * @throws IOException
	 */
	public String getFileName() {
		return fileName;
	}


	/**
	 * @see com.commsen.file.monitor.FileBuffer#getMaxSize()
	 */
	public int getMaxSize() {
		return this.maxSize;
	}


	/**
	 * @see com.commsen.file.monitor.FileBuffer#getSize()
	 */
	synchronized public int getSize() {
		return this.buffer.size();
	}


	/**
	 * @see com.commsen.file.monitor.FileBuffer#readLine()
	 */
	synchronized public String readLine() {
		return buffer.poll();	
	}


	/*
	 * (non-Javadoc)
	 * @see com.commsen.liferay.portlet.tailgate.FileObserver#addLine(java.lang.String)
	 */
	synchronized public boolean addLine(final String line) {
		boolean result;
		result = buffer.add(line);
		
		while (buffer.size() > maxSize) {
			buffer.remove();
		}
		return result;
	}


	public void setStartTime(long time) {
		startTime = time;
	}


	
	public long getStartTime() {
		return startTime;
	}

}
