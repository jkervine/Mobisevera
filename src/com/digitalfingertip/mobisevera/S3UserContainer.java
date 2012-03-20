package com.digitalfingertip.mobisevera;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.util.Log;

public class S3UserContainer {

	private static final String TAG = "Sevedroid";
	private static final S3UserContainer instance = new S3UserContainer();
	private static String myXml = null;
	private static XPath xpath = null;
	private static InputSource inputSource = null;

	// The xpath expressions for digging out the valued needed for this class's methods

	private static final String XPATH_USER_NAMES = 
			"/s:Envelope/s:Body/x:GetUserByNameResponse/x:GetUserByNameResult/a:FirstName";

	private static final String XPATH_USER_ACTIVE = 
			"/s:Envelope/s:Body/x:GetUserByNameResponse/x:GetUserByNameResult/a:IsActive";
	
	private static final String XPATH_USER_GUID = 
			"/s:Envelope/s:Body/x:GetUserByNameResponse/x:GetUserByNameResult/a:GUID";

	private static final String XPATH_USER_LANGUAGE = 
			"/s:Envelope/s:Body/x:GetUserByNameResponse/x:GetUserByNameResult/a:LanguageCode";
	
	// protect constructor, force access through getInstance
	private S3UserContainer() {
		xpath = XPathFactory.newInstance().newXPath();
	}

	public static S3UserContainer getInstance() {
		XPathFactory xpf = XPathFactory.newInstance();
		xpath = xpf.newXPath();
		return instance;
	}

	public static S3UserContainer getInstance(final String xmlForUsers) {
		myXml = xmlForUsers;
		xpath = XPathFactory.newInstance().newXPath();
		return instance;
	}

	public void setUsersXML(final String xmlForUsers) {
		myXml = xmlForUsers;
		xpath = XPathFactory.newInstance().newXPath();
	}

	public List<S3UserItem> getUsers() {
		NodeList isActiveNodes = null;
		NodeList nameNodes = null;
		NodeList guidNodes = null;
		NodeList languageNodes = null;
		S3NamespaceContext mnsp = new S3NamespaceContext();
		xpath.setNamespaceContext(mnsp);
		if(xpath == null) {
			Log.e(TAG,"There's something wrong with class, xpath parser is null.");
			return null;
		}
		try {
			inputSource = new InputSource (new StringReader(myXml));
			isActiveNodes = (NodeList)xpath.evaluate(XPATH_USER_ACTIVE,inputSource,XPathConstants.NODESET);
			Log.d(TAG,"isLockedNodes match:"+isActiveNodes.getLength()+" items.");
			inputSource = new InputSource(new StringReader(myXml));
			nameNodes = (NodeList)xpath.evaluate(XPATH_USER_NAMES,inputSource,XPathConstants.NODESET);
			Log.d(TAG,"nameNodes match:"+nameNodes.getLength()+" items.");
			inputSource = new InputSource(new StringReader(myXml));
			guidNodes = (NodeList)xpath.evaluate(XPATH_USER_GUID,inputSource,XPathConstants.NODESET);
			Log.d(TAG,"guidNodes match:"+guidNodes.getLength()+" items.");
			inputSource = new InputSource(new StringReader(myXml));
			languageNodes = (NodeList)xpath.evaluate(XPATH_USER_LANGUAGE,inputSource,XPathConstants.NODESET);
			Log.d(TAG,"languageNodes match:"+languageNodes.getLength()+" items.");
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
		if(totalItems != languageNodes.getLength()) {
			Log.e(TAG,"Node lists are not of same length! (isActive & language lists");
		}
		ArrayList<S3UserItem> res = new ArrayList<S3UserItem>(totalItems);
		for(int i=0; i< totalItems; i++) {
			S3UserItem UserItem = new S3UserItem();
			Node isActiveNode = isActiveNodes.item(i);
			Node nameNode = nameNodes.item(i);
			Node guidNode = guidNodes.item(i);
			Node languageNode = languageNodes.item(i);
			UserItem.setIsActive(isActiveNode.getTextContent());
			UserItem.setUserGUID(guidNode.getTextContent());
			UserItem.setFirstName(nameNode.getTextContent());
			UserItem.setLanguage(languageNode.getTextContent());
			res.add(UserItem);
		}
		return res;

	}

	/**
	 * Container class for one Severa 3 User type. 
	 * @author juha
	 *
	 */

	public class S3UserItem {
		private String firstName;
		private String isActive;
		private String userGUID;
		private String language;
		public String getFirstName() {
			return firstName;
		}
		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}
		public String getIsActive() {
			return isActive;
		}
		public void setIsActive(String isActive) {
			this.isActive = isActive;
		}
		public String getUserGUID() {
			return userGUID;
		}
		public void setUserGUID(String userGUID) {
			this.userGUID = userGUID;
		}
		public String getLanguage() {
			return language;
		}
		public void setLanguage(String language) {
			this.language = language;
		}
		
		
	}	
}
