/*
Copyright (c) 2007 Win Myo Htet <wmhtet@aunndroid.com>
          
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.
*/

package com.aunndroid.Tolking;

import com.aunndroid.TetradsDropLite.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.util.Log;
import android.widget.EditText;

public class Chat extends Dialog implements OnClickListener, ConstantInterface{

	private EditText tolkEditText;
	private Button tolkButton;
	private EditText toEditText;
	private static Tolk tolk;
	private static Resources res;
	private static int LITE_LIMIT=999999999;
//	private int LITE_LIMIT=3;
	private static int mLite=0;
	private String sessionID;
	private static String to;
	private static String me;
	private static final String TAG="Chat";
	private static final boolean DEBUG=false;
	private static final boolean VERBOSE =false;
	private static final boolean TEMP=false;


	public Chat(Context context,Tolk tolk ) {
		super(context);
		res=context.getResources();
    	setContentView(R.layout.chat);
    	Chat.tolk=tolk;
		this.setTitle(R.string.LETS_TOLK);
		me=res.getString(R.string.ME);
		toEditText=(EditText)this.findViewById(R.id.to_edit_text);
		tolkEditText=(EditText)this.findViewById(R.id.tolk_edit_text);
		tolkButton=(Button)this.findViewById(R.id.tolk_chat_button);
		tolkButton.setOnClickListener(this);
		this.setCancelable(true);
	}

	@Override
	public void onClick(View arg0) {
		String chat=" "+tolkEditText.getText().toString();
		to=toEditText.getText().toString();
		sendChat(chat,sessionID,to,false);
		tolkEditText.setText("");
		dismiss();
	}

	protected void onStart() {
		super.onStart();
		sessionID=tolk.getSessionID();
		toEditText.setText(tolk.getFrom());
	}

	public static void sendChat(String chat, String sessionID, String friendID,boolean bypass) {
		if(DEBUG)
			Log.d(TAG,"sendChat");
		if(TEMP)
			Log.d(TAG,"sendTolkToTolk :"+sessionID+TOLKING_CODE+chat);
		if(bypass){
			tolk.xmppAgentSendMsg(sessionID+TOLKING_CODE+chat,tolk.getFqFriendID());
			if(tolk.isUseMessageBox())
				MessageBox.setMessage(me+" :"+chat);
			return;
		}
		if(mLite<LITE_LIMIT){
			if(friendID.equalsIgnoreCase(tolk.getFriendID())){
				tolk.xmppAgentSendMsg(sessionID+TOLKING_CODE+chat,tolk.getFqFriendID());
				if(tolk.isUseMessageBox())
					MessageBox.setMessage(me+" :"+chat);
			}else{
				if(tolk.isUseMessageBox())
					MessageBox.setMessage(me+"->"+to+" :"+chat);
				to+="@gmail.com";
				tolk.xmppAgentSendMsg(chat,to);
			}
		}
		else{
			if(friendID.equalsIgnoreCase(tolk.getFriendID()))
				tolk.xmppAgentSendMsg(sessionID+TOLKING_CODE+res.getString(R.string.TOLKING_CHEAP), tolk.getFqFriendID());
			else{
				to+="@gmail.com";
				tolk.xmppAgentSendMsg(TOLKING_CODE+res.getString(R.string.TOLKING_CHEAP), to);
			}
			tolk.callToast(res.getString(R.string.CHEAP_TOLKER));
		}
		mLite++;
	}

	public static int getmLite() {
		return mLite;
	}

	public static void setmLite(int mLite) {
		Chat.mLite = mLite;
	}

}
