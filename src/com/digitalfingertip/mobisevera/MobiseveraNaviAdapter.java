package com.digitalfingertip.mobisevera;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MobiseveraNaviAdapter extends ArrayAdapter<String> {

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
		TextView res = null;
		if(convertView == null) {
			if(context != null) {
				lInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			}
			if(lInflater != null) {
				res = (TextView)lInflater.inflate(R.layout.mobisevera_list_item, parent, false);
			}
		} else {
			res = (TextView)convertView;
		}
		res.setText(strings[position]);
		return res;
	}
	
}
