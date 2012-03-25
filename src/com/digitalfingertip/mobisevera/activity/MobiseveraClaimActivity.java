package com.digitalfingertip.mobisevera.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TimePicker;

import com.digitalfingertip.mobisevera.MobiseveraConstants;
import com.digitalfingertip.mobisevera.MobiseveraNaviAdapter;
import com.digitalfingertip.mobisevera.MobiseveraNaviContainer;
import com.digitalfingertip.mobisevera.R;
import com.digitalfingertip.mobisevera.S3CaseItem;
import com.digitalfingertip.mobisevera.S3PhaseItem;
import com.digitalfingertip.mobisevera.S3WorkTypeItem;

/**
 * This activity is used to pick the amount of time the user want to claim and launch subactivities to
 * get the project, phase and work type
 * @author juha
 *
 */

public class MobiseveraClaimActivity extends Activity implements OnClickListener, OnItemClickListener {

	/**
	 * Containers for selected project, phase and worktype.
	 */
	
	private S3CaseItem selectedCase = null;
	private S3PhaseItem selectedPhase = null;
	private S3WorkTypeItem selectedWorkType = null;
	
	private String selectedDescription = null;
	
	MobiseveraNaviAdapter listAdapter = null;
	
	public static final String TAG = "Sevedroid";
	private static final int PROJECT_NAVI_INDEX = 0;
	private static final int PHASE_NAVI_INDEX = 1;
	private static final int WORKTYPE_NAVI_INDEX = 2;
	private static final int DESCRIPTION_NAVI_INDEX = 3;
	private static final int DIALOG_ID_SELECT_PROJECT_BEFORE_PHASE = 0;
	private static final int DIALOG_ID_SELECT_PHASE_BEFORE_WORKTYPE = 1;
	
