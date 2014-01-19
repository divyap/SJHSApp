package com.sjhs.app.Resources;

import java.io.ByteArrayInputStream;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
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

@Path("/App")
public class AppResource {

	@GET
	@Path("/login")
	@Produces(MediaType.TEXT_PLAIN)
	public String getUserSession(@QueryParam("userid") String userid, @QueryParam("pwd") String pwd) {
		System.out.println("<============login API ==========>");
		System.out.println("Userid===>" + userid);
		System.out.println("pwd===>" + pwd);
		boolean result = false;
        StringBuffer res = new StringBuffer("");
        
        //DB Connection object creation
		DBConnection myDB = new DBConnection();
		Connection conn = null;
		CallableStatement cs = null;
		ResultSet rs = null;
		
		int c = 0;
		try
	    {
	        //************** USER AUTHENTICATION *****************
			// Set up the environment for creating the initial context
	        Hashtable<String, String> env = new Hashtable<String, String>();
	        userid = userid.trim();
	        pwd = pwd.trim();
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
	        } else {
	        	System.out.print("LdapContext is not null ==>" + ctx);
	        	result = true;
	        }
	        
	        // Close the context when we're done
		    ctx.close();
        
		    if(!result) {
		    	res.append("Lookup context is not found.");
		    } else {
		    	//********User is authenticated ****************
		    	System.out.print("user ==>" + userid + " is authenticated ..");
		    	String msg = "Welcome " +userid + ",";
		    	res.append(msg);
		    	//************** USER AUTHORIZATION *****************
				String sql = "{ call sjhsReports.dbo.PLU_Ministry(?) }";
				conn = myDB.getDBConnection();
				cs = conn.prepareCall(sql);
				cs.setString(1, userid);
				rs = cs.executeQuery();

				while (rs.next()) {
					if (c != 0)
						res.append(", ");
					String ministry = rs.getString("Ministry");
					c = c + 1;
					res = res.append(ministry);
				}

		    }
		
	    } catch (NamingException ne)  {           
	    	System.out.println("Lookup failed: " + ne);
	    	String errMsg = "Login failed for " + userid + "!";
	    	res.append(errMsg);
	    } catch (Exception e) {
	    	System.out.println("Lookup failed: " + e);
	    }finally {
			try {
				if(rs!=null) 
					rs.close();
				if(cs!=null)
					cs.close();
				if(conn!=null)
					myDB.closeConnection();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    System.out.println("resultSet===>" + res.toString());
	    return res.toString();
	}
	
	@GET
	@Path("/getCensus")
	@Produces(MediaType.TEXT_PLAIN)
	public String getPatientCensus(@QueryParam("ministry") String ministry) {
		System.out.println("<============getPatientCensus API ==========>");
		System.out.println("ministry ===>" + ministry);
		StringBuffer res = new StringBuffer("");
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy hh:mm a");
		
		Connection conn = null;
		CallableStatement cs = null;
		ResultSet rs = null;
		
		//Create DBConnection
		DBConnection myDB = new DBConnection();		

		int c = 0;
		
		String sql = "{ call sjhsSSRS.dbo.SJHSApp_Patient_Census(?) }";
		System.out.println("Prepated sql call ===>" + sql);
		try {
			conn = myDB.getDBConnection();
			cs = conn.prepareCall(sql);
			cs.setString(1, ministry);
			rs = cs.executeQuery();
			System.out.println("Resultset ===>" + rs.getFetchSize());
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
		} finally {
			try {
				rs.close();
				cs.close();
				myDB.closeConnection();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("resultset==>" + res.toString());
		return res.toString();

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
	
	public static void main(String[] args) {
		
		AppResource res = new AppResource();
		String result = res.getUserSession("prakashdi", "chintoo@3");
		System.out.println("user authenticated and Authorized ? ==>" + result);
		
		String patCensus =  res.getPatientCensus("PVH");
		System.out.println("Patient Census ==>" + patCensus);
	}
	
}