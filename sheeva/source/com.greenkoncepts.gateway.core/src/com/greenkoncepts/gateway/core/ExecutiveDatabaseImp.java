package com.greenkoncepts.gateway.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greenkoncepts.gateway.api.database.DbService;

public class ExecutiveDatabaseImp {
	protected DbService datadb = null;
	protected Logger mLogger = LoggerFactory.getLogger(getClass().getSimpleName());
	
	synchronized public void setDatabaseService(DbService db) {
		datadb = db;
	}

	public void createGatewayStatus() {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = datadb.getConnection();
			String sql = " CREATE TABLE IF NOT EXISTS PUBLIC.GATEWAY_STATUS ";
			sql += " (ID INTEGER  GENERATED ALWAYS AS IDENTITY ";
			sql += " (START WITH 1), ";
			sql += " up_time VARCHAR(20), ";
			sql += " start_time VARCHAR(20), ";
			sql += " total_reading_data varchar(20), ";
			sql += " total_sent_data varchar(20), ";
			sql += " start_time_reading varchar(20), ";
			sql += " start_time_sending varchar(20), ";
			sql += " network_errors varchar(20), ";
			sql += " device_errors varchar(20), ";
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
		Map<String,String> map = getGatewayStatus();
		if(map == null || map.size() ==0){
			String timeStamString = String.valueOf(System.currentTimeMillis());
			insertGatewayStatus("0","0","0","0",timeStamString, timeStamString, "0","0");
		}
		

	}