	/**
	 * Current hour as selected by the user for claiming 
	 */
	private int mHour = 0;
	/**
	 * Current minute as selected byt the user for claiming
	 */
	private int mMinute = 0;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG,"OnCreate called on MobiseveraClaimActivity");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_claim);
		TimePicker claimTimePicker = (TimePicker)findViewById(R.id.claimTimePicker);
		Button showHourWidgetButton = (Button)findViewById(R.id.showHourWidgetButton);
		moveTimeFromPickerToButton(mHour, mMinute);
		showHourWidgetButton.setOnClickListener(this);
		claimTimePicker.setIs24HourView(true);
		claimTimePicker.setVisibility(View.GONE);
        updateNaviTitles();		
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.showHourWidgetButton) {
			//If timepicker is invisible, then make it visible, if it is visible, then
			//get the shown date to be displayed in the button and make datepicker go gone.
			Log.d(TAG,"Show time widget button clicked!");
			TimePicker claimTimePicker = (TimePicker)findViewById(R.id.claimTimePicker);
			if(claimTimePicker.getVisibility() == View.GONE) {
				Log.d(TAG,"Moving time from button to picker and making picker visible.");
				moveTimeFromButtonToTimePicker();
				claimTimePicker.setVisibility(View.VISIBLE);
				return;
			} else if(claimTimePicker.getVisibility() == View.VISIBLE);
				Log.d(TAG,"Moving time from picker to button and hiding picker.");
				moveTimeFromPickerToButton(claimTimePicker.getCurrentHour(),claimTimePicker.getCurrentMinute());
				claimTimePicker.setVisibility(View.GONE);
				return;
		}
	}
	
	
	
	@Override
	protected Dialog onCreateDialog(int id) {
		String message = "";
		switch(id) {
		case DIALOG_ID_SELECT_PHASE_BEFORE_WORKTYPE:
			message = getString(R.string.dialog_text_select_phase_before_worktype);
			break;
		case DIALOG_ID_SELECT_PROJECT_BEFORE_PHASE:
			message = getString(R.string.dialog_text_select_project_before_phase);
			break;
		} 
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)
			.setCancelable(false)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					return;
				}
			});
		AlertDialog alert = builder.create();
		return alert;	
	}

	private void moveTimeFromButtonToTimePicker() {
		TimePicker claimTimePicker = (TimePicker)findViewById(R.id.claimTimePicker);
		claimTimePicker.setCurrentHour(mHour);
		claimTimePicker.setCurrentMinute(mMinute);
	}

	/**
	 * Move hour and minutedisplay from datepicker to a button value, while maintaining the
	 * hour and minute values as instance parameters to this activity.
	 * @param currentHour
	 * @param currentMinute
	 */

	private void moveTimeFromPickerToButton(Integer currentHour,
			Integer currentMinute) {
		Button showHourWidgetButton = (Button)this.findViewById(R.id.showHourWidgetButton);
		this.mHour = currentHour;
		this.mMinute = currentMinute;
		String hourLabel = getString(R.string.hour_label);
		String minutesLabel = getString(R.string.minute_label);
		showHourWidgetButton.setText(""+mHour+hourLabel+" "+mMinute+minutesLabel);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Log.d(TAG,"OnItemClick called for MobiseveraClaimActivity. Click on pos: "+position);
		Intent newIntent = MobiseveraNaviContainer.getIntentForNaviSelection(this, 
				MobiseveraNaviContainer.MAIN_CLAIM_ACTIVITY, position);
		int requestCode = MobiseveraNaviContainer.getRequestCodeForNaviSelection(MobiseveraNaviContainer.MAIN_CLAIM_ACTIVITY, 
				position);
		//set appropriate GUID as extra data to intent, because it is needed in the query
		if(position == PROJECT_NAVI_INDEX) {
			//TODO: For consistency, it would be nice that the required USER GUID would be passed in from here.
			newIntent.putExtra(MobiseveraConstants.GUID_PARAMETER_EXTRA_ID, ""); 
		} else if(position == PHASE_NAVI_INDEX) {
			if(selectedCase == null) {
				showDialog(DIALOG_ID_SELECT_PROJECT_BEFORE_PHASE);
				return;
			}
			newIntent.putExtra(MobiseveraConstants.GUID_PARAMETER_EXTRA_ID, selectedCase.getCaseGuid());
		} else if(position == WORKTYPE_NAVI_INDEX) {
			if(selectedPhase == null) {
				showDialog(DIALOG_ID_SELECT_PHASE_BEFORE_WORKTYPE);
				return;
			}
			newIntent.putExtra(MobiseveraConstants.GUID_PARAMETER_EXTRA_ID, selectedPhase.getPhaseGUID());
		}
		Log.d(TAG,"Launching new activity with class: "+newIntent.getClass()+" with request code: "+requestCode);
		startActivityForResult(newIntent,requestCode);
		
	}
	
	/**
	 * Update the navigation with new information when it is received. For example, when user selects a new project,
	 * this changes the "Project [not selected] to Project: [project name].
	 * Data is coming from this class's instance variables
	 */
	
	private void updateNaviTitles() {
		Log.d(TAG,"Updating navititles. ");
		String[] naviTitles = MobiseveraNaviContainer.getNaviarrayForActivity(this, MobiseveraNaviContainer.MAIN_CLAIM_ACTIVITY);
		for(int i = 0; i < naviTitles.length; i++) {
			switch(i) {
			case 0: 
				if(selectedCase != null && selectedCase.getCaseInternalName() != null) {
					naviTitles[i] = naviTitles[i].replace(MobiseveraConstants.SUBST_PATTERN, selectedCase.getCaseInternalName());
				} else {
					naviTitles[i] = naviTitles[i].replace(MobiseveraConstants.SUBST_PATTERN, 
							getString(R.string.not_selected));					
				}
				break;
			case 1:
				if(selectedPhase != null && selectedPhase.getPhaseName() != null) {
					naviTitles[i] = naviTitles[i].replace(MobiseveraConstants.SUBST_PATTERN, selectedPhase.getPhaseName());
				} else {
					naviTitles[i] = naviTitles[i].replace(MobiseveraConstants.SUBST_PATTERN, 
							getString(R.string.not_selected));					
				}
				break;
			case 2: 
				if(selectedWorkType != null && selectedWorkType.getWorkTypeName() != null) {
					naviTitles[i] = naviTitles[i].replace(MobiseveraConstants.SUBST_PATTERN, selectedWorkType.getWorkTypeName());
				} else {
					naviTitles[i] = naviTitles[i].replace(MobiseveraConstants.SUBST_PATTERN, 
							getString(R.string.not_selected));					
				}
				break;
			case 3:
				if(selectedDescription != null) {
					naviTitles[i] = naviTitles[i].replace(MobiseveraConstants.SUBST_PATTERN, truncateDesc(selectedDescription));
				} else {
					naviTitles[i] = naviTitles[i].replace(MobiseveraConstants.SUBST_PATTERN, 
							getString(R.string.not_selected));
				}
			}
		}
		ListView lv = (ListView)this.findViewById(R.id.main_claim_navi_list);
		listAdapter = new MobiseveraNaviAdapter(this,R.layout.mobisevera_list_item,naviTitles);
        lv.setOnItemClickListener(this);
		lv.setAdapter(listAdapter);
	}
	
	/**
	 * Make the given claim description text shorter (17 chars + 3 for ellipsis) so that it can fit in Mobisevera menu
	 * @param selectedDescriptionParam
	 * @return
	 */
	
	private CharSequence truncateDesc(final String selectedDescriptionParam) {
		if(selectedDescriptionParam == null) {
			return null;
		} else {
			try {
				return(selectedDescriptionParam.substring(0,17)+"...");
			} catch (IndexOutOfBoundsException e) {
				return selectedDescriptionParam;
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG,"onActivityResult of MobiseveraClaimActivity called! request: "+requestCode+" result:"+resultCode);
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == MobiseveraNaviContainer.REQUEST_CODE_GET_PHASE) {
			Log.d(TAG,"Get phase:");
			S3PhaseItem phaseItem = (S3PhaseItem)data.getParcelableExtra(MobiseveraConstants.PHASE_PARCEL_EXTRA_ID);
			if(phaseItem != null) {
				this.selectedPhase=phaseItem;
				updateNaviTitles();
			} else {
				Log.e(TAG,"Got null phase bean from the intent.");
				return;
			}
		} else if(requestCode == MobiseveraNaviContainer.REQUEST_CODE_GET_PROJECT) {
			Log.d(TAG,"Get project:");
				S3CaseItem caseItem = (S3CaseItem)data.getParcelableExtra(MobiseveraConstants.CASE_PARCEL_EXTRA_ID);
				if(caseItem != null) {
					this.selectedCase=caseItem;
					updateNaviTitles();
				} else {
					Log.e(TAG,"Got null case bean from the intent.");
					return;
				}
		} else if(requestCode == MobiseveraNaviContainer.REQUEST_CODE_GET_WORKTYPE) {
			Log.d(TAG,"Get worktype:");
			S3WorkTypeItem workTypeItem = (S3WorkTypeItem)data.getParcelableExtra(MobiseveraConstants.WORKTYPE_PARCEL_EXTRA_ID);
			if(workTypeItem != null) {
				this.selectedWorkType=workTypeItem;
				updateNaviTitles();
			} else {
				Log.e(TAG,"Got null worktype bean from the intent.");
				return;
			}
		} else if(requestCode == MobiseveraNaviContainer.REQUEST_CODE_GET_DESCRIPTION) {
			Log.d(TAG,"Get description:");
		} else {
			throw new IllegalStateException("Activity called with unsupported requestcode: "+requestCode);
		}
	}
	

}
