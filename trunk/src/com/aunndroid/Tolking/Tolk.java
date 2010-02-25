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
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.StringUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

public class Tolk implements ConstantInterface{

	private int noAck=0;
	private int errorCode;
	private int diffTime=0;
	private int waitTime;
	private int tolkPhase;
	private String A2A;
	private String OK;
	private String ACK;
	private String fqFriendID="";
	private String friendID;
	private String from;
	private String EMPTY_SESSION_ID="0000";
	private String sessionID;
	private String errorMsg;
	private String ack;
	private boolean server;
	private boolean useMessageBox=false;
	private Chat mChat;

	private XMPPagent xmppAgent;
	private TolkError myTolkError;
	private TolkDialog myTolkDialog;

	private Context context;
	private Timer myTimer;
	private Handler myHandler;
	private Toast myToast;
	private Queue<Message> messageQueue;
	private Resources res;
	private AlertDialog errorAlertDialog;

	private static int errorDialogcounter;
	//static final
	private static final String TAG="Tolk : ";
	private static final int HANDSHAKE=0;
	private static final int GAME_PLAY=1;
	private static final int CLOSE_CONNECTION=2;
	private static final boolean liteGame=false;
	private static final boolean DEBUG=false;
	private static final boolean VERBOSE=false;
	private static final boolean TEMP=false;

	public Tolk(Context context, String appName) {
		if(DEBUG)
			Log.d(TAG,"Tolk(Context context, String appName) : ");
		this.context=context;
		xmppAgent = new XMPPagent(context, this);
		mChat=new Chat(context,this);
		ack="";//=GID;
		myToast=new Toast(context);
		myTolkError=new TolkError(context,this);
		myHandler = new Handler();
		myTolkDialog=new TolkDialog();
		res=this.context.getResources();
		A2A=res.getString(R.string.A2A);
		OK=res.getString(R.string.OK);
		ACK=""+ACK_CODE;
		messageQueue=new LinkedList<Message>();
	}

	public void onDestroy(){
		if(DEBUG)
			Log.d(TAG,"onDestroy");
		this.resetSetting();
		xmppAgent.onDestroy();
		if(myTimer!=null){
			myTimer.cancel();
		}
	}

	public void onDestroyCalled() {
		onDestroy();
		((Game)(this.context)).tolkOnDestroy();
	}

	/**
	 * When a2a is clicked, it will check to see if it is the first time login or not if(xmppAgent.getStoredLogin()==0)
	 * if it is, then it will be prompted with the login Dialog Box.
	 *
	 * If it is not first time login, then it will check if it is connected or not to save time and process,
	 * create connection if it is not connected. Otherwise call startDialog to launch the Alert Dialog Box to "Start" or "Wait" for the
	 * connection.
	 * */
	public void a2a() {
		if(DEBUG)
			Log.d(TAG," a2a :");
		if (xmppAgent.getStoredLogin()==0){
			xmppAgent.show();
		}
		else{
			if(!xmppAgent.isConnected()){
				xmppAgent.createConnection();
			}
			else{
				myTolkDialog.startDialog();
			}
		}
	}

	public void changeLoginInfo() {
		this.xmppAgent.setStoredLogin(0);
		this.xmppAgent.show();
	}

	/**
	 * doHandlerPost is created because View won't allow update from thread other than main thread.
	 * XMPP create a separate thread for connection. When the message arrive from XMPP,
	 * the connection calls the receive method and so on ... until the update to the View takes place.
	 * Then it fails because the update come from XMPP connection which is on a separate thread.
	 * There are a few other non XMPP thread, like logging into gtalk, which takes quite a bit of time and
	 * has to put it in a separate thread not to get the  application not responding system message.
	 * */
	public void doHandlerPost(int runnable){
		switch (runnable){
		case rSTART_DIALOG:
			myHandler.post(new Runnable(){public void run(){myTolkDialog.startDialog();}});
			break;
		case rNO_RESPONSE_DIALOG:
			myHandler.post(new Runnable() { public void run() { noResponse();}});
			break;
		case rRECEIVE_ERROR:
			myHandler.post(new Runnable (){public void run(){receiveError();}});
			break;
		case rRECEIVE_MESSAGE:
			myHandler.post(new Runnable (){public void run(){receiveMsg();}});
			break;
		case rCONNECTION_ERROR_DIALOG:
			myHandler.post(new Runnable (){public void run(){myTolkDialog.connectionErrorAlertDialog();}});
			break;
		case rFRIEND_CONNECTION_LOST:
			myHandler.post(new Runnable(){public void run(){ callToast(xmppAgent.getFriendID()+res.getString(R.string.FRIEND_CONNECTION_LOST_MSG));
				//myTolkDialog.friendConnectionLostDialog();
				}});
			break;
		case rAUTO_DISCONNECT:
			myHandler.post(new Runnable(){public void run(){myTolkDialog.autoDisconnectDialog();}});
			break;
		default :
			myTolkDialog.defaultHittingDialog("doHandlerPost");
			break;
		}
	}

