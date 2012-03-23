package com.digitalfingertip.mobisevera.activity;

import com.digitalfingertip.mobisevera.MobiseveraNaviAdapter;
import com.digitalfingertip.mobisevera.MobiseveraNaviContainer;
import com.digitalfingertip.mobisevera.R;
import com.digitalfingertip.mobisevera.R.id;
import com.digitalfingertip.mobisevera.R.layout;
import com.digitalfingertip.mobisevera.R.string;

import android.app.Activity;
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

/**
 * This activity is used to pick the amount of time the user want to claim and launch subactivities to
 * get the project, phase and work type
 * @author juha
 *
 */

public class MobiseveraClaimActivity extends Activity implements OnClickListener, OnItemClickListener {

	public static final String TAG = "Sevedroid";
	
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
		ListView lv = (ListView)findViewById(R.id.main_claim_navi_list);
		moveTimeFromPickerToButton(mHour, mMinute);
		showHourWidgetButton.setOnClickListener(this);
		claimTimePicker.setIs24HourView(true);
		claimTimePicker.setVisibility(View.GONE);
		String[] naviTitles = MobiseveraNaviContainer.getNaviarrayForActivity(this, MobiseveraNaviContainer.MAIN_CLAIM_ACTIVITY);
        MobiseveraNaviAdapter listAdapter = new MobiseveraNaviAdapter(this,R.layout.mobisevera_list_item,naviTitles);
        lv.setOnItemClickListener(this);
		lv.setAdapter(listAdapter);
		
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
		Log.d(TAG,"Launching new activity with class: "+newIntent.getClass()+" with request code: "+requestCode);
		startActivityForResult(newIntent,requestCode);
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG,"onActivity of MobiseveraClaimActivity called! request: "+requestCode+" result:"+resultCode);
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == MobiseveraNaviContainer.REQUEST_CODE_GET_PHASE) {
			
		} else if(requestCode == MobiseveraNaviContainer.REQUEST_CODE_GET_PROJECT) {
			
		} else if(requestCode == MobiseveraNaviContainer.REQUEST_CODE_GET_WORKTYPE) {
			
		} else if(requestCode == MobiseveraNaviContainer.REQUEST_CODE_GET_DESCRIPTION) {
			
		} else {
			throw new IllegalStateException("Activity called with unsupported requestcode: "+requestCode);
		}
	}
	

}
