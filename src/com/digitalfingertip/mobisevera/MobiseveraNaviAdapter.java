package com.digitalfingertip.mobisevera;

import com.digitalfingertip.mobisevera.activity.MobiseveraClaimActivity;
import com.digitalfingertip.mobisevera.activity.MobiseveraFrontpageActivity;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MobiseveraNaviAdapter extends ArrayAdapter<String> {

	private static final String TAG = "Sevedroid"; 
	private Activity context = null;
	private LayoutInflater lInflater = null;
	private float currentRotation = 0;
	private String [] strings = null;
	private int mSelectedEventID = 0;
	public MobiseveraNaviAdapter(Activity context, int textViewResourceId) {
		super(context, textViewResourceId);
		this.context = context;
	}

	public MobiseveraNaviAdapter(
			Activity parent,
			int listItemResourceId, 
			String[] strings,
			int ... eventId
			) {
		super(parent, listItemResourceId, strings);
		//this.mSelectedPosition;
		this.context = parent;
		this.strings = strings;
		if(eventId != null && eventId.length != 0) {
			mSelectedEventID = eventId[0];
		}
	}
	
	public void turn(ImageView turnImg)
	{
	    RotateAnimation anim = new RotateAnimation(currentRotation, currentRotation + 30,
	            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,0.5f);
	    currentRotation = (currentRotation + 90) % 360;

	    anim.setInterpolator(new LinearInterpolator());
	    anim.setDuration(1000);
	    anim.setFillEnabled(true);

	    anim.setFillAfter(true);
	    turnImg.startAnimation(anim);
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
		ImageView rightIconView = (ImageView)res.findViewById(R.id.list_item_icon_right);
		//This tests whether the class using the adapter wants to show some item as "expanded". For now
		//only means that it has a downward arrow instead of the right-pointing arrow.
		//we now a bit awkwardly need to list all of the combinations here, but TODO: find a better way
		if((context instanceof MobiseveraFrontpageActivity && 
				MobiseveraNaviContainer.isInExpandedState(MobiseveraNaviContainer.NAVI_FRONT_ACTIVITY, position)) || 
		(context instanceof MobiseveraClaimActivity &&
				MobiseveraNaviContainer.isInExpandedState(MobiseveraNaviContainer.MAIN_CLAIM_ACTIVITY, position)))
		{
			Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.arrow_down);
			rightIconView.setImageBitmap(bm);
		} else {
			Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.arrow_right);
			rightIconView.setImageBitmap(bm);			
		}
		TextView textContent = ((TextView)res.findViewById(R.id.list_item_content));
		Log.d(TAG,"Added string:"+strings[position]+" to position "+position);
		textContent.setText(strings[position]);
		return res;
	}
	
}