	/**method to call the myHandler*/
	public void onStartDialog(){
		doHandlerPost(rSTART_DIALOG);
	}

	/**method to call the myHandler*/
	public void onConnectionErrorAlertDialog(String msg){
		errorMsg=msg;
		doHandlerPost(rCONNECTION_ERROR_DIALOG);
	}

	/**method to call the myHandler*/
	public void onReceiveError(int errorCode, String errorMsg){
		this.errorCode=errorCode;
		this.errorMsg=errorMsg;
		doHandlerPost(rRECEIVE_ERROR);
	}

	/**method to call the myHandler*/
	private void receiveError() {
		myTolkError.handleError(errorCode, errorMsg);
		errorMsg="";
	}

	/**
	 * Will be used to notify the user Trivial thing like non Aunndroid user is IM'ing the user
	 * */
	public void callToast(String msg){
		if(TEMP)
			Log.d(TAG,"callToast :"+msg);
		if(useMessageBox){
			MessageBox.setMessage(msg);
		}else{
		myToast=Toast.makeText(context, msg, Toast.LENGTH_LONG);
		myToast.show();
		}
	}


	/**
	 * So this is where the myHandler and Thread management came in.
	 * Since, android View won't accept update originated from threads other than main thread,
	 * we have to create a myHandler and runnable so that other thread can invoke them.
	 * myHandler is running in the main thread and myHandler will call post with runnable,
	 * which has desired method running inside run().
	 *
	 * Look in a Tolk object constructor for myHandler and runnable creation
	 * with desired method running inside runnable run()
	 * */
	public  void onReceiveMsg(Message message){
		if(DEBUG)
			Log.d(TAG,"onReceiveMsg : "+message.getBody());
		messageQueue.add(message);
		doHandlerPost(rRECEIVE_MESSAGE);
	}


	/**
	 * Up being called up by XMPPAgent via myHandler, it will fetch the message in the XMPPAgent
	 * mMessage=xmppAgent.getMMessage();
	 * Then extracts information accordingly and passes them to another receiveMsg for further processing
	 * */
	public void receiveMsg() {
		if(DEBUG)
			Log.d(TAG,"receiveMsg()");
		while(!messageQueue.isEmpty()){
			Message message=messageQueue.remove();
			String friendUserName = StringUtils.parseName(message.getFrom());
			String resource = StringUtils.parseResource(message.getFrom());
			receiveMsg(message.getBody(),friendUserName,resource);
		}
	}


	/**
	 * Here we will be categorizing messages according to the phases.
	 * Before we do that, we will set aside message from non Aunndroid client by checking "XMPP resource"
	 * We will let user know about the IM received from non Aunndroid client by using Toast.
	 * Then message will be processed accordingly.
	 * There will be three phases: HandShake, Initialization and GamePlay. (I am thinking about adding closing)
	 * */
	public void receiveMsg(String body, String from, String resource) {
		if(DEBUG)
			Log.d(TAG,"receiveMsg :bdy "+body+" from:"+from+" Resource:"+resource);
		this.from=from;
		if(!resource.equalsIgnoreCase(res.getString(R.string.RESOURCE_ID_CODE))){
			String to;
			to=from+"@gmail.com/"+resource;
			xmppAgent.sendMsg(String.format(res.getString(R.string.COURTESY_REPLY),res.getString(R.string.RESOURCE_ID_CODE)), to);
			callToast(from+res.getString(R.string.JUST_IM_YOU)+":"+body);
			return;
		}
		if(sessionID==null)		//"if sessionID is null" means the app is not started properly yet. Maybe, I will leave this out by initiating the sessionID
			return;
		switch(tolkPhase){
		case HANDSHAKE:
			if(VERBOSE)
				Log.d(TAG,"Handshake");

			if(friendID==null||friendID.equalsIgnoreCase("")){
				friendID=from;
				fqFriendID=friendID+"@gmail.com/"+resource;
			}
			handleHandShake(body,from);		//handleHandShake method will handle HandShake for both server and client
			break;
		case GAME_PLAY:
			if(VERBOSE)
				Log.d(TAG,"GamePlay");
			handleGamePlay(body,resource);
			break;
		case CLOSE_CONNECTION:
			if(VERBOSE)
			Log.d(TAG,"CloseConnection");
			resetSetting();
			break;
		default :
			if(VERBOSE)
			Log.d(TAG,"Default");
			myTolkDialog.defaultHittingDialog("receiveMsg");
			break;
		}
	}

