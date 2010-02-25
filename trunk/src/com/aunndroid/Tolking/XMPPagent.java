/*
Copyright (c) 2007 Win Myo Htet <wmhtet@aunndroid.com>
          
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.
*/

package com.aunndroid.Tolking;
import com.aunndroid.TetradsDropLite.R;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.os.Handler;
import android.util.Log;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.PrivacyProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.Base64;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.provider.AdHocCommandDataProvider;
import org.jivesoftware.smackx.provider.BytestreamsProvider;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.IBBProviders;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.MessageEventProvider;
import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.provider.RosterExchangeProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.search.UserSearch;


/**
 * Gather the xmpp settings and create an XMPPConnection
 */
public class XMPPagent extends Dialog implements android.view.View.OnClickListener, ConstantInterface {

	private XMPPConnection connection;
	private FileTransferManager ftManager;
	@SuppressWarnings("unused")
	private ServiceDiscoveryManager sdm;
	private PacketListener chatListener;
	private PacketListener errorListener;
	private PacketListener presenceAvailableListener;
	private PacketListener presenceUnavailableListener;
	private FileTransferListener myFileTransferListener;
	private OutgoingFileTransfer outFTransfer;

	private int storedLogin;
	private long fileSentLength;
	private long fileReceivedLength;
	private String username;
	private String password ;
	private String friendID;
	private String fQfriendID;
	private boolean friendConnection;
	private boolean timerTaskMessageQueue=false;
	private boolean ftSuccess;

	private static int connectionMonitorTicker=0;

	private Tolk tolk;

	private ProgressDialog pd;
	private Resources res;
	private Timer mTimer;
	private Handler handler;
	private Queue<Message> messageQueue;
	private Context context;
	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;
	private Timer queueTimer;
	private ArrayList<String> onlineFriendList;

	private static final String TAG="XMPPagent : ";
	//	private static final String packageString="com.aunndroid.tester3";
	private static final String filePath="/data/data/com.aunndroid.TetradsDrop/tmp.jpg";
	private static final boolean DEBUG=false;
	private static final boolean XMPP_DEBUG = false;
	private static final boolean TEMP=false;

	public XMPPagent(Context context, Tolk tolk) {
		super(context);
		this.context=context;
		res=context.getResources();
		preferences = context.getSharedPreferences(res.getString(R.string.prefFile_CODE), android.content.Context.MODE_PRIVATE);
		editor = preferences.edit();
		storedLogin = preferences.getInt(res.getString(R.string.STORELOGIN_CODE), 0);
		username=preferences.getString(res.getString(R.string.USERNAME_CODE), "");
		String Encrypted_password=preferences.getString(res.getString(R.string.PASSWORD_CODE), "");
		this.tolk=tolk;
		if(Encrypted_password.length()!=0)
			password=(String)Base64.decodeToObject(Encrypted_password);
		pd=new ProgressDialog(this.context);

		handler=new Handler();
		queueTimer=new Timer();
		messageQueue=new LinkedList<Message>();
		onlineFriendList=new ArrayList<String>();
	}

	protected void progressDialog(String msg){
		pd=new ProgressDialog(this.context);
		pd.setCancelable(false);
		pd.setMessage(msg);
		pd.show();
	}

	protected void progressDialogDismiss(){
		if(DEBUG)
			Log.d(TAG,"progressDialogDismiss()");
		pd.setCancelable(true);
		if(pd.isShowing())
			pd.dismiss();
	}

	protected void checkIdle() {
		if(connectionMonitorTicker==0){
			if(context!=null&&connection!=null){
				if(connection.isConnected()){
					tolk.doHandlerPost(rAUTO_DISCONNECT);
					tolk.onDestroyCalled();
				}
			}
		}else{
			connectionMonitorTicker=0;
			mTimer=new Timer();
			mTimer.schedule(new TimerTask(){public void run(){checkIdle();}}, TEN_MINUTES);
		}
	}

