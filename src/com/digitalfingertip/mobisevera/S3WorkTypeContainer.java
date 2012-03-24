package com.digitalfingertip.mobisevera;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.content.Context;
import android.util.Log;

public class S3WorkTypeContainer {

	private static final String TAG = "Sevedroid";
	private static final S3WorkTypeContainer instance = new S3WorkTypeContainer();
	private static String myXml = null;
	private static XPath xpath = null;
	private static InputSource inputSource = null;

	// The xpath expressions for digging out the valued needed for this class's methods

	private static final String XPATH_WORKTYPE_NAMES = 
			"/s:Envelope/s:Body/x:GetWorkTypesByPhaseGUIDResponse/x:GetWorkTypesByPhaseGUIDResult/a:WorkType/a:Name";

	private static final String XPATH_WORKTYPE_ACTIVE = 
			"/s:Envelope/s:Body/x:GetWorkTypesByPhaseGUIDResponse/x:GetWorkTypesByPhaseGUIDResult/a:WorkType/a:IsActive";
	
	private static final String XPATH_WORKTYPE_GUID = 
			"/s:Envelope/s:Body/x:GetWorkTypesByPhaseGUIDResponse/x:GetWorkTypesByPhaseGUIDResult/a:WorkType/a:GUID";
	
	// protect constructor, force access through getInstance
	private S3WorkTypeContainer() {
		xpath = XPathFactory.newInstance().newXPath();
	}

	public static S3WorkTypeContainer getInstance() {
		XPathFactory xpf = XPathFactory.newInstance();
		xpath = xpf.newXPath();
		return instance;
	}

	public static S3WorkTypeContainer getInstance(final String xmlForWorkTypes) {
		myXml = xmlForWorkTypes;
		xpath = XPathFactory.newInstance().newXPath();
		return instance;
	}

	public void setWorkTypesXML(final String xmlForWorkTypes) {
		myXml = xmlForWorkTypes;
		xpath = XPathFactory.newInstance().newXPath();
	}

	public ArrayList<S3WorkTypeItem> getWorkTypes() {
		NodeList isActiveNodes = null;
		NodeList nameNodes = null;
		NodeList guidNodes = null;
		S3NamespaceContext mnsp = new S3NamespaceContext();
		xpath.setNamespaceContext(mnsp);
		if(xpath == null) {
			Log.e(TAG,"There's something wrong with class, xpath parser is null.");
			return null;
		}
		try {
			inputSource = new InputSource (new StringReader(myXml));
			isActiveNodes = (NodeList)xpath.evaluate(XPATH_WORKTYPE_ACTIVE,inputSource,XPathConstants.NODESET);
			Log.d(TAG,"isLockedNodes match:"+isActiveNodes.getLength()+" items.");
			inputSource = new InputSource(new StringReader(myXml));
			nameNodes = (NodeList)xpath.evaluate(XPATH_WORKTYPE_NAMES,inputSource,XPathConstants.NODESET);
			Log.d(TAG,"nameNodes match:"+nameNodes.getLength()+" items.");
			inputSource = new InputSource(new StringReader(myXml));
			guidNodes = (NodeList)xpath.evaluate(XPATH_WORKTYPE_GUID,inputSource,XPathConstants.NODESET);
			Log.d(TAG,"guidNodes match:"+guidNodes.getLength()+" items.");
		} catch (XPathExpressionException e) {
			Log.e(TAG,"XPathExpression evaluation failed, exception is:",e);
			return null;
		}
		// sanity checks
		int totalItems = isActiveNodes.getLength();
		if(totalItems != nameNodes.getLength()) {
			Log.e(TAG,"Node lists are not of same lenght! (isAcitve & name) lists");
			return null;
		}
		if(totalItems != guidNodes.getLength()) {
			Log.e(TAG,"Node lists are not of same length! (isActive & guid lists)");
			return null;
		}
		ArrayList<S3WorkTypeItem> res = new ArrayList<S3WorkTypeItem>(totalItems);
		for(int i=0; i< totalItems; i++) {
			S3WorkTypeItem workTypeItem = new S3WorkTypeItem();
			Node isActiveNode = isActiveNodes.item(i);
			Node nameNode = nameNodes.item(i);
			Node guidNode = guidNodes.item(i);
			workTypeItem.setIsActive(isActiveNode.getTextContent());
			workTypeItem.setWorkTypeGUID(guidNode.getTextContent());
			workTypeItem.setWorkTypeName(nameNode.getTextContent());
			res.add(workTypeItem);
		}
		return res;

	}
	
	/**
	 * Returns a worktype item, which can be used as a first element of the spinner,
	 * displaying [select work type].
	 * @return
	 */
	
	public static S3WorkTypeItem getEmptySelectorElement(Context c) {
		S3WorkTypeItem res = new S3WorkTypeItem();
		res.setWorkTypeName(c.getString(R.string.select_worktype_item_title));
		res.setWorkTypeGUID(null);
		return res;
	}
}