	public void friendConnectionLost() {
		if(DEBUG)
			Log.d(TAG,"friendConnectionLost()");
		doHandlerPost(rFRIEND_CONNECTION_LOST);
		this.resetSetting();
	}

	private void handleGamePlay(String body,String resource) {
		if(TEMP)
			Log.d(TAG,"handleGamePlay body:"+body+" Resource:"+resource);
		//Standard game play if sessionID is the same
		String to;
		if(sessionID.equalsIgnoreCase(body.substring(ZERO,SESSION_ID_LENGTH))){
			int what=Integer.parseInt(body.substring(4,9));
			switch(what){
			case TOLK_GAME_PLAY_CODE:
				((Game)(this.context)).receiveMsgFromTolk(body.substring(SESSION_ID_LENGTH+5));
				break;
			case TOLKING_CODE:
				callToast(from+":"+body.substring(9));
				break;

			case FILE_SIZE_CODE:
					long filesize=Long.parseLong(body.substring(FILE_SIZE_LENGTH));
				if(TEMP)
					Log.d(TAG,"FileSize :"+filesize);
				this.xmppAgent.progressDialogDismiss();
				boolean success=filesize==this.xmppAgent.getFileSentLength();
				fileTransferFinished(success);
				this.xmppAgent.setFtSuccess(success);
				break;
			default:
				myTolkDialog.defaultHittingDialog("handleGamePlay "+body);
				break;
			}
		}else {
			int what=Integer.parseInt(body.substring(4,9));
			switch(what){
			case INVITE_CODE:
				if(!friendID.equalsIgnoreCase(from)){
					to=from+"@gmail.com/"+resource;
					xmppAgent.sendMsg(body.substring(ZERO,SESSION_ID_LENGTH)+ACK,to); 			//replying ACK for (3rd party)different invite from different user.
					xmppAgent.sendMsg(EMPTY_SESSION_ID+ALREADY_PLAYING_CODE,to);									//Give reason for the decline
					this.callToast(from+res.getString(R.string.DECLINIED_INVITE));
				}
				break;
			case REQUEST_CODE:
				to=from+"@gmail.com/"+resource;
				if(VERBOSE)
					Log.d(TAG,"Recevied :"+body+" From:"+to);
				xmppAgent.sendMsg(body.substring(ZERO,ACK_LENGTH),to);
				break;
			case ACK_CODE:
				if(VERBOSE)
					Log.d(TAG,"ACK Receive");
				break;
			case FILE_SIZE_CODE:
					long filesize=Long.parseLong(body.substring(FILE_SIZE_LENGTH));
				if(TEMP)
					Log.d(TAG,"FileSize :"+filesize);
				this.xmppAgent.progressDialogDismiss();
				boolean success=filesize==this.xmppAgent.getFileSentLength();
				fileTransferFinished(success);
				this.xmppAgent.setFtSuccess(success);
				break;
			case ALREADY_PLAYING_CODE:
				to=from+"@gmail.com/"+resource;
				xmppAgent.sendMsg(EMPTY_SESSION_ID+ALREADY_PLAYING_CODE,to);

				break;
			default:
				if(TEMP)
					Log.d(TAG,"handleGamePlay :"+body);
				myTolkDialog.defaultHittingDialog("handleGamePlay "+body);
				break;
			}
		}
	}

	public void fileTransferFinished(final boolean success) {
		myHandler.post(new Runnable(){public void run(){((Game)(context)).fileTransferFinished(success);}});
	}

	/**
	 * function called from other class to send MSG over the XMPP
	 * */
	public void sendMsgToTolk(String strFromApp) {
		if(TEMP)
			Log.d(TAG,"sendMsgToTolk "+strFromApp);
		if(tolkPhase==GAME_PLAY)
			xmppAgent.sendMsg(sessionID+TOLK_GAME_PLAY_CODE+strFromApp,fqFriendID);
		else{
		//	myTolkDialog.connectFirstDialog();
			Log.d(TAG,"Crashe here?");
			myHandler.post(new Runnable(){public void run(){callToast(res.getString(R.string.CONNECT_FIRST_MSG));}});
		}
	}

	public void xmppAgentSendMsg(String str, String friend){
		xmppAgent.sendMsg(str,friend);
	}
	public void chatShow(){
		mChat.show();
	}

