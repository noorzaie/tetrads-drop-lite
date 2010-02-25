/*
Copyright (c) 2007 Win Myo Htet <wmhtet@aunndroid.com>
          
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.
*/

package com.aunndroid.Engine;

import com.aunndroid.View.TdView;
import com.aunndroid.View.SView;
import com.aunndroid.TetradsDropLite.R;
import com.aunndroid.TetradsDropLite.TetradsDrop;
import com.aunndroid.Tolking.Chat;
import com.aunndroid.Tolking.ConstantInterface;
import com.aunndroid.Tolking.MessageBox;
import com.aunndroid.Tolking.Tolk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.admob.android.ads.*;

public class Game extends Activity implements OnClickListener, ConstantInterface{

	private TdView mTdView;
	private SView mSView;
	private Button a2aButton;
	private Button tolkButton;
	private Button tolkButton1;
	private EditText tolkEditText;
	private ToggleButton singleToggleButton;
	private TextView numTextView;
	private BlockOps mBlockOps;
	private Handler myHandler;
	private int dropTime=DROP_TIME;
	private int lineClear=0;
	private int gameLevel=0;
	private int gameScore=0;
	private boolean mRunning=false;
	private boolean mGameOver=true;
	private int block;
	private int blocked;
	private Tolk tolk;
	private int gamePhase;
	private Resources res;
	private Toast myToast;
	private ProgressDialog pd;
	private boolean mCompete;
	private boolean mCollaborate;
	private boolean tolkGame=false;
	private boolean singleGame=false;
	private boolean searchAunnDroid=true;
	private AlertDialog myAlertDialog;
	private int mCounter=3;
	private int clickCount=0;
	private int counterTime;
	private int line;
	private boolean liteGame=true;
	private static int LITE_REGION = -1;
	private boolean mUseMessageBox=false;
	private ScrollView mScrollView;
	private TextView mbTextView;
	private int screenDimension=-1;

	private static final int DROP_TIME=500; //2500 500
	private static final int STARTER_TIME = 2500;
	private static final int FOLLOWER_TIME = 2530;
	private static final String KEY_DROP_TIME="dropTime";
	private static final String KEY_LINE_CLEAR="lineClear";
	private static final String KEY_GAME_LEVEL="gameLevel";
	private static final String KEY_GAME_SCORE="gameScore";
	private static final String KEY_GAME_OVER="mGameOver";
	private static final String KEY_SINGLE_GAME = "singleGame";
	private static final String KEY_BLOCK="block";
	private static final String KEY_BLOCKED="blocked";
	private static final String KEY_SCREEN_DIMENSION="screenDimension";

	private static final String KEY_BLOCK_NUM="blocknum";
	private static final String KEY_BLOCK_ORIENT="blockorient";
	private static final String KEY_NEXT_BLOCK_NUM="nextBlockNum";
	private static final String KEY_NEXT_BLOCK_ORIENT="nextBlockOrient";
	private static final String KEY_BLOCK_X="blockx";
	private static final String KEY_BLOCK_Y="blocky";
	private static final String KEY_FIELD_POSITION="fieldPosition";

	private static final String FORCE_ERROR="---###FORCING_ERROR###---";
	public static final int FOLLOWER = 1;
	public static final int STARTER = 0;
	private static final String TAG = "TetradsDropLite : Game : ";

	private static final int rSearchAunnDroid=8;
	private static final int rLiteVersion = 7;
	private static final int rSendDeletLine = 6;
	private static final int rCollabGameOver = 5;
	private static final int rDisplayNum=4;
	private static final int rDoGameOver=3;
	private static final int rSideDoDraw=2;
	private static final int rDoDraw=1;
	private static final int rDoDrawRect=0;
	public static final int INITIALIZATION=0;
	public static final int GAME_PLAY=1;
	public static final int CLOSING = 2;
	private static final boolean DEBUG=false;
	private static final boolean VERBOSE=false;
	private static final boolean TEMP=false;
	private static final boolean ADTEST=false;
	
	private AdView mAd;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		if(DEBUG)
			Log.d(TAG, "onCreate");
		if(screenDimension==-1){
			this.screenDimension=TetradsDrop.getScreenDimension();
			if(this.screenDimension==-1)
				calculateScreenDimension();
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.tdlayout);
		mBlockOps=BlockOps.getInstanceOf();
		myHandler = new Handler();
		res=this.getResources();
		this.tolk=new Tolk(this,res.getString(R.string.app_name));
		
		mAd=(AdView) this.findViewById(R.id.ad);
//		mAd.setVisibility(View.VISIBLE);
		AdManager.setInTestMode(ADTEST);
		
		mTdView=(TdView) this.findViewById(R.id.tDview);
		pd=new ProgressDialog(this);
		mSView=(SView)this.findViewById(R.id.sView);
		mSView.setTdView(mTdView);
		numTextView=(TextView)this.findViewById(R.id.number);
		this.a2aButton=(Button)this.findViewById(R.id.a2a_button);
		this.a2aButton.setOnClickListener(this);
		this.tolkButton1=(Button)this.findViewById(R.id.tolk_button1);
		this.tolkButton1.setOnClickListener(this);
		this.singleToggleButton=(ToggleButton)this.findViewById(R.id.single_toggle_button);
		this.singleToggleButton.setOnClickListener(this);
		
