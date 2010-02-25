/*
Copyright (c) 2007 Win Myo Htet <wmhtet@aunndroid.com>
          
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.
*/

package com.aunndroid.Tolking;

import com.aunndroid.TetradsDropLite.R;

import com.aunndroid.Engine.Game;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

public class MessageBox {
	private static TextView messageBox;
	private static Game mGame;
	private static final String TAG="MessageBox :";
	private static final int rSET_MESSAGE = 0;
	private static String buffer="";
	private static String handlerMsg;
	private Resources res;
	private static Handler myHandler;
	private static final boolean DEBUG=false;
	private static final boolean VERBOSE =false;
	private static final boolean TEMP=false;

	public MessageBox(TextView tv, Game game){
		messageBox=tv;
		res=((Context)game).getResources();
		if(buffer.equalsIgnoreCase(""))
			buffer=res.getString(R.string.WHAT_IS_A2A);
		messageBox.setText(buffer);
		mGame=game;
		myHandler = new Handler();
	}

	public static void setMessage(String msg){
		if(DEBUG)
			Log.d(TAG,"setMessage : "+msg);
		buffer=buffer+msg+"\n";
		messageBox.setText(buffer+"\n");
		mGame.updateScrollView();
	}

	public void onDestroy(){
		buffer=res.getString(R.string.WHAT_IS_A2A);
	}

	public static void doHandlerPost(int runnable){
		switch (runnable){
		case rSET_MESSAGE:
			myHandler.post(new Runnable(){public void run(){setMessage(handlerMsg);}});
			break;
		default :
			break;
		}
	}

	public static void handlerSetMessage(String string) {
		handlerMsg=string;
		doHandlerPost(rSET_MESSAGE);
	}
}
