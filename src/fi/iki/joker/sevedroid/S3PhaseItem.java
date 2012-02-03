package fi.iki.joker.sevedroid;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Container class for one Severa 3 case. A list of these is returned after parsing the answer
 * to getAllCases api call.
 * @author juha
 *
 */

public class S3PhaseItem implements Parcelable {
	
	private String phaseName;
	private String phaseLocked;
	private String phaseGUID;
	
	public S3PhaseItem() {
		phaseName = null;
		phaseLocked = null;
		phaseGUID = null;
	}
	
	public S3PhaseItem(Parcel parcel) {
		phaseGUID = parcel.readString();
			phaseLocked = parcel.readString();
		phaseName = parcel.readString();
	}
	public String getPhaseName() {
		return phaseName;
	}
	public void setPhaseName(String phaseName) {
		this.phaseName = phaseName;
	}
	public String getPhaseLocked() {
		return phaseLocked;
	}
	public void setPhaseLocked(String phaseLocked) {
		this.phaseLocked = phaseLocked;
	}
	public String getPhaseGUID() {
		return phaseGUID;
	}
	public void setPhaseGUID(String phaseGUID) {
		this.phaseGUID = phaseGUID;
	}

	public String toString() {
		if("true".equals(this.phaseLocked)) {
			return this.phaseName+" (locked)";
		} else {
			return this.phaseName;
		}
	}
	@Override
	public int describeContents() {
		return this.hashCode();
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.phaseGUID);
		dest.writeString(this.phaseLocked);
		dest.writeString(this.phaseName);
	}

	public static android.os.Parcelable.Creator<S3PhaseItem> CREATOR = new Parcelable.Creator<S3PhaseItem>() {

		@Override
		public S3PhaseItem createFromParcel(Parcel parcel) {
			return new S3PhaseItem(parcel);
		}

		@Override
		public S3PhaseItem[] newArray(int count) {		
			return new S3PhaseItem[count];
		}
	};
}