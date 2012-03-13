package com.digitalfingertip.mobisevera;

import java.util.ArrayList;
import java.util.Calendar;

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
import android.widget.DatePicker;
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
	protected DatePicker startDatePicker = null;
	protected DatePicker endDatePicker = null;
	protected LoadHourEntriesXMLTask loadHoursTask = null;
	protected ProgressDialog pd = null;

	private static  int currentlySelectedCase = 0;
	
	private static final int NO_PROJECTS_DIALOG_ID = 001;
	private static final int STARTED_HOURS_QUERY_DIALOG_ID = 002;
	private static final int NO_HOURS_TO_SHOW_DIALOG = 003;
	private static final int NOT_CONNECTED_DIALOG_ID = 004;
	
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
		projectList = extrasBundle.getParcelableArrayList(MobiseveraProjectActivity.PROJECTLIST_BUNDLE_KEY);
		if(projectList == null || projectList.isEmpty()) {
			showDialog(NO_PROJECTS_DIALOG_ID);
		} else {
			//Insert first item to the list to indicate that user wishes to see claims for all projects
			S3CaseItem everyProjectCaseItem = new S3CaseItem();
			everyProjectCaseItem.setCaseInternalName("[All projects]");
			everyProjectCaseItem.setCaseGuid(MobiseveraConstants.MAGIC_CASE_GUID_FOR_ALL_WILDCARD);
			everyProjectCaseItem.setCaseAccountName("");
			projectList.add(0, everyProjectCaseItem );
			projectSpinner = (Spinner)findViewById(R.id.queryui_projectnamespinner);
			ArrayAdapter<S3CaseItem> projectAdapter = 
					new ArrayAdapter<S3CaseItem>(this, android.R.layout.simple_spinner_dropdown_item, projectList);
	        projectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        projectSpinner.setAdapter(projectAdapter);
	        projectSpinner.setOnItemSelectedListener(this);
		}
		startDatePicker = (DatePicker)findViewById(R.id.datePickerFromDate);
		endDatePicker = (DatePicker)findViewById(R.id.datePickerToDate);
		Button submitButton = (Button)findViewById(R.id.query_button_id);
		submitButton.setOnClickListener(this);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		//TODO:refactor method, remove repetition
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
		} else if (id == STARTED_HOURS_QUERY_DIALOG_ID) {
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
		} else if(id == NO_HOURS_TO_SHOW_DIALOG) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("No claims found for this project.")
			       .setCancelable(false)
			       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                QueryHourEntries.this.finish();
			           }
			       });
			AlertDialog alert = builder.create();
			return alert;	
		} else if(id == NOT_CONNECTED_DIALOG_ID) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Querying hour entries requires a working network connection. Please get connected.")
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
	public void onItemSelected(AdapterView<?> adapterView, View view, int position,
			long id) {
		QueryHourEntries.currentlySelectedCase = position;
		Log.d(TAG,"Selecting position:"+position);
	}

	@Override
	public void onNothingSelected(AdapterView<?> adapterView) {
		QueryHourEntries.currentlySelectedCase = 0;
		Log.d(TAG,"Selecting position 0.");
	}

	@Override
	public void onClick(View buttonView) {
		
		Log.d(TAG, "onClick on the QueryHourentries.");
		if(MobiseveraCommsUtils.checkIfConnected(this) == false) {
			showDialog(NOT_CONNECTED_DIALOG_ID);
			return;
		}
		//get parameters: startDate, endDate, userGuid
		Calendar startDateCal = Calendar.getInstance();
		startDateCal.set(startDatePicker.getYear(), startDatePicker.getMonth(), startDatePicker.getDayOfMonth());
		
		String startDateStr = MobiseveraConstants.S3_DATE_FORMATTER.format(startDateCal.getTime());
		Calendar endDateCal = Calendar.getInstance();
		endDateCal.set(endDatePicker.getYear(), endDatePicker.getMonth(), endDatePicker.getDayOfMonth());
		String endDateStr = MobiseveraConstants.S3_DATE_FORMATTER.format(endDateCal.getTime());
		
		if(startDateStr != null && startDateStr.length() == 10) { //"yyyy-MM-dd".length == 10 
			Log.d(TAG,"StartDateStr is:"+startDateStr);
		} else {
			throw new IllegalStateException("startDateString is null or of wrong length! ("+startDateStr+")");
		}
		if(endDateStr != null && endDateStr.length() == 10) { //"yyyy-MM-dd".length == 10 
			Log.d(TAG,"EndDateStr is:"+endDateStr);
		} else {
			throw new IllegalStateException("endDateString is null or of wrong length! ("+endDateStr+")");
		}
			
		MobiseveraContentStore scs = new MobiseveraContentStore(this);
		String userGuid = scs.fetchUserGUID();
		if(userGuid == null) {
			throw new IllegalStateException("UserGuid is null - cannot query hours.");
		}
		loadHoursTask = new LoadHourEntriesXMLTask(this);
		loadHoursTask.execute(startDateStr, endDateStr, userGuid);
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
			//expecting three String parameters; startDate, endDate, userGuid
			if(args != null && args.length == 3) {
				Log.d(TAG,"Good parameters, start to load hours...");
			} else {
				throw new IllegalArgumentException("Null or wrong amount of arguments for the background thread (loadHours).");
			}
			MobiseveraCommsUtils scu = new MobiseveraCommsUtils();
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
			mParent.receiveHoursLoadingReadyEvent(result);
		}
	}

	public void receiveHoursLoadingReadyEvent(ArrayList<S3HourEntryItem> hoursList) {
		Intent listIntent = new Intent();
		listIntent.setClass(this, ListHourEntries.class);
		Bundle bundle = new Bundle();
		if(QueryHourEntries.currentlySelectedCase == 0) {
			bundle.putParcelableArrayList(ListHourEntries.HOUR_ENTRIES_KEY, hoursList);
			Log.d(TAG,"As all cases are selected, not filtering.");
		} else {
			String currentlySelectedCaseGUID = projectList.get(QueryHourEntries.currentlySelectedCase).getCaseGuid();
			Log.d(TAG,"Currently selected case GUID:"+currentlySelectedCaseGUID);
			for(int i = 0; i < hoursList.size(); i++) {
				if(hoursList.get(i).getCaseGuid().equalsIgnoreCase(currentlySelectedCaseGUID)) {
					// ok, keep in list
				} else {
					// remove hour entry since user doesn't want to see this
					hoursList.remove(i);
				}
			}
			Log.d(TAG,"After filtering, hours list contains "+hoursList.size()+" entries.");
			bundle.putParcelableArrayList(ListHourEntries.HOUR_ENTRIES_KEY, hoursList);
		}
		if(hoursList.size() == 0) {
			showDialog(NO_HOURS_TO_SHOW_DIALOG);
			return;
		} else {
			listIntent.putExtras(bundle);
			startActivity(listIntent);
		}
	}
}