	/**
	 * Starting the handshake process
	 * */
	public void startHandShake(){			//These codes are to be read from ##SERVER## point of view as it is initiating the handleHandShake session
		if(DEBUG)
			Log.d(TAG,"startHandShake : ");
		friendID=xmppAgent.getFriendID();
		fqFriendID=xmppAgent.getFQFriendID();
		if(xmppAgent.checkFriendOnlineStatus(fqFriendID)){
			tolkPhase=HANDSHAKE;
			Time time =new Time();
			time.setToNow();
			sessionID=time.toString().substring(11, 15);
			String invite=sessionID+INVITE_CODE+(String.format(res.getString(R.string.GAME_INVITE),res.getString(R.string.RESOURCE_ID_CODE)));
			xmppAgent.sendMsg(invite,fqFriendID);
			ack=sessionID+ACK;
			server=true;
			waitTime=IN_SECONDS;
			createTimer(waitTime);}
		else{
			this.myTolkDialog.appNotRunningMsgDialog();
		}
	}

	/**
	 * We will sort out the message according to the length of the message body.
	 * That way, it will be easier to organize instruction easily and saving cpu cycle from all String comparison, I hope.
	 *
	switch(length){
		case INVITE_LENGTH:
			if(sessionID.length()==0){}
			else{
				Why would the user be in handshake phase and sessionID is not "" ? Could it happen in a split moment?
				No. Cause we will be setting the initializing phase once the user accept it.

				Ah... think again, we should wait for the ack to be completed in the handshake phase.
				forget what does the line above mean, well,anyway, my logic is still at work.
				so the user get invite from someone and has not replied yet while another invite(the 3rd party) come in.
				instead of displaying 3rd party invite (to display over the current one), the app will simply reply to
				the 3rd party player that it is already playing. Thus, removing the confusion from user of having 2 invites

			}
			break;
		case ACK_LENGTH:
			//We don't have to worry about "from" who the ACK come cause the sessionID will be unique.
			//hopefully no one send out invite at the same minute and second ;)
			if(body.equalsIgnoreCase(ack)){	}
			else{
					//For ##CLIENT##, this is the first Ack after reply. Ah...I have assumed that the reply is ACCEPT and will change phase
					//How about reply is CANCEL? well, we won't care about not receiving ack then cause we don't have any more business
					//to do with after CANCEL anyway. The basic setting are also updated when the cancel is pressed.
				}
			}
			break;
		case ACCEPT_LENGTH:
			if(body.equalsIgnoreCase(sessionID+ACCEPT_MSG)){}
			else if (body.equalsIgnoreCase(sessionID+CANCEL_MSG)){}
			break;
		case ALREADY_PLAYING_LENGTH:
			if(body.equalsIgnoreCase(ALREADY_PLAYING)){	}
			break;
		default:
			break;
		}
		@author Win Myo Htet
	 * @param from
	 */
	private void handleHandShake(String body, String from) {
		if(DEBUG)
			Log.d(TAG,"handleHandShake:String: "+body);

		int what=Integer.parseInt(body.substring(4,9));
		switch(what){
		case INVITE_CODE:						//In this "case INVITE_LENGTH: " These codes are all to be read from the ##CLIENT## point of view
			if(VERBOSE)
				Log.d(TAG,"handleHandShake:String: INVITE_LENGTH");
			if(sessionID.equalsIgnoreCase(EMPTY_SESSION_ID)){			//and if user is currently not playing according to sessionID
				sessionID=body.substring(ZERO,SESSION_ID_LENGTH);

				Time time =new Time();
				time.setToNow();
				String sCurrTime=time.toString().substring(11, 15);
				calculateDiffTime((int)Integer.parseInt(sessionID),(int)Integer.parseInt(sCurrTime));

				ack=sessionID+ACK;
				xmppAgent.sendMsg(ack,fqFriendID);				//Reply ACK as the other party is expecting it.
				this.myTolkDialog.handleInviteMsgDialog();
			}
			else{
				xmppAgent.sendMsg(body.substring(ZERO,SESSION_ID_LENGTH)+ACK,fqFriendID); 			//replying ACK for (3rd party)different invite from different user.
				xmppAgent.sendMsg(EMPTY_SESSION_ID+ALREADY_PLAYING_CODE,fqFriendID);									//Give reason for the decline
				this.callToast(this.from+res.getString(R.string.DECLINIED_INVITE));						//Let user know about the invite via toast
			}
			break;
		case ACK_CODE:
			if(VERBOSE)
				Log.d(TAG,"handleHandShake:String: ACK_LENGTH:");
			if(body.equalsIgnoreCase(ack)){
				if(server){					//For ##SERVER##, this ack will be the first to receive it and will need the ACCEPT message before it can change phase
					noAck=0;
					myTimer.cancel();		//Cancel the timer for the 15 seconds wait for ack but create a timer for 1.5minute wait for response
					waitTime=IN_MINUTES;
					createTimer(waitTime);
				}
				else{
					tolkPhase=GAME_PLAY;
					((Game)(this.context)).startA2A(Game.FOLLOWER);
					myTimer.cancel();
				}
			}
			break;
		case ACCEPT_CODE:
			if(VERBOSE)
				Log.d(TAG,"handleHandShake:String: ACCEPT_LENGTH:");
			Time time =new Time();
			time.setToNow();
			String sCurrTime=time.toString().substring(11, 15);
			calculateDiffTime((int)Integer.parseInt(body.substring(ACCEPT_LENGTH-4)),(int)Integer.parseInt(sCurrTime));

			xmppAgent.sendMsg(ack,fqFriendID);
			tolkPhase=GAME_PLAY;
			((Game)(this.context)).startA2A(Game.STARTER);
			myTimer.cancel();
			break;
		case CANCEL_CODE:
			this.myTolkDialog.cancelReplyMsgDialog();				//So we need to report the Pzl15 Cancel Message
			myTimer.cancel();					//We need to kill the timer whether we receive the ACCEPT or CANCEL. We receive the message.
			break;
		case ALREADY_PLAYING_CODE:
				this.myTolkDialog.alreadyPlayingDialog();
				if(myTimer!=null)
				myTimer.cancel();

			break;
		case TOLKING_CODE:
			callToast(from+":"+body.substring(9));
			break;
		case FILE_SIZE_CODE:
			long filesize=Long.parseLong(body.substring(FILE_SIZE_LENGTH));
			if(TEMP)
				Log.d(TAG,"FileSize :"+filesize);
			this.xmppAgent.progressDialogDismiss();
			boolean success=filesize==this.xmppAgent.getFileSentLength();
			fileTransferFinished(success);
			this.xmppAgent.setFtSuccess(success);
			break;
		case TOLK_GAME_PLAY_CODE:
			break;
		default :
			myTolkDialog.defaultHittingDialog("handleHandShake :"+body);

			break;
		}
	}

