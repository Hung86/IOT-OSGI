package com.greenkoncepts.gateway.schedule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greenkoncepts.gateway.api.database.DbService;

public class ExecutiveDatabaseImp {
	protected DbService datadb = null;
	protected Logger mLogger = LoggerFactory.getLogger(getClass().getSimpleName());

	public ExecutiveDatabaseImp(DbService datadb) {
		this.datadb = datadb;
	}

	public void createNodeObjectTable() {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = datadb.getConnection();
			String sql = " CREATE TABLE IF NOT EXISTS PUBLIC.SCHEDULER_NODE_OBJECT "
					+ " (ID INTEGER  GENERATED ALWAYS AS IDENTITY (START WITH 1), "
					+ " scheduler_id INT, category VARCHAR(5), device_id VARCHAR(5), "
					+ " channel_id VARCHAR(5), subchannel_id VARCHAR(5), "
					+ " PRIMARY KEY (ID));";
			stmt = conn.prepareStatement(sql);
			stmt.executeUpdate();
		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		} catch (Exception e) {
			mLogger.error("Exception", e);
		} finally {
			if (stmt != null) {
				datadb.close(stmt);
			}
			if (conn != null) {
				datadb.close(conn);
			}
		}
	}

	public void createSchedulerObjectTable() {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = datadb.getConnection();
			String sql = " CREATE TABLE IF NOT EXISTS PUBLIC.SCHEDULER_OBJECT ";
			sql += " (ID INT NOT NULL, ";
			sql += " groupid INT, ";
			sql += " minute INT, ";
			sql += " hour INT, ";
			sql += " day_of_week VARCHAR(50), ";
			sql += " relay_status INT, ";
			sql += " ao_value INT, ";
			sql += " start_time TIMESTAMP, ";
			sql += " end_time TIMESTAMP, ";
			sql += " PRIMARY KEY (ID) );";
			stmt = conn.prepareStatement(sql);
			stmt.executeUpdate();

		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		} catch (Exception e) {
			mLogger.error("Exception", e);
		} finally {
			if (stmt != null) {
				datadb.close(stmt);
			}
			if (conn != null) {
				datadb.close(conn);
			}
		}
	}

	public boolean insertSchedulerObject(SchedulerObject schedulerObject) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean result = false;
		if (schedulerObject != null) {
			try {
				conn = datadb.getConnection();
				String sql = " INSERT INTO PUBLIC.SCHEDULER_OBJECT (ID, groupid, minute,hour, day_of_week, relay_status,ao_value, "
				 + " start_time , end_time) VALUES (?, ?, ?, ?, ?, ? , ? , ? , ?) ; ";
				stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				stmt.setInt(1, schedulerObject.getScheduleId());
				stmt.setInt(2, schedulerObject.getGroupId());
				stmt.setInt(3, schedulerObject.getMinute());
				stmt.setInt(4, schedulerObject.getHour());
				stmt.setString(5, schedulerObject.getDayOfWeek());
				stmt.setInt(6, schedulerObject.isRelayStatus() ? 1 : 0);
				stmt.setInt(7, schedulerObject.getAoValue());
				if (schedulerObject.getStartDate() != null) {
					stmt.setTimestamp(8, new Timestamp(schedulerObject.getStartDate().getTime()));
				} else {
					stmt.setTimestamp(8, null);
				}
				
				if (schedulerObject.getEndDate() != null) {
					stmt.setTimestamp(9, new Timestamp(schedulerObject.getEndDate().getTime()));
				} else {
					stmt.setTimestamp(9, null);
				}
				stmt.executeUpdate();
				
				insertSchedulerNodeObjectBySchedulerId(conn, schedulerObject.getScheduleId(), schedulerObject.getNodeList());				
				result = true;
			} catch (SQLException e) {
				mLogger.error("SQLException", e);
				if (conn != null) {
					try {
						conn.rollback();
					} catch (Exception e1) {
						mLogger.error("Exception of rollback ", e1);
					}
				}
			} catch (Exception e) {
				mLogger.error("Exception", e);
			} finally {
				if (stmt != null) {
					datadb.close(stmt);
				}

				if (conn != null) {
					datadb.close(conn);
				}
			}
		}
		return result;
	}

	public boolean insertSchedulerObjects(SchedulerObject[] schedulerObject) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean result = false;
		if ((schedulerObject != null) && (schedulerObject.length > 0)) {
			try {
				conn = datadb.getConnection();
				String sql = " INSERT INTO PUBLIC.SCHEDULER_OBJECT  ";
				sql += " ( ID, groupid, minute, hour, day_of_week, relay_status,ao_value, ";
				sql += " start_time , end_time)";
				sql += " VALUES (?, ?, ?, ?, ?, ?, ?, ?) ; ";
				stmt = conn.prepareStatement(sql);

				for (int i = 0; i < schedulerObject.length; i++) {
					stmt.setInt(1, schedulerObject[i].getScheduleId());
					stmt.setInt(2, schedulerObject[i].getGroupId());
					stmt.setInt(3, schedulerObject[i].getMinute());
					stmt.setInt(4, schedulerObject[i].getHour());
					stmt.setString(5, schedulerObject[i].getDayOfWeek());
					stmt.setInt(6, schedulerObject[i].isRelayStatus() ? 1 : 0);
					stmt.setInt(7, schedulerObject[i].getAoValue());
					if (schedulerObject[i].getStartDate() != null) {
						stmt.setTimestamp(8, new Timestamp(schedulerObject[i].getStartDate().getTime()));
					} else {
						stmt.setTimestamp(8, null);
					}
					
					if (schedulerObject[i].getEndDate() != null) {
						stmt.setTimestamp(9, new Timestamp(schedulerObject[i].getEndDate().getTime()));
					} else {
						stmt.setTimestamp(9, null);
					}
					stmt.addBatch();
				}

				stmt.executeBatch();
				stmt.clearBatch();
				result = true;
			} catch (SQLException e) {
				mLogger.error("SQLException", e);
				if (conn != null) {
					try {
						conn.rollback();
					} catch (Exception e1) {
						mLogger.error("Exception of rollback ", e1);
					}
				}
			} catch (Exception e) {
				mLogger.error("Exception", e);
			} finally {
				if (stmt != null) {
					datadb.close(stmt);
				}

				if (conn != null) {
					datadb.close(conn);
				}
			}
		}
		return result;
	}

	public boolean insertSchedulerNodeObjectBySchedulerId(Connection conn, int scheId, List<Node> nodeList) throws SQLException {
		PreparedStatement stmt = null;
		boolean result = false;
		if ((conn != null) && (nodeList != null) && (nodeList.size() > 0)) {
			try {
				//conn = datadb.getConnection();
				String sql = " INSERT INTO PUBLIC.SCHEDULER_NODE_OBJECT  "
						+ " ( scheduler_id, category, device_id, channel_id, subchannel_id)"
						+ " VALUES (?, ?, ?, ? , ?) ; ";
				stmt = conn.prepareStatement(sql);
				for (int i = 0; i < nodeList.size(); i++) {
					stmt.setInt(1, scheId);
					stmt.setString(2, nodeList.get(i).getCategory());
					stmt.setString(3, nodeList.get(i).getDeviceId());
					stmt.setString(4, nodeList.get(i).getChannelId());
					stmt.setString(5, nodeList.get(i).getSubchannelId());
					stmt.addBatch();
				}
				stmt.executeBatch();
				stmt.clearBatch();
				result = true;
			} catch (SQLException e) {
				throw e;
			} catch (Exception e) {
				throw e;
			} finally {
				if (stmt != null) {
					datadb.close(stmt);
				}
			}
		}
		return result;
	}

	public boolean insertSchedulerNodeObject(Connection conn, int scheId, Node node) throws SQLException {
		PreparedStatement stmt = null;
		boolean result = false;
		if ((conn != null) && (node != null)) {
			try {
				//conn = datadb.getConnection();
				String sql = " INSERT INTO PUBLIC.SCHEDULER_NODE_OBJECT  "
						+ " ( scheduler_id, category, device_id, channel_id,subchannel_id)"
						+ " VALUES (?, ?, ?, ? , ?) ; ";
				stmt = conn.prepareStatement(sql);
				stmt.setInt(1, scheId);
				stmt.setString(2, node.getCategory());
				stmt.setString(3, node.getDeviceId());
				stmt.setString(4, node.getChannelId());
				stmt.setString(5, node.getSubchannelId());
				stmt.executeUpdate();
				result = true;
			} catch (SQLException e) {
				throw e;
			} catch (Exception e) {
				throw e;
			} finally {
				if (stmt != null) {
					datadb.close(stmt);
				}
			}
		}
		return result;
	}

	/**
	 * @param schedulerObject
	 * @return
	 */
	public boolean updateSchedulerObject(SchedulerObject schedulerObject) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean result = false;
		if (schedulerObject != null) {
			try {
				conn = datadb.getConnection();
				String sql = " UPDATE PUBLIC.SCHEDULER_OBJECT SET minute = ?, hour = ?, day_of_week = ?, "
						+ "relay_status = ? , ao_value = ? , start_time = ?, end_time = ? WHERE ID = ? ";
				stmt = conn.prepareStatement(sql);
				stmt.setInt(1, schedulerObject.getMinute());
				stmt.setInt(2, schedulerObject.getHour());
				stmt.setString(3, schedulerObject.getDayOfWeek());
				stmt.setInt(4, schedulerObject.isRelayStatus() ? 1 : 0);
				stmt.setInt(5, schedulerObject.getAoValue());
				if (schedulerObject.getStartDate() != null) {
					stmt.setTimestamp(6, new Timestamp(schedulerObject.getStartDate().getTime()));
				} else {
					stmt.setTimestamp(6, null);
				}
				
				if (schedulerObject.getEndDate() != null) {
					stmt.setTimestamp(7, new Timestamp(schedulerObject.getEndDate().getTime()));
				} else {
					stmt.setTimestamp(7, null);
				}
				stmt.setInt(8, schedulerObject.getScheduleId());
				stmt.executeUpdate();
				
				deleteSchedulerNodeObjectBySchedulerId(conn, schedulerObject.getScheduleId());
				insertSchedulerNodeObjectBySchedulerId(conn, schedulerObject.getScheduleId(), schedulerObject.getNodeList());
				result = true;
			} catch (SQLException e) {
				mLogger.error("SQLException", e);
				if (conn != null) {
					try {
						conn.rollback();
					} catch (Exception e1) {
						mLogger.error("Exception of rollback ", e1);
					}
				}
			} catch (Exception e) {
				mLogger.error("Exception", e);
			} finally {
				if (stmt != null) {
					datadb.close(stmt);
				}

				if (conn != null) {
					datadb.close(conn);
				}
			}

		}
		return result;
	}

	public boolean updateSchedulerObjects(SchedulerObject[] schedulerObject) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean result = false;
		if ((schedulerObject != null) && (schedulerObject.length > 0)) {
			try {
				conn = datadb.getConnection();
				String sql = " UPDATE PUBLIC.SCHEDULER_OBJECT SET minute = ?, hour = ?, day_of_week = ?, "
						+ "relay_status = ? , ao_value = ? , start_time = ?, end_time = ? WHERE ID = ? ";
				stmt = conn.prepareStatement(sql);

				for (int i = 0; i < schedulerObject.length; i++) {
					stmt.setInt(1, schedulerObject[i].getMinute());
					stmt.setInt(2, schedulerObject[i].getHour());
					stmt.setString(3, schedulerObject[i].getDayOfWeek());
					stmt.setInt(4, schedulerObject[i].isRelayStatus() ? 1 : 0);
					stmt.setInt(5, schedulerObject[i].getAoValue());
					if (schedulerObject[i].getStartDate() != null) {
						stmt.setTimestamp(6, new Timestamp(schedulerObject[i].getStartDate().getTime()));
					} else {
						stmt.setTimestamp(6, null);
					}
					
					if (schedulerObject[i].getEndDate() != null) {
						stmt.setTimestamp(7, new Timestamp(schedulerObject[i].getEndDate().getTime()));
					} else {
						stmt.setTimestamp(7, null);
					}
					stmt.setInt(8, schedulerObject[i].getScheduleId());
					stmt.addBatch();
				}
				stmt.executeBatch();
				stmt.clearBatch();
				result = true;
			} catch (SQLException e) {
				mLogger.error("SQLException", e);
				if (conn != null) {
					try {
						conn.rollback();
					} catch (Exception e1) {
						mLogger.error("Exception of rollback ", e1);
					}
				}
			} catch (Exception e) {
				mLogger.error("Exception", e);
			} finally {
				if (stmt != null) {
					datadb.close(stmt);
				}

				if (conn != null) {
					datadb.close(conn);
				}
			}
		}
		return result;
	}

	public boolean deleteSchedulerObject(int schedulerId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean result = false;
		if (schedulerId >= 0) {
			try {
				conn = datadb.getConnection();
				String sql = " DELETE FROM PUBLIC.SCHEDULER_OBJECT where ID = ? ";
				stmt = conn.prepareStatement(sql);
				stmt.setInt(1, schedulerId);
				stmt.executeUpdate();
				
				deleteSchedulerNodeObjectBySchedulerId(conn, schedulerId);
				result = true;
			} catch (SQLException e) {
				mLogger.error("SQLException", e);
				if (conn != null) {
					try {
						conn.rollback();
					} catch (Exception e1) {
						mLogger.error("Exception of rollback ", e1);
					}
				}
			} catch (Exception e) {
				mLogger.error("Exception", e);
			} finally {
				if (stmt != null) {
					datadb.close(stmt);
				}

				if (conn != null) {
					datadb.close(conn);
				}
			}

		}
		return result;
	}

	public boolean deleteSchedulerObjects(int[] schedulerIds) {
		Connection conn = null;
		PreparedStatement stmt = null;
		boolean result = false;
		try {
			conn = datadb.getConnection();
			String sql = " DELETE FROM PUBLIC.SCHEDULER_OBJECT where ID = ? ";
			stmt = conn.prepareStatement(sql);

			for (int i = 0; i < schedulerIds.length; i++) {
				stmt.setInt(1, schedulerIds[i]);
				stmt.addBatch();
			}

			stmt.executeBatch();
			stmt.clearBatch();
			result = true;
		} catch (SQLException e) {
			mLogger.error("SQLException", e);
			if (conn != null) {
				try {
					conn.rollback();
				} catch (Exception e1) {
					mLogger.error("Exception of rollback ", e1);
				}
			}
		} catch (Exception e) {
			mLogger.error("Exception", e);
		} finally {
			if (stmt != null) {
				datadb.close(stmt);
			}

			if (conn != null) {
				datadb.close(conn);
			}
		}

		return result;
	}

	public boolean deleteSchedulerNodeObjectBySchedulerId(Connection conn, int scheId) throws SQLException {
		PreparedStatement stmt = null;
		boolean result = false;
		if ((conn != null) && (scheId >= 0)) {
			try {
				//conn = datadb.getConnection();
				String sql = " DELETE FROM PUBLIC.SCHEDULER_NODE_OBJECT where scheduler_id = ? ";
				stmt = conn.prepareStatement(sql);
				stmt.setInt(1, scheId);
				stmt.executeUpdate();
				result = true;
			} catch (SQLException e) {
				throw e;
			} catch (Exception e) {
				throw e;
			} finally {
				if (stmt != null) {
					datadb.close(stmt);
				}
			}

		}
		return result;
	}

	public boolean deleteSchedulerNodeObject(Connection conn, int scheId, Node node) {
		PreparedStatement stmt = null;
		boolean result = false;
		if (scheId >= 0) {
			try {
				conn = datadb.getConnection();
				String sql = " DELETE FROM PUBLIC.SCHEDULER_NODE_OBJECT where scheduler_id = ? "
						+ "AND category = ? AND device_id = ? AND channel_id = ? AND subchannel_id ? ;";
				stmt = conn.prepareStatement(sql);
				stmt.setInt(1, scheId);
				stmt.setString(2, node.getCategory());
				stmt.setString(3, node.getDeviceId());
				stmt.setString(4, node.getChannelId());
				stmt.setString(5, node.getSubchannelId());
				stmt.executeUpdate();
				result = true;
			} catch (SQLException e) {
				mLogger.error("SQLException", e);
				if (conn != null) {
					try {
						conn.rollback();
					} catch (Exception e1) {
						mLogger.error("Exception of rollback ", e1);
					}
				}
			} catch (Exception e) {
				mLogger.error("Exception", e);
			} finally {
				if (stmt != null) {
					datadb.close(stmt);
				}
			}

		}
		return result;
	}

	public SchedulerObject getSchedulerObject(int scheduledId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		SchedulerObject schedulerObject = new SchedulerObject();
		if (scheduledId >= 0) {
			try {
				conn = datadb.getConnection();
				String sql = " SELECT *  FROM PUBLIC.SCHEDULER_OBJECT where ID = ? ";
				stmt = conn.prepareStatement(sql);
				rs = stmt.executeQuery();
				if (rs != null) {
					schedulerObject.setScheduleId(rs.getInt("id"));
					schedulerObject.setGroupId(rs.getInt("groupid"));
					schedulerObject.setMinute(rs.getInt("minute"));
					schedulerObject.setHour(rs.getInt("minute"));
					schedulerObject.setDayOfWeek(rs.getString("day_of_week"));
					schedulerObject.setRelayStatus((rs.getInt("relay_status") == 0) ? false : true);
					schedulerObject.setAoValue(rs.getInt("ao_value"));
					if (rs.getTimestamp("start_time") != null) {
						schedulerObject.setStartDate(new Date(rs.getTimestamp("start_time").getTime()));
					}
					
					if (rs.getTimestamp("end_time") != null) {
						schedulerObject.setEndDate(new Date(rs.getTimestamp("end_time").getTime()));
					}
				}
			} catch (SQLException e) {
				mLogger.error("SQLException", e);
			} catch (Exception e) {
				mLogger.error("Exception", e);
			} finally {
				if (rs != null) {
					datadb.close(rs);
				}
				if (stmt != null) {
					datadb.close(stmt);
				}
				if (conn != null) {
					datadb.close(conn);
				}
			}

		}
		return schedulerObject;
	}

	public List<SchedulerObject> getAllSchedulerObjects() {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<SchedulerObject> schedulerObjectList = new ArrayList<SchedulerObject>();

		try {
			conn = datadb.getConnection();
			String sql = " SELECT *  FROM PUBLIC.SCHEDULER_OBJECT ;";
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();

			while (rs.next()) {
				SchedulerObject schedulerObject = new SchedulerObject();
				schedulerObject.setScheduleId(rs.getInt("id"));
				schedulerObject.setGroupId(rs.getInt("groupid"));
				schedulerObject.setMinute(rs.getInt("minute"));
				schedulerObject.setHour(rs.getInt("hour"));
				schedulerObject.setDayOfWeek(rs.getString("day_of_week"));
				schedulerObject.setRelayStatus((rs.getInt("relay_status") == 0) ? false : true);
				schedulerObject.setAoValue(rs.getInt("ao_value"));
				if (rs.getTimestamp("start_time") != null) {
					schedulerObject.setStartDate(new Date(rs.getTimestamp("start_time").getTime()));
				}
				
				if (rs.getTimestamp("end_time") != null) {
					schedulerObject.setEndDate(new Date(rs.getTimestamp("end_time").getTime()));
				}
				schedulerObject.setNodeList(getSchedulerNodeObjectBySchedulerId(conn, schedulerObject.getScheduleId()));
				schedulerObjectList.add(schedulerObject);
			}
		} catch (SQLException e) {
			mLogger.error("SQLException", e);
		} catch (Exception e) {
			mLogger.error("Exception", e);
		} finally {
			if (rs != null) {
				datadb.close(rs);
			}
			if (stmt != null) {
				datadb.close(stmt);
			}
			if (conn != null) {
				datadb.close(conn);
			}
		}

		return schedulerObjectList;
	}

	public List<Node> getSchedulerNodeObjectBySchedulerId(Connection conn, int scheId) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Node> result = new ArrayList<Node>();
		if ((conn != null) && (scheId >= 0)) {
			try {
			//	conn = datadb.getConnection();
				String sql = " SELECT * FROM PUBLIC.SCHEDULER_NODE_OBJECT where scheduler_id = ? ;";
				stmt = conn.prepareStatement(sql);
				stmt.setInt(1, scheId);
				rs = stmt.executeQuery();
				while (rs.next()) {
					Node node = new Node("", "", "", "");
					node.setCategory(rs.getString("category"));
					node.setDeviceId(rs.getString("device_id"));
					node.setChannelId(rs.getString("channel_id"));
					node.setSubchannelId(rs.getString("subchannel_id"));
					result.add(node);
				}
			} catch (SQLException e) {
				mLogger.error("SQLException", e);
			} catch (Exception e) {
				mLogger.error("Exception", e);
			} finally {
				if (stmt != null) {
					datadb.close(stmt);
				}
			}

		}
		return result;
	}

}
