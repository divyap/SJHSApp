package com.sjhs.app.dbconnection;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;

public class DBConnection { 
	
	public static Properties properties = null;
	
	private Connection conn = null;
	
	private ServletContext context;
	
	public static String USER_NAME;
	
	public static String PASSWORD;
	
	public static String DRIVER;

	public static String URL;

	public static String DB_NAME;

	public DBConnection(){
		System.out.println("Constructor " + context); // null here
	}
	

	public static void loadProperties() throws IOException {
        // Get the inputStream
        InputStream inputStream = DBConnection.class.getClassLoader().getResourceAsStream("jdbc.properties");

        properties = new Properties();

        System.out.println("InputStream is: " + inputStream);

        // load the inputStream using the Properties
        properties.load(inputStream);
        // get the value of the property
        USER_NAME = properties.getProperty("jdbc.user");
        PASSWORD = properties.getProperty("jdbc.password");
        DRIVER = properties.getProperty("jdbc.driver");
        URL = properties.getProperty("jdbc.url");
        DB_NAME = properties.getProperty("jdbc.database");
        System.out.println("Property values are: " + USER_NAME + " " + PASSWORD + 
        					" " + DRIVER+ " " + URL);
    }
	
	 	
	public Connection getDBConnection() {
		// read properties and set the database parameters
		//setDBProperties();
		try {

			loadProperties();
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