	protected void onStart() {
		super.onStart();
		if(DEBUG)
			Log.d(TAG,"onStart");
		if(storedLogin==0){
			setContentView(R.layout.login);
			getWindow().setFlags(4, 4);
			setTitle(res.getString(R.string.ENTER_USERNAME_AND_PASSWORD));
			Button ok = (Button) findViewById(R.id.ok);
			setText(R.id.userid);
			setText(R.id.password);
			ok.setOnClickListener(this);
		}
		else{
			setContentView(R.layout.a2a);
			getWindow().setFlags(4, 4);
			setTitle(R.string.friendId_box_title);
			Button ok = (Button) findViewById(R.id.ok);
			//Before you invite, make sure your friend is running the App and waiting.
			setText(R.id.friendid);
			ok.setOnClickListener(this);
			this.setOnCancelListener(new DialogInterface.OnCancelListener() {

				@Override
				public void onCancel(DialogInterface dialog) {
					// TODO Auto-generated method stub
					tolk.resetSetting();
				}
			});
		}
	}


	protected void onDestroy(){
		if(DEBUG)
			Log.d(TAG,"onDestroy");
		if(connection!=null){
			connection.disconnect();
			connection=null;
		}
	}

	/**
	 * When filetransfer fails for a large file, the connection usually get screwed up.
	 * In such case, connection even refuse to disconnect and the process is stuck.
	 * So we are removing listeners from connection and ftManager, to get a clean disconnect.
	 * */
	public void connectionCleanUp(){
		if(DEBUG)
			Log.d(TAG,"connectionCleanUp()");
		if(ftManager!=null){
			ftManager.removeFileTransferListener(myFileTransferListener);
		}
		if(connection!=null){
			connection.removePacketListener(chatListener);
			connection.removePacketListener(errorListener);
			connection.removePacketListener(presenceAvailableListener);
			connection.removePacketListener(presenceUnavailableListener);
		}
		onDestroy();
	}

	public void onClick(View v) {
		if(DEBUG)
			Log.d(TAG,"onClick");
		TextView a2a_error_msg=(TextView) this.findViewById(R.id.a2a_error_msg);;
		if(storedLogin==0){
			String ENCRYPTED_password;
			username = getText(R.id.userid);
			password = getText(R.id.password);
			ENCRYPTED_password=Base64.encodeObject(password);
			editor.putString(res.getString(R.string.USERNAME_CODE), username);
			editor.putString(res.getString(R.string.PASSWORD_CODE), ENCRYPTED_password);
			editor.commit();
			createConnectionFirstTime();
		}
		else{
			friendID=getText(R.id.friendid);
			while(friendID.equalsIgnoreCase(username)){
				//tolk.friendIDisUserName();
				a2a_error_msg.setText(R.string.FRIENDID_IS_USERNAME);
				a2a_error_msg.setVisibility(TextView.VISIBLE);
				return;
			}
			editor.putString(res.getString(R.string.FRIEND_ID_CODE), friendID);
			editor.commit();
			setFriendID(friendID);
			setFQFriendID(friendID);
			tolk.startHandShake();
		}
		this.dismiss();
		if(a2a_error_msg!=null&&a2a_error_msg.getVisibility()==TextView.VISIBLE){
			a2a_error_msg.setVisibility(TextView.INVISIBLE);
		}
	}



