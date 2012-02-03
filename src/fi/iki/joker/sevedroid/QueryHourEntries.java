package fi.iki.joker.sevedroid;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class QueryHourEntries extends Activity implements OnClickListener,
		OnItemSelectedListener {

	public static final String TAG = "Sevedroid";
	
	/**
	 * The parcelable list containing the hours this user id has access to and the parcel identifier
	 */
	protected ArrayList<S3HourEntryItem> hoursList = null;
	protected static final String HOURSITEMLIST_PARCEL_ID = "caseItemParcelID";
	
	/**
	 * The parcelable list containing the Cases this user id has access to and the parcel identifier
	 */
	protected ArrayList<S3CaseItem> projectList = null;
	protected static final String CASEITEMLIST_PARCEL_ID = "caseItemParcelID";
	
	private static final int NO_PROJECTS_DIALOG_ID = 001;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.queryui);
		Intent starter = this.getIntent();
		if(starter == null) {
			Log.d(TAG,"Cannot obtain intent which started this activity.");
			throw new IllegalArgumentException("Cannot obtain required values to create the activity (intent).");
		}
		Bundle extrasBundle = starter.getExtras();
		if(extrasBundle == null) {
			Log.d(TAG,"Cannot obtain intent which started this activity.");
			throw new IllegalArgumentException("Cannot obtain required values to create the activity (intent's extras).");
		} else {
			Log.d(TAG,"Extras bundle is not null");
		}
		projectList = extrasBundle.getParcelableArrayList(SevedroidProjectActivity.PROJECTLIST_BUNDLE_KEY);
		if(projectList == null || projectList.isEmpty()) {
			showDialog(NO_PROJECTS_DIALOG_ID);
		} else {
			Spinner projectSpinner = (Spinner)findViewById(R.id.queryui_projectnamespinner);
			ArrayAdapter<S3CaseItem> projectAdapter = 
					new ArrayAdapter<S3CaseItem>(this, android.R.layout.simple_spinner_dropdown_item, projectList);
	        projectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        projectSpinner.setAdapter(projectAdapter);
	        projectSpinner.setOnItemSelectedListener(this);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if(id == NO_PROJECTS_DIALOG_ID) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("There are no projects available")
			       .setCancelable(false)
			       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                QueryHourEntries.this.finish();
			           }
			       });
			AlertDialog alert = builder.create();
			return alert;	
		}
		return null;
	}
	
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub

	}
	
	/**
	 * AsyncTask for loading the cases XML from S3 SOAP service 
	 * params: doinBackground(startDate, endDate, userGuid)
	 */
	
	private class LoadHourEntriesXMLTask extends AsyncTask<String, Integer, Boolean> {
		
		private SevedroidProjectActivity mParent;
		public static final int STATUS_INIT = 1;
		public static final int STATUS_TRANSFERRING = 2;
		public static final int STATUS_PARSING = 3;
		public static final int STATUS_RETURNING = 4;
		
		public LoadHourEntriesXMLTask(SevedroidProjectActivity parent) {
			mParent = parent;
		}
		
		@Override
		protected Boolean doInBackground(String... args) {
			Log.d(TAG,"Started doInBackground for LoadHourEntriesXMLTask!");
			mParent.projectNameSpinner.setClickable(false);
			publishProgress(STATUS_INIT);
			SeveraCommsUtils scu = new SeveraCommsUtils();
			S3HourEntryContainer S3HourEntries = S3HourEntryContainer.getInstance();
			S3CaseContainer S3CC = S3CaseContainer.getInstance();
			String startDate = args[0];
			String endDate = args[1];
			String userGuid = args[2]; 
			//The next invocation is the one that takes time
			publishProgress(STATUS_TRANSFERRING);
			S3HourEntries.setHourEntriesXML(scu.getHourEntriesByDateAndUserGUID(mParent, startDate, endDate, userGuid));
	        publishProgress(STATUS_PARSING);
			String [] distinctCaseGuids = S3HourEntries.getDistinctCaseGUIDsFromHourEntryList(S3HourEntries.getHourEntries());
			mParent.projectList.clear();
			for(int i = 0; i < distinctCaseGuids.length; i++) {
				String caseXML = scu.getCaseXMLByGUID(mParent, distinctCaseGuids[i]);
				S3CC.setCaseXML(caseXML);
				mParent.projectList.add(S3CC.getCase());
			}
			
			publishProgress(STATUS_RETURNING);
			if(mParent.projectList.isEmpty()) {
				Log.e(TAG,"Project list is empty! Returning with nothing to tell.");
				return new Boolean(false);
			} else {
				Log.d(TAG,"Project list is not empty, enabling projectspinner...");
				mParent.projectNameSpinner.setClickable(true);
				return new Boolean(true);
			}
		}
		
		protected void onProgressUpdate(Integer... progress) {
			Log.d(TAG,"Setting progress to: "+progress[0].toString());
	         setProgress(progress[0]);
	     }
		
		@Override
		protected void onPostExecute(Boolean result) {
			Log.d(TAG,"onPostExecute on LoadCasesXMLTask firing.");
			mParent.receiveProjectLoadingReadyEvent();
		}
	}

}
