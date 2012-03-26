package com.digitalfingertip.mobisevera.activity;

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
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.digitalfingertip.mobisevera.MobiseveraCommsUtils;
import com.digitalfingertip.mobisevera.MobiseveraConstants;
import com.digitalfingertip.mobisevera.MobiseveraContentStore;
import com.digitalfingertip.mobisevera.MobiseveraNaviContainer;
import com.digitalfingertip.mobisevera.R;
import com.digitalfingertip.mobisevera.S3CaseContainer;
import com.digitalfingertip.mobisevera.S3CaseItem;

/**
 * This class is responsible for displaying a list of projects (or Cases, as severa calls them) to the user and then
 * when user selects one, relaying it back to the calling activity
 * @author juha
 *
 */

public class MobiseveraSelectProject extends Activity implements OnItemSelectedListener {

	private static final String TAG = "Sevedroid";
	private static final int NOT_CONNECTED_DIALOG_ID = 1;
	private static final int DIALOG_ID_MISSING_CASEGUID = 2;
	private static boolean selected = false;
	
	/**
	 * The list containing the Cases this user id has access to and the parcel identifier
	 */
	protected ArrayList<S3CaseItem> projectList = null;
	protected static final String CASEITEMLIST_PARCEL_ID = "caseItemParcelID";
	
	ProgressBar projectsProgress = null;
	
