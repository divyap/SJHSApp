package com.sjhs.app.pns;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javapns.Push;

import javapns.communication.KeystoreManager;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.notification.AppleNotificationServer;
import javapns.notification.AppleNotificationServerBasicImpl;
import javapns.notification.PushedNotification;
import javapns.notification.PushedNotifications;

import javapns.notification.PushNotificationPayload;

/**
 * @author Divya Prakash
 * 
 *         Class to push notification to Apple server
 * 
 */
public class PNS {

	private static String keystore = "SurveyCerts.p12";
	private static String password = "Inn@123";
	private static Log log = LogFactory.getLog(PNS.class);

	public static List<String> pushNotification(List<String> devices,
			String surveyName, String message) {

		System.out.println("Verifying Keystore");
		// Verify the ssl certificate keystore
		verifyKeystore(keystore, password, false);
		log.error("Keystore Verified");

		System.out.println("After log Keystore Verified");

		List<String> retVal = new ArrayList<String>();
		try {
			// Push a complex alert to one or more devices

			PushNotificationPayload complexPL = PushNotificationPayload
					.complex();
			complexPL.addCustomAlertActionLocKey("Take Survey");
			complexPL.addCustomAlertLocKey(message);
			complexPL.addBadge(45);
			complexPL.addSound("default");
			complexPL.addCustomDictionary("SurveyName", surveyName);

			PushedNotifications result = Push.payload(complexPL, keystore,
					password, false, devices);

			Iterator<PushedNotification> iterFailed = result
					.getFailedNotifications().iterator();

			PushedNotification eachFailed = null;
			while (iterFailed.hasNext()) {
				eachFailed = iterFailed.next();
				eachFailed.getException().printStackTrace();

			}

			Iterator<PushedNotification> iter = result
					.getSuccessfulNotifications().iterator();
			PushedNotification eachPassed = null;

			while (iter.hasNext()) {
				eachPassed = iter.next();
				retVal.add(eachPassed.getDevice().getDeviceId());
			}
		}

		catch (CommunicationException e1) {
			e1.printStackTrace();
		} catch (KeystoreException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return retVal;
	}

	/**
	 * Validate a keystore reference.
	 */

	public static void verifyKeystore(Object keystoreReference,
			String password, boolean production) {
		try {
			System.out.print("Validating keystore reference: ");
			KeystoreManager.validateKeystoreParameter(keystoreReference);
			System.out.println("VALID  (keystore was found)");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (password != null) {
			try {
				System.out.print("Verifying keystore content: ");
				AppleNotificationServer server = new AppleNotificationServerBasicImpl(
						keystoreReference, password, production);
				KeystoreManager
						.verifyKeystoreContent(server, keystoreReference);
				System.out.println("VERIFIED  (no common mistakes detected)");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
