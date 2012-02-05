package fi.iki.joker.sevedroid;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

public class ListHourEntries extends ListActivity {

	public static final String HOUR_ENTRIES_KEY = "Hour_entries_key";
	
	private static final int DIALOG_ID_INTENT_NULL = 0;
	private static final int DIALOG_ID_EXTRAS_BUNDLE_NULL = 1;
	private static final int DIALOG_ID_PARCEL_ATTRS_NULL = 2;

	private static final String TAG = "Sevedroid";
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "ListHourEntries onCreate called.");
		setContentView(R.layout.hours_list);
		Intent hoursListIntent = getIntent();
		if(hoursListIntent == null) {
			showDialog(DIALOG_ID_INTENT_NULL);
			return;
		}
		Bundle attrsBundle = hoursListIntent.getExtras();
		if(attrsBundle == null) {
			showDialog(DIALOG_ID_EXTRAS_BUNDLE_NULL);
			return;
		}
		ArrayList<S3HourEntryItem> listItems = attrsBundle.getParcelableArrayList(HOUR_ENTRIES_KEY);
		if(listItems == null) {
			showDialog(DIALOG_ID_PARCEL_ATTRS_NULL);
			return;
		}
		this.setListAdapter(new ArrayAdapter<S3HourEntryItem>(this,android.R.layout.simple_list_item_1,listItems));
		Log.d(TAG, "onCreate done.");
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (id) {
			case DIALOG_ID_INTENT_NULL:
				builder.setMessage("Error (1) in listing your hours. Sorry. Please report this to:"+
						SevedroidConstants.DF_SUPPORT_EMAIL+". Thank you.")
				     .setCancelable(false)
				     .setPositiveButton("OK", new DialogInterface.OnClickListener() {
				    	 public void onClick(DialogInterface dialog, int id) {
				    		 ListHourEntries.this.finish();
				    	 }
					 });
				break;
			case DIALOG_ID_EXTRAS_BUNDLE_NULL:
				builder.setMessage("Error (1) in listing your hours. Sorry. Please report this to:"+
						SevedroidConstants.DF_SUPPORT_EMAIL+". Thank you.")
				     .setCancelable(false)
				     .setPositiveButton("OK", new DialogInterface.OnClickListener() {
				    	 public void onClick(DialogInterface dialog, int id) {
				    		 ListHourEntries.this.finish();
				    	 }
					 });
				break;
			case DIALOG_ID_PARCEL_ATTRS_NULL:
				builder.setMessage("Error (1) in listing your hours. Sorry. Please report this to:"+
						SevedroidConstants.DF_SUPPORT_EMAIL+". Thank you.")
				     .setCancelable(false)
				     .setPositiveButton("OK", new DialogInterface.OnClickListener() {
				    	 public void onClick(DialogInterface dialog, int id) {
				    		 ListHourEntries.this.finish();
				    	 }
					 });
				break;
		}
		return super.onCreateDialog(id);
	}
	
	
	
}
