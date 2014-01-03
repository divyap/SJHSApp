package com.sjhs.app.Resources;

import java.io.ByteArrayInputStream;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import com.sjhs.app.dbconnection.DBConnection;
import javax.naming.*;
import javax.naming.directory.*;

/**
 * @author Divya Prakash
 * 
 */

@Path("/Survey")
public class AppResource {

	public static final String SURVEYA = "SurveyA";
	public static final String SURVEYB = "SurveyB";
	public static final String SURVEYA_REM = "SurveyA Rem";

	
	@GET
	@Path("/login")
	@Produces(MediaType.TEXT_PLAIN)
	public String getUserSession(@QueryParam("userid") String userid) {

		try
	    {
	        // Set up the environment for creating the initial context
	        Hashtable<String, String> env = new Hashtable<String, String>();
	        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	        env.put(Context.PROVIDER_URL, "ldap://ldap_server:389");
	        // 
	        env.put(Context.SECURITY_AUTHENTICATION, "simple");
	        env.put(Context.SECURITY_PRINCIPAL, "domain\\user"); //we have 2 \\ because it's a escape char
	        env.put(Context.SECURITY_CREDENTIALS, "test");
	
	        // Create the initial context
	
	        DirContext ctx = new InitialDirContext(env);
	        boolean result = ctx != null;
	
	        if(ctx != null)
	            ctx.close();
	
	        return "true";
	    }
	    catch (Exception e)
	    {           
	        return "false";
	    }
	}
	
	
	/*
	@GET
	@Path("/loginold")
	@Produces(MediaType.TEXT_PLAIN)
	public String getUserSession(@QueryParam("userid") String userid) {
		System.out.println(userid);
		String sql = "SELECT USER_ID FROM USER WHERE USER_ID = ?";
		try {
			DBConnection myDB = new DBConnection();
			Connection conn = myDB.getDBConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userid);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				return "1";
			}

			return "0";
		} catch (Exception e) {
			e.printStackTrace();
			return "0";
		}

	}
	
	*/

	@POST
	@Path("/submitAnswers")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public String submitAnswers(@FormParam("answers") String result) {
		try {
			System.out.println(result);
			SAXReader reader = new SAXReader();
			Document doc = reader.read(new ByteArrayInputStream(result
					.getBytes()));
			Element resultEle = doc.getRootElement();
			String surveyName = resultEle.attributeValue("surveyName");
			String userID = resultEle.attributeValue("userID");
			result = result.replace("'", "\\\'");
			String sql = "INSERT INTO SURVEY_ANSWERS (SURVEY_NAME,USER_ID,ANSWERS) VALUES ('"
					+ surveyName + "','" + userID + "','" + result + "')";

			DBConnection myDB = new DBConnection();
			Statement statement = myDB.getDBConnection().createStatement();
			statement.executeUpdate(sql);
			statement.close();
			return "1";
		} catch (Exception e) {
			e.printStackTrace();
			return "0";
		}
	}

	@GET
	@Path("/getSentSurvey")
	@Produces(MediaType.TEXT_PLAIN)
	public String getSentSurvey(@QueryParam("userid") String userid) {
		StringBuffer res = new StringBuffer("");
		SimpleDateFormat sdf1 = new SimpleDateFormat("MMM dd yyyy");
		SimpleDateFormat sdf2 = new SimpleDateFormat("hh:mm a");

		int c = 0;
		String sql = "SELECT DATE , SURVEY_NAME FROM SURVEY_NOTIFICATION WHERE USER_ID = ?";
		try {
			DBConnection myDB = new DBConnection();
			Connection conn = myDB.getDBConnection();
			PreparedStatement psmt = conn.prepareStatement(sql);
			psmt.setString(1, userid);
			ResultSet rs = psmt.executeQuery();

			while (rs.next()) {
				if (c != 0)
					res.append(", ");
				Date date = rs.getDate("DATE");
				Time time = rs.getTime("DATE");
				String surveyName = rs.getString("SURVEY_NAME");
				c = c + 1;
				res = res.append(sdf1.format(date) + " " + sdf2.format(time)
						+ " " + surveyName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res.toString();

	}

	@POST
	@Path("/addDevice")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public void addDevice(@FormParam("deviceID") String deviceid,
			@FormParam("deviceToken") String devicetoken) {
		try {
			System.out.println("Device ID- " + deviceid);
			deviceid = deviceid.replaceAll("\\-", "");
			deviceid = deviceid.toLowerCase();
			System.out.println("New Device ID- " + deviceid);
			String sql = "UPDATE USER SET DEVICE_TOKEN = ? WHERE DEVICE_ID = ?";
			DBConnection myDB = new DBConnection();
			Connection conn = myDB.getDBConnection();
			PreparedStatement psmt = conn.prepareStatement(sql);
			psmt.setString(1, devicetoken);
			psmt.setString(2, deviceid);
			psmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@GET
	@Path("/testData")
	@Produces(MediaType.TEXT_PLAIN)
	public String getTestData() {
		StringBuffer res = new StringBuffer("");
		try {
			String sql = "SELECT top 10 * FROM sjhsSSRS.dbo.SSRS_CPT_Dictionary";
			String key;
			int c = 0;
			DBConnection myDB = new DBConnection();
			Connection conn = myDB.getDBConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				if (c != 0)
					res.append(", ");
				key = rs.getString(1);
				System.out.println("Intitution==>" + key);
				c = c + 1;
				res = res.append(key);
			}
			
			myDB.closeConnection();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res.toString();
	}

}