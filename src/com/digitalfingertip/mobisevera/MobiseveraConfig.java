package com.digitalfingertip.mobisevera;

import java.util.List;

import com.digitalfingertip.mobisevera.S3UserContainer.S3UserItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

public class MobiseveraConfig extends Activity implements OnCheckedChangeListener, OnClickListener {

	private static final String TAG = "Sevedroid";
	private static final int NOT_CONNECTED_DIALOG_ID = 1;
	private MobiseveraContentStore mContentStore = null;
	
	//TODO: new feature: should offer a checkbox for the used to enable/disable the loading of inactive work items
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.apikeyinput);
		mContentStore = new MobiseveraContentStore(this);
		CheckBox cb = (CheckBox)findViewById(R.id.apikey_show_password);
		EditText ed = (EditText)findViewById(R.id.apikey_edittext);
		Button submit = (Button)findViewById(R.id.apikey_submit_button);
		
		ed.setText(mContentStore.fetchApiKey());
		cb.setOnCheckedChangeListener(this);
		submit.setOnClickListener(this);
		
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		EditText passwordEd = (EditText)findViewById(R.id.apikey_edittext);
		if(isChecked) {
			Log.d(TAG, "Changing password text to plaintext.");
			passwordEd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
		} else {
			Log.d(TAG, "Hiding password text.");
			passwordEd.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
		}
		
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.apikey_submit_button) {
			if(MobiseveraCommsUtils.checkIfConnected(this) == false) {
				showDialog(NOT_CONNECTED_DIALOG_ID);
				return;
			}
			EditText fName = (EditText)findViewById(R.id.fname_edittext);
			EditText lName = (EditText)findViewById(R.id.lname_edittext);
			if((fName.getText() == null) || (fName.getText().toString().equals(""))) {
				Toast.makeText(this, "You need to supply your first name!", Toast.LENGTH_SHORT).show();
				//should make the field here red
				return;
			}
			if((lName.getText() == null) || (fName.getText().toString().equals(""))) {
				Toast.makeText(this, "You need to supply your last name!", Toast.LENGTH_SHORT).show();
				return;
			}
			EditText passwordEd = (EditText)findViewById(R.id.apikey_edittext);
			String apikey = passwordEd.getText().toString();
			mContentStore.insertApiKey(apikey);
			String [] params = new String[2];
			params[0] = fName.getText().toString();
			params[1] = lName.getText().toString();
			new LoadUserTask(this).execute(params);
		}
	}
	
	public void notifyOnUserLoadedSuccess(S3UserItem userItem) {
		Log.d(TAG,"NotifyOnUserLoaded - is successful:"+(userItem != null));
		if(userItem != null) {
			MobiseveraContentStore scs = new MobiseveraContentStore(this);
		    scs.insertUserFirstName(userItem.getFirstName());
		    scs.insertUserGUID(userItem.getUserGUID());
		    scs.insertUserIsActive(userItem.getIsActive());
			Toast.makeText(this,"Configuration works, saving! Welcome, "+userItem.getFirstName()+"!",Toast.LENGTH_LONG).show();
			this.setResult(Activity.RESULT_OK);
			this.finish();
		} else {
			Toast.makeText(this,"Failed to connect. Please check API Key and name you entered...",Toast.LENGTH_LONG).show();
		}
	}
	
	
	
	@Override
	protected Dialog onCreateDialog(int id) {
		String alertMessage = null;
		switch (id) {
		case NOT_CONNECTED_DIALOG_ID :
			alertMessage = "Configuring Sevedroid requires a working network connection. Please ensure yours is working.";
			break;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(alertMessage)
		.setCancelable(false)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				return;
			}
		});
		AlertDialog alert = builder.create();
		return alert;	
	}



	/**
	 * Async task which takes, as it's input, the user's first and last name and 
	 * returns S3UserItem object based on a API query.
	 * @author juha
	 *
	 */
	
	private class LoadUserTask extends AsyncTask<String, Void, S3UserItem> {

		private Activity mParent = null;
		
		public LoadUserTask(Activity parent) {
			mParent = parent;
		}
		
		@Override
		protected S3UserItem doInBackground(String... params) {
			Log.d(TAG,"Starting doInBackgroud...");
			MobiseveraCommsUtils scu = new MobiseveraCommsUtils();
			Log.d(TAG,"SCU created...");
			String fName = params[0];
			String lName = params[1];
			String result = scu.getUserByName(mParent, fName, lName);
			S3UserContainer userContainer = S3UserContainer.getInstance(result);
			List<S3UserItem> userList = userContainer.getUsers();
			Log.d(TAG,"Result from SCU testApiConnection was:"+result);
			if(userList.size() != 1) {
				Log.e(TAG, "Error, more than one user returned by the query! ("+userList.size()+")");
				return null;
			} else {
				return userList.get(0);
			}
			
		}
		@Override
		protected void onPostExecute(S3UserItem result) {
			Log.d(TAG,"Staring onPostExecute of the ASyncTask...");
			notifyOnUserLoadedSuccess(result);
			Log.d(TAG,"Returning (with void) onPostExecute of the ASyncTask...");
		}
	}
	
}
