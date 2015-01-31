package com.navi.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.navi.model.Path;
import com.navi.util.DaoUtil;

public class PathDao {

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

	// ��ȡ��ǰ��·��Ϣ
	public Path getPathInfo(String num) {
		Path chengyus = new Path();
		getConnection();
		String sql = "select * from PathInfo where pointID = '" + num + "'";
		try {
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				chengyus.setPointID(num);
				chengyus.setStreetID(rs.getString("streetID"));
				chengyus.setPointname(rs.getString("pointname"));
				chengyus.setPointSurroundingInfo( rs.getString("pointSurroundingInfo"));
				chengyus.setPointSurroundingStreet( rs.getString("pointSurroundingStreet"));
				chengyus.setPointLongitude( rs.getString("pointLongitude"));
				chengyus.setPointLatitude( rs.getString("pointLatitude"));
				chengyus.setType(rs.getString("type"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DaoUtil.closeConnection(conn, stmt, rs);
		}
		return chengyus;
	}

	// ��ȡ��·��Ϣ
	public List<Path> getPaths() {
		List<Path> paths = new ArrayList<Path>();
		getConnection();
		String sql = "select * from PathInfo";
		try {
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				Path chengyus = new Path();
				chengyus.setPointID(rs.getString("pointID"));
				chengyus.setStreetID(rs.getString("streetID"));
				chengyus.setPointname(rs.getString("pointname"));
				chengyus.setPointSurroundingInfo( rs.getString("pointSurroundingInfo"));
				chengyus.setPointSurroundingStreet( rs.getString("pointSurroundingStreet"));
				chengyus.setPointLongitude( rs.getString("pointLongitude"));
				chengyus.setPointLatitude( rs.getString("pointLatitude"));
				chengyus.setType(rs.getString("type"));
				paths.add(chengyus);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DaoUtil.closeConnection(conn, stmt, rs);
		}
		return paths;
	}

}
