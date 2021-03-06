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

/**
 * This singleton class wraps in itself the functionality to handle Severa 3 case objects, ie.
 * those whih can be used to claim hours to. To use, feed it the SOAP xml message got from the 
 * getAllCasesXml method of the MobiseveraCommsUtils 
 * @author juha
 *
 */

public class S3CaseContainer {
	
	private static final String TAG = "Sevedroid";
	private static final S3CaseContainer instance = new S3CaseContainer();
	private static String myXml = null;
	private static XPath xpath = null;
	private static InputSource inputSource = null;
	
	// The xpath expressions for digging out the valued needed for this class's methods
	
	private static final String XPATH_ACCOUNT_NAMES = 
			"/s:Envelope/s:Body/x:GetAllCasesResponse/x:GetAllCasesResult/a:Case/a:AccountName";

	private static final String XPATH_INTERNAL_NAMES = 
			"/s:Envelope/s:Body/x:GetAllCasesResponse/x:GetAllCasesResult/a:Case/a:InternalName";
			
	private static final String XPATH_CASE_GUIDS = 
			"/s:Envelope/s:Body/x:GetAllCasesResponse/x:GetAllCasesResult/a:Case/a:GUID";
	
	// These are for querying a single item from GetCaseByGuid xml response
	
	private static final String XPATH_ACCOUNT_NAME =
			"/s:Envelope/s:Body/x:GetCaseByGUIDResponse/x:GetCaseByGUIDResult/a:AccountName";
	
	private static final String XPATH_INTERNAL_NAME =
			"/s:Envelope/s:Body/x:GetCaseByGUIDResponse/x:GetCaseByGUIDResult/a:InternalName";
	
	private static final String XPATH_CASE_GUI =
			"/s:Envelope/s:Body/x:GetCaseByGUIDResponse/x:GetCaseByGUIDResult/a:GUID";
	
	
	
	// protect constructor, force access through getInstance
	private S3CaseContainer() {
		xpath = XPathFactory.newInstance().newXPath();
	}
	
	public static S3CaseContainer getInstance() {
		XPathFactory xpf = XPathFactory.newInstance();
		xpath = xpf.newXPath();
		return instance;
	}
	
	public static S3CaseContainer getInstance(final String xmlForCases) {
		myXml = xmlForCases;
		xpath = XPathFactory.newInstance().newXPath();
		return instance;
	}
	
	public void setCasesXML(final String xmlForCases) {
		myXml = xmlForCases;
		xpath = XPathFactory.newInstance().newXPath();
	}
	
	public void setCaseXML(final String xmlForCase) {
		//Internally, it's just xml. This however requires the caller to understand whether they are parsing
		//a response to GetCaseByGUID or GetCases - bit awkward.
		//TODO: Think of a way to make this explicitly known to the caller
		setCasesXML(xmlForCase);
	}
	
	/**
	 * Get a list of all cases (or projects) which are available to the user
	 * @return
	 */
	
	public ArrayList<S3CaseItem> getCases() {
		NodeList accountNodes = null;
		NodeList internalNameNodes = null;
		NodeList guidNodes = null;
		S3NamespaceContext mnsp = new S3NamespaceContext();
		xpath.setNamespaceContext(mnsp);
		if(xpath == null) {
			Log.e(TAG,"There's something wrong with class, xpath parser is null.");
			return null;
		}
		try {
			inputSource = new InputSource (new StringReader(myXml));
			accountNodes = (NodeList)xpath.evaluate(XPATH_ACCOUNT_NAMES,inputSource,XPathConstants.NODESET);
			Log.d(TAG,"accountNodes match:"+accountNodes.getLength()+" items.");
			inputSource = new InputSource(new StringReader(myXml));
			internalNameNodes = (NodeList)xpath.evaluate(XPATH_INTERNAL_NAMES,inputSource,XPathConstants.NODESET);
			Log.d(TAG,"internalNameNodes match:"+internalNameNodes.getLength()+" items.");
			inputSource = new InputSource(new StringReader(myXml));
			guidNodes = (NodeList)xpath.evaluate(XPATH_CASE_GUIDS,inputSource,XPathConstants.NODESET);
			Log.d(TAG,"guidNodes match:"+guidNodes.getLength()+" items.");
		} catch (XPathExpressionException e) {
			Log.e(TAG,"XPathExpression evaluation failed, exception is:",e);
			return null;
		}
		// sanity checks
		int totalItems = accountNodes.getLength();
		if(totalItems != internalNameNodes.getLength()) {
			Log.e(TAG,"Node lists are not of same lenght! (account & internalName) lists");
			return null;
		}
		if(totalItems != guidNodes.getLength()) {
			Log.e(TAG,"Node lists are not of same length! (account & guid lists)");
			return null;
		}
		ArrayList<S3CaseItem> res = new ArrayList<S3CaseItem>(totalItems);
		for(int i=0; i< totalItems; i++) {
			S3CaseItem caseItem = new S3CaseItem();
			Node accountNode = accountNodes.item(i);
			Node internalNameNode = internalNameNodes.item(i);
			Node guidNode = guidNodes.item(i);
			caseItem.setCaseAccountName(accountNode.getTextContent());
			caseItem.setCaseGuid(guidNode.getTextContent());
			caseItem.setCaseInternalName(internalNameNode.getTextContent());
			res.add(caseItem);
		}
		return res;
		
	}
	
	public S3CaseItem getCase() {
		Node accountNode = null;
		Node internalNameNode = null;
		Node guidNode = null;
		S3NamespaceContext mnsp = new S3NamespaceContext();
		xpath.setNamespaceContext(mnsp);
		if(xpath == null) {
			Log.e(TAG,"There's something wrong with class, xpath parser is null.");
			return null;
		}
		try {
			inputSource = new InputSource (new StringReader(myXml));
			accountNode = (Node)xpath.evaluate(XPATH_ACCOUNT_NAME,inputSource,XPathConstants.NODE);
			Log.d(TAG,"accountNode match:"+accountNode+" items.");
			inputSource = new InputSource(new StringReader(myXml));
			internalNameNode = (Node)xpath.evaluate(XPATH_INTERNAL_NAMES,inputSource,XPathConstants.NODE);
			Log.d(TAG,"internalNameNodes match:"+internalNameNode+" items.");
			inputSource = new InputSource(new StringReader(myXml));
			guidNode = (Node)xpath.evaluate(XPATH_CASE_GUIDS,inputSource,XPathConstants.NODE);
			Log.d(TAG,"guidNodes match:"+guidNode+" items.");
		} catch (XPathExpressionException e) {
			Log.e(TAG,"XPathExpression evaluation failed, exception is:",e);
			return null;
		}
		S3CaseItem res = new S3CaseItem();
		res.setCaseAccountName(accountNode.getTextContent());
		res.setCaseGuid(guidNode.getTextContent());
		res.setCaseInternalName(internalNameNode.getTextContent());
		return res;	
	}
			
	/**
	 * Returns a case item, which can be used as a first element of the spinner,
	 * displaying [select project].
	 * @return
	 */
	
	public static S3CaseItem getEmptySelectorElement(Context c) {
		S3CaseItem res = new S3CaseItem();
		res.setCaseAccountName(c.getString(R.string.select_case_item_title));
		res.setCaseGuid(null);
		res.setCaseInternalName(c.getString(R.string.select_case_item_title));
		return res;
	}
	
}