		if(screenDimension!=QVGA){
			tolkEditText=(EditText)this.findViewById(R.id.tolk_edit_text);
			tolkButton=(Button)this.findViewById(R.id.tolk_button);
			tolkButton.setOnClickListener(this);
			int screenOrientation=res.getConfiguration().orientation;
			if(screenDimension==HVGA&&screenOrientation==Configuration.ORIENTATION_PORTRAIT){
				mUseMessageBox=false;
			}else {
				mUseMessageBox=true;
				tolk.setUseMessageBox(mUseMessageBox);
				this.mbTextView=(TextView)this.findViewById(R.id.message_box);
				new MessageBox(mbTextView,this);
				mScrollView=(ScrollView)this.findViewById(R.id.scroll_view);
			}
		}else if(screenDimension==QVGA){
			mUseMessageBox=false;
		}		
		mTdView.requestFocus();
	}
	
	private void calculateScreenDimension() {		
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
	if(TEMP)  Log.d(TAG,"onCreate Height :"+metrics.heightPixels+" Width"+metrics.widthPixels);
		int min=Math.min(metrics.heightPixels, metrics.widthPixels);
		int max=Math.max(metrics.heightPixels, metrics.widthPixels);
		if(min==240&&max==320)
			setScreenDimension(QVGA);
		else if(min==320&&max==480)
			setScreenDimension(HVGA);
		else if(min==480&&max==800)
			setScreenDimension(WVGA800);
		else if(min==480&&max==854)
			setScreenDimension(WVGA854);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if(DEBUG)
			Log.d(TAG, "onConfigurationChanged");
		setContentView(R.layout.tdlayout);
		mBlockOps=BlockOps.getInstanceOf();
		mTdView=(TdView) this.findViewById(R.id.tDview);
		pd=new ProgressDialog(this);
		mSView=(SView)this.findViewById(R.id.sView);

		numTextView=(TextView)this.findViewById(R.id.number);
		this.a2aButton=(Button)this.findViewById(R.id.a2a_button);
		this.a2aButton.setOnClickListener(this);
		this.tolkButton1=(Button)this.findViewById(R.id.tolk_button1);
		this.tolkButton1.setOnClickListener(this);
		this.singleToggleButton=(ToggleButton)this.findViewById(R.id.single_toggle_button);
		this.singleToggleButton.setOnClickListener(this);
		this.singleToggleButton.setChecked(mRunning);
		if(screenDimension!=QVGA){
			tolkEditText=(EditText)this.findViewById(R.id.tolk_edit_text);
			tolkEditText.clearFocus();
			tolkButton=(Button)this.findViewById(R.id.tolk_button);
			tolkButton.setOnClickListener(this);
			int screenOrientation=res.getConfiguration().orientation;
			if(screenDimension==HVGA&&screenOrientation==Configuration.ORIENTATION_PORTRAIT){
				mUseMessageBox=false;
			}else {
				mUseMessageBox=true;
				tolk.setUseMessageBox(mUseMessageBox);
				this.mbTextView=(TextView)this.findViewById(R.id.message_box);
				new MessageBox(mbTextView,this);
				mScrollView=(ScrollView)this.findViewById(R.id.scroll_view);
			}
		}else if(screenDimension==QVGA){
			mUseMessageBox=false;
		}
		if(tolk.isConnected()){
			RelativeLayout rlEditText=(RelativeLayout) this.findViewById(R.id.tolk_layout);
			rlEditText.setVisibility(View.VISIBLE);			
		}
		mTdView.requestFocus();
	}

	@Override
	public void onClick(View v) {
		if(DEBUG)
			Log.d(TAG,"onClick");
		switch(v.getId()){
		case R.id.a2a_button:
			tolkGameResetSetting();
			RelativeLayout rlEditText=(RelativeLayout) this.findViewById(R.id.tolk_layout);
			rlEditText.setVisibility(View.VISIBLE);
			this.tolkEditText.setVisibility(View.VISIBLE);
			this.tolk.a2a();
			mTdView.requestFocus();
			break;
		case R.id.single_toggle_button:
			singlePlay();
			break;
		case R.id.tolk_button1:
			if(!this.tolk.isConnected() || this.tolk.getFqFriendID().equalsIgnoreCase("")){
				tolk.connectFirstDialogCall();
			}else
				tolk.chatShow();
			this.searchAunnDroid=true;
			mTdView.requestFocus();
			break;
		case R.id.tolk_button:
			if(!this.tolk.isConnected() || this.tolk.getFriendID().equalsIgnoreCase("")){
				tolk.connectFirstDialogCall();
			}else{
				String chat=" "+tolkEditText.getText().toString();
				Chat.sendChat(chat,tolk.getSessionID(),tolk.getFriendID(),false);
				tolkEditText.setText("");			
				}
			this.searchAunnDroid=true;
			mTdView.requestFocus();
			break;		
		default :
			break;
		}
	}

	protected void onDestroy(){
		if(DEBUG)
			Log.d(TAG,"onDestroy");
//        mSensorManager.unregisterListener(mTdView);
		tolk.onDestroy();super.onDestroy();
	}

