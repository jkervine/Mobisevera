package com.digitalfingertip.mobisevera;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MobiseveraNaviAdapter extends ArrayAdapter<String> {

	private static final String TAG = "Sevedroid"; 
	private Context context = null;
	private LayoutInflater lInflater = null;
	private String [] strings = null;
	public MobiseveraNaviAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		this.context = context;
	}

	public MobiseveraNaviAdapter(
			Activity parent,
			int listItemResourceId, String[] strings) {
		super(parent, listItemResourceId, strings);
		this.context = parent;
		this.strings = strings;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View res = null;
		if(convertView == null) {
			if(context != null) {
				lInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			}
			if(lInflater != null) {
				res = (View)lInflater.inflate(R.layout.mobisevera_list_item, parent, false);
			}
		} else {
			res = (View)convertView;
		}
		TextView textContent = ((TextView)res.findViewById(R.id.list_item_content));
		Log.d(TAG,"Added string:"+strings[position]+" to position "+position);
		textContent.setText(strings[position]);
		return res;
	}
	
}
