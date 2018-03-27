package com.greenkoncepts.gateway.adapter.bosch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
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
	
	private void createXDKSettingTable() {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = datadb.getConnection();
			String sql = " CREATE TABLE IF NOT EXISTS PUBLIC.BOSCH_DEVICE_SETTING ( ";
			sql += " ID INTEGER  GENERATED ALWAYS AS IDENTITY (START WITH 1), ";
			sql += " DEVICE_ID INTEGER , ";
			sql += " LABEL VARCHAR(100) , ";
			sql += " SENSOR VARCHAR(50) , ";
			sql += " OFFSET VARCHAR(20) , ";
			sql += " DELTA VARCHAR(20) , ";
			sql += " PRIMARY KEY (ID) ); ";
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
	
	private void createXDKConfigurationTable() {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = datadb.getConnection();
			String sql = " CREATE TABLE IF NOT EXISTS PUBLIC.BOSCH_DEVICE_CONFIG ( ";
			sql += " ID INTEGER  GENERATED ALWAYS AS IDENTITY (START WITH 1), ";
			sql += " DEVICE_ID INTEGER , ";
			sql += " NAME VARCHAR(50) , ";
			sql += " VALUE VARCHAR(50) , ";
			sql += " PRIMARY KEY (ID) ); ";
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

	private void createDeviceAttributeTable() {
		createXDKSettingTable();
		createXDKConfigurationTable();
	}
	
	public boolean InsertXDKSettings(String deviceid, List<Map<String, String>>data) {
		boolean result = false;
		if ((data == null) || (data.size() == 0)) {
			return false;
		}
		
		List<Map<String, String>> lists = getDeviceList();
		String deviceid_db = "";
		for (Map<String, String> map : lists) {
			if (map.get("device_instanceid").equals(deviceid)) {
				deviceid_db = map.get("device_id");
				break;
			}
		}
		
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = datadb.getConnection();
			String sql = " INSERT INTO PUBLIC.BOSCH_DEVICE_SETTING  ";
			sql += " ( DEVICE_ID,LABEL,SENSOR, OFFSET, DELTA) ";
			sql += " VALUES (?, ?, ?, ? , ?) ; ";
			stmt = conn.prepareStatement(sql);
			for (Map<String, String> sensor : data) {
				int i = 1;
				stmt.setString(i++, deviceid_db);
				stmt.setString(i++, sensor.get("label"));
				stmt.setString(i++, sensor.get("sensor"));
				stmt.setString(i++, sensor.get("offset"));
				stmt.setString(i++, sensor.get("delta"));
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
			datadb.close(conn);
		}
		return result;
	}
	
	public boolean InsertXDKConfigs(String deviceid, List<Map<String, String>>data) {
		boolean result = false;
		if ((data == null) || (data.size() == 0)) {
			return false;
		}
		
		
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = datadb.getConnection();
			String sql = " INSERT INTO PUBLIC.BOSCH_DEVICE_CONFIG  ";
			sql += " ( DEVICE_ID,NAME,VALUE) ";
			sql += " VALUES (?, ?, ?) ; ";
			stmt = conn.prepareStatement(sql);
			for (Map<String, String> sensor : data) {
				int i = 1;
				stmt.setString(i++, "0");
				stmt.setString(i++, sensor.get("name"));
				stmt.setString(i++, sensor.get("value"));
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
			datadb.close(conn);
		}
		return result;
	}
	
	public boolean InsertDeviceAttributes(String deviceid, List<Map<String, String>>data) {
		boolean result = false;
		if ((data == null) || (data.size() == 0)) {
			return false;
		}
		Map<String, String> first = data.get(0);
		if (first.containsKey("tablename")) {
			data.remove(0);
			InsertXDKConfigs(deviceid, data);
		} else {
			InsertXDKSettings(deviceid, data);
		}
		return result;
	
	}
	
	public boolean updateXDKSettings(String deviceid, List<Map<String, String>> data) {
		if ((data == null) || (data.size() == 0)) {
			return false;
		}
		
		boolean result = false;
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = datadb.getConnection();
			String sql = " UPDATE PUBLIC.BOSCH_DEVICE_SETTING ";
			sql += "  SET LABEL = ?, SENSOR = ?, OFFSET = ?, DELTA = ? ";
			sql += " WHERE ID = ? ";
			stmt = conn.prepareStatement(sql);
			for (Map<String, String> sensor : data) {
				int i = 1;
				stmt.setString(i++, sensor.get("label"));
				stmt.setString(i++, sensor.get("sensor"));
				stmt.setString(i++, sensor.get("offset"));
				stmt.setString(i++, sensor.get("delta"));
				stmt.setString(i++, sensor.get("data_point"));
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
			datadb.close(conn);
		}
	
		return result;
	}

	public boolean updateXDKConfigs(String deviceid, List<Map<String, String>> data) {
		if ((data == null) || (data.size() == 0)) {
			return false;
		}
		
		boolean result = false;
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = datadb.getConnection();
			String sql = " UPDATE PUBLIC.BOSCH_DEVICE_CONFIG ";
			sql += "  SET NAME = ?, VALUE = ? ";
			sql += " WHERE ID = ? ";
			stmt = conn.prepareStatement(sql);
			for (Map<String, String> sensor : data) {
				int i = 1;
				stmt.setString(i++, sensor.get("name"));
				stmt.setString(i++, sensor.get("value"));
				stmt.setString(i++, sensor.get("data_point"));
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
			datadb.close(conn);
		}
	
		return result;
	}
	
	public boolean updateDeviceAttributes(String deviceid, List<Map<String, String>> data) {
		if ((data == null) || (data.size() == 0)) {
			return false;
		}
		boolean result = false;
		Map<String, String> first = data.get(0);
		if (first.containsKey("tablename")) {
			data.remove(0);
			updateXDKConfigs(deviceid, data);
		} else {
			updateXDKSettings(deviceid, data);
		}
	
		return result;
	}
	
	public List<Map<String, String>> getXDKSettings(String instanceId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Map<String, String>> result = new ArrayList<Map<String,String>>();
		try {
			conn = datadb.getConnection();
			String sql = " select da.* ";
			sql += " From  device d ,adapter a,BOSCH_DEVICE_SETTING da";
			sql += " WHERE a.id = d.adapter_id  and d.id = da.device_id and d.device_instanceid =  ? ";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, instanceId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				Map<String, String> map = new Hashtable<String, String>();
				map.put("data_point", rs.getString("ID"));
				map.put("label", rs.getString("LABEL"));
				map.put("sensor", rs.getString("SENSOR"));
				map.put("offset", rs.getString("OFFSET"));
				map.put("delta", rs.getString("DELTA"));
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
	
	public List<Map<String, String>> getXDKConfigs(String instanceId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Map<String, String>> result = new ArrayList<Map<String,String>>();
		try {
			conn = datadb.getConnection();
			String sql = " select da.* ";
			sql += " From BOSCH_DEVICE_CONFIG da";
			sql += " WHERE da.device_id =  ? ";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, "0");
			rs = stmt.executeQuery();
			while (rs.next()) {
				Map<String, String> map = new Hashtable<String, String>();
				map.put("data_point", rs.getString("ID"));
				map.put("name", rs.getString("NAME"));
				map.put("value", rs.getString("VALUE"));
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
		List<Map<String, String>> result = new ArrayList<Map<String,String>>();
		List<Map<String, String>> settings = getXDKSettings(instanceId);
		List<Map<String, String>> configs = getXDKConfigs(instanceId);
		if (!settings.isEmpty()) {
			result.addAll(settings);
		}
		
		if (!configs.isEmpty()) {
			result.addAll(configs);
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
			String sql = " select distinct da.* ";
			sql += " From  device d ,adapter a,BOSCH_DEVICE_SETTING da";
			sql += " WHERE a.id = d.adapter_id and d.id = da.device_id  ";
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("label", rs.getString("LABEL"));
				map.put("sensor", rs.getString("SENSOR"));
				map.put("offset", rs.getString("OFFSET"));
				map.put("delta", rs.getString("DELTA"));
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
	
	public boolean deleteXDKConfigs(String deviceAddress) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean result = false;
		try {
			conn = datadb.getConnection();
			String sql = " delete from  BOSCH_DEVICE_CONFIG ";
			sql += " where  device_id  = ? ";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, "0");
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
	
	public boolean importXDKData(String deviceid, List<Map<String, String>> attributes) {
		if(getXDKConfigs(deviceid).isEmpty()) {
			return InsertXDKConfigs(deviceid, attributes); 
		}
		if(deleteXDKConfigs(deviceid)) {
			return InsertXDKConfigs(deviceid, attributes);
		}
		return false;
	}
}
