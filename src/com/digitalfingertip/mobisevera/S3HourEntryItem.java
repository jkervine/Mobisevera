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

public class S3HourEntryItem implements Parcelable {
	
	private static final String TAG = "Sevedroid";
	private String caseGuid;
	private String quantity;
	private String description;
	
	public S3HourEntryItem() {
		this.caseGuid = null;
		this.quantity = null;
		this.description = null;
	}
	
	public S3HourEntryItem(Parcel source) {
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
	
	


	public static Parcelable.Creator<S3HourEntryItem> CREATOR = new Parcelable.Creator<S3HourEntryItem>() {

		@Override
		public S3HourEntryItem createFromParcel(Parcel source) {
			return new S3HourEntryItem(source);
		}

		@Override
		public S3HourEntryItem[] newArray(int size) {
			return new S3HourEntryItem[size];
		}
	
	};
	
}

