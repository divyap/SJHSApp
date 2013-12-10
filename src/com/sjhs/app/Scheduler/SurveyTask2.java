package com.sjhs.app.Scheduler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import com.sjhs.app.Resources.SurveyResource;
import com.sjhs.app.dbconnection.DBConnection;
import com.sjhs.app.pns.PNS;

public class SurveyTask2 extends TimerTask {
	Date endTime = null;
	String message;
	String surveyName;

	public SurveyTask2(Date endTime, String message, String surveyName) {
		super();
		this.endTime = endTime;
		this.message = message;
		this.surveyName = surveyName;
	}

	/**
	 * Called on a background thread by Timer
	 */

	public void sendSurvey() {
		// Do your work here; it's 3:30:00 PM!

		try {

			List<String> deviceTokens = new ArrayList<String>();
			Map<String, String> userMap = new HashMap<String, String>();

			String userid;
			String sql = "SELECT USER_ID , DEVICE_TOKEN FROM USER WHERE USER_ID NOT IN (SELECT USER_ID FROM SURVEY_ANSWERS WHERE SURVEY_NAME = ?) AND GROUP_NO = ?";

			DBConnection myDB = new DBConnection();
			Connection conn = myDB.getDBConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, SurveyResource.SURVEYA);
			ps.setString(2, "1");
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				userid = rs.getString("USER_ID");
				deviceTokens.add(rs.getString("DEVICE_TOKEN"));
				userMap.put(rs.getString("DEVICE_TOKEN"), userid);
			}

			// PNS
			List<String> passedDevices = PNS.pushNotification(deviceTokens,
					SurveyResource.SURVEYA, message);
			
			Iterator<String> iter = passedDevices.iterator();
			String deviceToken = null;

			while (iter.hasNext()) {
				deviceToken = iter.next();
				userid = userMap.get(deviceToken);

				sql = "INSERT INTO SURVEY_NOTIFICATION (USER_ID , DATE , SURVEY_NAME) VALUES (? , ? , ?)";
				ps = conn.prepareStatement(sql);
				ps.setString(1, userid);

				Date date = new Date();
				long t = date.getTime();
				Timestamp timestamp = new Timestamp(t);

				ps.setTimestamp(2, timestamp);

				ps.setString(3, surveyName);
				ps.executeUpdate();
			}
			myDB.closeConnection();
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}

	}

	@Override
	public void run() {
		if (new Date().after(endTime))
			cancel();
		else
			sendSurvey();
		// TODO Auto-generated method stub

	}

}
