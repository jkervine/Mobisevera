package fi.iki.joker.sevedroid;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Container class for one Severa 3 work type. 
 * @author juha
 *
 */

public class S3WorkTypeItem implements Parcelable {
	
	private String workTypeName;
	private String isActive;
	private String workTypeGUID;
	
	public S3WorkTypeItem() {
		workTypeName = null;
		isActive = null;
		workTypeGUID = null;
	}
	
	public S3WorkTypeItem(Parcel source) {
		isActive = source.readString();
		workTypeGUID = source.readString();
		workTypeName =  source.readString();
	}
	public String getWorkTypeName() {
		return workTypeName;
	}
	public void setWorkTypeName(String workTypeName) {
		this.workTypeName = workTypeName;
	}
	public String getIsActive() {
		return isActive;
	}
	public void setIsActive(String isActive) {
		this.isActive = isActive;
	}
	public String getWorkTypeGUID() {
		return workTypeGUID;
	}
	public void setWorkTypeGUID(String workTypeGUID) {
		this.workTypeGUID = workTypeGUID;
	}
	public String toString() {
		String str = this.workTypeName+"(";
		if(Boolean.parseBoolean(this.isActive)) {
			str=str+"active)";
		} else {
			str=str+"inactive)";
		}
		return str;
	}
	@Override
	public int describeContents() {
		return this.hashCode();
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.isActive);
		dest.writeString(this.workTypeGUID);
		dest.writeString(this.workTypeName);
	}


	public static Parcelable.Creator<S3WorkTypeItem> CREATOR = new Parcelable.Creator<S3WorkTypeItem> () {

		@Override
		public S3WorkTypeItem createFromParcel(Parcel source) {
			return new S3WorkTypeItem(source);
		}

		@Override
		public S3WorkTypeItem[] newArray(int size) {
			return new S3WorkTypeItem[size];
		}
	};
}