	public boolean insertGatewayStatus(String uptime,String startTime ,
		 String totalReadingData, String totalSentData, String startTimeReading, String startTimeSending,
			String networkErrors,String deviceErrors) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean result = false;
		try {
			conn = datadb.getConnection();
			String sql = " INSERT INTO PUBLIC.GATEWAY_STATUS  ";
			sql += " ( up_time ,start_time, total_reading_data ,total_sent_data , start_time_reading, start_time_sending,network_errors ,device_errors) ";
			sql += " VALUES (?, ? , ? ,? , ? , ?, ?, ?) ";
			int i = 1;
			stmt = conn.prepareStatement(sql);
			stmt.setString(i++, uptime);
			stmt.setString(i++, startTime);
			stmt.setString(i++, totalReadingData);
			stmt.setString(i++, totalSentData);
			stmt.setString(i++, startTimeReading);
			stmt.setString(i++, startTimeSending);
			stmt.setString(i++, networkErrors);
			stmt.setString(i++, deviceErrors);
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
	public boolean updateGatewayStatus( Long totalReadData, Long totalSentData) {
		
		if(totalReadData != null){
			 updateTotalRead(totalReadData);
		}
		if(totalSentData != null){
			 updateTotalSent(totalSentData);
		}
		return true ;
	}
	public boolean updateTotalRead(Long totalReadData){
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean result = false;
		
		try {
			conn = datadb.getConnection();
			if (totalReadData == 0) {
				String timeStamp = String.valueOf(System.currentTimeMillis());
				String sql = " update PUBLIC.GATEWAY_STATUS  ";
				sql += " set  total_reading_data = ?,  start_time_reading = ? ";
				stmt = conn.prepareStatement(sql);
				stmt.setLong(1, totalReadData);
				stmt.setString(2, timeStamp);
			} else {
				String sql = " update PUBLIC.GATEWAY_STATUS  ";
				sql += " set  total_reading_data = ? ";
				stmt = conn.prepareStatement(sql);
				stmt.setLong(1, totalReadData);
			}
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
	public boolean updateTotalSent(Long totalSentData){
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean result = false;
		try {
			conn = datadb.getConnection();
			if (totalSentData == 0) {
				String timeStamp = String.valueOf(System.currentTimeMillis());
				String sql = " update PUBLIC.GATEWAY_STATUS  ";
				sql += " set  total_sent_data = ? , start_time_sending = ? ";
				stmt = conn.prepareStatement(sql);
				stmt.setLong(1, totalSentData);
				stmt.setString(2, timeStamp);
			} else {
				String sql = " update PUBLIC.GATEWAY_STATUS  ";
				sql += " set  total_sent_data = ? ";
				stmt = conn.prepareStatement(sql);
				stmt.setLong(1, totalSentData);
			}
			
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
	
	public void createBridge() {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = datadb.getConnection();
			String sql = " CREATE TABLE IF NOT EXISTS PUBLIC.BRIDGE ";
			sql += " (ID INTEGER  GENERATED ALWAYS AS IDENTITY ";
			sql += " (START WITH 1), ";
			sql += " bridge_mode  VARCHAR(20), ";
			sql += " log_mode VARCHAR(5), ";
			sql += " customer_id VARCHAR(5),";
			sql += " gateway_id VARCHAR(20), ";
			sql += " remote_host varchar(50), ";
			sql += " remote_port varchar(10), ";
			sql += " protocol_version varchar(5), ";
			sql += " check_period_query varchar(10), ";
			sql += " check_period_internet varchar(10), ";
			sql += " delay_start_query varchar(10), ";
			sql += " delay_start_internet varchar(10), ";
			sql += " metadata_interval_time varchar(10), ";
			sql += " buffer_send_limit varchar(15), ";
			sql += " socket_err_max varchar(10), ";
			sql += " mqtt_client_id varchar(100), ";
			sql += " mqtt_topic varchar(20), ";
			sql += " mqtt_sub_topic varchar(100), ";
			sql += " mqtt_ssl varchar(5), ";
			sql += " mqtt_qos  VARCHAR(5), ";
			sql += " mqtt_clean_session  VARCHAR(5), ";
			sql += " mqtt_keep_alive_interval  varchar(10), ";
			sql += " mqtt_connection_timeout  varchar(10), ";
			sql += " rest_host  varchar(50), ";
			sql += " rest_port  varchar(10), ";
			sql += " last_modified  varchar(20), ";
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
	
	public boolean insertBridge(String bridgeMode,String logMode,String customerId,String gatewayId,
			String remoteHost,String remotePort,String protocolVersion,String checkPeriodQuery,
			String checkPeriodInternet, String delayStartQuery,String delayStartInternet, String metadata_interval_time,
			String bufferSendLimit,String socketErrMax,String mqttClientId,
			String mqttTopic,String mqttSubTopic,String mqttSsl,String mqttQos,String mqttCleanSession,
			String mqttKeepAliveInterval,String mqttConnectionTimeout,String restHost,String restPort
			,String lastModified
			) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean result = false;
		try {
			conn = datadb.getConnection();
			String sql = " INSERT INTO PUBLIC.BRIDGE  ";
			sql += " ( bridge_mode,log_mode, customer_id, gateway_id,remote_host,remote_port,protocol_version,check_period_query ";
			sql += " ,check_period_internet,delay_start_query,delay_start_internet,metadata_interval_time,buffer_send_limit,socket_err_max,mqtt_client_id ";
			sql += " ,mqtt_topic,mqtt_sub_topic,mqtt_ssl,mqtt_qos,mqtt_clean_session,mqtt_keep_alive_interval,mqtt_connection_timeout ";
			sql += " ,rest_host,rest_port,last_modified )";
			sql += " VALUES (?,?,?,?,? ,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
			stmt = conn.prepareStatement(sql);
			int i = 1 ;
			stmt.setString(i++, bridgeMode);
			stmt.setString(i++, logMode);
			stmt.setString(i++, customerId);
			stmt.setString(i++, gatewayId);
			stmt.setString(i++, remoteHost);
			stmt.setString(i++, remotePort);
			stmt.setString(i++, protocolVersion);
			stmt.setString(i++, checkPeriodQuery);
			
			stmt.setString(i++, checkPeriodInternet);
			stmt.setString(i++, delayStartQuery);
			stmt.setString(i++, delayStartInternet);
			stmt.setString(i++, metadata_interval_time);
			stmt.setString(i++, bufferSendLimit);
			stmt.setString(i++, socketErrMax);
			stmt.setString(i++, mqttClientId);
			stmt.setString(i++, mqttTopic);
			stmt.setString(i++, mqttSubTopic);
			
			stmt.setString(i++, mqttSsl);
			stmt.setString(i++, mqttQos);
			stmt.setString(i++, mqttCleanSession);
			stmt.setString(i++, mqttKeepAliveInterval);
			stmt.setString(i++, mqttConnectionTimeout);
			stmt.setString(i++, restHost);
			stmt.setString(i++, restPort);
			stmt.setString(i++, lastModified);
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

	public Map<String, String> getGatewayStatus() {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Map<String, String> map = new HashMap<String, String>();
		try {
			conn = datadb.getConnection();
			String sql = " select * ";
			sql += " from GATEWAY_STATUS ";
		
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				map.put("up_time", rs.getString("up_time"));
				map.put("total_reading_data", rs.getString("total_reading_data") == null ? "0" :  rs.getString("total_reading_data"));
				map.put("total_sent_data", rs.getString("total_sent_data") == null ? "0" : rs.getString("total_sent_data"));
				map.put("start_time_reading", rs.getString("start_time_reading"));
				map.put("start_time_sending", rs.getString("start_time_sending"));
				map.put("network_errors", rs.getString("network_errors"));
				map.put("device_errors", rs.getString("device_errors"));
				map.put("start_time", rs.getString("start_time"));
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
		return map;
	}
	 public Map<String, String> getCurrentBridgeSettings(){
		 String mode = getMode();
		 if (!"".equals(mode)) {
			 return getBridgeSettingsBy(mode);
		 }
		 return new HashMap<String, String>();
		
	 }
	 
	 public String getMode(){
		 Connection conn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			String mode =  "";
			try {
				conn = datadb.getConnection();
				String sql = " select bridge_mode ";
				sql += " from BRIDGE  ";
			
				stmt = conn.prepareStatement(sql);
				rs = stmt.executeQuery();
				
				while (rs.next()) {
					mode = rs.getString("bridge_mode");
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
			return mode;
	 }
	 public Map<String, String> getBridgeSettingsBy(String mode){
		 
		 Connection conn = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			Map<String, String> map = new HashMap<String, String>();
			try {
				conn = datadb.getConnection();
				String sql = " select * ";
				sql += " from BRIDGE";
				stmt = conn.prepareStatement(sql);
				rs = stmt.executeQuery();
				map.put("bridge_mode", mode);
				if("socket".equals(mode)){
					while (rs.next()) {
						map.put("customer_id", rs.getString("customer_id"));
						map.put("gateway_id", rs.getString("gateway_id"));
						map.put("remote_host", rs.getString("remote_host"));
						map.put("remote_port", rs.getString("remote_port"));
						map.put("protocol_version", rs.getString("protocol_version"));
						map.put("check_period_query", rs.getString("check_period_query"));
						map.put("check_period_internet", rs.getString("check_period_internet"));
						map.put("delay_start_query", rs.getString("delay_start_query"));
						map.put("delay_start_internet", rs.getString("delay_start_internet"));
						map.put("buffer_send_limit", rs.getString("buffer_send_limit"));
						map.put("socket_err_max", rs.getString("socket_err_max"));
						map.put("log_mode", rs.getString("log_mode"));
						map.put("last_modified", rs.getString("last_modified"));
					}
				}else {
					while (rs.next()) {
						
						map.put("log_mode", rs.getString("log_mode"));
						map.put("customer_id", rs.getString("customer_id"));
						map.put("gateway_id", rs.getString("gateway_id"));
						map.put("remote_host", rs.getString("remote_host"));
						map.put("remote_port", rs.getString("remote_port"));
						
						map.put("mqtt_client_id", rs.getString("mqtt_client_id"));
						map.put("mqtt_topic", rs.getString("mqtt_topic"));
						map.put("mqtt_sub_topic", rs.getString("mqtt_sub_topic"));
						map.put("mqtt_ssl", rs.getString("mqtt_ssl"));
						
						map.put("check_period_query", rs.getString("check_period_query"));
						map.put("check_period_internet", rs.getString("check_period_internet"));
						map.put("delay_start_query", rs.getString("delay_start_query"));
						map.put("delay_start_internet", rs.getString("delay_start_internet"));
						map.put("metadata_interval_time", rs.getString("metadata_interval_time"));
						
						map.put("mqtt_qos", rs.getString("mqtt_qos"));
						map.put("mqtt_clean_session", rs.getString("mqtt_clean_session"));
						map.put("mqtt_keep_alive_interval", rs.getString("mqtt_keep_alive_interval"));
						map.put("mqtt_connection_timeout", rs.getString("mqtt_connection_timeout"));
						map.put("rest_host", rs.getString("rest_host"));
						map.put("rest_port", rs.getString("rest_port"));
						map.put("last_modified", rs.getString("last_modified"));
						
						map.put("buffer_send_limit", rs.getString("buffer_send_limit"));
						
					}
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
			return map;
		 
	 }
	 public boolean updateBridgeSettings(Map<String, String> settings){
		 System.out.println("settings : " + settings);
		 Connection conn = null;
			PreparedStatement stmt = null;
			boolean result = false;
			try {
				conn = datadb.getConnection();
				String mode = settings.get("bridge_mode");
				String sql = "";
				if("socket".equals(mode)){
					sql = " update PUBLIC.BRIDGE  ";
					sql += " set bridge_mode = ? , customer_id = ?,gateway_id = ? , remote_host = ?, remote_port = ? ,protocol_version = ?,check_period_query = ? ,check_period_internet = ?,delay_start_query = ? ";
					sql += " ,delay_start_internet = ? , buffer_send_limit = ? , log_mode = ? , last_modified = ? , socket_err_max = ? " ;

				}else {
					sql = " update PUBLIC.BRIDGE  ";
					sql += " set  bridge_mode = ? ,  customer_id = ?,gateway_id = ? , remote_host = ?, remote_port = ? ,mqtt_topic = ?,mqtt_sub_topic = ? ,mqtt_ssl = ?,mqtt_qos = ? ";
					sql += ",mqtt_clean_session = ? , mqtt_keep_alive_interval = ? , mqtt_connection_timeout = ? ,mqtt_client_id = ? ,check_period_query = ? " ;
					sql += ",check_period_internet = ? ,delay_start_query = ? , delay_start_internet = ?, metadata_interval_time = ?, buffer_send_limit = ?, rest_host = ? , rest_port = ? , log_mode = ? , last_modified = ? ";


				}
				int i = 1 ;
				stmt = conn.prepareStatement(sql);
				if("socket".equals(mode)){
					stmt.setString(i++, settings.get("bridge_mode"));
					stmt.setString(i++, settings.get("customer_id"));
					stmt.setString(i++, settings.get("gateway_id"));
					stmt.setString(i++, settings.get("remote_host"));
					stmt.setString(i++, settings.get("remote_port"));
					
					stmt.setString(i++, settings.get("protocol_version"));
					stmt.setString(i++, settings.get("check_period_query"));
					stmt.setString(i++, settings.get("check_period_internet"));
					stmt.setString(i++, settings.get("delay_start_query"));
					
					stmt.setString(i++, settings.get("delay_start_internet"));
					stmt.setString(i++, settings.get("buffer_send_limit"));
					stmt.setString(i++, settings.get("log_mode"));
					stmt.setString(i++, settings.get("last_modified"));
					stmt.setString(i++, settings.get("socket_err_max"));
				}else{
					stmt.setString(i++, settings.get("bridge_mode"));
					stmt.setString(i++, settings.get("customer_id"));
					stmt.setString(i++, settings.get("gateway_id"));
					stmt.setString(i++, settings.get("remote_host"));
					stmt.setString(i++, settings.get("remote_port"));
					
					stmt.setString(i++, settings.get("mqtt_topic"));
					stmt.setString(i++, settings.get("mqtt_sub_topic"));
					stmt.setString(i++, settings.get("mqtt_ssl"));
					stmt.setString(i++, settings.get("mqtt_qos"));
					
					stmt.setString(i++, settings.get("mqtt_clean_session"));
					stmt.setString(i++, settings.get("mqtt_keep_alive_interval"));
					stmt.setString(i++, settings.get("mqtt_connection_timeout"));
					stmt.setString(i++, settings.get("mqtt_client_id"));
					stmt.setString(i++, settings.get("check_period_query"));
					
					stmt.setString(i++, settings.get("check_period_internet"));
					stmt.setString(i++, settings.get("delay_start_query"));
					stmt.setString(i++, settings.get("delay_start_internet"));
					stmt.setString(i++, settings.get("metadata_interval_time"));
					
					stmt.setString(i++, settings.get("buffer_send_limit"));
					stmt.setString(i++, settings.get("rest_host") == null ? "" : settings.get("rest_host"));
					stmt.setString(i++, settings.get("rest_port") == null ? "" : settings.get("rest_port"));
					stmt.setString(i++, settings.get("log_mode"));
					stmt.setString(i++, settings.get("last_modified"));
				}
				
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
}
	
