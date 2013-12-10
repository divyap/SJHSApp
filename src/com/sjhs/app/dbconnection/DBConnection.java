package com.sjhs.app.dbconnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

	private Connection conn = null;

	public static final String USER_NAME = "azsqladmin";

	public static final String PASSWORD = "passSys0!";

	public static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

	public static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=azViews";

	public static final String DB_NAME = "azViews";

	public Connection getDBConnection() {

		try {
			Class.forName(DRIVER);
			//String url = URL + DB_NAME;
			String url = URL;
			conn = (Connection) DriverManager.getConnection(url, USER_NAME,	PASSWORD);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}

	public void closeConnection() {
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
