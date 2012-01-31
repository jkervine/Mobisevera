package fi.iki.joker.sevedroid;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import fi.iki.joker.sevedroid.S3HourEntryContainer.S3HourEntry;

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
			"/s:Envelope/s:Body/x:GetHourEntriesByDateAndUserGUIDResponse/x:GetHourEntriesByDateAndUserGUIDResult/a:HourEntry/a:CaseGUID";

	private S3HourEntryContainer() {
		
	}
	
	private S3HourEntryContainer getInstance() {
		return instance;
	}
	
	public ArrayList<S3HourEntry> getCases() {
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
			// TODO Auto-generated catch block
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
		ArrayList<S3HourEntry> res = new ArrayList<S3HourEntry>(totalItems);
		for(int i=0; i< totalItems; i++) {
			S3HourEntry HourEntry = new S3HourEntry();
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
	 * Container class for one Severa 3 case. A list of these is returned after parsing the answer
	 * to getAllCases api call.
	 * @author juha
	 *
	 */
	
	protected class S3HourEntry implements Parcelable {
		private String caseGuid;
		private String quantity;
		private String description;
		
		public S3HourEntry() {
			this.caseGuid = null;
			this.quantity = null;
			this.description = null;
		}
		
		public S3HourEntry(Parcel source) {
			Log.d(TAG, "Recostructing from Parcel...");
			this.caseGuid = source.readString();
			this.quantity = source.readString();
			this.description = source.readString();
		}
		
		public String getCaseGuid() {
			return caseGuid;
		}
		public void setCaseGuid(String caseGuid) {
			this.caseGuid = caseGuid;
		}
		public String getQuantity() {
			return quantity;
		}
		public void setQuantity(String quantity) {
			this.quantity = quantity;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public String toString() {
			return this.description+" - "+quantity;
		}
		@Override
		public int describeContents() {
			return this.hashCode();
		}
		@Override
		public void writeToParcel(Parcel parcel, int flags) {
			Log.d(TAG, "Writing to parcel :"+this.hashCode()+" with flags: "+flags);
			parcel.writeString(caseGuid);
			parcel.writeString(quantity);
			parcel.writeString(description);
		}
		
		
	}
	
	public class S3HourEntryCreator implements Parcelable.Creator<S3HourEntry> {

		@Override
		public S3HourEntry createFromParcel(Parcel source) {
			return new S3HourEntry(source);
		}

		@Override
		public S3HourEntry[] newArray(int size) {
			return new S3HourEntry[size];
		}
		
	}
	
}
