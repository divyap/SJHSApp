package com.sjhs.app.listener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.sjhs.app.Resources.SurveyResource;
import com.sjhs.app.Scheduler.SurveyTask1;
import com.sjhs.app.Scheduler.SurveyTask2;
import com.sjhs.app.Scheduler.SurveyTaskWeek2and3;
import com.sjhs.app.dbconnection.DBConnection;
/**
 * @author Divya Prakash
 * 
 */
public class Listener implements ServletContextListener {
	Map<String, String> config = new HashMap<String, String>();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");

	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
	}

	public void contextInitialized(ServletContextEvent arg0) {
		System.out.println("Context Initialised");

		initConfiguration();

		scheduleWeek1();
		scheduleWeek2();
		scheduleWeek3();
		scheduleWeek4();

	}

	private void initConfiguration() {
		try {

			String sql = "SELECT * FROM CONFIGURATION";
			String key, value;

			DBConnection myDB = new DBConnection();
			Connection conn = myDB.getDBConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				key = rs.getString(1);
				value = rs.getString(2); 
				config.put(key, value);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void scheduleWeek1() {
		try {
			String week1Datestart = config.get("WEEK1_START");
			String week1Dateend = config.get("WEEK1_END");
			Date week1StartDate = sdf.parse(week1Datestart);
			Date week1EndDate = sdf.parse(week1Dateend);

			String week1TimeFirstSurvey = config.get("WEEK1_FIRST_SURVEY_TIME");
			Date week1FirstSurveyTime = sdf1.parse(week1TimeFirstSurvey);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(week1FirstSurveyTime);
			int h = calendar.get(Calendar.HOUR_OF_DAY);
			int m = calendar.get(Calendar.MINUTE);
			int s = calendar.get(Calendar.SECOND);

			calendar.setTime(week1StartDate);
			calendar.set(Calendar.HOUR_OF_DAY, h);
			calendar.set(Calendar.MINUTE, m);
			calendar.set(Calendar.SECOND, s);

			Date surveyTime = calendar.getTime();

			Timer time = new Timer();
			time.schedule(new SurveyTask1(week1EndDate, "Take Survey A",
					SurveyResource.SURVEYA), surveyTime, 24 * 60 * 60 * 1000);

			// Create a Date corresponding to 03:30:00 PM.

			String week1TimeFirstSurveyRem = config
					.get("WEEK1_FIRST_SURVEY_REM_TIME");
			Date week1FirstSurveyRemTime = sdf1.parse(week1TimeFirstSurveyRem);

			calendar.setTime(week1FirstSurveyRemTime);
			h = calendar.get(Calendar.HOUR_OF_DAY);
			m = calendar.get(Calendar.MINUTE);
			s = calendar.get(Calendar.SECOND);

			calendar.setTime(week1StartDate);
			calendar.set(Calendar.HOUR_OF_DAY, h);
			calendar.set(Calendar.MINUTE, m);
			calendar.set(Calendar.SECOND, s);

			surveyTime = calendar.getTime();
			time = new Timer();
			time.schedule(new SurveyTask2(week1EndDate,
					"Reminder to take Survey A", SurveyResource.SURVEYA_REM),
					surveyTime, 24 * 60 * 60 * 1000); // Schedule at 3:30 pm

			// Create a Date corresponding to 6:30:00 PM.

			String week1TimeSecondSurvey = config
					.get("WEEK1_SECOND_SURVEY_TIME");
			Date week1SecondSurveyTime = sdf1.parse(week1TimeSecondSurvey);

			calendar.setTime(week1SecondSurveyTime);
			h = calendar.get(Calendar.HOUR_OF_DAY);
			m = calendar.get(Calendar.MINUTE);
			s = calendar.get(Calendar.SECOND);

			calendar.setTime(week1StartDate);
			calendar.set(Calendar.HOUR_OF_DAY, h);
			calendar.set(Calendar.MINUTE, m);
			calendar.set(Calendar.SECOND, s);

			surveyTime = calendar.getTime();
			time = new Timer();
			time.schedule(new SurveyTask1(week1EndDate, "Take Survey B",
					SurveyResource.SURVEYB), surveyTime, 24 * 60 * 60 * 1000); // Schedule
																				// at
																				// 6:30
																				// pm
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void scheduleWeek2() {
		try {
			String week2Datestart = config.get("WEEK2_START");
			Date week2StartDate = sdf.parse(week2Datestart);

			String week2TimeMorningMsg = config.get("WEEK2_MORNING_MSG_TIME");
			Date week2MorningMsgTime = sdf1.parse(week2TimeMorningMsg);
			Calendar calendar1 = Calendar.getInstance();
			calendar1.setTime(week2MorningMsgTime);
			int h = calendar1.get(Calendar.HOUR_OF_DAY);
			int m = calendar1.get(Calendar.MINUTE);
			int s = calendar1.get(Calendar.SECOND);

			calendar1.setTime(week2StartDate);
			calendar1.set(Calendar.HOUR_OF_DAY, h);
			calendar1.set(Calendar.MINUTE, m);
			calendar1.set(Calendar.SECOND, s);

			String week2TimeEveningMsg = config.get("WEEK2_EVENING_MSG_TIME");
			Date week2EveningMsgTime = sdf1.parse(week2TimeEveningMsg);
			Calendar calendar2 = Calendar.getInstance();
			calendar2.setTime(week2EveningMsgTime);
			h = calendar2.get(Calendar.HOUR_OF_DAY);
			m = calendar2.get(Calendar.MINUTE);
			s = calendar2.get(Calendar.SECOND);

			calendar2.setTime(week2StartDate);
			calendar2.set(Calendar.HOUR_OF_DAY, h);
			calendar2.set(Calendar.MINUTE, m);
			calendar2.set(Calendar.SECOND, s);

			String sql = "SELECT MESSAGE FROM EDU_MSG_NOTIFICATION WHERE GROUP_NO='WEEK2'";

			DBConnection myDB = new DBConnection();
			Connection conn = myDB.getDBConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();

			boolean morning = true;
			while (rs.next()) {
				Date scheduleDate = null;

				if (morning) {
					scheduleDate = calendar1.getTime();
					calendar1.add(Calendar.DATE, 1);
				} else {
					scheduleDate = calendar2.getTime();
					calendar2.add(Calendar.DATE, 1);
				}
				Timer time = new Timer();
				time.schedule(new SurveyTaskWeek2and3(rs.getString(1)),
						scheduleDate);
				morning = !morning;

			}

			myDB.closeConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// start week2Task
	}

	private void scheduleWeek3() {
		try {
			String week3Datestart = config.get("WEEK3_START");
			Date week3StartDate = sdf.parse(week3Datestart);

			String week3TimeMorningMsg = config.get("WEEK3_MORNING_MSG_TIME");
			Date week3MorningMsgTime = sdf1.parse(week3TimeMorningMsg);
			Calendar calendar1 = Calendar.getInstance();
			calendar1.setTime(week3MorningMsgTime);
			int h = calendar1.get(Calendar.HOUR_OF_DAY);
			int m = calendar1.get(Calendar.MINUTE);
			int s = calendar1.get(Calendar.SECOND);

			calendar1.setTime(week3StartDate);
			calendar1.set(Calendar.HOUR_OF_DAY, h);
			calendar1.set(Calendar.MINUTE, m);
			calendar1.set(Calendar.SECOND, s);

			String week3TimeEveningMsg = config.get("WEEK3_EVENING_MSG_TIME");
			Date week3EveningMsgTime = sdf1.parse(week3TimeEveningMsg);
			Calendar calendar2 = Calendar.getInstance();
			calendar2.setTime(week3EveningMsgTime);
			h = calendar2.get(Calendar.HOUR_OF_DAY);
			m = calendar2.get(Calendar.MINUTE);
			s = calendar2.get(Calendar.SECOND);

			calendar2.setTime(week3StartDate);
			calendar2.set(Calendar.HOUR_OF_DAY, h);
			calendar2.set(Calendar.MINUTE, m);
			calendar2.set(Calendar.SECOND, s);

			String sql = "SELECT MESSAGE FROM EDU_MSG_NOTIFICATION WHERE GROUP_NO='WEEK3'";

			DBConnection myDB = new DBConnection();
			Connection conn = myDB.getDBConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();

			boolean morning = true;
			while (rs.next()) {
				Date scheduleDate = null;

				if (morning) {
					scheduleDate = calendar1.getTime();
					calendar1.add(Calendar.DATE, 1);
				} else {
					scheduleDate = calendar2.getTime();
					calendar2.add(Calendar.DATE, 1);
				}

				Timer time = new Timer();
				time.schedule(new SurveyTaskWeek2and3(rs.getString(1)),
						scheduleDate);
				morning = !morning;
			}
			myDB.closeConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void scheduleWeek4() {
		try {
			String week4Datestart = config.get("WEEK4_START");
			String week4Dateend = config.get("WEEK4_END");
			Date week4StartDate = sdf.parse(week4Datestart);
			Date week4EndDate = sdf.parse(week4Dateend);

			String week4TimeFirstSurvey = config.get("WEEK4_FIRST_SURVEY_TIME");
			Date week4FirstSurveyTime = sdf1.parse(week4TimeFirstSurvey);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(week4FirstSurveyTime);
			int h = calendar.get(Calendar.HOUR_OF_DAY);
			int m = calendar.get(Calendar.MINUTE);
			int s = calendar.get(Calendar.SECOND);

			calendar.setTime(week4StartDate);
			calendar.set(Calendar.HOUR_OF_DAY, h);
			calendar.set(Calendar.MINUTE, m);
			calendar.set(Calendar.SECOND, s);

			Date surveyTime = calendar.getTime();
			Timer time = new Timer();
			time.schedule(new SurveyTask1(week4EndDate, "Take Survey A",
					SurveyResource.SURVEYA), surveyTime, 24 * 60 * 60 * 1000);

			// Create a Date corresponding to 03:30:00 PM.

			String week4TimeFirstSurveyRem = config
					.get("WEEK4_FIRST_SURVEY_REM_TIME");
			Date week4FirstSurveyRemTime = sdf1.parse(week4TimeFirstSurveyRem);

			calendar.setTime(week4FirstSurveyRemTime);
			h = calendar.get(Calendar.HOUR_OF_DAY);
			m = calendar.get(Calendar.MINUTE);
			s = calendar.get(Calendar.SECOND);

			calendar.setTime(week4StartDate);
			calendar.set(Calendar.HOUR_OF_DAY, h);
			calendar.set(Calendar.MINUTE, m);
			calendar.set(Calendar.SECOND, s);

			surveyTime = calendar.getTime();
			time = new Timer();
			time.schedule(new SurveyTask2(week4EndDate,
					"Reminder to take Survey A", SurveyResource.SURVEYA_REM),
					surveyTime, 24 * 60 * 60 * 1000); // Schedule at 3:30 pm

			// Create a Date corresponding to 6:30:00 PM.

			String week4TimeSecondSurvey = config
					.get("WEEK4_SECOND_SURVEY_TIME");
			Date week4SecondSurveyTime = sdf1.parse(week4TimeSecondSurvey);

			calendar.setTime(week4SecondSurveyTime);
			h = calendar.get(Calendar.HOUR_OF_DAY);
			m = calendar.get(Calendar.MINUTE);
			s = calendar.get(Calendar.SECOND);

			calendar.setTime(week4StartDate);
			calendar.set(Calendar.HOUR_OF_DAY, h);
			calendar.set(Calendar.MINUTE, m);
			calendar.set(Calendar.SECOND, s);

			surveyTime = calendar.getTime();
			time = new Timer();
			time.schedule(new SurveyTask1(week4EndDate, "Take Survey B",
					SurveyResource.SURVEYB), surveyTime, 24 * 60 * 60 * 1000); // Schedule
																				// at
																				// 6:30
																				// pm
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main (String[] args) {
		
	}
	}

}