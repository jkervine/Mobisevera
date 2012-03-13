package com.digitalfingertip.mobisevera;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

public class S3PullParser {

	private static final String TAG = "Sevedroid";
	private XmlPullParser xpp = null;
	
	public void parseSoapEnvelope(String envelope) {
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			if(xpp == null) {
				xpp = factory.newPullParser();
			}
			xpp.setInput(new StringReader(envelope));
			this.ProcessDocument(xpp);
		} catch (XmlPullParserException e) {
			Log.e(TAG, "Failed to parse:"+e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void ProcessDocument(XmlPullParser xpp) throws XmlPullParserException, IOException {
		int eventType = xpp.getEventType();
		while(eventType != XmlPullParser.END_DOCUMENT) {
			if(eventType == XmlPullParser.START_DOCUMENT) {
	              System.out.println("Start document");
	          } else if(eventType == XmlPullParser.START_TAG) {
	              System.out.println("Start tag "+xpp.getName());
	          } else if(eventType == XmlPullParser.END_TAG) {
	              System.out.println("End tag "+xpp.getName());
	          } else if(eventType == XmlPullParser.TEXT) {
	              System.out.println("Text "+xpp.getText());
	          }
	          eventType = xpp.next();
		}
	}

	public static LinkedList<S3CaseContainer> parseAllCasesXML(
			String allCasesXml) {
		return null;
	}
	
}
