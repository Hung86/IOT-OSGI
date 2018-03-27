package com.greenkoncepts.gateway.phidgets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.database.AExecutiveDatabase;

public class ExecutiveDatabaseImp extends AExecutiveDatabase {	
	ExecutiveDatabaseImp(String adapter) {
		super(adapter);
	}

	@Override
	public void initTables() {
		createAdapterTable();
		createDeviceTable();
		createDeviceAttributeTable();
	}

	private void createDeviceAttributeTable() {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = datadb.getConnection();
			String sql = " CREATE TABLE IF NOT EXISTS PUBLIC.PHIDGETS_DEVICE_ATTRIBUTE ( ";
			sql += " ID INTEGER  GENERATED ALWAYS AS IDENTITY (START WITH 1), ";
			sql += " DEVICE_ID INTEGER , ";
			sql += " CHANNEL VARCHAR(5) , ";
			sql += " SENSOR_TYPE VARCHAR(10), ";
			sql += " THRESHOLD VARCHAR(10), ";
			sql += " TRIGGER_MIN VARCHAR(10), ";
			sql += " PRIMARY KEY (ID) ); ";
			stmt = conn.prepareStatement(sql);
			stmt.executeUpdate();
			attributedTable = "PHIDGETS_DEVICE_ATTRIBUTE";

		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		} catch (Exception e) {
			mLogger.error("Exception", e);
		} finally {
			datadb.close(stmt);
			datadb.close(conn);
		}
	}
	
	public boolean insertDeviceList(Connection conn, List<Map<String, String>> deviceList) {
		boolean result = super.insertDeviceList(conn, deviceList);
		/*if (result) {
			for (Map<String, String> dev : deviceList) {
				String id = dev.get("device_instanceid");
				String cat = dev.get("device_category");
				InsertDeviceAttributes(conn, id, cat);
			}
		}*/
		return result;
	}

	public boolean InsertDeviceAttributes(Connection conn, String deviceInstance, String category) {
		PreparedStatement stmt = null;

		List<Map<String, String>> lists = getDeviceList();
		String deviceId = "";
		for (Map<String, String> map : lists) {
			if (map.get("device_instanceid").equals(deviceInstance)) {
				deviceId = map.get("device_id");
				break;
			}
		}
		boolean result = false;
		try {
			String sql = " INSERT INTO PUBLIC.PHIDGETS_DEVICE_ATTRIBUTE  ";
			sql += " ( CHANNEL,SENSOR_TYPE, THRESHOLD, TRIGGER_MIN,DEVICE_ID) ";
			sql += " VALUES (?, ?, ?, ? , ? ) ; ";
			stmt = conn.prepareStatement(sql);
			for (int i = 0; i < 8; i++) {
				int j = 1;
				stmt.setString(j++, "");
				stmt.setString(j++, "");
				stmt.setString(j++, "");
				stmt.setString(j++, "");
				stmt.setString(j++, deviceId);
				stmt.executeUpdate();
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

	public List<Map<String, String>> getAllDeviceAttributes() {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		try {
			conn = datadb.getConnection();
			String sql = " select da.* ";
			sql += " From  device d ,adapter a," + attributedTable + " da";
			sql += " WHERE ";
			sql += " a.id = d.adapter_id  and d.id = da.device_id  ";
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("channel", rs.getString("CHANNEL"));
				map.put("sensor_type", rs.getString("SENSOR_TYPE"));
				map.put("threshold", rs.getString("THRESHOLD"));
				map.put("trigger_min", rs.getString("TRIGGER_MIN"));
				map.put("data_point", rs.getString("ID"));
				map.put("device_id", rs.getString("device_id"));
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

	public List<Map<String, String>> getDeviceAttributes(String instanceId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		try {
			conn = datadb.getConnection();
			String sql = " select da.* ";
			sql += " From  device d ,adapter a," + attributedTable + " da";
			sql += " WHERE ";
			sql += " a.id = d.adapter_id  and d.id = da.device_id and d.device_instanceid =  ? ";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, instanceId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("channel", rs.getString("CHANNEL"));
				map.put("sensor_type", rs.getString("SENSOR_TYPE"));
				map.put("threshold", rs.getString("THRESHOLD"));
				map.put("trigger_min", rs.getString("TRIGGER_MIN"));
				map.put("data_point", rs.getString("ID"));
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

	public boolean updateDeviceAttributes(String deviceInstance, List<Map<String, String>> attributes) {

		if (attributes == null || attributes.size() == 0) {
			return true;
		}

		List<Map<String, String>> lists = getDeviceList();
		String deviceId = "";
		for (Map<String, String> map : lists) {
			if (map.get("device_instanceid").equals(deviceInstance)) {
				deviceId = map.get("device_id");
				break;
			}
		}
		PreparedStatement stmt = null;
		boolean result = false;
		Connection conn = null;

		try {
			conn = datadb.getConnection();
			String sql = " UPDATE PUBLIC.PHIDGETS_DEVICE_ATTRIBUTE ";
			sql += "  SET CHANNEL = ?, SENSOR_TYPE = ?, THRESHOLD = ?,";
			sql += " TRIGGER_MIN = ?  ";
			sql += " WHERE DEVICE_ID = ? AND ID = ?  ";
			stmt = conn.prepareStatement(sql);
			for (Map<String, String> item : attributes) {
				stmt.setString(1, item.get("channel"));
				stmt.setString(2, item.get("sensor_type"));
				stmt.setString(3, item.get("threshold"));
				stmt.setString(4, item.get("trigger_min"));
				stmt.setString(5, deviceId);
				stmt.setString(6, item.get("data_point"));
				int test = stmt.executeUpdate();
				System.out.println(test);
			}
			
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
	public boolean importData(Map<String, String> adapters, List<Map<String, String>> devices, List<Map<String, String>> attributes) {
		deleteAdapter();
		insertAdapterSettings(adapters);
		Connection conn = null;
		try {
			conn = datadb.getConnection();
		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		}
		super.insertDeviceList(conn, devices);
		insertDeviceList(devices);
		for (Map<String, String> device : devices) {
			String deviceId1 = device.get("device_id");
			List<Map<String, String>> list = new ArrayList<Map<String, String>>();
			for (Map<String, String> attribute : attributes) {
				String deviceId2 = attribute.get("device_id");
				if (deviceId1.equals(deviceId2)) {
					list.add(attribute);
				}
			}
			String deviceInstance = device.get("device_instanceid");
	
			InsertDeviceAttributes(deviceInstance, list);
		}

		return true;
	}
	public boolean InsertDeviceAttributes( String deviceInstance,List<Map<String,String>> attributes) {
		PreparedStatement stmt = null;

		List<Map<String, String>> lists = getDeviceList();
		String deviceId = "";
		for (Map<String, String> map : lists) {
			if (map.get("device_instanceid").equals(deviceInstance)) {
				deviceId = map.get("device_id");
				break;
			}
		}
		boolean result = false;
		Connection conn = null ;
		try {
			conn = datadb.getConnection();
			String sql = " INSERT INTO PUBLIC.PHIDGETS_DEVICE_ATTRIBUTE  ";
			sql += " ( CHANNEL,SENSOR_TYPE, THRESHOLD, TRIGGER_MIN,DEVICE_ID) ";
			sql += " VALUES (?, ?, ?, ? , ? ) ; ";
			stmt = conn.prepareStatement(sql);
			for(Map<String,String> attribute : attributes){
				int j = 1;
				stmt.setString(j++, attribute.get("channel") == null ? "" :  attribute.get("channel"));
				stmt.setString(j++, attribute.get("sensor_type") == null ? "" :  attribute.get("sensor_type"));
				stmt.setString(j++, attribute.get("threshold") == null ? "" :  attribute.get("threshold"));
				stmt.setString(j++, attribute.get("trigger_min") == null ? "" :  attribute.get("trigger_min"));
				stmt.setString(j++, deviceId);
				stmt.executeUpdate();
			}
			
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
}