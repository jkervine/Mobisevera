package com.digitalfingertip.mobisevera;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.util.Log;

/**
 * This class contains the logic for handling hour entries (ie. hour claims made by the user)
 * @author juha
 *
 */

public class S3HourEntryContainer {
	private static final String TAG = "Sevedroid";
	private static final S3HourEntryContainer instance = new S3HourEntryContainer();
	private static String myXml = null;
	private static XPath xpath = null;
	private static InputSource inputSource = null;
	
	// The xpath expressions for digging out the valued needed for this class's methods
	
	private static final String XPATH_HOURENTRY_QUANTITY = 
				"/s:Envelope/s:Body/x:GetHourEntriesByDateAndUserGUIDResponse/x:GetHourEntriesByDateAndUserGUIDResult/a:HourEntry/a:Quantity";
	private static final String XPATH_HOURENTRY_CASEGUID = 
			"/s:Envelope/s:Body/x:GetHourEntriesByDateAndUserGUIDResponse/x:GetHourEntriesByDateAndUserGUIDResult/a:HourEntry/a:CaseGUID";
	private static final String XPATH_HOURENTRY_DESCRIPTION = 
			"/s:Envelope/s:Body/x:GetHourEntriesByDateAndUserGUIDResponse/x:GetHourEntriesByDateAndUserGUIDResult/a:HourEntry/a:Description";

	private S3HourEntryContainer() {
		
	}
	
	public static S3HourEntryContainer getInstance() {
		return instance;
	}
	
	public void setHourEntriesXML(final String xmlForHourEntries) {
		myXml = xmlForHourEntries;
		xpath = XPathFactory.newInstance().newXPath();
	}
	
	public ArrayList<S3HourEntryItem> getHourEntries() {
		NodeList descriptionNodes = null;
		NodeList quantityNodes = null;
		NodeList guidNodes = null;
		S3NamespaceContext mnsp = new S3NamespaceContext();
		xpath.setNamespaceContext(mnsp);
		if(xpath == null) {
			Log.e(TAG,"There's something wrong with class, xpath parser is null.");
			return null;
		}
		try {
			inputSource = new InputSource (new StringReader(myXml));
			descriptionNodes = (NodeList)xpath.evaluate(XPATH_HOURENTRY_DESCRIPTION,inputSource,XPathConstants.NODESET);
			Log.d(TAG,"descriptionNodes match:"+descriptionNodes.getLength()+" items.");
			inputSource = new InputSource(new StringReader(myXml));
			quantityNodes = (NodeList)xpath.evaluate(XPATH_HOURENTRY_QUANTITY,inputSource,XPathConstants.NODESET);
			Log.d(TAG,"quantityNodes match:"+quantityNodes.getLength()+" items.");
			inputSource = new InputSource(new StringReader(myXml));
			guidNodes = (NodeList)xpath.evaluate(XPATH_HOURENTRY_CASEGUID,inputSource,XPathConstants.NODESET);
			Log.d(TAG,"guidNodes match:"+guidNodes.getLength()+" items.");
		} catch (XPathExpressionException e) {
			Log.e(TAG,"XPathExpression evaluation failed, exception is:",e);
			return null;
		}
		// sanity checks
		int totalItems = guidNodes.getLength();
		if(totalItems != descriptionNodes.getLength()) {
			Log.e(TAG,"Node lists are not of same lenght! (guid & description) lists");
			return null;
		}
		if(totalItems != quantityNodes.getLength()) {
			Log.e(TAG,"Node lists are not of same length! (guid & guantity lists)");
			return null;
		}
		ArrayList<S3HourEntryItem> res = new ArrayList<S3HourEntryItem>(totalItems);
		for(int i=0; i< totalItems; i++) {
			S3HourEntryItem HourEntry = new S3HourEntryItem();
			Node descriptionNode = descriptionNodes.item(i);
			Node quantityNode = quantityNodes.item(i);
			Node guidNode = guidNodes.item(i);
			HourEntry.setDescription(descriptionNode.getTextContent());
			HourEntry.setCaseGuid(guidNode.getTextContent());
			HourEntry.setQuantity(quantityNode.getTextContent());
			res.add(HourEntry);
		}
		return res;
		
	}
	
	/**
	 * Using queried work hours from getHourEntries, find which projects (or in S3 terms, "cases") are the targets of this work
	 * @return S3CaseContainer.S3CaseItems
	 */
	
	public String [] getDistinctCaseGUIDsFromHourEntryList(ArrayList<S3HourEntryItem> hoursList) {
		HashSet<String> distinctKeySet = new HashSet<String>();
		if(hoursList == null) {
			Log.d(TAG,"Got null hoursList, returning null...");
			return null;
		}
		Log.d(TAG,"Getting distinct projects from hourslist of "+hoursList.size()+" items.");
		for (S3HourEntryItem hoursItem : hoursList) {
			if(!distinctKeySet.contains(hoursItem.getCaseGuid())) {
				distinctKeySet.add(hoursItem.getCaseGuid());
			}
		}
		String[] distinctCaseGuids = new String[distinctKeySet.size()];
		Iterator<String> it = distinctKeySet.iterator();
		for (int i = 0; i < distinctKeySet.size(); i++) {
			distinctCaseGuids[i] = it.next();
		}
		Log.d(TAG,"Returning "+distinctCaseGuids.length+" distinct case guids.");
		return distinctCaseGuids;
	}
	
	
}
