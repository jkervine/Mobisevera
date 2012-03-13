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

import android.util.Log;

public class S3PhaseContainer {

	private static final String TAG = "Sevedroid";
	private static final S3PhaseContainer instance = new S3PhaseContainer();
	private static String myXml = null;
	private static XPath xpath = null;
	private static InputSource inputSource = null;

	// The xpath expressions for digging out the valued needed for this class's methods

	private static final String XPATH_PHASE_NAMES = 
			"/s:Envelope/s:Body/x:GetPhasesByCaseGUIDResponse/x:GetPhasesByCaseGUIDResult/a:Phase/a:Name";

	private static final String XPATH_PHASE_LOCKED = 
			"/s:Envelope/s:Body/x:GetPhasesByCaseGUIDResponse/x:GetPhasesByCaseGUIDResult/a:Phase/a:IsLocked";

	private static final String XPATH_PHASE_GUID = 
			"/s:Envelope/s:Body/x:GetPhasesByCaseGUIDResponse/x:GetPhasesByCaseGUIDResult/a:Phase/a:GUID";
	// protect constructor, force access through getInstance
	private S3PhaseContainer() {
		xpath = XPathFactory.newInstance().newXPath();
	}

	public static S3PhaseContainer getInstance() {
		XPathFactory xpf = XPathFactory.newInstance();
		xpath = xpf.newXPath();
		return instance;
	}

	public static S3PhaseContainer getInstance(final String xmlForPhases) {
		myXml = xmlForPhases;
		xpath = XPathFactory.newInstance().newXPath();
		return instance;
	}

	public void setPhasesXML(final String xmlForPhases) {
		myXml = xmlForPhases;
		xpath = XPathFactory.newInstance().newXPath();
	}

	public ArrayList<S3PhaseItem> getPhases() {
		NodeList isLockedNodes = null;
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
			isLockedNodes = (NodeList)xpath.evaluate(XPATH_PHASE_LOCKED,inputSource,XPathConstants.NODESET);
			Log.d(TAG,"isLockedNodes match:"+isLockedNodes.getLength()+" items.");
			inputSource = new InputSource(new StringReader(myXml));
			nameNodes = (NodeList)xpath.evaluate(XPATH_PHASE_NAMES,inputSource,XPathConstants.NODESET);
			Log.d(TAG,"nameNodes match:"+nameNodes.getLength()+" items.");
			inputSource = new InputSource(new StringReader(myXml));
			guidNodes = (NodeList)xpath.evaluate(XPATH_PHASE_GUID,inputSource,XPathConstants.NODESET);
			Log.d(TAG,"guidNodes match:"+guidNodes.getLength()+" items.");
		} catch (XPathExpressionException e) {
			Log.e(TAG,"XPathExpression evaluation failed, exception is:",e);
			return null;
		}
		// sanity checks
		int totalItems = isLockedNodes.getLength();
		if(totalItems != nameNodes.getLength()) {
			Log.e(TAG,"Node lists are not of same lenght! (isLocked & name) lists");
			return null;
		}
		if(totalItems != guidNodes.getLength()) {
			Log.e(TAG,"Node lists are not of same length! (isLocked & guid lists)");
			return null;
		}
		ArrayList<S3PhaseItem> res = new ArrayList<S3PhaseItem>(totalItems);
		for(int i=0; i< totalItems; i++) {
			S3PhaseItem phaseItem = new S3PhaseItem();
			Node isLockedNode = isLockedNodes.item(i);
			Node nameNode = nameNodes.item(i);
			Node guidNode = guidNodes.item(i);
			phaseItem.setPhaseLocked(isLockedNode.getTextContent());
			phaseItem.setPhaseGUID(guidNode.getTextContent());
			phaseItem.setPhaseName(nameNode.getTextContent());
			res.add(phaseItem);
		}
		return res;

	}

	
}

