package com.greenkoncepts.gateway.adapter.modbusconverter;

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
			String sql = " CREATE TABLE IF NOT EXISTS PUBLIC.MBUSTOMODBUSCONVERTER_DEVICE_ATTRIBUTE ( ";
			sql += " ID INTEGER  GENERATED ALWAYS AS IDENTITY (START WITH 1), ";
			sql += " DEVICE_ID INTEGER , ";
			sql += " REGISTER VARCHAR(10) , ";
			sql += " CHANNEL VARCHAR(10), ";
			sql += " DATA_TYPE VARCHAR(10), ";
			sql += " NAME VARCHAR(30), ";
			sql += " UNIT VARCHAR(10), ";
			sql += " MULTIPLIER VARCHAR(10), ";
			sql += " CONSUMPTION VARCHAR(10), ";
			sql += " PRIMARY KEY (ID) ); ";
			stmt = conn.prepareStatement(sql);
			stmt.executeUpdate();
			attributedTable = "MBUSTOMODBUSCONVERTER_DEVICE_ATTRIBUTE";

		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		} catch (Exception e) {
			mLogger.error("Exception", e);
		} finally {
			datadb.close(stmt);
			datadb.close(conn);
		}
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
				map.put("name", rs.getString("NAME"));
				map.put("data_type", rs.getString("DATA_TYPE"));
				map.put("unit", rs.getString("UNIT"));
				map.put("multiplier", rs.getString("MULTIPLIER"));
				map.put("consumption", rs.getString("CONSUMPTION"));
				map.put("data_point", rs.getString("ID"));
				map.put("register", rs.getString("REGISTER"));
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
				map.put("name", rs.getString("NAME"));
				map.put("data_type", rs.getString("DATA_TYPE"));
				map.put("unit", rs.getString("UNIT"));
				map.put("multiplier", rs.getString("MULTIPLIER"));
				map.put("consumption", rs.getString("CONSUMPTION"));
				map.put("register", rs.getString("REGISTER"));
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
		List<Map<String, String>> lists = getDeviceList();
		String deviceId = "";
		for (Map<String, String> map : lists) {
			if (map.get("device_instanceid").equals(deviceInstance)) {
				deviceId = map.get("device_id");
				break;
			}
		}
		List<Map<String, String>> update = new ArrayList<Map<String, String>>();
		List<Map<String, String>> insert = new ArrayList<Map<String, String>>();
		List<Map<String, String>> list = getDeviceAttributes(deviceInstance);
		List<String> dataPoints = new ArrayList<String>();
		if (list == null || list.size() == 0) {
			insert = attributes;
		} else {
			for (Map<String, String> updateAndInsert : attributes) {
				dataPoints.add(updateAndInsert.get("data_point"));
				if(updateAndInsert.get("data_point") == ""){
					insert.add(updateAndInsert);
				}
				else{
					update.add(updateAndInsert);
				}
			}
		}
		List<String> dataPointTotal = new ArrayList<String>();
		List<String> delete = new ArrayList<String>();
		for(Map<String,String> map : list){
			dataPointTotal.add(map.get("data_point"));
		}
		for(String item : dataPointTotal){
			if(!dataPoints.contains(item)){
				delete.add(item);
			}
		}
		
		PreparedStatement stmt = null;
		boolean result = false;
		Connection conn = null;

		try {
			conn = datadb.getConnection();
			String sql = " UPDATE PUBLIC.MBUSTOMODBUSCONVERTER_DEVICE_ATTRIBUTE ";
			sql += "  SET CHANNEL = ?, NAME = ?, DATA_TYPE = ?,";
			sql += " UNIT = ? , MULTIPLIER = ? , CONSUMPTION = ?,REGISTER  = ?";
			sql += " WHERE DEVICE_ID = ? AND ID = ?  ";
			stmt = conn.prepareStatement(sql);
			for (Map<String, String> item : update) {
				stmt.setString(1, item.get("channel"));
				stmt.setString(2, item.get("name"));
				stmt.setString(3, item.get("data_type"));
				stmt.setString(4, item.get("unit"));
				stmt.setString(5, item.get("multiplier"));
				stmt.setString(6, item.get("hasconsumption"));
				stmt.setString(7, item.get("register"));
				stmt.setString(8, deviceId);
				stmt.setString(9, item.get("data_point"));
				stmt.executeUpdate();
			}
			
			if (insert.size() > 0) {
				insertDeviceAttribute(conn, deviceInstance, insert);
			}
			if(delete.size() > 0){
				deleteDeviceAttribute(conn,deviceInstance,delete);
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

	private boolean deleteDeviceAttribute(Connection conn, String deviceInstance, List<String> deleteList) {
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
			String sql = " delete from PUBLIC.MBUSTOMODBUSCONVERTER_DEVICE_ATTRIBUTE  ";
			sql += " where device_id = ? and id = ? " ;
			stmt = conn.prepareStatement(sql);
			for (String item : deleteList) {
				int j = 1;
				stmt.setString(j++, deviceId);
				stmt.setString(j++, item);
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
		
		return result ;
		
	}

	private boolean insertDeviceAttribute(Connection conn, String deviceInstance, List<Map<String, String>> insert) {
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
			String sql = " INSERT INTO PUBLIC.MBUSTOMODBUSCONVERTER_DEVICE_ATTRIBUTE  ";
			sql += " ( CHANNEL,NAME, UNIT, DATA_TYPE,MULTIPLIER, ";
			sql += " CONSUMPTION,REGISTER ,DEVICE_ID)";
			sql += " VALUES (?, ?, ?, ? , ? , ? , ? , ?) ; ";
			stmt = conn.prepareStatement(sql);
			for (Map<String, String> item : insert) {
				int j = 1;
				stmt.setString(j++, item.get("channel"));
				stmt.setString(j++, item.get("name"));
				stmt.setString(j++, item.get("unit"));
				stmt.setString(j++, item.get("data_type"));
				stmt.setString(j++, item.get("multiplier"));
				stmt.setString(j++, item.get("hasconsumption"));
				stmt.setString(j++, item.get("register"));
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
	public boolean insertDeviceAttribute( String deviceInstance, List<Map<String, String>> insert) {
		PreparedStatement stmt = null;
		Connection conn = null;
		
			

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
			conn = datadb.getConnection();
			String sql = " INSERT INTO PUBLIC.MBUSTOMODBUSCONVERTER_DEVICE_ATTRIBUTE  ";
			sql += " ( CHANNEL,NAME, UNIT, DATA_TYPE,MULTIPLIER, ";
			sql += " CONSUMPTION,REGISTER ,DEVICE_ID)";
			sql += " VALUES (?, ?, ?, ? , ? , ? , ? , ?) ; ";
			stmt = conn.prepareStatement(sql);
			for (Map<String, String> item : insert) {
				int j = 1;
				stmt.setString(j++, item.get("channel"));
				stmt.setString(j++, item.get("name"));
				stmt.setString(j++, item.get("unit"));
				stmt.setString(j++, item.get("data_type"));
				stmt.setString(j++, item.get("multiplier"));
				stmt.setString(j++, item.get("hasconsumption"));
				stmt.setString(j++, item.get("register"));
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
	public boolean importData(Map<String, String> adapters, List<Map<String, String>> devices, List<Map<String, String>> attributes) {
		deleteAdapter();
		insertAdapterSettings(adapters);
		Connection conn = null;
		try {
			conn = datadb.getConnection();
		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		}
		/*super.insertDeviceList(conn, devices);*/
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

	private boolean InsertDeviceAttributes(String deviceInstance, String category, List<Map<String, String>> list) {
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
			String sql = " INSERT INTO PUBLIC.MBUSTOMODBUSCONVERTER_DEVICE_ATTRIBUTE  ";
			sql += " ( CHANNEL,NAME, UNIT, DATA_TYPE,MULTIPLIER, ";
			sql += " CONSUMPTION,REGISTER ,DEVICE_ID)";
			sql += " VALUES (?, ?, ?, ? , ? , ? , ? , ?) ; ";
			conn = datadb.getConnection();
			stmt = conn.prepareStatement(sql);
			for (Map<String, String> item : list) {
				int j = 1;
				stmt.setString(j++, item.get("channel"));
				stmt.setString(j++, item.get("name"));
				stmt.setString(j++, item.get("unit"));
				stmt.setString(j++, item.get("data_type"));
				stmt.setString(j++, item.get("multiplier"));
				stmt.setString(j++, item.get("hasconsumption"));
				stmt.setString(j++, item.get("register"));
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
	

}