	/**
	 * Since each phone might be off a bit in time to each other, when I need to do collision resolution based on time,
	 * I cannot assume all the phone has the exact same time. So I have to know about the time discrepancy between each phone.
	 * The informations are gathered during the handshake process.
	 *
	 * When the invite is received, the sessionID is the minute and second portion of the phone. So we calculate
	 * the difference right away by checking the time on the phone itself. That is for client side.
	 *
	 * For server side, when the server receive the Accept msg, the client has already attached the time info at the end of the Accept msg
	 *
	 * Since Minute and second relationship is not 100 base but 60, we have this method to do it.
	 * */
	public void calculateDiffTime(int oTime, int mTime){
		int oh=(int)oTime/100;
		int om=oTime%100;

		int mh=(int)mTime/100;
		int mm=mTime%100;

		diffTime=(oh-mh)*60+(om-mm);
		if(DEBUG)
			Log.d(TAG,"calculateDiffTime "+diffTime+" server:"+server);
	}

	/**
	 * One of the most crucial methods. Reset properties so that the new connection can be made
	 * */
	public  void resetSetting(){
		if(DEBUG)
			Log.d(TAG,"resetSetting");
		tolkPhase=HANDSHAKE;
		ack="";
		sessionID=EMPTY_SESSION_ID;
		friendID="";
//		fqFriendID="";
		noAck=0;
		server=false;
		setDiffTime(0);
	}

	/**
	 * Whenever we expect some response back, we create a timer and wait for the response.
	 * If the timer run out and still no response, noResponseDialog will be called via myHandler
	 * since Timer run a separate thread for TimerTask, which execute the call.
	 * */
	private  void createTimer(int waitTime) {
		if(DEBUG)
			Log.d(TAG,"createTimer");
		myTimer=new Timer();
		myTimer.schedule(new TimerTask(){public void run(){doHandlerPost(rNO_RESPONSE_DIALOG);}}, waitTime);
	}


	/**
	 * We will have a single noResponse for all events, we will check with the phase and other setting
	 * to see what is causing the noResponse to be called and display the message accordingly.
	 * Very Dynamic, huh! .... oh yeh?
	 *
	 *
	 * Why does only client have to wait for 3 times? Why not for server case?
	 * My current justification is that the server handshake ack is the first
	 * to be received, so we can assume the failed ack for the first is good
	 * enough indicator to dismiss the connection and start again.
	 * */
	public  void noResponse(){
		if(DEBUG)
			Log.d(TAG,"noResponseDialog");
		Time time =new Time();
		time.setToNow();
		switch(tolkPhase){
		case HANDSHAKE:
			if(server){
				if(waitTime==IN_SECONDS){						//waitTime==IN_SECONDS means that it is waiting for the ack
					this.myTolkDialog.appNotRunningMsgDialog();
				}
				else if(waitTime==IN_MINUTES){					//waitTime==IN_MINUTES means that it is waiting for the friend to respond.
					this.myTolkDialog.friendNotRespondingDialog();
				}
			}else{
				if(waitTime==IN_SECONDS){
					waitForAckThreeTime();
				}
			}
			break;
		case GAME_PLAY:
			break;
		default :
			myTolkDialog.defaultHittingDialog("noResponse");
			break;
		}
	}

