package com.greenkoncepts.gateway.api.database;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AExecutiveDatabase {
	protected DbService datadb = null;
	protected String adapterClass = "";
	protected String attributedTable = "";
	protected Logger mLogger = LoggerFactory.getLogger(getClass().getSimpleName());

	public AExecutiveDatabase(String adapterClass) {
		this.adapterClass = adapterClass;
	}

	synchronized public void setDatabaseService(DbService db) {
		datadb = db;
	}

	public void createAdapterTable() {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = datadb.getConnection();
			String sql = " CREATE TABLE  IF NOT EXISTS PUBLIC.ADAPTER ";
			sql += " ( ID INTEGER  GENERATED ALWAYS AS IDENTITY (START WITH 1),  ";
			sql += " ADAPTER_NAME VARCHAR(50), ";
			sql += " PORT VARCHAR(25), ";
			sql += " PROTOCOL VARCHAR(10),";
			sql += " SERIAL_PORT VARCHAR(20),";
			sql += " BAUDRATE VARCHAR(10),";
			sql += " STOP_BIT VARCHAR(5),";
			sql += " PARITY VARCHAR (5),";
			sql += " ADDRESS VARCHAR(100) ,";
			sql += " MAX_OI_PER_QUERY VARCHAR(5) ,";
			sql += " MULTI_PROPERTY VARCHAR(5) ,";
			sql += " QUERY_TIMEOUT VARCHAR(25) ,PRIMARY KEY (ID))  ;";
			stmt = conn.prepareStatement(sql);
			stmt.executeUpdate();

		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		} catch (Exception e) {
			mLogger.error("Exception", e);
		} finally {
			datadb.close(stmt);
			datadb.close(conn);
		}
	}

	public void createDeviceTable() {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = datadb.getConnection();
			String sql = " CREATE TABLE IF NOT EXISTS  PUBLIC.DEVICE ( ";
			sql += " ID INTEGER GENERATED ALWAYS AS IDENTITY (START WITH 1) , ";
			sql += " ADAPTER_ID INTEGER, ";
			sql += " DEVICE_ADDRESS VARCHAR(100),";
			sql += " DEVICE_NAME VARCHAR(25), ";
			sql += " DEVICE_CATEGORY VARCHAR(25), ";
			sql += " DEVICE_ALTERNATIVEID VARCHAR(10) DEFAULT '-1', ";
			sql += " DEVICE_NETWORK_NUMBER VARCHAR(25),";
			sql += " DEVICE_NETWORK_ADDRESS VARCHAR(25),";
			sql += " DEVICE_INSTANCEID VARCHAR(25),";
			sql += " DEVICE_VERSION VARCHAR(5) ,";
			sql += " PRIMARY KEY (ID),UNIQUE (DEVICE_ADDRESS,ADAPTER_ID,DEVICE_INSTANCEID))";
			stmt = conn.prepareStatement(sql);
			stmt.executeUpdate();

		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		} catch (Exception e) {
			mLogger.error("Exception", e);
		} finally {
			datadb.close(stmt);
			datadb.close(conn);
		}
	}

	public Map<String, String> getAdapterSettings() {
		ResultSet rs = null;
		Connection conn = null;
		PreparedStatement stmt = null;
		Map<String, String> result = new HashMap<String, String>();
		try {
			conn = datadb.getConnection();
			String sql = " select *";
			sql += " from adapter a  ";
			sql += " where a.adapter_name = ? ";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, adapterClass);

			rs = stmt.executeQuery();
			while (rs.next()) {
				result.put("adapter_name", rs.getString("adapter_name"));
				result.put("port", rs.getString("port"));
				result.put("query_timeout", rs.getString("query_timeout"));
				result.put("id", rs.getString("id"));
				result.put("protocol", rs.getString("protocol"));
				result.put("serial_port", rs.getString("serial_port"));
				result.put("baudrate", rs.getString("baudrate"));
				result.put("stop_bit", rs.getString("stop_bit"));
				result.put("parity", rs.getString("parity"));
				result.put("address", rs.getString("address"));
				result.put("max_oi_per_query", rs.getString("max_oi_per_query"));
				result.put("multi_property", rs.getString("multi_property"));
			}
		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		} catch (Exception e) {
			mLogger.error("Exception", e);
		} finally {
			datadb.close(rs);
			datadb.close(stmt);
			datadb.close(conn);
		}
		return result;
	}

	public List<Map<String, String>> getDeviceList() {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		try {
			conn = datadb.getConnection();
			String sql = " select d.* ";
			sql += " from adapter a, device d ";
			sql += " where a.id = d.adapter_id and a.adapter_name  = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, adapterClass);

			rs = stmt.executeQuery();
			while (rs.next()) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("device_id", rs.getString("id"));
				map.put("device_address", rs.getString("device_address"));
				map.put("device_name", rs.getString("device_name"));
				map.put("device_category", rs.getString("device_category"));
				map.put("device_instanceid", rs.getString("device_instanceid"));
				map.put("device_alternativeid", rs.getString("device_alternativeid"));
				map.put("device_network_number", rs.getString("device_network_number"));
				map.put("device_network_address", rs.getString("device_network_address"));
				map.put("device_version", rs.getString("device_version"));
				result.add(map);
			}
		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		} catch (Exception e) {
			mLogger.error("Exception", e);
		} finally {
			datadb.close(rs);
			datadb.close(stmt);
			datadb.close(conn);
		}
		return result;
	}

	public boolean insertAdapterSettings(Map<String, String> attribute) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean result = false;
		try {
			conn = datadb.getConnection();
			String sql = " INSERT INTO PUBLIC.ADAPTER ";
			sql += " ( ADAPTER_NAME, PORT, QUERY_TIMEOUT,PROTOCOL,SERIAL_PORT,BAUDRATE,STOP_BIT,PARITY,ADDRESS,MAX_OI_PER_QUERY,MULTI_PROPERTY) ";
			sql += " VALUES (?,?,?,?, ?, ?, ? ,?, ?, ? , ?)";
			stmt = conn.prepareStatement(sql);
			int i = 1;
			stmt.setString(i++, adapterClass);
			stmt.setString(i++, attribute.get("port"));
			stmt.setString(i++, attribute.get("query_timeout"));
			stmt.setString(i++, attribute.get("protocol") == null ? "" : attribute.get("protocol"));
			stmt.setString(i++, attribute.get("serial_port") == null ? "" : attribute.get("serial_port"));
			stmt.setString(i++, attribute.get("baudrate") == null ? "" : attribute.get("baudrate"));
			stmt.setString(i++, attribute.get("stop_bit") == null ? "" : attribute.get("stop_bit"));
			stmt.setString(i++, attribute.get("parity") == null ? "" : attribute.get("parity"));
			stmt.setString(i++, attribute.get("address") == null ? "" : attribute.get("address"));
			stmt.setString(i++, attribute.get("max_oi_per_query") == null ? "100" : attribute.get("max_oi_per_query"));
			stmt.setString(i++, attribute.get("multi_property") == null ? "true" : attribute.get("multi_property"));
			stmt.executeUpdate();
			result = true;
		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		} finally {
			datadb.close(stmt);
			datadb.close(conn);
		}
		return result;
	}

	public boolean insertDeviceList(List<Map<String, String>> deviceList) {
		Connection conn = null;
		boolean result = false;
		try {
			conn = datadb.getConnection();
			result = insertDeviceList(conn, deviceList);
		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		} catch (Exception e) {
			mLogger.error("Exception", e);
		} finally {
			datadb.close(conn);
		}
		return result;
	}

	public boolean insertDeviceList(Connection conn, List<Map<String, String>> deviceList) {
		PreparedStatement stmt = null;
		boolean result = false;
		Map<String, String> adapterInfos = getAdapterSettings();
		String adapterId = adapterInfos.get("id");
		if(deviceList == null || deviceList.size() == 0)
			return true ;

		try {
			String sql = "  INSERT INTO PUBLIC.device  ";
			sql += " (ADAPTER_ID, DEVICE_ADDRESS, DEVICE_NAME, DEVICE_CATEGORY,DEVICE_ALTERNATIVEID,DEVICE_INSTANCEID,DEVICE_VERSION,DEVICE_NETWORK_NUMBER,DEVICE_NETWORK_ADDRESS) VALUES ";
			sql += " (? , ?, ?, ?, ? , ? , ? , ? , ?);";
			stmt = conn.prepareStatement(sql);
			for (Map<String, String> item : deviceList) {
				stmt.setString(1, adapterId);
				stmt.setString(2, item.get("device_address") == null ? "" : item.get("device_address"));
				stmt.setString(3, item.get("device_name") == null ? "" : item.get("device_name"));
				stmt.setString(4, item.get("device_category") == null ? "" : item.get("device_category"));
				stmt.setString(5, item.get("device_alternativeid") == null ? "" : item.get("device_alternativeid") );
				stmt.setString(6, item.get("device_instanceid") == null ? "" : item.get("device_instanceid"));
				stmt.setString(7, item.get("device_version") == null ? "" : item.get("device_version"));
				stmt.setString(8, item.get("device_network_number") == null ? "" : item.get("device_network_number"));
				stmt.setString(9, item.get("device_network_address") == null ? "" : item.get("device_network_address"));
				stmt.addBatch();
			}
			stmt.executeBatch();
			result = true;
		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		} catch (Exception e) {
			mLogger.error("Exception", e);
		} finally {
			datadb.close(stmt);
		}
		return result;
	}

	public boolean updateAdapterSettings(Map<String, String> attribute) {
		Connection conn = null;
		boolean result = false;
		try {
			conn = datadb.getConnection();
			result = updateAdapterSettings(conn, attribute);
		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		} catch (Exception e) {
			mLogger.error("Exception", e);
		} finally {
			datadb.close(conn);
		}
		return result;
	}

	public boolean updateAdapterSettings(Connection conn, Map<String, String> attribute) {
		PreparedStatement stmt = null;
		boolean result = false;
		try {
			String sql = " UPDATE PUBLIC.ADAPTER ";
			sql += "  SET  PORT = ?, QUERY_TIMEOUT = ?, PROTOCOL = ?,";
			sql += " serial_port = ? , baudrate = ? , stop_bit = ? ,";
			sql += " parity = ? , address = ?, max_oi_per_query = ?, multi_property = ? ";
			sql += " WHERE ADAPTER_NAME = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, attribute.get("port"));
			stmt.setString(2, attribute.get("query_timeout"));
			stmt.setString(3, attribute.get("protocol") == null ? "" : attribute.get("protocol"));
			stmt.setString(4, attribute.get("serial_port") == null ? "" : attribute.get("serial_port"));
			stmt.setString(5, attribute.get("baudrate") == null ? "" : attribute.get("baudrate"));
			stmt.setString(6, attribute.get("stop_bit") == null ? "" : attribute.get("stop_bit"));
			stmt.setString(7, attribute.get("parity") == null ? "" : attribute.get("parity"));
			stmt.setString(8, attribute.get("address") == null ? "" : attribute.get("address"));
			stmt.setString(9, attribute.get("max_oi_per_query") == null ? "" : attribute.get("max_oi_per_query"));
			stmt.setString(10, attribute.get("multi_property") == null ? "" : attribute.get("multi_property"));
			stmt.setString(11, adapterClass);
			stmt.executeUpdate();
			result = true;
		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		} catch (Exception e) {
			mLogger.error("Exception", e);
		} finally {
			datadb.close(stmt);
		}

		return result;
	}

	public boolean updateDataObject(List<Map<String, Object>> objectList) {
		boolean result = true;
		Connection conn = null;
		try {
			conn = datadb.getConnection();
			for (Map<String, Object> item : objectList) {
				Method m;
				String functionName = (String) item.get("function");
				if (functionName.equals("updateAdapterSettings")) {
					m = AExecutiveDatabase.class.getMethod(functionName, Connection.class, Map.class);
				} else {
					m = AExecutiveDatabase.class.getMethod(functionName, Connection.class, List.class);
					
				}
				Boolean value = (Boolean) m.invoke(this, conn, item.get("data"));
				if (!value) {
					result = false;
					break;
				}

			}
		} catch (SecurityException e) {
			mLogger.error("SecurityException", e);
		} catch (NoSuchMethodException e) {
			mLogger.error("NoSuchMethodException", e);
		} catch (IllegalArgumentException e) {
			mLogger.error("IllegalArgumentException", e);
		} catch (IllegalAccessException e) {
			mLogger.error("IllegalAccessException", e);
		} catch (InvocationTargetException e) {
			mLogger.error("InvocationTargetException", e);
		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		} finally {
			if (!result) {
				datadb.rollback(conn);
			}
			datadb.close(conn);
		}
		return result;
	}

	public boolean updateDeviceList(List<Map<String, String>> deviceList) {
		if (deviceList == null || deviceList.size() == 0)
			return true;
		Connection conn = null;
		boolean result = false;
		try {
			conn = datadb.getConnection();
			result = updateDeviceList(conn, deviceList);
		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		} catch (Exception e) {
			mLogger.error("Exception", e);
		} finally {
			if (!result) {
				datadb.rollback(conn);
			}
			datadb.close(conn);
		}
		return result;
	}

	public boolean updateDeviceList(Connection conn, List<Map<String, String>> deviceList) {
		PreparedStatement stmt = null;
		boolean result = false;
		// Map<String, String> adapterInfos = getAdapterSettings();
		for (Map<String, String> map : deviceList) {
			for (String key : map.keySet()) {
				if (map.get(key) == null) {
					map.put(key, "");
				}
			}
		}
		
		try {
			String sql = " Update PUBLIC.device  SET";
			int i = 1;
			for (Map<String, String> item : deviceList) {
				sql += " device_address = ? ";
				sql += ",DEVICE_NAME =  ? ";
				sql += ", device_category = ? ";
				sql += ", device_instanceid = ? ";
				sql += ", device_alternativeid =  ?";
				sql += ", device_version = ? ";
				sql += ", device_network_number = ? ";
				sql += ", device_network_address = ? ";
				sql += " where id  = ? ";
				stmt = conn.prepareStatement(sql);
				stmt.setString(i++, item.get("device_address"));
				stmt.setString(i++, item.get("device_name"));
				stmt.setString(i++, item.get("device_category"));
				stmt.setString(i++, item.get("device_instanceid"));
				stmt.setString(i++, item.get("device_alternativeid"));
				stmt.setString(i++, item.get("device_version"));
				stmt.setString(i++, item.get("device_network_number"));
				stmt.setString(i++, item.get("device_network_address"));
				stmt.setString(i++, item.get("device_id"));
				stmt.executeUpdate();
				i = 1;
				sql = " Update PUBLIC.device  SET";
			}
			result = true;
		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		} catch (Exception e) {
			mLogger.error("Exception", e);
		} finally {
			datadb.close(stmt);
		}
		return result;
	}

	public boolean deleteAdapter() {

		PreparedStatement stmt = null;
		boolean result = false;
		Connection conn = null;

		try {
			conn = datadb.getConnection();
			deleteAllDeviceList(conn);
			String sql = " delete from adapter  ";
			sql += " where  adapter_name = ? ";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, adapterClass);
			stmt.executeUpdate();

			result = true;
		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		} catch (Exception e) {
			mLogger.error("Exception", e);
		} finally {
			datadb.close(stmt);
			datadb.close(conn);
		}
		

		return result;

	}

	public boolean deleteAllDeviceList(Connection conn) {
		deleteAllDeviceAttributes();
		PreparedStatement stmt = null;
		List<Map<String, String>> deviceList = getDeviceList();
		if(deviceList == null || deviceList.size() == 0)
			return true ;
		boolean result = false;
		try {
			String sql = " delete from device  ";
			sql += " where  id = ? ";
			stmt = conn.prepareStatement(sql);
			for (Map<String, String> device : deviceList) {
				stmt.setInt(1, Integer.valueOf(device.get("device_id")));
				stmt.addBatch();
			}
			stmt.executeBatch();

			result = true;
		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		} catch (Exception e) {
			mLogger.error("Exception", e);
		} finally {
			datadb.close(stmt);
		}
		

		return result;
	}

	public boolean deleteDeviceList(List<Map<String, String>> deviceList) {
		if (deviceList == null || deviceList.size() == 0) {
			return true;
		}
		Connection conn = null;
		boolean result = false;
		try {
			conn = datadb.getConnection();
			result = deleteDeviceList(conn, deviceList);
		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		} catch (Exception e) {
			mLogger.error("Exception", e);
		} finally {
			datadb.close(conn);
		}
		return result;
	}

	public boolean deleteDeviceList(Connection conn, List<Map<String, String>> deviceList) {
		deleteDeviceAttributes(conn, deviceList);
		PreparedStatement stmt = null;
		boolean result = false;
		try {
			String sql = " delete from device  ";
			sql += " where  id = ? ";
			stmt = conn.prepareStatement(sql);
			for (Map<String, String> device : deviceList) {
				stmt.setInt(1, Integer.valueOf(device.get("device_id")));
				stmt.addBatch();
			}
			stmt.executeBatch();

			result = true;
		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		} catch (Exception e) {
			mLogger.error("Exception", e);
		} finally {
			datadb.close(stmt);
		}

		return result;
	}

	public boolean deleteDeviceAttributes(Connection conn, List<Map<String, String>> deviList) {
		if (attributedTable.equals(""))
			return true;
		PreparedStatement stmt = null;
		boolean result = false;
		try {
			String sql = " delete from  " + attributedTable;
			sql += " where  device_id  = ? ";
			stmt = conn.prepareStatement(sql);
			for (Map<String, String> map : deviList) {
				stmt.setString(1, map.get("device_id"));
				stmt.addBatch();
			}
			stmt.executeBatch();
			result = true;
		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		} catch (Exception e) {
			mLogger.error("Exception", e);
		} finally {
			datadb.close(stmt);
		}
		return result;
	}

	public boolean deleteAllDeviceAttributes() {
		if (attributedTable.equals(""))
			return true;
		Connection conn = null ;
		PreparedStatement stmt = null;
		boolean result = false;
		List<Map<String, String>> lists = getDeviceList();
		if (lists == null || lists.size() == 0)
			return true;
		try {
			conn = datadb.getConnection();
			String sql = " delete from  " + attributedTable;
			stmt = conn.prepareStatement(sql);
			/*sql += " where  device_id  = ? ";
			stmt = conn.prepareStatement(sql);
			for (Map<String, String> map : lists) {
				stmt.setString(1, map.get("device_id"));
				stmt.addBatch();
			}*/
			stmt.executeUpdate();
			/*stmt.executeBatch();*/
			result = true;
		} catch (SQLException e) {
			
			mLogger.error("SQLException", e);
		} catch (Exception e) {
			mLogger.error("Exception", e);
		} finally {
			datadb.close(stmt);
			datadb.close(conn);
			
		}
		return result;
	}

	public String getDeviceId(String deviceAdress, String deviceInstance, String adapterId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String deviceId = "";
		try {
			conn = datadb.getConnection();
			String sql = " select d.* ";
			sql += " From  device d ,adapter a";
			sql += " WHERE d.device_address = ? and d.device_instanceid = ? ";
			sql += " and a.id = d.adapter_id and a.id  = ? ";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, deviceAdress);
			stmt.setString(2, deviceInstance);
			stmt.setString(3, adapterId);

			rs = stmt.executeQuery();
			while (rs.next()) {
				deviceId = rs.getString("id");
			}
		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		} catch (Exception e) {
			mLogger.error("Exception", e);
		} finally {
			datadb.close(rs);
			datadb.close(stmt);
			datadb.close(conn);
		}
		return deviceId;
	}

	public List<Map<String, String>> getAllDeviceAttributes() {
		return new ArrayList<Map<String, String>>();
	}

	public List<Object> getExportData() {
		Map<String,String> adapterSettings = getAdapterSettings();
		List<Map<String,String>> deviList = getDeviceList();
		List<Map<String,String>> deviceAttributes = getAllDeviceAttributes();
		List<Object> result = new ArrayList<Object>();
		result.add(adapterSettings);
		result.add(deviList);
		result.add(deviceAttributes);
		return result ;
	}
	
	
	
	public boolean importData(Map<String, String>adapters, List<Map<String, String>> devices, List<Map<String, String>> attributes) {
		deleteAdapter();
		insertAdapterSettings(adapters);
		insertDeviceList(devices);
		return true;
	}
	
	abstract public void initTables();

}