	ArrayAdapter<CharSequence> projectAdapter = null;
	Spinner projectNameSpinner = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.select_project_layout);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.activity_title);
        Log.d(TAG,"OnCreate called on MobiseveraSelectProject Activity!");
        if(MobiseveraCommsUtils.checkIfConnected(this) == false) {
			showDialog(NOT_CONNECTED_DIALOG_ID);
			return;
		}
        MobiseveraContentStore contentStore = new MobiseveraContentStore(this); 
        // test if the user has set the api key, if yes, then follow on to load up the projects
        // if not, then auto start the config activity
        if(contentStore.fetchApiKey() == null) {
        	Log.d(TAG,"Finishing  activity because the API key is not found in contentstore.");
        	Toast.makeText(this,"Please input your API key to use this app!",Toast.LENGTH_LONG).show();
			setResult(MobiseveraNaviContainer.RESULT_CODE_NO_API_KEY);
			this.finish();
			return;
        }
        if(savedInstanceState == null) {
        	Log.d(TAG, "Saved instance state is null, recreating Activity state.");
        	selected = false;
        	projectsProgress = (ProgressBar)findViewById(R.id.projectsLoadProgress);
        	projectNameSpinner = (Spinner)findViewById(R.id.projectNameSpinner);
        	projectList = new ArrayList<S3CaseItem>();
        	projectsProgress.setVisibility(View.VISIBLE);
        	new LoadCasesXMLTask(this).execute();
        	Toast.makeText(this, "Started to load projects... they will be available once loaded...", Toast.LENGTH_SHORT).show();
        } else {
        	Log.d(TAG, "Saved instance state is not null, restoring Activity state...");
        	projectsProgress = (ProgressBar)findViewById(R.id.projectsLoadProgress);
        	projectList = savedInstanceState.getParcelableArrayList(CASEITEMLIST_PARCEL_ID);
        	Log.d(TAG, "Restored projectList with "+((projectList == null) ? "null": projectList.size())+" items");
         	projectNameSpinner = (Spinner)findViewById(R.id.projectNameSpinner);
         	projectsProgress.setVisibility(View.GONE);
        }
        projectAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, projectList);
        projectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        projectNameSpinner.setAdapter(projectAdapter);
        projectNameSpinner.setOnItemSelectedListener(this);
        
    }
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		//TODO:Critical: (*) if instance state is saved during the projects are loading, spinners stay empty!!!
		Log.d(TAG,"onSaveInstanceState called!");
		outState.putParcelableArrayList(CASEITEMLIST_PARCEL_ID, projectList);
		selected = false; // otherwise onItemSelected fires after unparceling in onCreate
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int position,
			long id) {
		Log.d(TAG,"OnItemSelected called: adapterViewID:"+adapterView.getId()+"view ID:"+view.getId()+", pos:"+position+" id:"+id);
		if(!selected) {
			Log.d(TAG,"Ignoring the first onItemSelected due to layout draw.");
			selected = true;
			return;
		}
		S3CaseItem caseItem = projectList.get(position);
		Log.d(TAG,"User selected case pos: "+position+" with GUID: "+caseItem.getCaseGuid());
		Intent backToCallerIntent = new Intent();
		backToCallerIntent.putExtra(MobiseveraConstants.CASE_PARCEL_EXTRA_ID, caseItem);
		setResult(MobiseveraNaviContainer.RESULT_CODE_PROJECT_BEAN_LOADED, backToCallerIntent);
		this.finish();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if(id == NOT_CONNECTED_DIALOG_ID) {
			builder.setMessage("This action requires a network connection.")
			       .setCancelable(false)
			       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                MobiseveraSelectProject.this.finish();
			           }
			       });			
		}
		if(id == DIALOG_ID_MISSING_CASEGUID) {
			builder.setMessage("Project GUID missing in selection!")
			.setCancelable(false)
		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                MobiseveraSelectProject.this.finish();
		           }
		       });	
		}
		AlertDialog alert = builder.create();
		return alert;	
	}
	/**
	 * AsyncTask for loading the cases XML from S3 SOAP service 
	 */
	
	private class LoadCasesXMLTask extends AsyncTask<Void, Integer, Boolean> {
		
		private MobiseveraSelectProject mParent;
		public static final int STATUS_INIT = 1;
		public static final int STATUS_TRANSFERRING = 2;
		public static final int STATUS_PARSING = 3;
		public static final int STATUS_RETURNING = 4;
		
		public LoadCasesXMLTask(MobiseveraSelectProject parent) {
			mParent = parent;
		}
		
		@Override
		protected Boolean doInBackground(Void... nullArgs) {
			Log.d(TAG,"Started doInBackground for LoadCasesXMLTask!");
			if(mParent == null) {
				Log.e(TAG,"Backgroundtask cancelled because reference to parent activity is null!");
				this.cancel(true);
			}
			Spinner projectNameSpinner = (Spinner)mParent.findViewById(R.id.projectNameSpinner);
			projectNameSpinner.setClickable(false);
			publishProgress(STATUS_INIT);
			MobiseveraCommsUtils scu = new MobiseveraCommsUtils();
			S3CaseContainer S3Cases = S3CaseContainer.getInstance();
			//The next invocation is the one that takes time
	        publishProgress(STATUS_TRANSFERRING);
			S3Cases.setCasesXML(scu.getAllCasesXml(mParent));
	        publishProgress(STATUS_PARSING);
			mParent.projectList = S3Cases.getCases();
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
	
	public void receiveProjectLoadingReadyEvent() {
		Log.d(TAG,"Received project loading ready event on UI thread...");
		Log.d(TAG,"Here, length of loaded projects list: "+projectList.size());
		//append [select] as the first item
		projectList.add(0, S3CaseContainer.getEmptySelectorElement(this));
		projectSpinnerRefreshHack();
		Toast.makeText(this, "Projects are now loaded, you can now make your selection.", Toast.LENGTH_SHORT).show();
		projectsProgress.setVisibility(View.GONE);
	}
	
	/**
	 *  TODO: For some reason, I need to recreate the whole spinner, adapter, listener deal in order to
	 *  get the Spinner to refresh. Please debug & optimize.
	 *  DO NOT CALL FROM ASyncTask threads, will fail!
	 *  These also do have the side-effect that the onItemSelected will get called. Thus, the
	 *  binary switch there on that method.
	 */
	
	private void projectSpinnerRefreshHack() {
		this.projectAdapter.notifyDataSetChanged();
		projectNameSpinner = (Spinner)findViewById(R.id.projectNameSpinner);
        projectAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,projectList);
        projectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        projectNameSpinner.setOnItemSelectedListener(this);
        projectNameSpinner.setAdapter(projectAdapter);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Check if we really have any need for this (except that the interface requirement?)
		
	}
}
