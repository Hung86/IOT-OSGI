package com.greenkoncepts.gateway.adapter.bacnet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.database.AExecutiveDatabase;

public class ExecutiveDatabaseImp extends AExecutiveDatabase {	
	ExecutiveDatabaseImp(String adapter) {
		super(adapter);
	}

	public void initTables() {
		createAdapterTable();
		createDeviceTable();
		createDeviceAttributeTable();
		createValidationRule();
	}

	private void createValidationRule() {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = datadb.getConnection();
			String sql = " CREATE TABLE IF NOT EXISTS PUBLIC.VALIDATION ";
			sql += " (ID INTEGER  GENERATED ALWAYS AS IDENTITY ";
			sql += " (START WITH 1), ";
			sql += " DEVICE_ID INTEGER, ";
			sql += " DATA_POINT INTEGER,";
			sql += " CONDITION VARCHAR(100), ";
			sql += " ACTION varchar(50), ";
			sql += " PRIMARY KEY (ID) );";
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
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = datadb.getConnection();
			String sql = " CREATE TABLE IF NOT EXISTS PUBLIC.BACNET_DEVICE_ATTRIBUTE ( ";
			sql += " ID INTEGER  GENERATED ALWAYS AS IDENTITY (START WITH 1), ";
			sql += " DEVICE_ID INTEGER , ";
			sql += " FORMULA VARCHAR(1000) , ";
			sql += " OBJECT_IDENTIFIER VARCHAR(50), ";
			sql += " oi_measure_name VARCHAR(200), ";
			sql += " OI_MEASURE_UNIT VARCHAR(50), ";
			sql += " NAME VARCHAR(50), ";
			sql += " CHANNEL VARCHAR(50), ";
			sql += " DEFAULT_VALUE VARCHAR(50), ";
			sql += " MAX_VALUE VARCHAR(50), ";
			sql += " MIN_VALUE VARCHAR(50), ";
			sql += " MEASURE_NAME VARCHAR(50), ";
			sql += " MEASURE_UNIT VARCHAR(50), ";
			sql += " MEASURE_RATIO VARCHAR(50), ";
			sql += " CONSUMPTION VARCHAR(10),";
			sql += " TYPE VARCHAR(10),";
			sql += " PRIMARY KEY (ID) ); ";
			stmt = conn.prepareStatement(sql);
			stmt.executeUpdate();
			attributedTable = "BACNET_DEVICE_ATTRIBUTE";

		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		} catch (Exception e) {
			mLogger.error("Exception", e);
		} finally {
			datadb.close(stmt);
			datadb.close(conn);
		}
	}

	public int numOfDevice() {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int count = 0;
		try {
			conn = datadb.getConnection();
			String sql = " select count(*) as num ";
			sql += " From adapter a, device d ";
			sql += " WHERE a.id = d.adapter_id and a.adapter_name  = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, adapterClass);

			rs = stmt.executeQuery();
			while (rs.next()) {
				count = rs.getInt("num");
				break;
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
		return count;
	}


	public boolean InsertDeviceAttributes(String address, String deviceInstance, List<Map<String, String>> attributes) {
		if(attributes == null || attributes.size() == 0)
			return true ;
		Connection conn = null;
		PreparedStatement stmt = null;
		Map<String, String> adapterInfos = getAdapterSettings();
		String adapterId = adapterInfos.get("id");
		boolean result = false;
		try {
			conn = datadb.getConnection();
			String sql = " INSERT INTO PUBLIC.BACNET_DEVICE_ATTRIBUTE  ";
			sql += " ( DEVICE_ID,FORMULA, OBJECT_IDENTIFIER, OI_MEASURE_NAME,OI_MEASURE_UNIT, ";
			sql += " NAME,CHANNEL,DEFAULT_VALUE,MAX_VALUE,MIN_VALUE,MEASURE_NAME,MEASURE_UNIT,MEASURE_RATIO,CONSUMPTION,TYPE )";
			sql += " VALUES (?, ?, ?, ? , ? , ? , ? , ? , ? , ? , ? , ? , ?, ? , ?) ; ";
			stmt = conn.prepareStatement(sql);
			String deviceId = getDeviceId(address, deviceInstance, adapterId);
			int j = 1 ;
			for (Map<String, String> item : attributes) {
				int i = 1;
				stmt.setInt(i++, Integer.valueOf(deviceId));
				stmt.setString(i++, item.get("formula") == null ? "" : item.get("formula"));
				stmt.setString(i++, item.get("object_identifier") == null ? "" : item.get("object_identifier"));
				stmt.setString(i++, item.get("oi_measure_name") == null ? "" : item.get("oi_measure_name"));
				stmt.setString(i++, item.get("oi_measure_unit") == null ? "" : item.get("oi_measure_unit"));
				stmt.setString(i++, item.get("name") == null ? "" : item.get("name"));
				stmt.setString(i++, item.get("channel") == null ? "" : item.get("channel"));
				stmt.setString(i++, item.get("default_value") == null ? "" : item.get("default_value"));
				stmt.setString(i++, item.get("max_value") == null ? "" : item.get("max_value"));
				stmt.setString(i++, item.get("min_value") == null ? "" : item.get("min_value"));
				stmt.setString(i++, item.get("measure_name") == null ? "" : item.get("measure_name"));
				stmt.setString(i++, item.get("measure_unit") == null ? "" : item.get("measure_unit"));
				stmt.setString(i++, item.get("measure_ratio") == null ? "" : item.get("measure_ratio"));
				stmt.setString(i++, item.get("consumption") == null ? "" : item.get("consumption"));
				stmt.setString(i++, item.get("type") == null ? "data" : item.get("type"));
				
				stmt.addBatch();
				if(j % 1000 == 0){
					stmt.executeBatch();
					stmt.clearBatch() ;
				}
				j++;
			}
			stmt.executeBatch();
			result = true;
		} catch (SQLException e) {
			mLogger.error("SQLException", e);
			if (conn != null) {
				try {
					conn.rollback();
				} catch (Exception e1) {
					mLogger.error("Exception", e1);
				}
			}
		} catch (Exception e) {
			mLogger.error("Exception", e);
		} finally {
			datadb.close(stmt);
			datadb.close(conn);
		}
		return result;
	}



	public boolean updateDeviceAttributes(String address, String deviceInstance, List<Map<String, String>> attributes) {
		if (attributes.size() == 1) {
			Map<String, String> map = attributes.get(0);
			if (map.size() == 0)
				return true;
		}
		Connection conn = null;
		PreparedStatement stmt = null;
		Map<String, String> adapterInfos = getAdapterSettings();
		String adapterId = adapterInfos.get("id");
		String deviceId = getDeviceId(address, deviceInstance, adapterId);
		boolean result = false;
		try {
			conn = datadb.getConnection();
			conn.setAutoCommit(false);
		
			String sql = " UPDATE BACNET_DEVICE_ATTRIBUTE SET ";
			sql += " NAME = ? , CHANNEL = ? , DEFAULT_VALUE = ?, MAX_VALUE = ?, MIN_VALUE = ?, TYPE = ?,";
			sql += " MEASURE_NAME = ? , MEASURE_UNIT = ? , MEASURE_RATIO = ?, CONSUMPTION = ?,FORMULA = ? ";
			sql += " WHERE ID = ? ";
			stmt = conn.prepareStatement(sql);
			int i = 1;

			for (Map<String, String> item : attributes) {
				stmt.setString(i++, item.get("name") == null ? "" : item.get("name"));
				stmt.setString(i++, item.get("channel") == null ? "" : item.get("channel"));
				stmt.setString(i++, item.get("defaut_value") == null ? "" : item.get("default_value"));
				stmt.setString(i++, item.get("max_value") == null ? "" : item.get("max_value"));
				stmt.setString(i++, item.get("min_value") == null ? "" : item.get("min_value"));
				stmt.setString(i++, item.get("type") == null ? "" : item.get("type"));
				stmt.setString(i++, item.get("measure_name") == null ? "" : item.get("measure_name"));
				stmt.setString(i++, item.get("measure_unit") == null ? "" : item.get("measure_unit"));
				stmt.setString(i++, item.get("measure_ratio") == null ? "" : item.get("measure_ratio"));
				stmt.setString(i++, item.get("consumption") == null ? "" : String.valueOf(item.get("consumption")));
				stmt.setString(i++, item.get("formula") == null ? "" : String.valueOf(item.get("formula")));
				stmt.setString(i++, item.get("data_point"));
				stmt.addBatch();
				i = 1;
			}

			stmt.executeBatch();
			conn.commit();

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
	
	public boolean updateDeviceAttributesScanning(String address, String deviceInstance, List<Map<String, String>> attributes) {
		if (attributes.size() == 1) {
			Map<String, String> map = attributes.get(0);
			if (map.size() == 0)
				return true;
		}
		Connection conn = null;
		PreparedStatement stmt = null;
		Map<String, String> adapterInfos = getAdapterSettings();
		String adapterId = adapterInfos.get("id");
		String deviceId = getDeviceId(address, deviceInstance, adapterId);
		boolean result = false;
		try {
			conn = datadb.getConnection();
			conn.setAutoCommit(false);
		
			String sql = " UPDATE BACNET_DEVICE_ATTRIBUTE SET ";
			sql += " TYPE = ? ";
			sql += " WHERE ID = ? ";
			stmt = conn.prepareStatement(sql);
			int i = 1;

			for (Map<String, String> item : attributes) {
				stmt.setString(i++, item.get("type") == null ? "" : item.get("type"));
				stmt.setString(i++, item.get("data_point"));
				stmt.addBatch();
				i = 1;
			}
			

			stmt.executeBatch();
			conn.commit();

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
	
	public boolean updateDeviceAttributesConfiguration(String address, String deviceInstance, List<Map<String, String>> attributes) {
		if (attributes.size() == 1) {
			Map<String, String> map = attributes.get(0);
			if (map.size() == 0)
				return true;
		}
		Connection conn = null;
		PreparedStatement stmt = null;
		Map<String, String> adapterInfos = getAdapterSettings();
		String adapterId = adapterInfos.get("id");
		String deviceId = getDeviceId(address, deviceInstance, adapterId);
		boolean result = false;
		try {
			conn = datadb.getConnection();
			conn.setAutoCommit(false);
		
			String sql = " UPDATE BACNET_DEVICE_ATTRIBUTE SET ";
			sql += " NAME = ? , CHANNEL = ?, DEFAULT_VALUE = ?, MAX_VALUE = ?, MIN_VALUE = ?,";
			sql += " MEASURE_NAME = ? , MEASURE_UNIT = ? ";
			sql += " WHERE ID = ? ";
			stmt = conn.prepareStatement(sql);
			int i = 1;

			for (Map<String, String> item : attributes) {
				stmt.setString(i++, item.get("name") == null ? "" : item.get("name"));
				stmt.setString(i++, item.get("channel") == null ? "" : item.get("channel"));
				stmt.setString(i++, item.get("default_value") == null ? "" : item.get("default_value"));
				stmt.setString(i++, item.get("max_value") == null ? "" : item.get("max_value"));
				stmt.setString(i++, item.get("min_value") == null ? "" : item.get("min_value"));
				stmt.setString(i++, item.get("measure_name") == null ? "" : item.get("measure_name"));
				stmt.setString(i++, item.get("measure_unit") == null ? "" : item.get("measure_unit"));
				stmt.setString(i++, item.get("data_point"));
				stmt.addBatch();
				i = 1;
			}

			stmt.executeBatch();
			conn.commit();

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



	public boolean deleteDeviceAttributes(String address, String instanceId, List<String> dataPoints) {
		if (dataPoints == null || dataPoints.size() == 0) {
			return true;
		}
		Connection conn = null;
		PreparedStatement stmt = null;
		/*String adapterId = getAdapterSettings().get("id");
		String deviceId = getDeviceId(address, instanceId, adapterId);*/
		boolean result = false;
		try {
			conn = datadb.getConnection();
			String sql = " delete from bacnet_device_attribute  ";
			sql += " where id = ? ";
			stmt = conn.prepareStatement(sql);
			for (String dataPoint : dataPoints) {
				stmt.setInt(1, Integer.valueOf(dataPoint));
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
		deleteValidation(address, instanceId, dataPoints);

		return result;
	}

	public boolean deleteValidation(String address, String instanceId, List<String> dataPoints) {
		if (dataPoints == null || dataPoints.size() == 0)
			return true;
		Connection conn = null;
		PreparedStatement stmt = null;
		String adapterId = getAdapterSettings().get("id");
		String deviceId = getDeviceId(address, instanceId, adapterId);
		boolean result = false;
		try {
			conn = datadb.getConnection();
			String sql = " delete from VALIDATION  ";
			sql += " where device_id = ? and data_point = ? ";
			stmt = conn.prepareStatement(sql);
			for (String dataPoint : dataPoints) {
				stmt.setInt(1, Integer.valueOf(deviceId));
				stmt.setInt(2, Integer.valueOf(dataPoint));
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

	public boolean deleteValidation(String vadationId) {
		Connection conn = null;
		PreparedStatement stmt = null;

		boolean result = false;
		try {
			conn = datadb.getConnection();
			String sql = " delete from VALIDATION  ";
			sql += " where id = ? ";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, vadationId);

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

	public boolean deleteAllDeviceAttributes(String address, String instanceId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		String adapterId = getAdapterSettings().get("id");
		String deviceId = getDeviceId(address, instanceId, adapterId);
		boolean result = false;
		try {
			conn = datadb.getConnection();
			String sql = " delete from bacnet_device_attribute  ";
			sql += " where device_id = ? ";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, Integer.valueOf(deviceId));
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


	public List<Map<String, String>> getNodeScanningPage(String deviceAdress, String instanceId, String indexPage, int paging,String type) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		try {
			conn = datadb.getConnection();
			String sql = " select da.* ";
			sql += " from adapter a, device d ,bacnet_device_attribute da ";
			sql += " where a.id  = d.adapter_id and da.device_id = d.id and da.formula = '' ";
			sql += " and a.adapter_name = ? and d.device_address = ? and d.device_instanceid = ? ";
			if(!"null".equals(type)){
				sql += " and type = ? ";
			}
			
			if (paging != 0) {
				sql += "  limit ? , ?  ";
			}
			
			stmt = conn.prepareStatement(sql);
			int i = 1 ;
			stmt.setString(i++, adapterClass);
			stmt.setString(i++, deviceAdress);
			stmt.setString(i++, instanceId);
			if(!"null".equals(type)){
				stmt.setString(i++, type);
			}
			
			
			if (paging != 0) {
				stmt.setInt(i++, (Integer.valueOf(indexPage) - 1) * paging);
				stmt.setInt(i++, paging);
			}

			rs = stmt.executeQuery();

			while (rs.next()) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("data_point", rs.getString("ID"));
				map.put("object_identifier", rs.getString("OBJECT_IDENTIFIER"));
				map.put("oi_measure_name", rs.getString("oi_measure_name"));
				map.put("oi_measure_unit", rs.getString("OI_MEASURE_UNIT"));
				map.put("name", rs.getString("NAME"));
				map.put("channel", rs.getString("CHANNEL"));
				map.put("default_value", rs.getString("DEFAULT_VALUE"));
				map.put("max_value", rs.getString("MAX_VALUE"));
				map.put("min_value", rs.getString("MIN_VALUE"));
				map.put("measure_name", rs.getString("MEASURE_NAME"));
				map.put("measure_unit", rs.getString("MEASURE_UNIT"));
				map.put("measure_ratio", rs.getString("MEASURE_RATIO"));
				map.put("consumption", rs.getString("consumption"));
				map.put("device_id", rs.getString("DEVICE_ID"));
				map.put("type", rs.getString("TYPE"));
				if((rs.getString("MEASURE_NAME") == null || rs.getString("MEASURE_NAME").equals("")) && paging == 0){
					continue ;
				}
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
	public List<Map<String, String>> getRealNodeReadingPage(String deviceAdress, String instanceId, String indexPage, int paging) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		try {
			conn = datadb.getConnection();
			String sql = " select da.* ";
			sql += " from adapter a, device d ,bacnet_device_attribute da ";
			sql += " where a.id  = d.adapter_id and da.device_id = d.id and da.formula = '' ";
			sql += " and a.adapter_name = ? and d.device_address = ? and d.device_instanceid = ?  and type = 'data'  ";
			
			if (paging != 0) {
				sql += "  limit ? , ?  ";
			}
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, adapterClass);
			stmt.setString(2, deviceAdress);
			stmt.setString(3, instanceId);
			if (paging != 0) {
				stmt.setInt(4, (Integer.valueOf(indexPage) - 1) * paging);
				stmt.setInt(5, paging);
			}

			rs = stmt.executeQuery();

			while (rs.next()) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("data_point", rs.getString("ID"));
				map.put("object_identifier", rs.getString("OBJECT_IDENTIFIER"));
				map.put("oi_measure_name", rs.getString("oi_measure_name"));
				map.put("oi_measure_unit", rs.getString("OI_MEASURE_UNIT"));
				map.put("name", rs.getString("NAME"));
				map.put("channel", rs.getString("CHANNEL"));
				map.put("default_value", rs.getString("DEFAULT_VALUE"));
				map.put("max_value", rs.getString("MAX_VALUE"));
				map.put("min_value", rs.getString("MIN_VALUE"));
				map.put("measure_name", rs.getString("MEASURE_NAME"));
				map.put("measure_unit", rs.getString("MEASURE_UNIT"));
				map.put("measure_ratio", rs.getString("MEASURE_RATIO"));
				map.put("consumption", rs.getString("consumption"));
				map.put("device_id", rs.getString("DEVICE_ID"));
				map.put("type", rs.getString("TYPE"));
				if((rs.getString("MEASURE_NAME") == null || rs.getString("MEASURE_NAME").equals("")) && paging == 0){
					continue ;
				}
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
	public List<Map<String, String>> getNodeWrittingPage(String deviceAdress, String instanceId, String indexPage, int paging) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		try {
			conn = datadb.getConnection();
			String sql = " select da.* ";
			sql += " from adapter a, device d ,bacnet_device_attribute da ";
			sql += " where a.id  = d.adapter_id and da.device_id = d.id and da.formula = '' ";
			sql += " and a.adapter_name = ? and d.device_address = ? and d.device_instanceid = ? and type = 'setting' ";
			if (paging != 0) {
				sql += "  limit ? , ?  ";
			}
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, adapterClass);
			stmt.setString(2, deviceAdress);
			stmt.setString(3, instanceId);
			if (paging != 0) {
				stmt.setInt(4, (Integer.valueOf(indexPage) - 1) * paging);
				stmt.setInt(5, paging);
			}

			rs = stmt.executeQuery();

			while (rs.next()) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("data_point", rs.getString("ID"));
				map.put("object_identifier", rs.getString("OBJECT_IDENTIFIER"));
				map.put("oi_measure_name", rs.getString("oi_measure_name"));
				map.put("oi_measure_unit", rs.getString("OI_MEASURE_UNIT"));
				map.put("name", rs.getString("NAME"));
				map.put("channel", rs.getString("CHANNEL"));
				map.put("default_value", rs.getString("DEFAULT_VALUE"));
				map.put("max_value", rs.getString("MAX_VALUE"));
				map.put("min_value", rs.getString("MIN_VALUE"));
				map.put("measure_name", rs.getString("MEASURE_NAME"));
				map.put("measure_unit", rs.getString("MEASURE_UNIT"));
				map.put("measure_ratio", rs.getString("MEASURE_RATIO"));
				map.put("consumption", rs.getString("consumption"));
				map.put("device_id", rs.getString("DEVICE_ID"));
				map.put("type", rs.getString("TYPE"));
				if((rs.getString("MEASURE_NAME") == null || rs.getString("MEASURE_NAME").equals("")) && paging == 0){
					continue ;
				}
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

	public List<Map<String, String>> getAllAttributes(String deviceAdress, String instanceId) {
		/*
		 * Connection conn = null; PreparedStatement stmt = null; ResultSet rs = null; List<Map<String, String>> result = new ArrayList<Map<String,
		 * String>>(); try { conn = datadb.getConnection(); String sql = " select data_point, group_concat( "; sql +=
		 * " DISTINCT CONCAT(da.ATTRIBUTE_NAME,':',da.ATTRIBUTE_VALUE) "; sql += " ) as value  "; sql +=
		 * " From adapter a, device d , bacnet_device_attribute da"; sql +=
		 * " WHERE a.id = d.adapter_id and a.adapter_name  = ? and da.device_id = d.id "; sql +=
		 * " and d.device_address = ? and d.device_instanceid = ?"; sql += " group by da.data_point "; stmt = conn.prepareStatement(sql);
		 * stmt.setString(1, adapterClass); stmt.setString(2, deviceAdress); stmt.setString(3, instanceId); rs = stmt.executeQuery(); while
		 * (rs.next()) { Map<String, String> map = new HashMap<String, String>(); String value = rs.getString("value"); String[] listAttribute =
		 * value.split(","); for (String attribute : listAttribute) { String mapAtrribute[] = attribute.split(":"); if (mapAtrribute.length > 1)
		 * map.put(mapAtrribute[0], mapAtrribute[1]); } map.put("data_point", rs.getString("data_point")); result.add(map); } } catch (SQLException e)
		 * { mLogger.error("SQLException", e); } catch (Exception e) { mLogger.error("SQLException", e); } finally { datadb.close(rs); datadb.close(stmt); datadb.close(conn); }
		 * return result;
		 */

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		try {
			conn = datadb.getConnection();
			String sql = " select distinct da.* ";
			sql += " from adapter a, device d ,bacnet_device_attribute da ";
			sql += " where a.id  = d.adapter_id and da.device_id = d.id  ";
			sql += " and a.adapter_name = ? and d.device_address = ? and d.device_instanceid = ? ";
			

			stmt = conn.prepareStatement(sql);
			stmt.setString(1, adapterClass);
			stmt.setString(2, deviceAdress);
			stmt.setString(3, instanceId);

			rs = stmt.executeQuery();

			while (rs.next()) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("data_point", rs.getString("ID"));
				map.put("object_identifier", rs.getString("OBJECT_IDENTIFIER"));
				map.put("oi_measure_name", rs.getString("oi_measure_name"));
				map.put("oi_measure_unit", rs.getString("OI_MEASURE_UNIT"));
				map.put("name", rs.getString("NAME"));
				map.put("channel", rs.getString("CHANNEL"));
				map.put("default_value", rs.getString("DEFAULT_VALUE"));
				map.put("max_value", rs.getString("MAX_VALUE"));
				map.put("min_value", rs.getString("MIN_VALUE"));
				map.put("measure_name", rs.getString("MEASURE_NAME"));
				map.put("measure_unit", rs.getString("MEASURE_UNIT"));
				map.put("measure_ratio", rs.getString("MEASURE_RATIO"));
				map.put("consumption", rs.getString("consumption"));
				map.put("formula", rs.getString("FORMULA"));
				map.put("type", rs.getString("TYPE"));
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

	public List<Map<String, String>> getActivedNodeAttributes(String deviceAdress, String instanceId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		try {
			conn = datadb.getConnection();
			/*
			 * String sql = " select data_point, group_concat( "; sql += " DISTINCT CONCAT(da.ATTRIBUTE_NAME,':',da.ATTRIBUTE_VALUE) "; sql +=
			 * " ) as value  "; sql += " From adapter a, device d , device_attribute da"; sql +=
			 * " WHERE a.id = d.adapter_id and a.adapter_name  = ? and da.device_id = d.id "; sql +=
			 * " and d.device_address = ? and d.device_instanceid = ?" ; sql += " and da.data_point in ( "; sql +=
			 * " select distinct data_point from device_attribute where attribute_name = 'channel' and attribute_value != '' "; sql += ")"; sql
			 * +=" group by da.data_point ";
			 */

			String sql = " select da.* from adapter a , device d ,bacnet_device_attribute da ";
			sql += " where a.id = d.adapter_id and d.id = da.device_id  ";
			sql += " and da.channel != '' and a.adapter_name = ? and d.device_address = ? ";
			sql += " and d.device_instanceid = ? ";

			stmt = conn.prepareStatement(sql);
			stmt.setString(1, adapterClass);
			stmt.setString(2, deviceAdress);
			stmt.setString(3, instanceId);

			rs = stmt.executeQuery();

			while (rs.next()) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("object_identifier", rs.getString("OBJECT_IDENTIFIER"));
				map.put("oi_measure_name", rs.getString("oi_measure_name"));
				map.put("oi_measure_unit", rs.getString("OI_MEASURE_UNIT"));
				map.put("name", rs.getString("NAME"));
				map.put("channel", rs.getString("CHANNEL"));
				map.put("default_value", rs.getString("DEFAULT_VALUE"));
				map.put("max_value", rs.getString("MAX_VALUE"));
				map.put("min_value", rs.getString("MIN_VALUE"));
				map.put("measure_name", rs.getString("MEASURE_NAME"));
				map.put("measure_unit", rs.getString("MEASURE_UNIT"));
				map.put("measure_ratio", rs.getString("MEASURE_RATIO"));
				map.put("consumption", rs.getString("consumption"));
				map.put("data_point", rs.getString("id"));
				map.put("formula", rs.getString("FORMULA"));
				map.put("type", rs.getString("TYPE"));
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

	public Map<String, List<Map<String, String>>> getNodeAttributesByChannelGroup(String deviceAdress, String instanceId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Map<String, List<Map<String, String>>> result = new HashMap<String, List<Map<String, String>>>();
		Map<Integer, List<Integer>> channelAndDataPoint = new HashMap<Integer, List<Integer>>();
		List<Integer> dataPoints = getDataPointOfDevice(deviceAdress, instanceId);
		try {
			conn = datadb.getConnection();
			String sql = " select distinct da.* ";
			sql += " From adapter a, device d , device_attribute da";
			sql += " WHERE a.id = d.adapter_id and a.adapter_name  = ? and da.device_id = d.id ";
			sql += " and d.device_address = ? and d.device_instanceid = ?  and da.data_point = ? ";
			stmt = conn.prepareStatement(sql);
			List<Map<String, String>> maps = new ArrayList<Map<String, String>>();
			for (Integer channel : channelAndDataPoint.keySet()) {
				Map<String, String> map = new HashMap<String, String>();
				for (Integer dataPoint : dataPoints) {
					stmt.setString(1, adapterClass);
					stmt.setString(2, deviceAdress);
					stmt.setString(3, instanceId);
					stmt.setInt(4, dataPoint);
					rs = stmt.executeQuery();
					while (rs.next()) {
						map.put(rs.getString("device_attribue"), rs.getString("device_value"));
					}
					maps.add(map);
				}
				result.put(channel.toString(), maps);
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

	public Integer numberOfVirtualNode(String deviceAdress, String instanceId,String type) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int count = 0;
		try {
			conn = datadb.getConnection();
			String sql = " select count(distinct da.id) as number ";
			sql += " From adapter a, device d , bacnet_device_attribute da";
			sql += " WHERE a.id = d.adapter_id and a.adapter_name  = ? and da.device_id = d.id ";
			sql += " and d.device_address = ? and d.device_instanceid = ? and da.FORMULA != ''  ";
		
			
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, adapterClass);
			stmt.setString(2, deviceAdress);
			stmt.setString(3, instanceId);
			
			rs = stmt.executeQuery();

			while (rs.next()) {
				count = rs.getInt("number");
				break;
			}
		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		} finally {
			datadb.close(rs);
			datadb.close(stmt);
			datadb.close(conn);
		}
		return count;
	}

	public List<Map<String, String>> getVirtualNodeReadingPage(String deviceAdress, String instanceId, String indexPage, int paging) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		try {
			conn = datadb.getConnection();
			String sql = " select da.* ";
			sql += " from adapter a, device d ,bacnet_device_attribute da ";
			sql += " where a.id  = d.adapter_id and da.device_id = d.id and da.formula != '' ";
			sql += " and a.adapter_name = ? and d.device_address = ? and d.device_instanceid = ? ";
			if (paging != 0) {
				sql += "  limit ? , ?  ";
			}
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, adapterClass);
			stmt.setString(2, deviceAdress);
			stmt.setString(3, instanceId);
			if (paging != 0) {
				stmt.setInt(4, (Integer.valueOf(indexPage) - 1) * paging);
				stmt.setInt(5, paging);
			}

			rs = stmt.executeQuery();

			while (rs.next()) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("object_identifier", rs.getString("OBJECT_IDENTIFIER"));
				map.put("oi_measure_name", rs.getString("oi_measure_name"));
				map.put("oi_measure_unit", rs.getString("OI_MEASURE_UNIT"));
				map.put("name", rs.getString("NAME"));
				map.put("channel", rs.getString("CHANNEL"));
				map.put("default_value", rs.getString("DEFAULT_VALUE"));
				map.put("max_value", rs.getString("MAX_VALUE"));
				map.put("min_value", rs.getString("MIN_VALUE"));
				map.put("measure_name", rs.getString("MEASURE_NAME"));
				map.put("measure_unit", rs.getString("MEASURE_UNIT"));
				map.put("measure_ratio", rs.getString("MEASURE_RATIO"));
				map.put("consumption", rs.getString("consumption"));
				map.put("formula", rs.getString("FORMULA"));
				map.put("data_point", rs.getString("id"));
				map.put("type", rs.getString("TYPE"));
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

	public Integer numberOfRealNode(String deviceAdress, String instanceId,String type) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int count = 0;
		try {
			conn = datadb.getConnection();
			String sql = " select count(distinct da.id) as number ";
			sql += " From adapter a, device d , bacnet_device_attribute da";
			sql += " WHERE a.id = d.adapter_id and a.adapter_name  = ? and da.device_id = d.id ";
			sql += " and d.device_address = ? and d.device_instanceid = ? and da.formula = '' ";
			if(!"null".equals(type))
				sql += " and  da.type = ? ";
			
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, adapterClass);
			stmt.setString(2, deviceAdress);
			stmt.setString(3, instanceId);
			if(!"null".equals(type))
				stmt.setString(4, type);

			rs = stmt.executeQuery();

			while (rs.next()) {
				count = rs.getInt("number");
				break;
			}
		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		} finally {
			datadb.close(rs);
			datadb.close(stmt);
			datadb.close(conn);
		}
		return count;
	}
	public Integer numberOfReadNode(String deviceAdress, String instanceId,String type) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int count = 0;
		try {
			conn = datadb.getConnection();
			String sql = " select count(distinct da.id) as number ";
			sql += " From adapter a, device d , bacnet_device_attribute da";
			sql += " WHERE a.id = d.adapter_id and a.adapter_name  = ? and da.device_id = d.id ";
			sql += " and d.device_address = ? and d.device_instanceid = ? and da.formula = '' and da.type = 'data' ";
			if(!"null".equals(type))
				sql += " and da.type = ? ";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, adapterClass);
			stmt.setString(2, deviceAdress);
			stmt.setString(3, instanceId);
			if(!"null".equals(type))
				stmt.setString(4, type);

			rs = stmt.executeQuery();

			while (rs.next()) {
				count = rs.getInt("number");
				break;
			}
		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		} finally {
			datadb.close(rs);
			datadb.close(stmt);
			datadb.close(conn);
		}
		return count;
	}
	public Integer numberOfWriteNode(String deviceAdress, String instanceId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int count = 0;
		try {
			conn = datadb.getConnection();
			String sql = " select count(distinct da.id) as number ";
			sql += " From adapter a, device d , bacnet_device_attribute da";
			sql += " WHERE a.id = d.adapter_id and a.adapter_name  = ? and da.device_id = d.id ";
			sql += " and d.device_address = ? and d.device_instanceid = ? and da.formula = '' and type = 'setting' ";
			
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, adapterClass);
			stmt.setString(2, deviceAdress);
			stmt.setString(3, instanceId);
			
			rs = stmt.executeQuery();

			while (rs.next()) {
				count = rs.getInt("number");
				break;
			}
		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		} finally {
			datadb.close(rs);
			datadb.close(stmt);
			datadb.close(conn);
		}
		return count;
	}


	public List<Integer> getDataPointOfDevice(String deviceAdress, String instanceId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Integer> result = new ArrayList<Integer>();
		try {
			conn = datadb.getConnection();
			String sql = " select distinct da.data_point ";
			sql += " From adapter a, device d , device_attribute da";
			sql += " WHERE a.id = d.adapter_id and a.adapter_name  = ? and da.device_id = d.id ";
			sql += " and d.device_address = ? and d.device_instanceid =  ? ";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, adapterClass);
			stmt.setString(2, deviceAdress);
			stmt.setString(3, instanceId);

			rs = stmt.executeQuery();
			while (rs.next()) {
				result.add(rs.getInt("data_point"));
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

	public boolean insertValidationRule(String deviceIntanceId, String deviceAddress, String expressions, String action,
			String dataPoint) {
		Connection conn = null;
		PreparedStatement stmt = null;
		String adapterId = getAdapterSettings().get("id");
		String deviceId = getDeviceId(deviceAddress, deviceIntanceId, adapterId);
		boolean result = false;
		try {
			conn = datadb.getConnection();
			String sql = " INSERT INTO PUBLIC.VALIDATION  ";
			sql += " ( DEVICE_ID, DATA_POINT, CONDITION,ACTION) ";
			sql += " VALUES (?, ? , ?, ?) ";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, Integer.valueOf(deviceId));
			stmt.setInt(2, Integer.valueOf(dataPoint));
			stmt.setString(3, expressions);
			stmt.setString(4, action);
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

	public boolean updateValidationRule(String expressions, String action, String id) {
		Connection conn = null;
		PreparedStatement stmt = null;

		boolean result = false;
		try {
			conn = datadb.getConnection();
			String sql = " UPDATE PUBLIC.VALIDATION  ";
			sql += " Set CONDITION = ? ,ACTION = ? ";
			sql += " WHERE ID  = ?  ";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, expressions);
			stmt.setString(2, action);
			stmt.setInt(3, Integer.valueOf(id));
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
	public boolean checkUniqueObjectIdentify(String objectIdentifier,String deviceInstanceId, String deviceAddress) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
	
		try {
			conn = datadb.getConnection();
			String sql = " select id  ";
			sql += " From BACNET_DEVICE_ATTRIBUTE da ,DEVICE d";
			sql += " WHERE da.OBJECT_IDENTIFIER  = ? and da.DEVICE_ID  = d.id  and d.DEVICE_INSTANCEID = ? and d.DEVICE_ADDRESS = ? ";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, objectIdentifier);
			stmt.setString(2, deviceInstanceId);
			stmt.setString(3, deviceAddress);
			
			
			rs = stmt.executeQuery();
			while(rs.next()){
				if(rs.getString("id") != null){
					return true ;
				}
			}
		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		} catch (Exception e) {
			mLogger.error("Exception", e);
		} finally {
			datadb.close(stmt);
			datadb.close(conn);

		}

		return false;
	}

	public List<Map<String, String>> getValidationRule(String instanceId, String deviceAddress) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String adapterId = getAdapterSettings().get("id");
		String deviceId = getDeviceId(deviceAddress, instanceId, adapterId);
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		try {
			conn = datadb.getConnection();
			String sql = " SELECT V.condition,V.action,V.id,da.name FROM PUBLIC.VALIDATION V";
			sql += " , bacnet_device_attribute da ";
			sql += " WHERE V.DEVICE_ID = ? ";
			sql += " and  da.device_id = V.device_id  and v.data_point = da.id ";
			/*
			 * sql += " da.data_point  = v.data_point "; sql += " and da.attribute_name = 'name' ";
			 */
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, Integer.valueOf(deviceId));
			rs = stmt.executeQuery();
			while (rs.next()) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("condition", rs.getString("condition"));
				map.put("action", rs.getString("action"));
				map.put("id", rs.getString("id"));
				map.put("name", rs.getString("name"));

				result.add(map);
			}

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

	public Map<String, String> getValidationRuleByDataPoint(int dataPoint) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		String datapointid = String.valueOf(dataPoint);
		Map<String, String> map = new HashMap<String, String>();
		try {
			conn = datadb.getConnection();
			String sql = " SELECT V.condition,V.action,V.id FROM PUBLIC.VALIDATION V ";
			sql += " WHERE V.DATA_POINT = ? ";

			stmt = conn.prepareStatement(sql);
			stmt.setString(1, datapointid);
			rs = stmt.executeQuery();
			while (rs.next()) {

				map.put("condition", rs.getString("condition"));
				map.put("action", rs.getString("action"));
				map.put("id", rs.getString("id"));

				return map;
			}

		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		} catch (Exception e) {
			mLogger.error("Exception", e);
		} finally {
			datadb.close(stmt);
			datadb.close(conn);

		}

		return map;
	}

	private Integer maxDataPoint(String deviceId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		List<Integer> list = new ArrayList<Integer>();

		try {
			conn = datadb.getConnection();
			String sql = " select distinct data_point as data_point from device_attribute";
			sql += " WHERE DEVICE_ID = ? ";

			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, Integer.valueOf(deviceId));
			rs = stmt.executeQuery();
			while (rs.next()) {
				int dataPoint = rs.getInt("data_point");
				list.add(dataPoint);
			}

		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		} catch (Exception e) {
			mLogger.error("Exception", e);
		} finally {
			datadb.close(stmt);
			datadb.close(conn);

		}

		return Collections.max(list);
	}
	
	public List<Map<String,String>> getAllDeviceAttributes(){
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Map<String,String>> result = new ArrayList<Map<String,String>>();
		try {
			conn = datadb.getConnection();
			String sql = " select da.* ";
			sql += " From  device d ,adapter a,"+ attributedTable + " da";
			sql += " WHERE ";
			sql += " a.id = d.adapter_id  and d.id = da.device_id  ";
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("data_point", rs.getString("ID"));
				map.put("object_identifier", rs.getString("OBJECT_IDENTIFIER"));
				map.put("oi_measure_name", rs.getString("oi_measure_name"));
				map.put("oi_measure_unit", rs.getString("OI_MEASURE_UNIT"));
				map.put("name", rs.getString("NAME"));
				map.put("channel", rs.getString("CHANNEL"));
				map.put("default_value", rs.getString("DEFAULT_VALUE"));
				map.put("max_value", rs.getString("MAX_VALUE"));
				map.put("min_value", rs.getString("MIN_VALUE"));
				map.put("measure_name", rs.getString("MEASURE_NAME"));
				map.put("measure_unit", rs.getString("MEASURE_UNIT"));
				map.put("measure_ratio", rs.getString("MEASURE_RATIO"));
				map.put("consumption", rs.getString("CONSUMPTION"));
				map.put("formula", rs.getString("FORMULA"));
				map.put("device_id", rs.getString("DEVICE_ID"));
				map.put("type", rs.getString("type"));
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
	
}