	public void createConnectionFirstTime() {
		if(DEBUG)
			Log.d(TAG,"createConnectionFirstTime");
		pd.setMessage(res.getString(R.string.CONNECTING_TO_GOOGLE));
		pd.show();
		new Thread(new Runnable (){public void run(){
				// Create a connection
				ConnectionConfiguration connConfig =
					new ConnectionConfiguration(res.getString(R.string.HOST_CODE), PORT, res.getString(R.string.SERVICE_CODE));
				connConfig.setDebuggerEnabled(false);
				if(XMPP_DEBUG)
					connConfig.setDebuggerEnabled(true);
				connection = new XMPPConnection(connConfig);

				try {
					connection.connect();
					//    			Log.i("XMPPAgent", "[SettingsDialog] Connected to " + connection.getHost());
				} catch (XMPPException ex) {
					//    			Log.e("XMPPAgent", "[SettingsDialog] Failed to connect to " + connection.getHost());
					//    			Log.e("XMPPAgent", ex.toString());
					setConnection(null);
					editor.putInt(res.getString(R.string.STORELOGIN_CODE), 0); // will have to ask the user to login again, the connection is not established.
					dismiss();
					tolk.onConnectionErrorAlertDialog(res.getString(R.string.NO_CONNECTION));
					pd.dismiss();
					return;
				}
				try {
					connection.login(username, password,res.getString(R.string.RESOURCE_ID_CODE));
					//    			Log.i("XMPPAgent", "Logged in as " + connection.getUser());

					// Set the status to available
					Presence presence = new Presence(Presence.Type.available);
					presence.setPriority(PRESENCE_PRIORITY_MIN);
					presence.setStatus(res.getString(R.string.PRESENCE_STATUS));

					connection.sendPacket(presence);
					setConnection(connection);
					editor.putInt(res.getString(R.string.STORELOGIN_CODE), 1); // For successful login and it will remember the username and password
					storedLogin=1;
				} catch (XMPPException ex) {
					//    			Log.e("XMPPAgent", "[SettingsDialog] Failed to log in as " + username);
					//    			Log.e("XMPPAgent", ex.toString());
					setConnection(null);
					editor.putInt(res.getString(R.string.STORELOGIN_CODE), 0); // will have to ask the user to login again, username/password is not correct
					pd.dismiss();
					tolk.onConnectionErrorAlertDialog(res.getString(R.string.USER_PASSWORD));
					return;
				}
				editor.commit();
				pd.dismiss();
				if(connection.isAuthenticated()){
					tolk.onStartDialog();
					mTimer=new Timer();
					mTimer.schedule(new TimerTask(){public void run(){checkIdle();}}, TEN_MINUTES);
				}
			}}).start();

	}

	/*
	 * I have separated the createConnection and createConnectionFirstTime even though they both share a major chunk of codes.
	 * The reason is that by separating, createConnection doesn't have to go through condition checking which is only required for the first time.
	 * */
	public void createConnection() {
		if(DEBUG)
			Log.d(TAG,"createConnection : ");
		pd.setCancelable(true);
		pd.setMessage(res.getString(R.string.CONNECTING_TO_GOOGLE));
		pd.show();

		new Thread(new Runnable (){public void run(){
				// Create a connection
				ConnectionConfiguration connConfig =
					new ConnectionConfiguration(res.getString(R.string.HOST_CODE), PORT, res.getString(R.string.SERVICE_CODE));
				connConfig.setDebuggerEnabled(false);
				if(XMPP_DEBUG)
					connConfig.setDebuggerEnabled(true);

//				configure(ProviderManager.getInstance());

				connection = new XMPPConnection(connConfig);

				try {
					connection.connect();
					//				Log.i("XMPPAgent", "[SettingsDialog] Connected to " + connection.getHost());
				} catch (XMPPException ex) {
					//				Log.e("XMPPAgent", "[SettingsDialog] Failed to connect to " + connection.getHost());
					//				Log.e("XMPPAgent", ex.toString());
					setConnection(null);

					pd.dismiss();
					/*The reason to call the AlertDialog from tolk is that I don't want to create another
					 * handler in this class instead to make use of the existing one in tolk.
					 */
					tolk.onConnectionErrorAlertDialog(res.getString(R.string.NO_CONNECTION));

					/* return is added so that it won't go down executing the rest of the code to crash the program,
					 * or to launch startDialog while there already is an error connecting...
					 * */
					return;
				}
				try {
					connection.login(username, password,res.getString(R.string.RESOURCE_ID_CODE));
					//				Log.i("XMPPAgent", "Logged in as " + connection.getUser());

					// Set the status to available
					Presence presence = new Presence(Presence.Type.available);
					presence.setPriority(PRESENCE_PRIORITY_MIN);
					presence.setStatus(res.getString(R.string.PRESENCE_STATUS));

					//				XMPPConnection.DEBUG_ENABLED=false;

					connection.sendPacket(presence);
					setConnection(connection);
				} catch (XMPPException ex) {
					//				Log.e("XMPPAgent", "[SettingsDialog] Failed to log in as " + username);
					//				Log.e("XMPPAgent", ex.toString());
					setConnection(null);

					pd.dismiss();
					tolk.onConnectionErrorAlertDialog(res.getString(R.string.NO_CONNECTION));
					return;
				}
				pd.dismiss();
				if(connection.isAuthenticated()){
					tolk.onStartDialog();
					mTimer=new Timer();
					mTimer.schedule(new TimerTask(){public void run(){checkIdle();}}, TEN_MINUTES);
				}
			}}).start();
	}