/*    @Override
    protected void onStop() {
        mSensorManager.unregisterListener(mTdView);
        super.onStop();
    }
*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		if(DEBUG)
			Log.d(TAG,"onCreateOptionsMenu ");
		MenuInflater inflater=getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	public void updateScrollView(){
		if(DEBUG)
			Log.d(TAG,"updateScrollView");
//		int y=mScrollView.getScrollY();
//		int max=mScrollView.getMaxScrollAmount();

//	if(TEMP)Log.d(TAG,"getScrollY :"+y);
//	if(TEMP)Log.d(TAG,"getMaxScrollAmount :"+max);
		mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

		mTdView.bringToFront();
		mTdView.requestFocus();
	}

	public void chatShow(){
		if(DEBUG)
			Log.d(TAG,"chatShow");
		tolk.chatShow();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if(DEBUG)
			Log.d(TAG,"onOptionsItemSelected ");
		switch(item.getItemId()){
		case R.id.tolk_menu_button:
			if(!this.tolk.isConnected()){
				tolk.connectFirstDialogCall();
			}else
				tolk.chatShow();
			return true;
		case R.id.a2a_button:
			this.tolk.a2a();
			return true;
		case R.id.setting_button:
//			gInit.show();
			settingDialog();
			return true;
		}
		return false;
	}

	public void doHandlerPost(int r){
		if(VERBOSE)
			Log.d(TAG,"doHandlerPost : "+r);
		switch(r){
		case rDoDrawRect:
			myHandler.post(new Runnable (){public void run(){	mTdView.setRectForInvalidate(0,1);	}});
			break;
		case rDoDraw:
			myHandler.post(new Runnable (){public void run(){	mTdView.invalidate();	}});
			break;
		case rSideDoDraw:
			myHandler.post(new Runnable (){public void run(){	mSView.invalidateBlockArea();	}});
			break;
		case rDisplayNum:
			myHandler.post(new Runnable(){public void run(){	startCompeteGame();}});
			break;
		case rDoGameOver:
			myHandler.post(new Runnable (){
				public void run(){	
					singleToggleButton.setChecked(false);	
					mRunning=false;
					singleGameOverDialog();
					}
				});
			break;
		case rCollabGameOver:
			myHandler.post(new Runnable (){public void run(){	endOfCollaborateGame();	}});
			break;
		case rSendDeletLine:
			myHandler.post(new Runnable (){public void run(){	sendDeleteLineToTolk(line);	}});
			break;
		case rLiteVersion:
			myHandler.post(new Runnable (){public void run(){	LiteVersionDialog();	}});
			break;
		case rSearchAunnDroid: myHandler.post(new Runnable (){
			public void run(){	
				MessageBox.setMessage(res.getString(R.string.SEARCH_AUNNDROID));	
				mTdView.bringToFront();
				mTdView.requestFocus();}
			});	
			break;
		default:
			break;
		}
	}

	@Override
	protected void onPause(){
		super.onPause();
		if(DEBUG)
			Log.d(TAG, "onPause");
		/**
		 * If the user navigate away from the game, we just want to kill the game so that the other person is not stranded.
		 * We will close the connection too so that the system won't complaining of the using too much resources
		 * because of the stranded thread keeping the connection alive. We want to save the mGameOver information true,
		 * so that when the user come back, the user can start the single game.
		 * */

		/**
		 * Don't know if I am overdoing by having above if condition (No, the other user need to be inform)
		 * and checking about the same thing here again
		 * */
		//mGameOver=false, singleGame=true locked
		if(!mGameOver&&singleGame){
			mRunning=false;
			getPreferences(MODE_PRIVATE).edit().putInt(KEY_DROP_TIME,dropTime).commit();
			getPreferences(MODE_PRIVATE).edit().putInt(KEY_LINE_CLEAR,lineClear).commit();
			getPreferences(MODE_PRIVATE).edit().putInt(KEY_GAME_LEVEL,gameLevel).commit();
			getPreferences(MODE_PRIVATE).edit().putInt(KEY_GAME_SCORE,gameScore).commit();
			getPreferences(MODE_PRIVATE).edit().putBoolean(KEY_GAME_OVER,mGameOver).commit();
			getPreferences(MODE_PRIVATE).edit().putBoolean(KEY_SINGLE_GAME,singleGame).commit();
			getPreferences(MODE_PRIVATE).edit().putInt(KEY_SCREEN_DIMENSION,screenDimension).commit();
			getPreferences(MODE_PRIVATE).edit().putInt(KEY_BLOCK,block).commit();
			getPreferences(MODE_PRIVATE).edit().putInt(KEY_BLOCKED,blocked).commit();

			getPreferences(MODE_PRIVATE).edit().putInt(KEY_BLOCK_NUM,BlockOps.getBlocknum()).commit();
			getPreferences(MODE_PRIVATE).edit().putInt(KEY_BLOCK_ORIENT,BlockOps.getBlockorient()).commit();
			getPreferences(MODE_PRIVATE).edit().putInt(KEY_NEXT_BLOCK_NUM,BlockOps.getNextBlockNum()).commit();
			getPreferences(MODE_PRIVATE).edit().putInt(KEY_NEXT_BLOCK_ORIENT,BlockOps.getNextBlockOrient()).commit();
			getPreferences(MODE_PRIVATE).edit().putInt(KEY_BLOCK_X,BlockOps.getBlockx()).commit();
			getPreferences(MODE_PRIVATE).edit().putInt(KEY_BLOCK_Y,BlockOps.getBlocky()).commit();
			getPreferences(MODE_PRIVATE).edit().putString(KEY_FIELD_POSITION,mBlockOps.getFieldPosition()).commit();
		}else{
		//	mGameOver=true;
			//in case
			getPreferences(MODE_PRIVATE).edit().putBoolean(KEY_GAME_OVER,mGameOver).commit();
			getPreferences(MODE_PRIVATE).edit().putBoolean(KEY_SINGLE_GAME,singleGame).commit();
			getPreferences(MODE_PRIVATE).edit().putInt(KEY_SCREEN_DIMENSION,screenDimension).commit();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState){
		super.onSaveInstanceState(savedInstanceState);
		if(DEBUG)
			Log.d(TAG,"onSaveInstanceState");
	}

	@Override
	protected void onResume(){
		super.onResume();
		if(DEBUG)
			Log.d(TAG,"onResume ");
		screenDimension=getPreferences(MODE_PRIVATE).getInt(KEY_SCREEN_DIMENSION,screenDimension);
		mGameOver=getPreferences(MODE_PRIVATE).getBoolean(KEY_GAME_OVER,mGameOver);
		singleGame=getPreferences(MODE_PRIVATE).getBoolean(KEY_SINGLE_GAME,singleGame);
		if(!mGameOver&&singleGame){
			mRunning=false;
			this.singleToggleButton.setChecked(mRunning);
			dropTime=getPreferences(MODE_PRIVATE).getInt(KEY_DROP_TIME,dropTime);
			lineClear=getPreferences(MODE_PRIVATE).getInt(KEY_LINE_CLEAR,lineClear);
			gameLevel=getPreferences(MODE_PRIVATE).getInt(KEY_GAME_LEVEL,gameLevel);
			gameScore=getPreferences(MODE_PRIVATE).getInt(KEY_GAME_SCORE,gameScore);
			block=getPreferences(MODE_PRIVATE).getInt(KEY_BLOCK,block);
			blocked=getPreferences(MODE_PRIVATE).getInt(KEY_BLOCKED,blocked);
			if(mBlockOps==null){
				mBlockOps=BlockOps.getInstanceOf();
				BlockOps.setBlocknum(getPreferences(MODE_PRIVATE).getInt(KEY_BLOCK_NUM,BlockOps.getBlocknum()));
				BlockOps.setBlockorient(getPreferences(MODE_PRIVATE).getInt(KEY_BLOCK_ORIENT,BlockOps.getBlockorient()));
				BlockOps.setNextBlockNum(getPreferences(MODE_PRIVATE).getInt(KEY_NEXT_BLOCK_NUM,BlockOps.getNextBlockNum()));
				BlockOps.setNextBlockOrient(getPreferences(MODE_PRIVATE).getInt(KEY_NEXT_BLOCK_ORIENT,BlockOps.getNextBlockOrient()));
				BlockOps.setBlockx(getPreferences(MODE_PRIVATE).getInt(KEY_BLOCK_X,BlockOps.getBlockx()));
				BlockOps.setBlocky(getPreferences(MODE_PRIVATE).getInt(KEY_BLOCK_Y,BlockOps.getBlocky()));
				mBlockOps.updateFieldPosition(getPreferences(MODE_PRIVATE).getString(KEY_FIELD_POSITION,mBlockOps.getFieldPosition()));
				this.mTdView.setmBlockOps(mBlockOps);
			}
		}
 /*       boolean supported=mSensorManager.registerListener(mTdView,
        		mSensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER ),
                SensorManager.SENSOR_DELAY_UI);
        if (!supported) {
            mSensorManager.unregisterListener(mTdView);
            throw new UnsupportedOperationException("Accelerometer not supported");
          }
*/
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if(DEBUG)
			Log.d(TAG,"onRestoreInstanceState");
	}

	public void tolkOnDestroy() {
	}

	public void fileTransferFinished(boolean success) {
	}

	public void startA2A(int starter) {
		if(DEBUG)
			Log.d(TAG,"startA2A");
		if(starter==STARTER){
			new AlertDialog.Builder(this).setTitle(R.string.FRIEND_OR_FOE_TITLE)
			.setMessage(R.string.FRIEND_OR_FOE_MSG)
	/*		.setPositiveButton(R.string.COLLABORATE,new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int Button) {
					progressDialog(res.getString(R.string.WAIT_FOR_RESPONSE));
					tolk.sendMsgToTolk(""+COLLABORATE_CODE);
					mCompete=false;
					mCollaborate=true;
				}})
	*/		.setNegativeButton(R.string.COMPETE,new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int Button) {
					progressDialog(res.getString(R.string.WAIT_FOR_RESPONSE));
					tolk.sendMsgToTolk(""+COMPETE_CODE);
					mCompete=true;
					mCollaborate=false;
				}})
			.show();
		}else{
			progressDialog(res.getString(R.string.WAIT_FOR_GAME_OPTION));
		}
	}

	/**
	 This one need to be called from handler so that it can call connectFirst Dialog when
	 * */
	public void sendDeleteLineToTolk(int line) {
		if(DEBUG)
			Log.d(TAG,"sendSelectToTolk");
		if(line>1){
			if(line==4)line++;
			this.tolk.sendMsgToTolk(""+DELETE_LINE_CODE+line);
		}
	}

	protected void progressDialog(String msg){
		pd.setMessage(msg);
		pd.show();
	}

	protected void progressDialogDismiss(){
		if(DEBUG)
			Log.d(TAG,"progressDialogDismiss()");
		if(pd.isShowing())
			pd.dismiss();
	}

	/**
	 * When we receive the msg, we will be sorting them out according to the gamePhase
	 * */
	public void receiveMsgFromTolk(String msgStr){
		if(DEBUG)
			Log.d(TAG,"receiveMsgFromTolk "+msgStr);

		switch(gamePhase){
		case INITIALIZATION:
			handleInitialization(msgStr);
			break;
		case GAME_PLAY:
			handleGamePlay(msgStr);
			break;
		case CLOSING:
			handleClosing(msgStr);
			break;
		default:
			break;
		}
	}

	private void handleInitialization(String msgStr) {
		if(DEBUG)
			Log.d(TAG,"handleInitialization()");
//		int length=msgStr.length();
		int what=Integer.parseInt(msgStr.substring(0,5));
		switch(what){
		case(COMPETE_CODE):
			progressDialogDismiss();
			if(singleGame&&!mGameOver){
			// handleInitialization(msgStr); is inside onClick of the currentGameCanceledDialog(msgStr); to avoid layered Dialogs
				mRunning=false;
				singleToggleButton.setChecked(false);
				currentGameCanceledDialog(msgStr);
			}
			else{
				gameOptionDialog(COMPETE_CODE);
			}
			break;
		case (COLLABORATE_CODE):
			progressDialogDismiss();
			if(singleGame&&!mGameOver){
				// handleInitialization(msgStr); is inside onClick of the currentGameCanceledDialog(msgStr); to avoid layered Dialogs
				mRunning=false;
				singleToggleButton.setChecked(false);
				currentGameCanceledDialog(msgStr);
			}
			else{
				gameOptionDialog(COLLABORATE_CODE);
			}
			break;
		case(CONFIRMATION_CODE):
			//			this.gamePhase=Game.GAME_PLAY;
			this.progressDialogDismiss();
			if(mCompete)
				startCompeteGameCountDown(FOLLOWER);
			else{
				startCollaborateGame(FOLLOWER);
			}
			break;
		case (REQUEST_OPTION_CODE):
			this.progressDialogDismiss();
			requestOptionDialog();
			break;
		case FRIEND_END_GAME_CODE:
			closingDialog(res.getString(R.string.FRIEND_END_GAME_MSG));
			break;
		default:
			if(DEBUG)
				Log.d(TAG,"handleInitialization() :"+msgStr);
			errorHandling(msgStr);
			break;
		}
	}

	private void handleGamePlay(String msgStr) {
		if(DEBUG)
			Log.d(TAG,"handleGamePlay :"+msgStr+" mCompete:"+mCompete);
		if(mCompete){
//			int length=msgStr.length();
			int what=Integer.parseInt(msgStr.substring(0,5));
			switch(what){
			case (DELETE_LINE_CODE):
				int line=Integer.parseInt(msgStr.substring(5,6));
				mBlockOps.tetradAddlines(line, 1);
				mTdView.invalidate();
				break;
			case(GAME_OVER_CODE):
				mGameOver=true;
				mRunning=false;
//				doHandlerPost(rFriendLostGame);
				friendLostGameDialog();
				break;
			case FRIEND_END_GAME_CODE:
				closingDialog(res.getString(R.string.FRIEND_END_GAME_MSG));
				break;
			case LITE_VERSION_CODE:
				friendHasLiteVersionDialog();
				break;
			default:
				Log.d(TAG,"handleGamePlay :DEFAULT_HITTING: "+msgStr+" what:"+what);
				break;
			}
		}else{
//			int i=Integer.parseInt(msgStr.substring(5));
			int what=Integer.parseInt(msgStr.substring(0,5));
			//if(TEMP) Log.d(TAG,"handleGamePlay what:"+what);
			//String str=""+BLOCKS+bnum+borient+bNnum+bNorient;
			switch(what){
			case(BLOCKS_CODE):
				int bnum=Integer.parseInt(msgStr.substring(5,6));
				int borient=Integer.parseInt(msgStr.substring(6,7));
				int bNnum=Integer.parseInt(msgStr.substring(7,8));
				int bNorient=Integer.parseInt(msgStr.substring(8,9));
				BlockOps.setBlocknum(--bnum);
				BlockOps.setBlockorient(--borient);
				BlockOps.setNextBlockNum(--bNnum);
				BlockOps.setNextBlockOrient(--bNorient);
				BlockOps.setBlockxOrigin();
				mTdView.invalidate();
				mSView.invalidate();
				break;
			case (X_CODE):
				int x=Integer.parseInt(msgStr.substring(5));
				x--;
				mBlockOps.tetradBlockMove(x);
				mTdView.setRectForInvalidate(x,0);
				break;
			case (Y_CODE):
				int y=Integer.parseInt(msgStr.substring(5));
				if(--y==1){
					mBlockOps.tetradBlockDown();
					mTdView.setRectForInvalidate(0,1);
				}
				break;
			case (ORIENT_CODE):
				int orient=Integer.parseInt(msgStr.substring(5));
				orient--;
				mBlockOps.tetradBlockRotate(orient);
				mTdView.setRectForInvalidate(0,0);
				break;
			case (TURN_CHANGE_CODE):
				mRunning=true;
				blockLanded(true);
				mTdView.setSent(true);
				if(!mGameOver){
					callToast(res.getString(R.string.YOUR_TURN));
					startLoop();
				}
				break;
			case LITE_VERSION_CODE:
				friendHasLiteVersionDialog();
				break;
			case GAME_OVER_CODE:
				mGameOver=true;
				mRunning=false;
				endOfCollaborateGame();
				break;
			case FRIEND_END_GAME_CODE:
				closingDialog(res.getString(R.string.FRIEND_END_GAME_MSG));
				break;
			default:
				Log.d(TAG,"handleGamePlay :DEFAULT_HITTING: "+msgStr+" what:"+what);
				break;
			}
		}
	}

	private void handleClosing(String msgStr) {

	}

	private void startLoop() {
		if(DEBUG)
			Log.d(TAG,"startLoop ThreadID: "+Thread.currentThread().getId());
		mTdView.invalidate();
		mSView.invalidate();

		new Thread(	new Runnable(){
			public void run(){


				mBlockOps.setPlaying(true);
				if(mGameOver==true){
					block=mBlockOps.tetradMakeBlock();
					mBlockOps.initField();
					blocked=mBlockOps.tetradBlockDown();
					mGameOver=false;
					mBlockOps.tetradMakeNextBlock();

				}
				doHandlerPost(rSideDoDraw);
				while(block!=0&&mRunning){
					if(mCollaborate){
						int bnum=BlockOps.getBlocknum();
						int borient=BlockOps.getBlockorient();
//						int bx=BlockOps.getBlockx();
//						int by=BlockOps.getBlocky();
						int bNnum=BlockOps.getNextBlockNum();
						int bNorient=BlockOps.getNextBlockOrient();
						String str=""+BLOCKS_CODE+(++bnum)+(++borient)+(++bNnum)+(++bNorient);
						tolk.sendMsgToTolk(str);
					}
					while(blocked!=-1&&mRunning){

						doHandlerPost(rDoDrawRect);
						try {
							Thread.sleep(dropTime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						blocked=mBlockOps.tetradBlockDown();
						if(mCollaborate)
							tolk.sendMsgToTolk(""+Y_CODE+2);
					}
					if(mCollaborate&&mRunning){
						tolk.sendMsgToTolk(TURN_CHANGE_CODE+"1");
						mTdView.setSent(false);
					}
					blockLanded(false);
					if(mCollaborate)
						mRunning=false;
				}
				}}).start();
	}

	protected void blockLanded(boolean myTurn) {

		if(mRunning){
			mBlockOps.tetradSolidify ();
			line=mBlockOps.tetradRemovelines();
			if(mCompete&&line!=0)
				doHandlerPost(rSendDeletLine);
			addLineClear(line);
			//					lineClear++;
			int level=lineClear/10;
			if(level>gameLevel){
				dropTime=dropTime-25;
				gameLevel++;
				if(liteGame)
					mAd.requestFreshAd();
				
				if(gameLevel%LITE_REGION==0&&gameLevel>0&&searchAunnDroid){
					if(mbTextView!=null){
						doHandlerPost(rSearchAunnDroid);
						searchAunnDroid=false;
					}
				}
			}
			
			gameScore=(lineClear * (100 + 10 * gameLevel));
			doHandlerPost(rDoDraw);
			block=mBlockOps.tetradGetNextBlock();
			if(block==0){
				mGameOver=true;
				tolkGame=false;
				dropTime=DROP_TIME;
				if(singleGame||mCompete)
					doHandlerPost(rDoGameOver);
				if(mCompete){
					tolk.sendMsgToTolk(""+GAME_OVER_CODE);
					tolkGameResetSetting();
				}else if(mCollaborate){
//					tolk.sendMsgToTolk(""+GAME_OVER);
					mRunning=false;
					doHandlerPost(rCollabGameOver);
				}
				return;
			}
			if(mCompete||myTurn||singleGame)
				mBlockOps.tetradMakeNextBlock();
			doHandlerPost(rSideDoDraw);
			blocked=mBlockOps.tetradBlockDown();
		}
	}

	protected void startCollaborateGame(int starter) {
		if(DEBUG)
			Log.d(TAG,"startCollaborateGame");
		resetGameSetting();
		this.lineClear=0;
		this.gameScore=0;
		this.gameLevel=0;
		tolkGame=true;
		mTdView.setmCollaborate(true);
		if(starter==STARTER){
			callToast(res.getString(R.string.YOUR_TURN));
			gamePhase=GAME_PLAY;
			mRunning=true;
			mTdView.setSent(true);
			startLoop();
		}else{
			/**mGameOver is false here only because FOLLOWER will continue the game in the loop
			 * Only STARTER need mGameover true to initiate the game.
			 * */
			gamePhase=GAME_PLAY;
			mGameOver=false;
			mTdView.setSent(false);
		}
	}

	private void startCompeteGameCountDown(int from){
		if(DEBUG)
			Log.d(TAG,"startCompeteGameCountDown : ");
		mTdView.setmCollaborate(false);
//		tolkGameResetSetting();
		resetGameSetting();
		switch(from){
		case STARTER:
			counterTime=STARTER_TIME;
			break;
		case FOLLOWER:
			counterTime=FOLLOWER_TIME;
			break;
		default:
			break;
		}
		mCounter=3;

		/**
		 * Here is the Thread doing count down with sleep() while manipulating number View via handler
		 * */
		new Thread(	new Runnable(){	public void run(){
			for(int i=3;i>=0;i--){
				doHandlerPost(rDisplayNum); // myHandler.post(new Runnable(){public void run(){	startCompeteGame();}});
				try {
					Thread.sleep(counterTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}}).start();
	}

	private void startCompeteGame(){
		if(DEBUG)
			Log.d(TAG,"startCompeteGame mCounter: "+mCounter);
		/**
		 * When the big number is counting down at startCompeteGameCountDown(int from),
		 * it will call this method between numbers and mCounter will tell it, when the game can be start.
		 * */
		if(mCounter!=0){
			if(TEMP)
				Log.d(TAG,"startCompeteGame mCounter: "+mCounter);
			numTextView.setText(mCounter+" ");
			numTextView.setVisibility(TextView.VISIBLE);
			mCounter--;
		}else{
			numTextView.setVisibility(TextView.INVISIBLE);
			mCounter=3;
			gamePhase=GAME_PLAY;
			mCompete=true;
			mRunning=true;
			tolkGame=true;
			startLoop();
		}
	}

	public void tolkGameResetSetting(){
		if(DEBUG)
			Log.d(TAG,"tolkGameResetSetting()");
		this.tolk.resetSetting();
		this.tolkGame=false;
		gamePhase=Game.INITIALIZATION;
		mCompete=false;
		mCollaborate=false;
	}

	public void resetGameSetting(){
		if(DEBUG)
			Log.d(TAG,"resetGameSetting");
		mRunning=false;
		mGameOver=true;
		tolkGame=false;
		dropTime=DROP_TIME;
		mBlockOps.initField();
		mTdView.invalidate();
		mSView.invalidate();
		this.lineClear=0;
		this.gameScore=0;
		this.gameLevel=0;
	}

	private void singlePlay() {
		if(DEBUG)
			Log.d(TAG,"SINGLE_TOGGLE_BUTTON");
		//tolkGame will only be used in the tolk game so we are checking here if tolk game is in play only
		if(!tolkGame){
			mTdView.setmCollaborate(false);
			mRunning=!mRunning;
			this.singleToggleButton.setChecked(mRunning);
			//	if(loopThread==null)
			//mGameOver=false, singleGame=true locked
			clickCount++;
			if(mGameOver||!singleGame){
				resetGameSetting();
				mRunning=true;
				clickCount=0;
			}
			singleGame=true;
			if(clickCount>=3){
				resetGameSetting();
				clickCount=0;
				mRunning=true;
				this.singleToggleButton.setChecked(mRunning);
			}
			//	tolkGameResetSetting();
			startLoop();
		}else
			this.singleToggleButton.setChecked(false);
	}

	private void friendLostGameDialog() {
		new AlertDialog.Builder(this).setTitle(R.string.VICTORY_TITLE)
		.setMessage(tolk.getFriendID()+res.getString(R.string.VICTORY_MSG))
		.setPositiveButton(R.string.OK,new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int okButton) {
				tolkGameResetSetting();
				resetGameSetting();
			}})
			.show();
	}

	private void endOfCollaborateGame() {
		new AlertDialog.Builder(this).setTitle(R.string.COLLABORATE_GAMEOVER_TITLE)
		.setMessage(res.getString(R.string.COLLABORATE_GAMEOVER_MSG)+"\n"+
				res.getString(R.string.SCORE)+gameScore+"\n"+
				res.getString(R.string.LINES)+lineClear+"\n"+
				res.getString(R.string.LEVEL)+gameLevel)
		.setPositiveButton(R.string.OK,new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int okButton) {
				tolkGameResetSetting();
			}})
			.show();
	}

	private void singleGameOverDialog(){
		new AlertDialog.Builder(this).setTitle(R.string.SINGLE_GAMEOVER_TITLE)
		.setMessage(//res.getString(R.string.SINGLE_GAMEOVER_MSG)+"\n"+
				res.getString(R.string.SCORE)+gameScore+"\n"+
				res.getString(R.string.LINES)+lineClear+"\n"+
				res.getString(R.string.LEVEL)+gameLevel)
		.setPositiveButton(R.string.OK,new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int okButton) {
				tolkGameResetSetting();
			}})
			.show();		
	}
	
	private void settingDialog() {
		new AlertDialog.Builder(this).setTitle(R.string.setting)
		.setItems(R.array.setting_dialog_items,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	switch(which){
            	case 0:
            		tolk.changeLoginInfo();
            		break;
            	default:
            		break;
            	}
            }
        })
		.show();	
	}

	private void requestOptionDialog() {
		new AlertDialog.Builder(this).setTitle(R.string.REQUEST_OPTION_TITLE)
		.setMessage(tolk.getFriendID()+res.getString(R.string.REQUEST_OPTION_MSG))
		.setPositiveButton(R.string.OK,new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int okButton) {
				startA2A(STARTER);
			}})
			.show();
	}

	private void gameOptionDialog(int option) {
		String game;
		if(option==COMPETE_CODE){
			game=res.getString(R.string.COMPETE);
			mCompete=true;
			mCollaborate=false;
		}else{
			mCompete=false;
			mCollaborate=true;
			game=res.getString(R.string.COLLABORATE);
		}
		new AlertDialog.Builder(this).setTitle(R.string.GAME_OPTION_TITLE)
		.setMessage(game+res.getString(R.string.GAME_OPTION_MSG))
		.setPositiveButton(R.string.PLAY,new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int okButton) {
				if(mCompete){
					tolk.sendMsgToTolk(""+CONFIRMATION_CODE);
					startCompeteGameCountDown(STARTER);
				}else{
					tolk.sendMsgToTolk(""+CONFIRMATION_CODE);
					startCollaborateGame(STARTER);
				}
			}})
		/*	.setNegativeButton(R.string.REQUEST,new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int okButton) {
					tolk.sendMsgToTolk(""+REQUEST_OPTION_CODE);
				}})
		*/	.show();
	}

	private void currentGameCanceledDialog(final String msgStr) {
		if(DEBUG)
			Log.d(TAG,"currentGameCanceledDialog");
		new AlertDialog.Builder(this).setTitle(R.string.app_name)
		.setMessage(R.string.Cancel_Current_Game)
		.setPositiveButton(R.string.YES,new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int okButton) {
				tolkGame=false;
				singleGame=false;
				mGameOver=true;
				handleInitialization(msgStr);
			}})
			.show();
	}

	protected void closingDialog(String msg) {
		if(DEBUG)
			Log.d(TAG,"closingDialog");
		if(myAlertDialog!=null&&myAlertDialog.isShowing())
			myAlertDialog.dismiss();
		myAlertDialog=new AlertDialog.Builder(this)
		.setTitle(R.string.ENDING_GAME)
		.setMessage(msg)
		.setNeutralButton(R.string.END_SESSION, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				tolk.sendMsgToTolk(""+FRIEND_END_GAME_CODE);
				tolk.resetSetting();
				resetGameSetting();
//				setSinglePlay(true);
			}
		})
		.setNegativeButton(R.string.DISCONNECT, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				tolk.sendMsgToTolk(""+FRIEND_END_GAME_CODE);
				tolk.resetSetting();
				tolk.onDestroy();
				resetGameSetting();
//				setSinglePlay(true);
			}
		})
		.show();
	}

	private void LiteVersionDialog(){
		if(DEBUG)
			Log.d(TAG,"closingDialog");
		if(!singleGame)
			tolk.sendMsgToTolk(""+LITE_VERSION_CODE);

		if(myAlertDialog!=null&&myAlertDialog.isShowing())
			myAlertDialog.dismiss();
		myAlertDialog=new AlertDialog.Builder(this)
		.setTitle(R.string.LITE_GAME_TITLE)
		.setMessage(R.string.TETRADS_LITE_GAME_MSG)
		.setNeutralButton(R.string.END_SESSION, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				tolk.resetSetting();
				resetGameSetting();
//				setSinglePlay(true);
			}
		}).show();
	}

	private void friendHasLiteVersionDialog() {
		if(DEBUG)
			Log.d(TAG,"closingDialog");
		mRunning=false;
		if(myAlertDialog!=null&&myAlertDialog.isShowing())
			myAlertDialog.dismiss();
		myAlertDialog=new AlertDialog.Builder(this)
		.setTitle(R.string.LITE_GAME_TITLE)
		.setMessage(R.string.TETRADS_FRIEND_LITE_GAME_MSG)
		.setNeutralButton(R.string.END_SESSION, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				tolk.resetSetting();
				resetGameSetting();
//				setSinglePlay(true);
			}
		}).show();
	}
	/**
	 * This errorHandling is not well tested and dangerous ...
	 * @param msgStr
	 * */
	private void errorHandling(String msgStr) {
		callToast(res.getString(R.string.FUNNY_THING));
		if(tolk.isServer()){
			startA2A(STARTER);
		}else{
			tolk.sendMsgToTolk(FORCE_ERROR);
	//		newGame();
		}
	}

	/**
	 * Will be used to notify the user Trivial thing like non Aunndroid user is IM'ing the user
	 * */
	public void callToast(String msg){
		if(DEBUG)
			Log.d(TAG, "callToast");
		myToast=Toast.makeText(this, msg, Toast.LENGTH_LONG);
		myToast.setGravity(Gravity.CENTER, 0, -mTdView.getFieldHeight()/4);
		myToast.show();
	}

	public BlockOps getmBlockOps() {
		return mBlockOps;
	}

	public void setmBlockOps(BlockOps mBlockOps) {
		this.mBlockOps = mBlockOps;
	}

	public int getLineClear() {
		return lineClear;
	}

	public void setLineClear(int lineClear) {
		this.lineClear = lineClear;
	}

	public void addLineClear(int line){
		this.lineClear=this.lineClear+line;
	}

	public int getGameLevel() {
		return gameLevel;
	}

	public void setGameLevel(int gameLevel) {
		this.gameLevel = gameLevel;
	}

	public int getGameScore() {
		return gameScore;
	}

	public void setGameScore(int gameScore) {
		this.gameScore = gameScore;
	}

	public boolean ismRunning() {
		return mRunning;
	}

	public void setmRunning(boolean mRunning) {
		this.mRunning = mRunning;
	}

	public boolean isSingleGame() {
		return singleGame;
	}

	public void setSingleGame(boolean singleGame) {
		this.singleGame = singleGame;
	}

	public int getScreenDimension() {
		return screenDimension;
	}

	public void setScreenDimension(int screenDimension) {
		this.screenDimension = screenDimension;
	}

	public void friendConnectionLost() {
		mCompete=false;
		mCollaborate=false;
		mRunning=false;
		mGameOver=true;
		tolkGame=false;
		dropTime=DROP_TIME;
	}

	public void sendXYOrient(int xyOrient, int j) {
		switch(xyOrient){ //x=0,y=1,orient=2
		case 0:
			tolk.sendMsgToTolk(""+X_CODE+(++j));
			break;
		case 1:
			tolk.sendMsgToTolk(""+Y_CODE+(++j));
			break;
		case 2:
			tolk.sendMsgToTolk(""+ORIENT_CODE+(++j));
			break;
		default:
			break;
		}
	}
}