	/**
	 * instead of reporting the No response rightaway, we wait for three times
	 * Hm... just wait for 3 times ? Without pinging anymore? Well, I am only capable of that right now.
	 * */
	private void waitForAckThreeTime() {
		if(DEBUG)
			Log.d(TAG,"waitForAckThreeTime");
		if(noAck==2){
			this.myTolkDialog.gettingNoResponseMsgDialog();
		}
		else{
			noAck++;
			myTimer.cancel();
			waitTime=IN_SECONDS;
			createTimer(waitTime);
		}
	}

	public void cancelTimer() {
		this.myTimer.cancel();
	}

	public String getFriendID() {
		return friendID;
	}

	public void setFriendID(String friendID) {
		this.friendID = friendID;
	}

	public void setDiffTime(int diffTime) {
		this.diffTime = diffTime;
	}

	public int getDiffTime() {
		return diffTime;
	}

	public boolean isServer() {
		return server;
	}

	public void setServer(boolean server) {
		this.server = server;
	}

	public boolean isConnected() {
		return this.xmppAgent.isConnected();
	}

	public String getFqFriendID() {
		return fqFriendID;
	}

	public void setFqFriendID(String fqFriendID) {
		this.fqFriendID = fqFriendID;
	}

	public String getSessionID() {
		return sessionID;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	public void setUseMessageBox(boolean useMessageBox) {
		this.useMessageBox = useMessageBox;
	}

	public boolean isUseMessageBox() {
		return useMessageBox;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public void startFileTransfer(){
		this.xmppAgent.progressDialog(res.getString(R.string.SENDING_FILE));
		this.xmppAgent.fileTransferSent();
	}

	public void sendAckRequest() {
		String msg=""+REQUEST_CODE;
		xmppAgent.sendMsg(msg,fqFriendID);
	}

	public void sendFileSize(long fileReceivedLength, String to) {
		String msg=sessionID+FILE_SIZE_CODE+fileReceivedLength;
		xmppAgent.sendMsg(msg, to);
	}
	public String getUsername() {
		return this.xmppAgent.getUsername();
	}
	public void connectFirstDialogCall() {
	//	myTolkDialog.connectFirstDialog();
		callToast(res.getString(R.string.CONNECT_FIRST_MSG));
	}

	class TolkError {
		private String TAG="Tolk: TolkError : ";
		/**
		 * Represents a XMPP error sub-packet. Typically, a server responds to a request that has
		 * problems by sending the packet back and including an error packet. Each error has a code, type,
		 * error condition as well as as an optional text explanation. Typical errors are:<p>
		 *
		 * <table border=1>
		 *      <hr><td><b>Code</b></td><td><b>XMPP Error</b></td><td><b>Type</b></td></hr>
		 *      <tr><td>500</td><td>internal-server-error</td><td>WAIT</td></tr>
		 *      <tr><td>403</td><td>forbidden</td><td>AUTH</td></tr>
		 *      <tr><td>400</td<td>bad-request</td><td>MODIFY</td>></tr>
		 *      <tr><td>404</td><td>item-not-found</td><td>CANCEL</td></tr>
		 *      <tr><td>409</td><td>conflict</td><td>CANCEL</td></tr>
		 *      <tr><td>501</td><td>feature-not-implemented</td><td>CANCEL</td></tr>
		 *      <tr><td>302</td><td>gone</td><td>MODIFY</td></tr>
		 *      <tr><td>400</td><td>jid-malformed</td><td>MODIFY</td></tr>
		 *      <tr><td>406</td><td>no-acceptable</td><td> MODIFY</td></tr>
		 *      <tr><td>405</td><td>not-allowed</td><td>CANCEL</td></tr>
		 *      <tr><td>401</td><td>not-authorized</td><td>AUTH</td></tr>
		 *      <tr><td>402</td><td>payment-required</td><td>AUTH</td></tr>
		 *      <tr><td>404</td><td>recipient-unavailable</td><td>WAIT</td></tr>
		 *      <tr><td>302</td><td>redirect</td><td>MODIFY</td></tr>
		 *      <tr><td>407</td><td>registration-required</td><td>AUTH</td></tr>
		 *      <tr><td>404</td><td>remote-server-not-found</td><td>CANCEL</td></tr>
		 *      <tr><td>504</td><td>remote-server-timeout</td><td>WAIT</td></tr>
		 *      <tr><td>502</td><td>remote-server-error</td><td>CANCEL</td></tr>
		 *      <tr><td>500</td><td>resource-constraint</td><td>WAIT</td></tr>
		 *      <tr><td>503</td><td>service-unavailable</td><td>CANCEL</td></tr>
		 *      <tr><td>407</td><td>subscription-required</td><td>AUTH</td></tr>
		 *      <tr><td>500</td><td>undefined-condition</td><td>WAIT</td></tr>
		 *      <tr><td>400</td><td>unexpected-condition</td><td>WAIT</td></tr>
		 *      <tr><td>408</td><td>request-timeout</td><td>CANCEL</td></tr>
		 * </table>
		 *
		 * @author Matt Tucker
		 */

		public TolkError(Context context, Tolk tolk) {
		}

		public void handleError(int errorCode, String errorMsg){
			if(DEBUG)
				Log.d(TAG," handleError");
			errorDialogcounter++;

			if(myTimer!=null){
				cancelTimer();
			}
			if(errorAlertDialog!=null){
				errorAlertDialog.dismiss();
			}
			errorMsg=" : "+errorMsg;
			resetSetting();
			switch(errorCode){
			case 400:
				errorAlertDialog=new AlertDialog.Builder(context)
				.setTitle(res.getString(R.string.GTALK_ERROR))
				.setMessage(errorCode+errorMsg)
				.setPositiveButton(OK, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				})
				.show();
				break;
			case 503:
				errorAlertDialog=new AlertDialog.Builder(context)
				.setTitle(res.getString(R.string.GTALK_ERROR))
				.setMessage(errorCode+errorMsg)
				.setPositiveButton(OK, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				})
				.show();
				break;
			default:
				if(errorMsg==null)
					errorMsg=" ";
				errorAlertDialog=new AlertDialog.Builder(context)
				.setTitle(res.getString(R.string.GTALK_ERROR))
				.setMessage(errorCode+errorMsg+res.getString(R.string.RESETTING_THE_GAME))
				.setPositiveButton(OK, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				})
				.show();
				break;
			}

			if(errorDialogcounter==8){
				if(DEBUG)
					Log.d(TAG,"Error Doom!!!!!!");
				xmppAgent.onDestroy();
				System.gc();
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		}
	}

