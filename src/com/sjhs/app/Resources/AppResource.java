package com.sjhs.app.Resources;

import java.io.ByteArrayInputStream;

import java.sql.CallableStatement;
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
	public boolean getUserSession(@QueryParam("userid") String userid, @QueryParam("pwd") String pwd) {
        boolean result = false;
		try
	    {
	        // Set up the environment for creating the initial context
	        Hashtable<String, String> env = new Hashtable<String, String>();
	        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	        env.put(Context.PROVIDER_URL, "LDAP://SCR-WVS-SCDC01.stjoe.org");
	        env.put(Context.SECURITY_AUTHENTICATION, "simple");
	        env.put(Context.SECURITY_PRINCIPAL, "SJHS-NT\\"+userid); //we have 2 \\ because it's a escape char
	        env.put(Context.SECURITY_CREDENTIALS, pwd);
	
	        // Create the initial context
	
	        DirContext ctx = new InitialDirContext(env);
	        
	        if(ctx == null) {
	        	System.out.print("LdapContext is null ==>" + ctx);
	            result = false;
	        	ctx.close();
	        } else {
	        	System.out.print("LdapContext is not null ==>" + ctx);
	        	result = true;
	        }
	        System.out.print("user ==>" + userid + " is authenticated ..");

	        // Close the context when we're done
		    ctx.close();
        
	    }
	    catch (NamingException e)
	    {           
	    	System.out.println("Lookup failed: " + e);
	    }
	    return result;
	}
	
	
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
	@Path("/getPatientCensus")
	@Produces(MediaType.TEXT_PLAIN)
	public String getPatientCensus() {
		StringBuffer res = new StringBuffer("");
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy hh:mm a");
		CallableStatement cs = null;
		int c = 0;
		String sql = "{call sjhsSSRS.dbo.SJHSApp_Patient_Census}";
		try {
			DBConnection myDB = new DBConnection();
			Connection conn = myDB.getDBConnection();
			cs = conn.prepareCall(sql);
			ResultSet rs = cs.executeQuery();

			while (rs.next()) {
				if (c != 0)
					res.append(", ");
				Date admitDtTm = rs.getDate("AdmitDtTm");
				String institution = rs.getString("Institution");
				int pt_count = rs.getInt("pt_count");
				c = c + 1;
				res = res.append(sdf.format(admitDtTm) + " " + institution
						+ " " + pt_count);
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
	
	public static void main(String[] args) {
		
		AppResource res = new AppResource();
		boolean result = res.getUserSession("prakashdi", "chintoo@3");
		System.out.println("user authenticated? ==>" + result);
		
		String patCensus =  res.getPatientCensus();
		System.out.println("Patient Census ==>" + patCensus);
	}
	
}