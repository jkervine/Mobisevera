package com.digitalfingertip.mobisevera;


import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Container class for one Severa 3 case. A list of these is returned after parsing the answer
 * to getAllCases api call.
 * @author juha
 *
 */

public class S3CaseItem implements Parcelable {
	
	private static final String TAG = "Sevedroid";
	
	private String caseGuid;
	private String caseAccountName;
	private String caseInternalName;
		
	public S3CaseItem() {
		this.caseGuid = null;
		this.caseAccountName = null;
		this.caseInternalName = null;
	}
	
	public S3CaseItem(Parcel source) {
		Log.d(TAG, "Recostructing from Parcel...");
		this.caseGuid = source.readString();
		this.caseAccountName = source.readString();
		this.caseInternalName = source.readString();
	}
	
	public String getCaseGuid() {
		return caseGuid;
	}
	public void setCaseGuid(String caseGuid) {
		this.caseGuid = caseGuid;
	}
	public String getCaseAccountName() {
		return caseAccountName;
	}
	public void setCaseAccountName(String caseAccountName) {
		this.caseAccountName = caseAccountName;
	}
	public String getCaseInternalName() {
		return caseInternalName;
	}
	public void setCaseInternalName(String caseInternalName) {
		this.caseInternalName = caseInternalName;
	}
	public String toString() {
		return this.caseInternalName;
	}
	@Override
	public int describeContents() {
		return this.hashCode();
	}
	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		Log.d(TAG, "Writing to parcel :"+this.hashCode()+" with flags: "+flags);
		parcel.writeString(caseGuid);
		parcel.writeString(caseAccountName);
		parcel.writeString(caseInternalName);
	}
	
	public static final Parcelable.Creator<S3CaseItem> CREATOR = new Parcelable.Creator<S3CaseItem>() {

		@Override
		public S3CaseItem createFromParcel(Parcel source) {
			Log.d(TAG,"createFromParcel called!");
			return new S3CaseItem(source);
		}

		@Override
		public S3CaseItem[] newArray(int size) {
			Log.d(TAG, "newArray called!");
			return new S3CaseItem[size];
		}
		
	};
}

