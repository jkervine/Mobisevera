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
import com.digitalfingertip.mobisevera.S3PhaseContainer;
import com.digitalfingertip.mobisevera.S3PhaseItem;

public class MobiseveraSelectPhase extends Activity implements OnItemSelectedListener {

	private static final String TAG = "Sevedroid";
	private static final int NOT_CONNECTED_DIALOG_ID = 1;
	private static final int DIALOG_ID_MISSING_CASEGUID = 2;
	private static boolean selected = false;
	
	/**
	 * The list containing the Cases this user id has access to and the parcel identifier
	 */
	protected ArrayList<S3PhaseItem> phaseList = null;
	protected static final String PHASEITEMLIST_PARCEL_ID = "phaseItemParcelID";
	
	ProgressBar phasesProgress = null;
	
	ArrayAdapter<CharSequence> phaseAdapter = null;
	Spinner phaseNameSpinner = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_phase_layout);
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
        	phasesProgress = (ProgressBar)findViewById(R.id.phasesLoadProgress);
        	phaseNameSpinner = (Spinner)findViewById(R.id.phaseNameSpinner);
        	phaseList = new ArrayList<S3PhaseItem>();
        	phasesProgress.setVisibility(View.VISIBLE);
        	String caseGuid = getIntent().getStringExtra(MobiseveraConstants.GUID_PARAMETER_EXTRA_ID);
        	if(caseGuid == null || caseGuid.length() == 0) {
        		Log.e(TAG,"Error: caseGuid is null or empty when trying to get the phases for this case");
        		throw new IllegalStateException("CaseGuid is requires to get the phases of this case!");
        	}
        	new LoadPhasesXMLTask(this).execute(caseGuid);
        	Toast.makeText(this, "Started to load phases... they will be available once loaded...", Toast.LENGTH_SHORT).show();
        } else {
        	Log.d(TAG, "Saved instance state is not null, restoring Activity state...");
        	phasesProgress = (ProgressBar)findViewById(R.id.phasesLoadProgress);
        	phaseList = savedInstanceState.getParcelableArrayList(PHASEITEMLIST_PARCEL_ID);
        	Log.d(TAG, "Restored phaseList with "+((phaseList == null) ? "null": phaseList.size())+" items");
         	phaseNameSpinner = (Spinner)findViewById(R.id.phaseNameSpinner);
         	phasesProgress.setVisibility(View.GONE);
        }
        phaseAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, phaseList);
        phaseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        phaseNameSpinner.setAdapter(phaseAdapter);
        phaseNameSpinner.setOnItemSelectedListener(this);
        
    }
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		//TODO:Critical: (*) if instance state is saved during the phases are loading, spinners stay empty!!!
		Log.d(TAG,"onSaveInstanceState called!");
		outState.putParcelableArrayList(PHASEITEMLIST_PARCEL_ID, phaseList);
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
		S3PhaseItem phaseItem = phaseList.get(position);
		Log.d(TAG,"User selected case pos: "+position+" with GUID: "+phaseItem.getPhaseGUID());
		Intent backToCallerIntent = new Intent();
		backToCallerIntent.putExtra(MobiseveraConstants.PHASE_PARCEL_EXTRA_ID, phaseItem);
		setResult(MobiseveraNaviContainer.RESULT_CODE_PHASE_BEAN_LOADED, backToCallerIntent);
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
			                MobiseveraSelectPhase.this.finish();
			           }
			       });			
		}
		if(id == DIALOG_ID_MISSING_CASEGUID) {
			builder.setMessage("phase GUID missing in selection!")
			.setCancelable(false)
		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                MobiseveraSelectPhase.this.finish();
		           }
		       });	
		}
		AlertDialog alert = builder.create();
		return alert;	
	}
	/**
	 * AsyncTask for loading the phases XML from S3 SOAP service
	 */
	
	private class LoadPhasesXMLTask extends AsyncTask<String, Integer, Boolean> {
		
		private MobiseveraSelectPhase mParent;
		public static final int STATUS_INIT = 1;
		public static final int STATUS_TRANSFERRING = 2;
		public static final int STATUS_PARSING = 3;
		public static final int STATUS_RETURNING = 4;
		
		public LoadPhasesXMLTask(MobiseveraSelectPhase parent) {
			mParent = parent;
		}
		
		@Override
		protected Boolean doInBackground(String... caseGuid) {
			Log.d(TAG,"Started doInBackground for LoadPhasesXMLTask!");
			if(caseGuid != null && !caseGuid[0].isEmpty()) {
				Log.d(TAG, "Parameters checked OK.");
			} 
			mParent.phaseNameSpinner.setClickable(false);
			publishProgress(STATUS_INIT);
			MobiseveraCommsUtils scu = new MobiseveraCommsUtils();
			S3PhaseContainer S3Phases = S3PhaseContainer.getInstance();
			//The next invocation is the one that takes time
	        publishProgress(STATUS_TRANSFERRING);
			S3Phases.setPhasesXML(scu.getPhasesXMLByCaseGUID(mParent, caseGuid[0]));
	        publishProgress(STATUS_PARSING);
			mParent.phaseList = S3Phases.getPhases();
			publishProgress(STATUS_RETURNING);
			if(mParent.phaseList.isEmpty()) {
				Log.e(TAG,"Phase list is empty! Returning with nothing to tell.");
				return new Boolean(false);
			} else {
				Log.d(TAG,"Project list is not empty, enabling projectspinner...");
				mParent.phaseNameSpinner.setClickable(true);
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
			if(result == null) {
				
			} else {
				mParent.receivePhasesLoadingReadyEvent();
		
			}
		}
	}
	
	public void receivePhasesLoadingReadyEvent() {
		Log.d(TAG,"Received phase loading ready event on UI thread...");
		Log.d(TAG,"Here, length of loaded phases list: "+phaseList.size());
		//append [select] as the first item
		phaseList.add(0, S3PhaseContainer.getEmptySelectorElement(this));
		phaseSpinnerRefreshHack();
		Toast.makeText(this, "phases are now loaded, you can now make your selection.", Toast.LENGTH_SHORT).show();
		phasesProgress.setVisibility(View.GONE);
	}
	
	/**
	 *  TODO: For some reason, I need to recreate the whole spinner, adapter, listener deal in order to
	 *  get the Spinner to refresh. Please debug & optimize.
	 *  DO NOT CALL FROM ASyncTask threads, will fail!
	 *  These also do have the side-effect that the onItemSelected will get called. Thus, the
	 *  binary switch there on that method.
	 */
	
	private void phaseSpinnerRefreshHack() {
		this.phaseAdapter.notifyDataSetChanged();
		phaseNameSpinner = (Spinner)findViewById(R.id.phaseNameSpinner);
        phaseAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,phaseList);
        phaseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        phaseNameSpinner.setOnItemSelectedListener(this);
        phaseNameSpinner.setAdapter(phaseAdapter);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Check if we really have any need for this (except that the interface requirement?)
		
	}
}