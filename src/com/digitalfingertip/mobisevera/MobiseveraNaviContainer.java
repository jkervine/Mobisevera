package com.digitalfingertip.mobisevera;

import com.digitalfingertip.mobisevera.activity.MobiseveraClaimActivity;
import com.digitalfingertip.mobisevera.activity.MobiseveraConfig;
import com.digitalfingertip.mobisevera.activity.MobiseveraSelectPhase;
import com.digitalfingertip.mobisevera.activity.MobiseveraSelectProject;
import com.digitalfingertip.mobisevera.activity.MobiseveraSelectWorktype;
import com.digitalfingertip.mobisevera.activity.QueryHourEntries;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * This is a navigation container for one activity's navigation. Provides the data to fill up the arrayadapter which makes up
 * the navigation and provides the Intent which can then be used to launch new activities when navigation items are launched.
 * @author juha
 *
 */

public class MobiseveraNaviContainer {
	
	public static final String TAG = "Sevedroid";
	
	public static final int NAVI_FRONT_ACTIVITY = 1;
	public static final int MAIN_CLAIM_ACTIVITY = 2;
	
	public static String[] getNaviarrayForActivity(Context context, int activityConst) {
		Log.d(TAG,"Getting navi array on behalf of activity assigned with const: "+activityConst);
		String[] res = null;
		switch (activityConst) {
		case NAVI_FRONT_ACTIVITY:
			res = new String [] {
				context.getString(R.string.navi_frontpage_new_claim),
				context.getString(R.string.navi_frontpage_query_claims),
				context.getString(R.string.navi_frontpage_settings)
			};
			break;
		case MAIN_CLAIM_ACTIVITY:
			res = new String[] {
				context.getString(R.string.navi_main_claim_project),
				context.getString(R.string.navi_main_claim_phase),
				context.getString(R.string.navi_main_claim_worktype),
				context.getString(R.string.navi_main_claim_description)
			};
			break;
		}
		return res;
	}
	
	public static Intent getIntentForNaviSelection(Context pkgContext, int activityConst, int position) {
		Log.d(TAG,"Returning intent for navi selection.");
		Intent res = new Intent();
		if(activityConst == NAVI_FRONT_ACTIVITY) {
			switch(position) {
			case 0: 
				res.setClass(pkgContext, MobiseveraClaimActivity.class);
				break;
			case 1:
				res.setClass(pkgContext, QueryHourEntries.class);
				break;
			case 2:
				res.setClass(pkgContext, MobiseveraConfig.class);
			}
		}
		if(activityConst == MAIN_CLAIM_ACTIVITY) {
			switch(position) {
			case 0:
				res.setClass(pkgContext, MobiseveraSelectProject.class);
				break;
			case 1:
				res.setClass(pkgContext, MobiseveraSelectPhase.class);
				break;
			case 2:
				res.setClass(pkgContext, MobiseveraSelectWorktype.class);
				break;
			case 3:
				res.setClass(pkgContext, MobiseveraClaimActivity.class);
			}
		}
		Log.d(TAG,"Routing to activity class:"+res.getClass());
		return res;
	}
	
}