	public void setConnection (final XMPPConnection connection) {
		if(DEBUG)
			Log.d(TAG,"setConnection : ");
		new Thread(new Runnable (){public void run(){
			if (connection != null) {
				// Add a packet listener to get messages sent to us
				PacketFilter chat_filter = new MessageTypeFilter(Message.Type.chat);
				PacketFilter error_filter = new MessageTypeFilter(Message.Type.error);
				PacketFilter pesence_available_filter=new PresenceTypeFilter(Presence.Type.available);
				PacketFilter pesence_unavailable_filter=new PresenceTypeFilter(Presence.Type.unavailable);

				chatListener=new PacketListener() {
					public void processPacket(Packet packet) {
						if(DEBUG)
							Log.d(TAG," processingPacket");
						Message message = (Message) packet;
						if(DEBUG)
							Log.d(TAG," processingPacket :"+message.getBody());
						if (message.getBody() != null) {
							//						String fromName = StringUtils.parseBareAddress(message.getFrom());
							//						Log.i(TAG, "Got text [" + message.getBody() + "] from [" + fromName + "]"   + packet.toXML());
							tolk.onReceiveMsg(message);
						}
					}
				};
				connection.addPacketListener(chatListener, chat_filter);

				errorListener = new PacketListener() {
					public void processPacket(Packet packet) {
						XMPPError xmppError=packet.getError();
						if(xmppError==null){
						}
						else{
							if(DEBUG)
								Log.d(TAG,"XMPPError : "+xmppError.getCode()+" "+xmppError.getMessage());
							tolk.onReceiveError(xmppError.getCode(),xmppError.getMessage());
						}
					}
				};
				connection.addPacketListener(errorListener,error_filter);


				presenceAvailableListener=new PacketListener(){

					@Override
					public void processPacket(Packet packet) {
						// Monitor friend availability
						Presence fpresence=(Presence)packet;
						String jid=fpresence.getFrom();
						if(res.getString(R.string.RESOURCE_ID_CODE).equalsIgnoreCase(StringUtils.parseResource(jid))){
							if(!onlineFriendList.contains(jid)){
								onlineFriendList.add(jid);
								if(tolk.isUseMessageBox()){
									String friend_username=StringUtils.parseName(jid);
									if(!friend_username.equalsIgnoreCase(username))
										MessageBox.handlerSetMessage(friend_username+res.getString(R.string.IS_LOGON_TO)+res.getString(R.string.RESOURCE_ID_CODE)+".");

								}
								if(DEBUG)
									Log.d(TAG,"ArrayList Add(in): "+onlineFriendList.size()+" "+jid);
							}
						}
						if(DEBUG)
							Log.d(TAG,"ArrayList Add: "+onlineFriendList.size()+" "+jid);
					}};
				connection.addPacketListener(presenceAvailableListener, pesence_available_filter);

				presenceUnavailableListener=new PacketListener(){

					@Override
					public void processPacket(Packet packet) {
						if(DEBUG)
							Log.d(TAG,"presenceUnavailableListener : processingPacket :");
						// Monitor friend Unavailability
						Presence fpresence=(Presence)packet;
						String jid=fpresence.getFrom();
						if(res.getString(R.string.RESOURCE_ID_CODE).equalsIgnoreCase(StringUtils.parseResource(jid))){
							onlineFriendList.remove(jid);
							if(tolk.isUseMessageBox()){
								String friend_username=StringUtils.parseName(jid);
								MessageBox.handlerSetMessage(friend_username+res.getString(R.string.IS_LOGOFF_FROM)+res.getString(R.string.RESOURCE_ID_CODE)+".");
							}
							if(jid.equalsIgnoreCase(fQfriendID)){
								tolk.friendConnectionLost();
							}
						}
					}};
				connection.addPacketListener(presenceUnavailableListener, pesence_unavailable_filter);

				// Create the file transfer ftManager

				sdm=new ServiceDiscoveryManager(connection);
				ftManager = new FileTransferManager(connection);

				FileTransferNegotiator.setServiceEnabled(connection,true);

				// Create the listener
				myFileTransferListener=new FileTransferListener() {
					public void fileTransferRequest(FileTransferRequest request) {
						// Accept it
						try {
							if(DEBUG)
								Log.d(TAG," requestor "+request.getRequestor()+" FQFID "+getFQFriendID());
							if(request.getRequestor().equalsIgnoreCase(getFQFriendID())){
								if(DEBUG)
									Log.d(TAG,"1 requestor "+request.getRequestor()+" FQFID "+getFQFriendID());
								handler.post(new Runnable(){ public void run(){progressDialog(res.getString(R.string.RECEIVING_FILE));}});
								IncomingFileTransfer transfer = request.accept();
								transfer.recieveFile(new File(filePath));
								if(DEBUG)
									Log.d(TAG,"fileTransfer Check progress :"+transfer.getProgress()+" Thread id :"+Thread.currentThread().getId());

								double progress;
								double progressState=0;
								int iCompare=-1;
								int count=0;
								long fileSize=transfer.getFileSize();

								while(iCompare!=0){//|| ! transferDone){//transfer.getProgress()!=1.0 ||!transfer.isDone()){
									try {
//										if(DEBUG)
//											Log.d(TAG,"fileTransfer Check progress :"+transfer.getProgress()+" Thread id :"+Thread.currentThread().getId()+" isDone:"+transfer.isDone());
										Thread.sleep(1000);
										progress=transfer.getProgress();
										iCompare=Double.compare(progress, 1.0);
										if(count==11){
											if(Double.compare(progress, progressState)==0){
												iCompare=0;
												transfer.cancel();
											}
											progressState=progress;
											count=0;
										}
										count++;
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
								if(DEBUG)
									Log.d(TAG,"File Received");
								handler.post(new Runnable(){public void run(){progressDialogDismiss();}});
								fileReceivedLength= new File(filePath).length();
								if(DEBUG)
									Log.d(TAG,"fileReceivedLength :"+fileReceivedLength);
								tolk.sendFileSize(fileReceivedLength,getFQFriendID());
								tolk.fileTransferFinished(fileReceivedLength==fileSize);
							}
						} catch (XMPPException e) {
							e.printStackTrace();
						}
					}
				};
				ftManager.addFileTransferListener(myFileTransferListener);
			};
		}}).start();
	}

	public long getfileReceivedLength() {
		return fileReceivedLength;
	}

	public void setfileReceivedLength(int fileReceiveHash) {
		this.fileReceivedLength = fileReceiveHash;
	}

	public long getFileSentLength() {
		return fileSentLength;
	}

	public void setFileSentLength(int fileSentHash) {
		this.fileSentLength = fileSentHash;
	}

	public boolean checkFriendOnlineStatus(String fqFriendID) {
		return onlineFriendList.contains(fqFriendID);
	}

	public void sendMsgToFriend(String msg) {
		if(DEBUG)
			Log.d(TAG,"sendMsgToFriend : "+friendConnection+" msg: "+msg);
		if(friendConnection){
			String text = msg;
			Message message = new Message(fQfriendID, Message.Type.chat);
			message.setBody(text);
			connection.sendPacket(message);
			connectionMonitorTicker++;
		}else{

		}
	}

	public boolean isFriendConnection() {
		return friendConnection;
	}

	public void setFriendConnection(boolean friendConnection) {
		this.friendConnection = friendConnection;
	}


	/**
	 * sendMsg needs to queue the message and send them after MSG_DELAY_TIME
	 * so that there won't be a message congestion and lost of messages sent
	 * resulting in a data corruption.
	 * */
	public void sendMsg(String msg, String to) {
		if(DEBUG)
			Log.d(TAG,"sendMsg : "+msg+" :"+to);
		String text = msg;
		Message message = new Message(to, Message.Type.chat);
		message.setBody(text);

		messageQueue.add(message);
		if(DEBUG)
			Log.d(TAG,"sendMsg : "+messageQueue.size()+" "+messageQueue.isEmpty());
		if(!timerTaskMessageQueue){
			if(DEBUG)
				Log.d(TAG,"sendMsg : "+messageQueue.size()+" "+messageQueue.isEmpty());
		/*	new Thread(new Runnable (){
				public void run(){
					if(DEBUG)
						Log.d(TAG,"timerTaskMsgQueue : ");
					timerTaskMessageQueue=true;
					while(!messageQueue.isEmpty()){
						if(DEBUG)
							Log.d(TAG,"timerTaskMsgQueue : while");
						connection.sendPacket(messageQueue.remove());
						connectionMonitorTicker++;
						try {
							Thread.sleep(350);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					if(DEBUG)
						Log.d(TAG,"timerTaskMsgQueue : Exit ");
					timerTaskMessageQueue=false;
				}});
		*/

			queueTimer.cancel();
			queueTimer=new Timer();
			//	queueTimer.schedule(queueTimerTask, 0);
			queueTimer.schedule(new TimerTask (){
				public void run(){
					if(DEBUG)
						Log.d(TAG,"timerTaskMsgQueue : ");
					timerTaskMessageQueue=true;
					while(!messageQueue.isEmpty()){
						if(DEBUG)
							Log.d(TAG,"timerTaskMsgQueue : while");
						connection.sendPacket(messageQueue.remove());
						connectionMonitorTicker++;
						try {
							Thread.sleep(MSG_DELAY_TIME);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					if(DEBUG)
						Log.d(TAG,"timerTaskMsgQueue : Exit ");
					timerTaskMessageQueue=false;
				}} , 0);
		}
	}


	public boolean isConnected(){
		//   	Log.d(TAG,"isConnected");
		if(connection!=null){
			return connection.isAuthenticated();
		}
		return false;
	}



	public void setFriendID(String friendID) {
		this.friendID = friendID;
	}

	public String getFriendID() {
		return friendID;
	}
	public String getFQFriendID() {
		return fQfriendID;
	}


	public void setFQFriendID(String friendID) {
		if(!friendID.contains(res.getString(R.string.RESOURCE_ID_CODE)))
			this.fQfriendID=friendID+"@gmail.com/"+res.getString(R.string.RESOURCE_ID_CODE);
		else
			this.fQfriendID=friendID;
	}

	private String getText(int id) {
		EditText widget = (EditText) this.findViewById(id);
		return widget.getText().toString();
	}

	private void setText(int id) {
		String text="";
		switch(id){
		case R.id.friendid:
			text=preferences.getString(res.getString(R.string.FRIEND_ID_CODE), "");
			break;
		case R.id.userid:
			text=preferences.getString(res.getString(R.string.USERNAME_CODE), "");
			break;
		case R.id.password:
			text=preferences.getString(res.getString(R.string.PASSWORD_CODE), "");
			if(text.length()!=0){
				password=(String)Base64.decodeToObject(text);
				text=password;
			}
			break;
		default:
			text="";
			break;
		}
		EditText widget = (EditText) this.findViewById(id);
		widget.setText(text);
	}

	public int getStoredLogin() {
		return storedLogin;
	}

	public void setStoredLogin(int storedLogin) {
		this.storedLogin = storedLogin;
	}

	public boolean isFtSuccess() {
		return ftSuccess;
	}

	public void setFtSuccess(boolean ftSuccess) {
		this.ftSuccess = ftSuccess;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void fileTransferSent(){
		if(DEBUG)
			Log.d(TAG,"fileTransferSent");
		ftSuccess=true;
		new Thread(new Runnable (){public void run(){

			// Create the outgoing file transfer
			outFTransfer = ftManager.createOutgoingFileTransfer(getFQFriendID());//"aunndroid@gmail.com/spark34E47427");//"minsa2011@gmail.com/pidgin873646F6");//


			/*
			 * Since the client has more chance of failure and will tell me if it has issue thus we don't need the timer.
			 *
			 * Ah...but when the file transfer screwed up, the server cannot received any message any more! we still need timer!
			 */
			mTimer.schedule(new TimerTask(){
				public void run(){
					if(DEBUG)
						Log.d(TAG,"Timer is called for unFinished file transfer");
					handler.post(new Runnable(){

						public void run(){
							if(pd.isShowing()){
								pd.dismiss();
								ftSuccess=false;
								tolk.fileTransferFinished(ftSuccess);
							}
							}});
					}}, IN_MINUTES);

			// Send the file
			try {
				File file=new File(filePath);
				fileSentLength=file.length();
				if(DEBUG)
					Log.d(TAG,"fileSentLength :"+fileSentLength);

				outFTransfer.sendFile(file, "You won't believe this!");

				while(!outFTransfer.isDone())
				{
					if(outFTransfer.getStatus().equals(Status.error)) {
						Log.e(TAG,"ERROR!!! " + outFTransfer.getError());
					} else {
						if(DEBUG){
							Log.d(TAG,"Status: "+outFTransfer.getStatus());
							Log.d(TAG,"Progress: "+outFTransfer.getProgress());
						}
					}
					try {

						/*
						 * outFTrnsfer.cancel() needs 1000 ms time to get clean cancel before disconnecting.
						 *  I have spent quite a while here transfering a large file (sigh, yeh 3MB) over
						 *  and over waiting for it to fail.
						 * */
						if(!ftSuccess){
							outFTransfer.cancel();
							if(mTimer!=null)
								mTimer.cancel();
						}
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if(!ftSuccess){
					outFTransfer=null;
					connectionCleanUp();
				}

			} catch (XMPPException e) {
				e.printStackTrace();
			}

			try {
				System.in.read();
			} catch (IOException ex) {
				//ex.printStackTrace();
			}
		}}).start();
	}

	public void configure(ProviderManager pm) {
		//  Private Data Storage
		pm.addIQProvider("query","jabber:iq:private", new PrivateDataManager.PrivateDataIQProvider());
		//  Time
		try {
			pm.addIQProvider("query","jabber:iq:time", Class.forName("org.jivesoftware.smackx.packet.Time"));
		} catch (ClassNotFoundException e) {
			Log.w("TestClient", "Can't load class for org.jivesoftware.smackx.packet.Time");
		}

		//  Roster Exchange
		pm.addExtensionProvider("x","jabber:x:roster", new RosterExchangeProvider());

		//  Message Events
		pm.addExtensionProvider("x","jabber:x:event", new MessageEventProvider());

		//  Chat State
		pm.addExtensionProvider("active","http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
		pm.addExtensionProvider("composing","http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
		pm.addExtensionProvider("paused","http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
		pm.addExtensionProvider("inactive","http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());
		pm.addExtensionProvider("gone","http://jabber.org/protocol/chatstates", new ChatStateExtension.Provider());

		//  XHTML
		pm.addExtensionProvider("html","http://jabber.org/protocol/xhtml-im", new XHTMLExtensionProvider());

		//  Group Chat Invitations
		pm.addExtensionProvider("x","jabber:x:conference", new GroupChatInvitation.Provider());

		//  Service Discovery # Items
		pm.addIQProvider("query","http://jabber.org/protocol/disco#items", new DiscoverItemsProvider());

		//  Service Discovery # Info
		pm.addIQProvider("query","http://jabber.org/protocol/disco#info", new DiscoverInfoProvider());

		//  Data Forms
		pm.addExtensionProvider("x","jabber:x:data", new DataFormProvider());

		//  MUC User
		pm.addExtensionProvider("x","http://jabber.org/protocol/muc#user", new MUCUserProvider());

		//  MUC Admin
		pm.addIQProvider("query","http://jabber.org/protocol/muc#admin", new MUCAdminProvider());

		//  MUC Owner
		pm.addIQProvider("query","http://jabber.org/protocol/muc#owner", new MUCOwnerProvider());

		//  Delayed Delivery
		pm.addExtensionProvider("x","jabber:x:delay", new DelayInformationProvider());

		//  Version
		try {
			pm.addIQProvider("query","jabber:iq:version", Class.forName("org.jivesoftware.smackx.packet.Version"));
		} catch (ClassNotFoundException e) {
			//  Not sure what's happening here.
		}

		//  VCard
		pm.addIQProvider("vCard","vcard-temp", new VCardProvider());

		//  Offline Message Requests
		pm.addIQProvider("offline","http://jabber.org/protocol/offline", new OfflineMessageRequest.Provider());

		//  Offline Message Indicator
		pm.addExtensionProvider("offline","http://jabber.org/protocol/offline", new OfflineMessageInfo.Provider());

		//  Last Activity
		pm.addIQProvider("query","jabber:iq:last", new LastActivity.Provider());

		//  User Search
		pm.addIQProvider("query","jabber:iq:search", new UserSearch.Provider());

		//  SharedGroupsInfo
		pm.addIQProvider("sharedgroup","http://www.jivesoftware.org/protocol/sharedgroup", new SharedGroupsInfo.Provider());

		//  JEP-33: Extended Stanza Addressing
		pm.addExtensionProvider("addresses","http://jabber.org/protocol/address", new MultipleAddressesProvider());

		//   FileTransfer
		pm.addIQProvider("si","http://jabber.org/protocol/si", new StreamInitiationProvider());
		pm.addIQProvider("query","http://jabber.org/protocol/bytestreams", new BytestreamsProvider());
		pm.addIQProvider("open","http://jabber.org/protocol/ibb", new IBBProviders.Open());
		pm.addIQProvider("close","http://jabber.org/protocol/ibb", new IBBProviders.Close());
		pm.addExtensionProvider("data","http://jabber.org/protocol/ibb", new IBBProviders.Data());

		//  Privacy
		pm.addIQProvider("query","jabber:iq:privacy", new PrivacyProvider());
		pm.addIQProvider("command", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider());
		pm.addExtensionProvider("malformed-action", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider.MalformedActionError());
		pm.addExtensionProvider("bad-locale", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider.BadLocaleError());
		pm.addExtensionProvider("bad-payload", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider.BadPayloadError());
		pm.addExtensionProvider("bad-sessionid", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider.BadSessionIDError());
		pm.addExtensionProvider("session-expired", "http://jabber.org/protocol/commands", new AdHocCommandDataProvider.SessionExpiredError());
	}

	class PresenceTypeFilter implements PacketFilter {

	    private final Presence.Type type;

	    public PresenceTypeFilter(Presence.Type type) {
	        this.type = type;
	    }

	    public boolean accept(Packet packet) {
	        if (!(packet instanceof Presence)) {
	            return false;
	        }
	        else {
	            return ((Presence) packet).getType().equals(this.type);
	        }
	    }
	}
}

