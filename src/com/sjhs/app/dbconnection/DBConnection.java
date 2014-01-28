package com.sjhs.app.dbconnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;

public class DBConnection { 
	
	public DBConnection(){
		System.out.println("Constructor " + context); // null here
	}
	
	private Connection conn = null;
	private ServletContext context;
	
	public static final String USER_NAME = "sqlSSRS";
	
	public static final String PASSWORD = "$$r5sa";
	
	public static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	//public static String DRIVER = null;
	
	//DEV
	//public static final String URL = "jdbc:sqlserver://AMGDAT002-VD:1433;databaseName=";
	
	//UAT
	public static final String URL = "jdbc:sqlserver://SCRDCVAMGDAT02:1433;databaseName=";

	//PROD
	//public static final String URL = "jdbc:sqlserver://SCRDCPDAMGDAT2:1433;databaseName=";

	public static final String DB_NAME = "sjhsSSRS";

	
	@Context
	public void setServletContext(ServletContext context) {
        System.out.println("servlet context set here");
        this.context = context;
        System.out.println(context.getInitParameter("jdbc.user"));
    }
	
	/*
	@GET
	private void setDBProperties() {
		setServletContext(context);
		System.out.println(context);
		USER_NAME = context.getInitParameter("jdbc.user");
		PASSWORD = context.getInitParameter("jdbc.password");
		DRIVER = context.getInitParameter("jdbc.driver");
		URL = context.getInitParameter("jdbc.url");
		DB_NAME = context.getInitParameter("jdbc.database");

       //get the property value and print it out
        System.out.println(USER_NAME);
        System.out.println(PASSWORD);
        System.out.println(DRIVER);

	}
*/
	 	
	public Connection getDBConnection() {
		// read properties and set the database parameters
		//setDBProperties();
		try {

			Class.forName(DRIVER);
			//String url = URL + DB_NAME;
			String url = URL + DB_NAME;
			conn = (Connection) DriverManager.getConnection(url, USER_NAME,	PASSWORD);
			System.out.println("Created new connection..." + conn);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}

	public void closeConnection() {
		try {
			conn.close();
			System.out.println("Connection is closed..." + conn);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		try {
			String sql = "SELECT top 10 * FROM sjhsSSRS.dbo.SSRS_CPT_Dictionary";
			String key;

			DBConnection myDB = new DBConnection();
			Connection conn = myDB.getDBConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				key = rs.getString(1);
				System.out.println("Intitution==>" + key);
			}
			
			myDB.closeConnection();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