	class TolkDialog {
		private static final String TAG = "Tolk: TolkDialog:";

		public TolkDialog() {
		}

		public void friendConnectionLostDialog() {
			if(DEBUG)
				Log.d(TAG,"friendConnectionLostDialog()");
			new AlertDialog.Builder(context)
			.setTitle(R.string.FRIEND_CONNECTION_LOST_TITLE)
			.setMessage(xmppAgent.getFriendID()+res.getString(R.string.FRIEND_CONNECTION_LOST_MSG))
			.setPositiveButton(OK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					resetSetting();
					((Game)context).friendConnectionLost();
				}
			})
			.show();
		}

		public void startDialog(){
			if(DEBUG)
				Log.d(TAG,"startDialog");
			errorDialogcounter=0;
			new AlertDialog.Builder(context)
			.setTitle(A2A)
			.setPositiveButton(res.getString(R.string.START), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					if(liteGame){
						liteGameDialog();
					}else{
						xmppAgent.show();
						server=true;
					}
				}
			})
			.setNeutralButton(res.getString(R.string.WAIT),new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					server=false;
					tolkPhase=HANDSHAKE;
					sessionID=EMPTY_SESSION_ID;
				}
			})
			.setNegativeButton(res.getString(R.string.DISCONNECT),new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					onDestroy();
				}
			})
			.show();
		}

		protected void liteGameDialog() {
			new AlertDialog.Builder(context)
			.setTitle(R.string.LITE_GAME_TITLE)
			.setMessage(R.string.LITE_GAME_MSG)
			.setPositiveButton(OK,new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					//We need to update the setting as the other player do not accept the current request.
					//These update settings are in the ##SERVER## side.
					resetSetting();
				}
			})
			.show();
		}

		public void handleInviteMsgDialog() {
			if(DEBUG)
				Log.d(TAG,"handleInviteMsgDialog");
			new AlertDialog.Builder(context)
			.setTitle(A2A)
			.setMessage(from+String.format(res.getString(R.string.INVITE_MSG),res.getString(R.string.RESOURCE_ID_CODE)))
			.setPositiveButton(R.string.START, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Time time =new Time();
					time.setToNow();
					String sCurrTime=time.toString().substring(11, 15);
					// I have to set from, friendID,fqFriendID here definitly since we will be playing with that person
					friendID=from;
					fqFriendID=friendID+"@gmail.com/"+res.getString(R.string.RESOURCE_ID_CODE);

					xmppAgent.setFriendID(from);
					xmppAgent.setFQFriendID(fqFriendID);
					xmppAgent.sendMsg(sessionID+ACCEPT_CODE+sCurrTime,fqFriendID);
					waitTime=IN_SECONDS;
					createTimer(waitTime);
				}
			})
			.setNegativeButton(R.string.CANCEL,new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Time time =new Time();
					time.setToNow();
					String sCurrTime=time.toString().substring(11, 15);

					xmppAgent.sendMsg(sessionID+CANCEL_CODE+sCurrTime,fqFriendID);
					resetSetting();
					//We need to update the setting as we do not accept the current request.
					//These update settings are in the ##CLIENT## side.
					//We have another setting update for the same variables for the ##SERVER## side below.
				}
			})
			.show();
		}

		public void cancelReplyMsgDialog() {
			if(DEBUG)
				Log.d(TAG,"cancelReplyMsgDialog");
			new AlertDialog.Builder(context)
			.setTitle(A2A)
			.setMessage(from+res.getString(R.string.CANCEL_REPLY_MSG))
			.setPositiveButton(OK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					//We need to update the setting as the other player do not accept the current request.
					//These update settings are in the ##SERVER## side.
					resetSetting();
				}
			})
			.show();
		}

		public void alreadyPlayingDialog(){
			if(DEBUG)
				Log.d(TAG,"alreadyPlayingDialog");
			new AlertDialog.Builder(context)
			.setTitle(A2A)
			.setMessage(from+String.format(res.getString(R.string.ALREADY_PLAYING_MSG),res.getString(R.string.RESOURCE_ID_CODE)))
			.setPositiveButton(OK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					//We need to update ACK as the other player do not accept the current request.
					//These update settings are in the ##SERVER## side.
					resetSetting();
				}
			})
			.show();
		}

		public void appNotRunningMsgDialog() {
			if(DEBUG)
				Log.d(TAG,"appNotRunningMsgDialog");
			new AlertDialog.Builder(context)
			.setTitle(A2A)
			.setMessage(String.format(res.getString(R.string.APP_NOT_RUNNING_MSG),res.getString(R.string.RESOURCE_ID_CODE)))
			.setPositiveButton(OK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					resetSetting();
				}
			})
			.show();
		}

		public void friendNotRespondingDialog() {
			if(DEBUG)
				Log.d(TAG,"friendNotRespondingDialog");
			new AlertDialog.Builder(context)
			.setTitle(A2A)
			.setMessage(res.getString(R.string.FRIEND_NOT_RESPONDING))
			.setPositiveButton(R.string.WAIT, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					waitTime=IN_SECONDS;
					createTimer(waitTime);
				}
			})
			.setNegativeButton(R.string.CANCEL,new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					resetSetting();
				}
			})
			.show();
		}

		public void gettingNoResponseMsgDialog() {
			if(DEBUG)
				Log.d(TAG,"gettingNoResponseMsgDialog");
			new AlertDialog.Builder(context)
			.setTitle(A2A)
			.setMessage(res.getString(R.string.GETTING_NO_RESPONSE_MSG))
			.setPositiveButton(OK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					resetSetting();
				}
			})
			.show();
		}

		private void connectionErrorAlertDialog() {
			if(DEBUG)
				Log.d(TAG,"connectionErrorAlertDialog");
			new AlertDialog.Builder(context)
			.setTitle(res.getString(R.string.CONNECTION_ERROR))
			.setMessage(errorMsg)
			.setPositiveButton(OK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				}
			})
			.setNegativeButton(R.string.CHANGE_LOGIN_INFO, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					xmppAgent.setStoredLogin(0);
				}
			})
			.show();
			errorMsg="";
		}

		public void defaultHittingDialog(String method) {
			if(DEBUG)
				Log.d(TAG,"defaultHittingDialog");
			new AlertDialog.Builder(context)
			.setTitle(res.getString(R.string.DEFAULT_HITTER_TITLE))
			.setMessage(method+ res.getString(R.string.DEFAULT_HITTER_MSG))
			.setPositiveButton(OK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					resetSetting();
				}
			})
			.show();
		}

		public void connectFirstDialog() {
			new AlertDialog.Builder(context)
			.setTitle(res.getString(R.string.CONNECT_FIRST_TITLE))
			.setMessage(res.getString(R.string.CONNECT_FIRST_MSG))
			.setPositiveButton(OK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				}
			})
			.show();
		}

		public void autoDisconnectDialog() {
			new AlertDialog.Builder(context)
			.setTitle(res.getString(R.string.AUTO_DISCONNECT_TITLE))
			.setMessage(res.getString(R.string.AUTO_DISCONNECT_MSG))
			.setPositiveButton(OK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				}
			})
			.show();
		}
	}
}
