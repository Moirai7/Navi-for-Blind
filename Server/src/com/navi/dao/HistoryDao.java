package com.navi.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.navi.model.History;
import com.navi.util.DaoUtil;

public class HistoryDao {

	// ��Ա����
	Connection conn = null;
	Statement stmt = null;
	ResultSet rs = null;
	// ������
	final String driver = "oracle.jdbc.driver.OracleDriver";
	final String uri = "jdbc:oracle:" + "thin:@127.0.0.1:1521:XE";

	// ��ȡ����
	private void getConnection() {
		try {
			Class.forName(driver);
			String user = "blind";// �û���,ϵͳĬ�ϵ��˻���
			String password = "123";// �㰲װʱѡ���õ�����
			conn = DriverManager.getConnection(uri, user, password);
			stmt = conn.createStatement();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean saveHistory(History history) {
		getConnection();
		String sql = "insert into historyInfo (historyid,userid, pointid, time) values (S_S_Depart.Nextval, '"
				+ history.getUserID() + "', '" + history.getPointID() + "','" + history.getTime() + "')";
		try {
			conn.setAutoCommit(false);
			stmt.execute(sql);
			conn.commit();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			return false;
		} finally {
			DaoUtil.closeConnection(conn, stmt, rs);
		}
	}

	// ��ȡ����
	public List<History> getHistory(String userid) {
		List<History> chengyus = new ArrayList<History>();
		getConnection();
		String sql = "select * from historyInfo where rownum <= 17 and userid = '" + userid  + "' order by historyid" ;
		try {
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				History history = new History();
				history.setPointID(rs.getString("pointid"));
				history.setTime(rs.getString("time"));
				history.setUserID(rs.getString("userid"));
				chengyus.add(history );
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DaoUtil.closeConnection(conn, stmt, rs);
		}
		return chengyus;
	}
}