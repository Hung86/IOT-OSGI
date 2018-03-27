package com.greenkoncepts.gateway.adapter.dummy;

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
			String sql = " CREATE TABLE IF NOT EXISTS PUBLIC.BRAINCHILD_DEVICE_ATTRIBUTE ( ";
			sql += " ID INTEGER  GENERATED ALWAYS AS IDENTITY (START WITH 1), ";
			sql += " DEVICE_ID INTEGER , ";
			sql += " CHANNEL VARCHAR(5) , ";
			sql += " NAME VARCHAR(25), ";
			sql += " UNIT VARCHAR(10), ";
			sql += " LOWEST VARCHAR(10), ";
			sql += " HIGHEST VARCHAR(10), ";
			sql += " RATIO VARCHAR(10), ";
			sql += " FORMULA VARCHAR(100), ";
			sql += " PRIMARY KEY (ID) ); ";
			stmt = conn.prepareStatement(sql);
			stmt.executeUpdate();
			attributedTable = "BRAINCHILD_DEVICE_ATTRIBUTE";

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
		if (result) {
			for (Map<String, String> dev : deviceList) {
				String id = dev.get("device_instanceid");
				String cat = dev.get("device_category");
				InsertDeviceAttributes(conn, id, cat);
			}
		}
		return result;
	}

	public boolean InsertDeviceAttributes(Connection conn, String deviceInstance, String category) {
		int loopInterval = 0;
		if ("3000".equals(category)) {
			loopInterval = 16;
		} else if ("3001".equals(category)) {
			loopInterval = 8;
		} else if ("3003".endsWith(category)) {
			loopInterval = 8;
		}

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
			String sql = " INSERT INTO PUBLIC.BRAINCHILD_DEVICE_ATTRIBUTE  ";
			sql += " ( CHANNEL,NAME, UNIT, LOWEST,HIGHEST, ";
			sql += " RATIO , FORMULA, DEVICE_ID)";
			sql += " VALUES (?, ?, ?, ? , ? , ? , ? , ?) ; ";
			stmt = conn.prepareStatement(sql);
			for (int i = 0; i < loopInterval; i++) {
				int j = 1;
				stmt.setString(j++, "");
				stmt.setString(j++, "");
				stmt.setString(j++, "");
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
			String sql = " select distinct da.* ";
			sql += " From  device d ,adapter a," + attributedTable + " da";
			sql += " WHERE ";
			sql += " a.id = d.adapter_id  and d.id = da.device_id  ";
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("channel", rs.getString("CHANNEL"));
				map.put("name", rs.getString("NAME"));
				map.put("unit", rs.getString("UNIT"));
				map.put("min", rs.getString("LOWEST"));
				map.put("max", rs.getString("HIGHEST"));
				map.put("ratio", rs.getString("RATIO"));
				map.put("formula", rs.getString("FORMULA"));
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
			stmt.setInt(1, Integer.valueOf(instanceId));
			rs = stmt.executeQuery();
			while (rs.next()) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("channel", rs.getString("CHANNEL"));
				map.put("name", rs.getString("NAME"));
				map.put("unit", rs.getString("UNIT"));
				map.put("min", rs.getString("LOWEST"));
				map.put("max", rs.getString("HIGHEST"));
				map.put("ratio", rs.getString("RATIO"));
				map.put("formula", rs.getString("FORMULA"));
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
			String sql = " UPDATE PUBLIC.BRAINCHILD_DEVICE_ATTRIBUTE ";
			sql += "  SET CHANNEL = ?, NAME = ?, UNIT = ?,";
			sql += " LOWEST = ? , HIGHEST = ? , RATIO = ?, ";
			sql += " FORMULA = ? WHERE DEVICE_ID = ? AND ID = ?  ";
			stmt = conn.prepareStatement(sql);
			for (Map<String, String> item : attributes) {
				stmt.setString(1, item.get("channel"));
				stmt.setString(2, item.get("name"));
				stmt.setString(3, item.get("unit"));
				stmt.setString(4, item.get("min"));
				stmt.setString(5, item.get("max"));
				stmt.setString(6, item.get("ratio"));
				stmt.setString(7, item.get("formula"));
				stmt.setString(8, deviceId);
				stmt.setString(9, item.get("data_point"));
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
			// TODO Auto-generated catch block
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
			String category = device.get("device_category");
			InsertDeviceAttributes(deviceInstance, category, list);
		}

		return true;
	}

	public boolean InsertDeviceAttributes(String deviceInstance, String category, List<Map<String, String>> attributes) {
		PreparedStatement stmt = null;

		List<Map<String, String>> lists = getDeviceList();
		String deviceId = "";
		for (Map<String, String> map : lists) {
			if (map.get("device_instanceid").equals(deviceInstance)) {
				deviceId = map.get("device_id");
				break;
			}
		}
		Connection conn = null;
		boolean result = false;
		try {
			conn = datadb.getConnection();
			String sql = " INSERT INTO PUBLIC.BRAINCHILD_DEVICE_ATTRIBUTE  ";
			sql += " ( CHANNEL,NAME, UNIT, LOWEST,HIGHEST, ";
			sql += " RATIO , FORMULA, DEVICE_ID)";
			sql += " VALUES (?, ?, ?, ? , ? , ? , ? , ?) ; ";
			stmt = conn.prepareStatement(sql);
			for (Map<String, String> attribute : attributes) {
				int j = 1;
				stmt.setString(j++, attribute.get("channel") == null ? "" : attribute.get("channel"));
				stmt.setString(j++, attribute.get("name") == null ? "" : attribute.get("name"));
				stmt.setString(j++, attribute.get("unit") == null ? "" : attribute.get("unit"));
				stmt.setString(j++, attribute.get("min") == null ? "" : attribute.get("min"));
				stmt.setString(j++, attribute.get("max") == null ? "" : attribute.get("max"));
				stmt.setString(j++, attribute.get("ratio") == null ? "" : attribute.get("ratio"));
				stmt.setString(j++, attribute.get("formula") == null ? "" : attribute.get("formula"));
				stmt.setString(j++, deviceId);
				int effect = stmt.executeUpdate();
				System.out.println("insert : "+ effect);
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
}
