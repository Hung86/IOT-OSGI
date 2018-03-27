package com.greenkoncepts.gateway.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greenkoncepts.gateway.util.Util;

public class DataBuffer {
	public static int RAMBUFF_MAX_SIZE 		= 500000;//Max 500KB
	public final static String BUFFER_PROP  = "buffer.prop";
	public final static String NAND_STORAGE = "buffer/";
	public final static String SD_STORAGE 	= "buffer_SD/";
	public final static int VALID_MINIMUM_NAND = 90;//MB , change for sheevaplug
	public final static int VALID_MINIMUM_SD = 10;//MB

	private Logger mLogger;
	private int currIndex = -1;
	private int maxIndex = -1;
	private int bufferSize = 0;
	public DataBuffer() {
		mLogger = LoggerFactory.getLogger(getClass().getSimpleName());
		initialize();
	}


	private void initialize() {
		try {
			File nandStorage = new File(NAND_STORAGE);
			if (!nandStorage.exists()) {
				nandStorage.mkdirs();
				mLogger.info("[initialize] ... create NAND buffer folder ");
			} else {
				mLogger.info("[initialize] ... NAND buffer folder already exists ");
			}

			getBufferConfig();
		} catch (Exception e) {
			mLogger.error("Exception", e);
		}
	}

	private void getBufferConfig() {
		Properties _props = Util.getPropertiesFile(BUFFER_PROP);
		if (!_props.isEmpty()) {
			currIndex = Integer.parseInt(_props.getProperty("current_index","-1"));
			maxIndex = Integer.parseInt(_props.getProperty("max_index","-1"));
			bufferSize = Integer.parseInt(_props.getProperty("buffer_size","0"));
			mLogger.info("getBufferConfig : current Index=" + currIndex + " & buffer Size=" + bufferSize);			
		} else {
			mLogger.warn("Can't find configuration file: " + BUFFER_PROP + ", Try to create a default one.");
			setBufferConfig();
		}
	}
	
	
	private void setBufferConfig() {
    	mLogger.debug( "setBufferConfig : current Index=" + currIndex + " & buffer Size=" + bufferSize);
    	Properties _props = new Properties();
    	_props.setProperty("current_index", String.valueOf(currIndex));
    	_props.setProperty("max_index", String.valueOf(maxIndex));
	    _props.setProperty("buffer_size", String.valueOf(bufferSize));
	    Util.setPropertiesFile(BUFFER_PROP, _props);
    }
	
	public ArrayList<String> loadCurrentBuffer() {
		ArrayList<String> msg = loadCurrentBuffer(NAND_STORAGE);
		if (msg.isEmpty()) {
			msg = loadCurrentBuffer(SD_STORAGE);
		}
		return msg;
	}

	public ArrayList<String> loadCurrentBuffer(String location) {
		String bufferPath = location + "data_" + currIndex + ".buf";
		ArrayList<String> msg = new ArrayList<String>();
		File bufferFile = new File(bufferPath);
		if (bufferFile.exists()) {
			mLogger.info("loadCurrentBuffer :  " + bufferPath);
			try {
				String str = "";
				BufferedReader bufIn = new BufferedReader(new FileReader(bufferFile));
				while (((str = bufIn.readLine()) != null) && (!str.trim().equals(""))) {
					msg.add(str);
				}
				bufIn.close();
				deleteCurrentBuffer(location);
			} catch (Exception e) {
				mLogger.error("Exception", e);
			}
		}
		return msg;
	} 
	
	public void deleteCurrentBuffer(String location) {
		String bufferPath = location + "data_" + currIndex + ".buf";
		mLogger.info("deleteCurrentBuffer :  " + bufferPath);
		currIndex--;
		bufferSize--;
		if (currIndex < 0) {
			if (bufferSize > 0) {
				currIndex = maxIndex;
			} else {
				maxIndex = currIndex;
			}
		}
		setBufferConfig();
		new File(bufferPath).delete();
	}
	
	public void deleteAllBuffer(String location) {
		String bufferPath = location + "data_" + currIndex + ".buf";
		mLogger.info("deleteAllBuffer :  " + bufferPath);
		File file = new File(bufferPath);
		while(file.exists()) {
			deleteCurrentBuffer(location);
			bufferPath = location + "data_" + currIndex + ".buf";
			file = new File(bufferPath);
		}
		
	}
	
