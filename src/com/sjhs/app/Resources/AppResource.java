package com.sjhs.app.Resources;

import java.io.ByteArrayInputStream;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.node.ObjectNode;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import com.sjhs.app.dbconnection.DBConnection;
import com.sjhs.app.dbconnection.ResultSetSerializer;

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
		String jsonString = null;
		boolean result = false;
		int logVal = 0;
		ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> userJSON = new HashMap<String,Object>();
        ArrayList<String> minArray = new ArrayList<String>();
        
        userJSON.put("userid", userid);
        
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
	            logVal = 0;
	        } else {
	        	System.out.print("LdapContext is not null ==>" + ctx);
	        	result = true;
	        	logVal = 1;
	        }
	        
	        // Close the context when we're done
		    ctx.close();
        
		    if(!result) {
		    	userJSON.put("msg", "Lookup context is not found.");
		    	userJSON.put("login", new Integer(logVal));
		    } else {
		    	//********User is authenticated ****************
		    	System.out.print("user ==>" + userid + " is authenticated ..");
		    	String msg = "Welcome " +userid + " !";
		    	userJSON.put("msg", msg);
		    	userJSON.put("login", new Integer(logVal));

		    	//************** USER AUTHORIZATION *****************
				String sql = "{ call sjhsReports.dbo.PLU_Ministry(?) }";
				conn = myDB.getDBConnection();
				cs = conn.prepareCall(sql);
				cs.setString(1, userid);
				rs = cs.executeQuery();

				while (rs.next()) {
					String ministry = rs.getString("Ministry");
					minArray.add(ministry);
				}
				userJSON.put("ministry", minArray);
		    }
		    jsonString = mapper.writeValueAsString(userJSON);
	    } catch (NamingException ne)  {           
	    	System.out.println("Lookup failed: " + ne);
	    	String errMsg = "Login failed for " + userid + "!";
	    	userJSON.put("msg", errMsg);
	    } catch (Exception e) {
	    	String errMsg = "Login failed for " + userid + "!";
	    	userJSON.put("msg", errMsg);
	    	System.out.println("Lookup failed: " + e);
	    }finally {
			try {
				if(rs!=null) {
					rs.close();
				}else { 
					String errMsg = "ERROR: ResultSet is null!";
					userJSON.put("msg", errMsg);
					return "ERROR: ResultSet is null!";
					}
				if(cs!=null) {
					cs.close();
				}else { 
					String errMsg = "ERROR: CallableStatement is null!";
					userJSON.put("msg", errMsg);
					return "ERROR: CallableStatement is null!";
				}
				if(conn!=null) {
					myDB.closeConnection();
				}else { 
					String errMsg = "ERROR: Connection Object is null!";
					userJSON.put("msg", errMsg);
					return "ERROR: Connection Object is null!";
				}
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    System.out.println("result ===>" + jsonString);

	    return jsonString;
	}
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("/getCensus")
	@Produces(MediaType.TEXT_PLAIN)
	public String getPatientCensus(@QueryParam("ministry") String ministry) {
		System.out.println("<============getPatientCensus API ==========>");
		System.out.println("ministry ===>" + ministry);
		String jsonString = null;
		Connection conn = null;
		CallableStatement cs = null;
		ResultSet rs = null;
		ArrayList resultList = new ArrayList();

		//Create DBConnection
		DBConnection myDB = new DBConnection();		
		ObjectMapper mapper = new ObjectMapper();

		String sql = "{ call sjhsSSRS.dbo.SJHSApp_Patient_Census_ministry(?) }";
		System.out.println("Prepated sql call ===>" + sql);
		try {
			conn = myDB.getDBConnection();
			cs = conn.prepareCall(sql);
			cs.setString(1, ministry);
			rs = cs.executeQuery();
			while (rs.next()) {
				int admitHr = rs.getInt("AdmitHr");
				String institution = rs.getString("Institution");
				int pt_count = rs.getInt("pt_count");
				String admitDt = rs.getString("AdmitDt");
				ArrayList tempArr = new ArrayList();
				tempArr.add(0, institution);
				tempArr.add(1, admitDt);
				tempArr.add(2, admitHr);
				tempArr.add(3, pt_count);
				resultList.add(tempArr);
			}

			Map<String,Object> censusJSON = getCensusAllJSON(resultList);
			jsonString = mapper.writeValueAsString(censusJSON);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(rs!=null) {
					rs.close();
				}else { return "ERROR: ResultSet is null!";}
				if(cs!=null) {
					cs.close();
				}else { return "ERROR: CallableStatement is null!";}
				if(conn!=null) {
					myDB.closeConnection();
				}else { return "ERROR: Connection Object is null!";}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("JSON String Result ==>" + jsonString);

		return jsonString;
	}

	private Map<String,Object> getCensusAllJSON(ArrayList arr) {
		Map<String,Object> censusJSON = new HashMap<String,Object>();
		int size = 0;
		System.out.println("Resultset size ===>" + size);
		if(arr != null)
			size = arr.size();
		String ministry = null;
		Map<String, Object> ministryMap = new HashMap<String, Object>();;
		Map<String, Object> dateMap = null;
		Map<Integer, Integer> countMap = null;
		for(int i=0; i <size; i++) {
			ArrayList tempArr1 = (ArrayList) arr.get(i);
			ministry = (String)tempArr1.get(0);
			System.out.println("iteration begins. Ministry Name ===>" + i + " " + ministry);
			if(!ministryMap.containsKey(ministry)) {
				System.out.println("Ministry does not exists in map ...>");
				dateMap = new HashMap<String, Object>();
				countMap = new HashMap<Integer, Integer>();
				String admitDt = (String)tempArr1.get(1);
				Integer admitHr = (Integer)tempArr1.get(2);
				Integer pt_count = (Integer)tempArr1.get(3);
				countMap.put(admitHr, pt_count);
				dateMap.put(admitDt, countMap);
				ministryMap.put(ministry, dateMap);
				System.out.println("new Row added ==>" + ministry + " " + admitDt + " " + admitHr +" " + pt_count);
				System.out.println("MinistryMap so far..==> " + ministryMap.toString() );
				
			} else {
				System.out.println("Ministry is present in map...>" + ministry);
				String admitDt = (String)tempArr1.get(1);
				Integer admitHr = (Integer)tempArr1.get(2);
				Integer pt_count = (Integer)tempArr1.get(3);
				Map<String, Object> dateMap1 = (Map<String, Object>)ministryMap.get(ministry);
				if(dateMap1.containsKey(admitDt)) {
					Map<Integer, Integer> countMap1 = (Map<Integer, Integer>)dateMap1.get(admitDt);
					if(!countMap1.containsKey(admitHr)) {
						countMap1.put(admitHr, pt_count);
						dateMap1.put(admitDt, countMap1);
						ministryMap.put(ministry,dateMap1);
						System.out.println("new count added to existing ministry-date ==>" + ministry + " " + admitDt + " " + admitHr +" " + pt_count);
						System.out.println("MinistryMap so far.. ==> " + ministryMap.toString());
					}else {
						System.out.println("AdmitHr is alreay present for minstry ==> " + ministry + " " + admitDt);
					}
				}else {
					countMap = new HashMap<Integer, Integer>();
					countMap.put(admitHr, pt_count);
					dateMap1.put(admitDt, countMap);
					ministryMap.put(ministry, dateMap1);
					System.out.println("new count and Date added to existing ministry ==>" + ministry + " " + admitDt + " " + admitHr +" " + pt_count);
					System.out.println("minstryMap so far.. ==> " + ministryMap.toString());
				}
			}
		}
		censusJSON.put("census", ministryMap);
		System.out.println("JSON String for census ===>" + censusJSON.toString());
		return censusJSON;
	}
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("/getCensusAll")
	@Produces(MediaType.TEXT_PLAIN)
	public String getPatientCensusAll() {
		System.out.println("<============getPatientCensusAll API ==========>");
		String jsonString = null;
		StringBuffer res = new StringBuffer("");
		
		Connection conn = null;
		CallableStatement cs = null;
		ResultSet rs = null;
		
		//Create DBConnection
		DBConnection myDB = new DBConnection();		
		ObjectMapper mapper = new ObjectMapper();
		//Creating JSON Object
		ArrayList resultList = new ArrayList();
		Version version = new Version(1, 0, 0, "SNAPSHOT");
		SimpleModule module = new SimpleModule("census", version);
		module.addSerializer(new ResultSetSerializer());
		mapper.registerModule(module);
		String sql = "{ call sjhsSSRS.dbo.SJHSApp_Patient_Census }";
		System.out.println("Prepated sql call ===>" + sql);
		try {
			conn = myDB.getDBConnection();
			cs = conn.prepareCall(sql);
			rs = cs.executeQuery();
			while (rs.next()) {
				int admitHr = rs.getInt("AdmitHr");
				String institution = rs.getString("Institution");
				int pt_count = rs.getInt("pt_count");
				String admitDt = rs.getString("AdmitDt");
				ArrayList tempArr = new ArrayList();
				tempArr.add(0, institution);
				tempArr.add(1, admitDt);
				tempArr.add(2, admitHr);
				tempArr.add(3, pt_count);
				resultList.add(tempArr);
			}
			Map<String,Object> censusJSON = getCensusAllJSON(resultList);
			jsonString = mapper.writeValueAsString(censusJSON);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(rs!=null) {
					rs.close();
				}else { return "ERROR: ResultSet is null!";}
				if(cs!=null) {
					cs.close();
				}else { return "ERROR: CallableStatement is null!";}
				if(conn!=null) {
					myDB.closeConnection();
				}else { return "ERROR: Connection Object is null!";}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("JSON Result String ==>" + jsonString);

		return jsonString;
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