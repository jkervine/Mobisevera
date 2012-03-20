package com.digitalfingertip.mobisevera.activity;

import com.digitalfingertip.mobisevera.MobiseveraNaviAdapter;
import com.digitalfingertip.mobisevera.MobiseveraNaviContainer;
import com.digitalfingertip.mobisevera.R;
import com.digitalfingertip.mobisevera.R.layout;
import com.digitalfingertip.mobisevera.R.menu;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class MobiseveraFrontpageActivity extends ListActivity implements OnItemClickListener {

	
	public static final String TAG = "Sevedroid";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.frontpage);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.activity_title);
        String[] naviTitles = MobiseveraNaviContainer.getNaviarrayForActivity(this, MobiseveraNaviContainer.NAVI_FRONT_ACTIVITY);
        MobiseveraNaviAdapter listAdapter = new MobiseveraNaviAdapter(this,R.layout.mobisevera_list_item,naviTitles);
        this.getListView().setOnItemClickListener(this);
		this.setListAdapter(listAdapter);
		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return super.onContextItemSelected(item);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Log.d(TAG,"Item clicked in position "+position);
		Intent newIntent = MobiseveraNaviContainer.getIntentForNaviSelection(this, 
				MobiseveraNaviContainer.NAVI_FRONT_ACTIVITY, position);
		startActivity(newIntent);
	}
	
	

	
	
}