	public void saveBufferData(ArrayList<String> bufferArray, boolean append) {
		String prefix = NAND_STORAGE;
		boolean hasSave = false;
		int index = currIndex;
		if (!append || (bufferSize <= 0)) {
			index++;
			hasSave = true;
		}
		if (index >= bufferSize) {
			if (usableSpaceNand() > 0) {
				bufferSize++;
				maxIndex = index;
				prefix = NAND_STORAGE;
			} else if (usableSpaceSdCard() > 0) {
				bufferSize++;
				maxIndex = index;
				prefix = SD_STORAGE;
			} else {
				mLogger.warn("...no free space for storing data in buffer . Start rolling Buffer");
				index = 0;
			}
		} else {// rolling case
			if (new File(NAND_STORAGE + "data_" + index + ".buf").exists()) {
				prefix = NAND_STORAGE;
			} else if (new File(SD_STORAGE + "data_" + index + ".buf").exists()) {
				prefix = SD_STORAGE;
			} else {
				if (usableSpaceNand() > 0) {
					prefix = NAND_STORAGE;
				} else if (usableSpaceSdCard() > 0) {
					prefix = SD_STORAGE;
				} else {
					if (maxIndex > currIndex) {
						File delFile;
						boolean okDel = false;
						mLogger.error("[saveBufferData] buffer rolling is error ! it can not store data. Should delete file from " + maxIndex
								+ " to " + currIndex);

						for (int idx = maxIndex; idx > currIndex; idx--) {
							delFile = new File(NAND_STORAGE + "data_" + idx + ".buf");
							if (delFile.exists()) {
								okDel = true;
							} else {
								delFile = new File(SD_STORAGE + "data_" + idx + ".buf");
								if (delFile.exists()) {
									okDel = true;
								}
							}

							if (okDel) {
								delFile.delete();
								bufferSize--;
								okDel = false;
							}

						}
						maxIndex = currIndex;
						saveBufferData(bufferArray, false);
					} else {
						mLogger.error("[saveBufferData] buffer rolling is ERROR ERROR ERROR! it can not store data. ");
						return;
					}
				}
			}
		}

		currIndex = index;
		
		if (hasSave) {
			setBufferConfig();
		}

		if ((currIndex < 0) || (bufferSize < 0)) {
			mLogger.warn("...wrong currIndex or bufferSize. Reset them to default value");
			maxIndex = currIndex = -1;
			bufferSize = 0;
			return;
		}

		String bufferPath = prefix + "data_" + currIndex + ".buf";
		mLogger.info("saveBufferData :  " + bufferPath);
		try {
			long size = 0;
			File tmpBufferFile = new File(bufferPath);
			if (!tmpBufferFile.exists()) {
				tmpBufferFile.createNewFile();
			} else {
				if (append) {
					size = tmpBufferFile.length();
				}
			}
			FileWriter writeToFile = new FileWriter(tmpBufferFile, append);

			while (!bufferArray.isEmpty()) {
				String current = bufferArray.get(0);
				if (size + current.length() > RAMBUFF_MAX_SIZE) {
					break;
				}
				size += current.length();
				current = current.endsWith("\n") ? current : current + "\n";
				writeToFile.write(current);
				bufferArray.remove(0);
			}
			writeToFile.flush();
			writeToFile.close();

			if (!bufferArray.isEmpty()) {
				saveBufferData(bufferArray, false);
			}
		} catch (IOException e) {
			mLogger.error("IOException", e);
		}
	}
	
	
	
	public long usableSpaceNand() {
		long size = getUsableSpaceInMB(NAND_STORAGE);
		long nand = size - VALID_MINIMUM_NAND;
		return nand < 0 ? 0 : nand ;
	}
	
	public long usableSpaceSdCard() {
		long nandSize = getUsableSpaceInMB(NAND_STORAGE);
		long sdSize = getUsableSpaceInMB(SD_STORAGE);
		
		if (nandSize == sdSize) {
			return 0;
		}
		long sd = sdSize - VALID_MINIMUM_SD;
		return sd < 0 ? 0 : sd;

	}
	
	public long getUsableSpaceInMB(String location) {
		try {
			File store = new File(location);
			long sizeMB = (store.getUsableSpace()/1024)/1024;
			mLogger.info("[getUsableSpaceInMB] The space of " + location + " is left for buffer that is  " + sizeMB);
			return sizeMB;
		} catch (Exception e) {
			mLogger.error("Exception", e);
		}
		return 0;
	}
	
	
	public boolean hasData() {
		return (bufferSize > 0) ? true : false;
	}
	
	
}
