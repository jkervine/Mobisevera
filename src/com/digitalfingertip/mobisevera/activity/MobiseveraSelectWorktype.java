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
import com.digitalfingertip.mobisevera.S3WorkTypeContainer;
import com.digitalfingertip.mobisevera.S3WorkTypeItem;

public class MobiseveraSelectWorktype extends Activity implements OnItemSelectedListener {
	
	private static final String TAG = "Sevedroid";
	private static final int NOT_CONNECTED_DIALOG_ID = 1;
	private static final int DIALOG_ID_MISSING_CASEGUID = 2;
	private static boolean selected = false;
	
	/**
	 * The list containing the Cases this user id has access to and the parcel identifier
	 */
	protected ArrayList<S3WorkTypeItem> workTypeList = null;
	protected static final String WORKTYPEITEMLIST_PARCEL_ID = "workTypeItemParcelID";
	
	ProgressBar workTypesProgress = null;
	
	ArrayAdapter<CharSequence> workTypeAdapter = null;
	Spinner workTypeNameSpinner = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.select_worktype_layout);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.activity_title);
        Log.d(TAG,"OnCreate called on MobiseveraSelectphase Activity!");
        if(MobiseveraCommsUtils.checkIfConnected(this) == false) {
			showDialog(NOT_CONNECTED_DIALOG_ID);
			return;
		}
        MobiseveraContentStore contentStore = new MobiseveraContentStore(this); 
        // test if the user has set the api key, if yes, then follow on to load up the phases
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
        	workTypesProgress = (ProgressBar)findViewById(R.id.workTypesLoadProgress);
        	workTypeNameSpinner = (Spinner)findViewById(R.id.workTypeNameSpinner);
        	workTypeList = new ArrayList<S3WorkTypeItem>();
        	workTypesProgress.setVisibility(View.VISIBLE);
        	String phaseGuid = getIntent().getStringExtra(MobiseveraConstants.GUID_PARAMETER_EXTRA_ID);
        	if(phaseGuid == null || phaseGuid.length() == 0) {
        		Log.e(TAG,"Error: phaseGuid is null or empty when trying to get the worktypes for this phase");
        		throw new IllegalStateException("PhaseGuid is required to get the worktypes of this phase!");
        	}
        	new LoadWorkTypesXMLTask(this).execute(phaseGuid);
        	Toast.makeText(this, "Started to load worktypes... they will be available once loaded...", Toast.LENGTH_SHORT).show();
        } else {
        	Log.d(TAG, "Saved instance state is not null, restoring Activity state...");
        	workTypesProgress = (ProgressBar)findViewById(R.id.workTypesLoadProgress);
        	workTypeList = savedInstanceState.getParcelableArrayList(WORKTYPEITEMLIST_PARCEL_ID);
        	Log.d(TAG, "Restored workTypeList with "+((workTypeList == null) ? "null": workTypeList.size())+" items");
         	workTypeNameSpinner = (Spinner)findViewById(R.id.workTypeNameSpinner);
         	workTypesProgress.setVisibility(View.GONE);
        }
        workTypeAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, workTypeList);
        workTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        workTypeNameSpinner.setAdapter(workTypeAdapter);
        workTypeNameSpinner.setOnItemSelectedListener(this);
        
    }
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		//TODO:Critical: (*) if instance state is saved during the phases are loading, spinners stay empty!!!
		Log.d(TAG,"onSaveInstanceState called!");
		outState.putParcelableArrayList(WORKTYPEITEMLIST_PARCEL_ID, workTypeList);
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
		S3WorkTypeItem workTypeItem = workTypeList.get(position);
		Log.d(TAG,"User selected case pos: "+position+" with GUID: "+workTypeItem.getWorkTypeGUID());
		Intent backToCallerIntent = new Intent();
		backToCallerIntent.putExtra(MobiseveraConstants.WORKTYPE_PARCEL_EXTRA_ID, workTypeItem);
		setResult(MobiseveraNaviContainer.RESULT_CODE_WORKTYPE_BEAN_LOADED, backToCallerIntent);
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
			                MobiseveraSelectWorktype.this.finish();
			           }
			       });			
		}
		if(id == DIALOG_ID_MISSING_CASEGUID) {
			builder.setMessage("phase GUID missing in selection!")
			.setCancelable(false)
		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                MobiseveraSelectWorktype.this.finish();
		           }
		       });	
		}
		AlertDialog alert = builder.create();
		return alert;	
	}
	
	/**
	 * AsyncTask for loading the worktypes XML from S3 SOAP service
	 */
	
	private class LoadWorkTypesXMLTask extends AsyncTask<String, Integer, Boolean> {
		
		private MobiseveraSelectWorktype mParent;
		public static final int STATUS_INIT = 1;
		public static final int STATUS_TRANSFERRING = 2;
		public static final int STATUS_PARSING = 3;
		public static final int STATUS_RETURNING = 4;
		
		public LoadWorkTypesXMLTask(MobiseveraSelectWorktype parent) {
			mParent = parent;
		}
		
		@Override
		protected Boolean doInBackground(String... phaseGuid) {
			Log.d(TAG,"Started doInBackground for LoadPhasesXMLTask!");
			if(phaseGuid != null && phaseGuid[0].isEmpty()) {
				Log.d(TAG,"Paramters checked OK.");
			} 
			mParent.workTypeNameSpinner.setClickable(false);
			publishProgress(STATUS_INIT);
			MobiseveraCommsUtils scu = new MobiseveraCommsUtils();
			S3WorkTypeContainer S3WorkTypes = S3WorkTypeContainer.getInstance();
			//The next invocation is the one that takes time
	        publishProgress(STATUS_TRANSFERRING);
			S3WorkTypes.setWorkTypesXML(scu.getWorkTypesXMLByPhaseGUID(mParent, phaseGuid[0]));
	        publishProgress(STATUS_PARSING);
			mParent.workTypeList = S3WorkTypes.getWorkTypes();
			publishProgress(STATUS_RETURNING);
			if(mParent.workTypeList.isEmpty()) {
				Log.e(TAG,"Worktype list is empty! Returning with nothing to tell.");
				return new Boolean(false);
			} else {
				Log.d(TAG,"Work type list is not empty, enabling worktypespinner...");
				mParent.workTypeNameSpinner.setClickable(true);
				return new Boolean(true);
			}
		}
		
		protected void onProgressUpdate(Integer... progress) {
			Log.d(TAG,"Setting progress to: "+progress[0].toString());
	         setProgress(progress[0]);
	     }
		
		@Override
		protected void onPostExecute(Boolean result) {
			Log.d(TAG,"onPostExecute on LoadWorkTypesXMLTask firing.");
			mParent.receiveWorkTypesLoadingReadyEvent();
		}
	}
	
	
	public void receiveWorkTypesLoadingReadyEvent() {
		Log.d(TAG,"Received phase loading ready event on UI thread...");
		Log.d(TAG,"Here, length of loaded phases list: "+workTypeList.size());
		//append [select] as the first item
		workTypeList.add(0, S3WorkTypeContainer.getEmptySelectorElement(this));
		workTypeSpinnerRefreshHack();
		Toast.makeText(this, "phases are now loaded, you can now make your selection.", Toast.LENGTH_SHORT).show();
		workTypesProgress.setVisibility(View.GONE);
	}
	
	/**
	 *  TODO: For some reason, I need to recreate the whole spinner, adapter, listener deal in order to
	 *  get the Spinner to refresh. Please debug & optimize.
	 *  DO NOT CALL FROM ASyncTask threads, will fail!
	 *  These also do have the side-effect that the onItemSelected will get called. Thus, the
	 *  binary switch there on that method.
	 */
	
	private void workTypeSpinnerRefreshHack() {
		this.workTypeAdapter.notifyDataSetChanged();
		workTypeNameSpinner = (Spinner)findViewById(R.id.workTypeNameSpinner);
        workTypeAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,workTypeList);
        workTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        workTypeNameSpinner.setOnItemSelectedListener(this);
        workTypeNameSpinner.setAdapter(workTypeAdapter);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Check if we really have any need for this (except that the interface requirement?)
		
	}
}
