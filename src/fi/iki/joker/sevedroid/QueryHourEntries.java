package fi.iki.joker.sevedroid;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.widget.Button;
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
	
	protected Spinner projectSpinner = null;
	protected boolean queryCancelled = false;
	protected LoadHourEntriesXMLTask loadHoursTask = null;
	protected ProgressDialog pd = null;
	
	private static final int NO_PROJECTS_DIALOG_ID = 001;
	private static final int STARTED_HOURS_QUERY_DIALOG_ID = 002;
	
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
			projectSpinner = (Spinner)findViewById(R.id.queryui_projectnamespinner);
			ArrayAdapter<S3CaseItem> projectAdapter = 
					new ArrayAdapter<S3CaseItem>(this, android.R.layout.simple_spinner_dropdown_item, projectList);
	        projectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        projectSpinner.setAdapter(projectAdapter);
	        projectSpinner.setOnItemSelectedListener(this);
		}
		Button submitButton = (Button)findViewById(R.id.query_button_id);
		submitButton.setOnClickListener(this);
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
		} if (id == STARTED_HOURS_QUERY_DIALOG_ID) {
			pd = new ProgressDialog(this);
			pd.setMessage("Querying hours... ");
			pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pd.show();
			pd.setCancelable(true);
			pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					if(loadHoursTask != null) {
						loadHoursTask.cancel(true);
					}
					pd.dismiss();
				}
			});
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
	public void onClick(View buttonView) {
		loadHoursTask = new LoadHourEntriesXMLTask(this);
		loadHoursTask.execute();
	}
	
	/**
	 * AsyncTask for loading the cases XML from S3 SOAP service 
	 * params: doinBackground(startDate, endDate, userGuid)
	 */
	
	private class LoadHourEntriesXMLTask extends AsyncTask<String, Integer, ArrayList<S3HourEntryItem>> {
		
		private QueryHourEntries mParent;
		public static final int STATUS_INIT = 1;
		public static final int STATUS_TRANSFERRING = 2;
		public static final int STATUS_PARSING = 3;
		public static final int STATUS_RETURNING = 4;
		
		public LoadHourEntriesXMLTask(QueryHourEntries parent) {
			mParent = parent;
		}
		
		@Override
		protected ArrayList<S3HourEntryItem> doInBackground(String... args) {
			Log.d(TAG,"Started doInBackground for LoadHourEntriesXMLTask!");
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
	        Log.d(TAG,"Done with SOAP call, starting to parse response...");
			publishProgress(STATUS_PARSING);
			Log.d(TAG,"Parsing done.");
			ArrayList<S3HourEntryItem> res = S3HourEntries.getHourEntries();
	        publishProgress(STATUS_RETURNING);
	        Log.d(TAG,"Background query thread returning (items: "+res.size()+")");
	        return res;
		}
		
		protected void onProgressUpdate(Integer... progress) {
			Log.d(TAG,"Setting progress to: "+progress[0].toString());
	         setProgress(progress[0]);
	     }
		
		@Override
		protected void onPostExecute(ArrayList<S3HourEntryItem> result) {
			Log.d(TAG,"onPostExecute on LoadCasesXMLTask firing.");
			mParent.receiveHoursLoadingReadyEvent();
		}
	}

	public void receiveHoursLoadingReadyEvent() {
		Intent listIntent = new Intent();
		listIntent.setClass(this, ListHourEntries.class);
	}

}
