package com.sjhs.app.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesReader {

	/**
	 * @param args
	 */


	public PropertiesReader() {

    }
	
	public static void doSomeOperation() throws IOException {
        // Get the inputStream
        InputStream inputStream = PropertiesReader.class.getClassLoader().getResourceAsStream("WEB-INF/jdbc.properties");

        Properties properties = new Properties();

        System.out.println("InputStream is: " + inputStream);

        // load the inputStream using the Properties
        properties.load(inputStream);
        // get the value of the property
        String propValue = properties.getProperty("jdbc.url");

        System.out.println("Property value is: " + propValue);
    }
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 try {
			doSomeOperation();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
