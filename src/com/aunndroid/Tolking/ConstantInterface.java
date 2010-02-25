/*
Copyright (c) 2007 Win Myo Htet <wmhtet@aunndroid.com>
          
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.
*/

package com.aunndroid.Tolking;

public interface ConstantInterface {


	/*The absolute Minimum is -128. Once it goes minus, different clients handle it differently.
	 * So, it is only safe to use 0 where most of the clients agree upon how to respond to it.
	 */
	static int PRESENCE_PRIORITY_MIN=0;
	static int PORT=5222;

	/*
	 * Length
	static int SESSION_ID_LENGTH=4;
	static int ACK_LENGTH=8;
	static int ACCEPT_LENGTH=12;
	static int ALREADY_PLAYING_LENGTH=15;
	static int GAME_INVITE_LENGTH=28;
	static int INVITE_LENGTH=32;
	 * */
	//Runnable
	static int rSTART_DIALOG=0;
	static int rNO_RESPONSE_DIALOG= 1;
	static int rRECEIVE_ERROR= 2;
	static int rRECEIVE_MESSAGE= 3;
	static int rCONNECTION_ERROR_DIALOG=4;
	static int rFRIEND_CONNECTION_LOST=5;
	static int rAUTO_DISCONNECT=6;

	static int STARTER=0;
	static int FOLLOWER=1;

	static int QVGA=0;
	static int HVGA=1;
	static int WVGA800=2;
	static int WVGA854=3;
	static int WQVGA432=4;
	static int WQVGA400=5;
	static int SQUARE=6;

	//HandShake
	// Invite:AunnDroid:TetradsDrop:2705
	static int RESOURCE_ID_LENGTH=11;
	static int ZERO=0;
	static int SESSION_ID_LENGTH=4;
	static int INVITE_LENGTH=RESOURCE_ID_LENGTH+31;
//	static int GAME_INVITE_LENGTH=INVITE_LENGTH-SESSION_ID_LENGTH;
	static int IN_SECONDS=15000;
	static int IN_MINUTES=150000;
	static int MSG_DELAY_TIME=350;

	static int ACK_LENGTH=9;
	static int ACCEPT_LENGTH=13;
//	static int ALREADY_PLAYING_LENGTH=5;
	static int ACK_REQUEST_LENGTH=9;
	static int FILE_SIZE_LENGTH=9;

	//71828 18284 59045 23536 02874 71352 66249
/*	<string name="ACCEPT_CODE">QUNDRVBU</string> <!--ACCEPT -->
	<string name="CANCEL_CODE">Q0FOQ0VM</string>	<!-- CANCEL -->
	<string name="REQUEST_CODE">UkVRVUVTVA==</string>
	<string name="FILE_SIZE_CODE">RklMRV9TSVpF</string>
	<string name="ALREADY_PLAYING_CODE">QUxSRUFEWV9QTEFZSU5H</string>
	<string name="TOLKING_CODE">VE9MS0lORw==</string>
	<string name="ACK_CODE">QUNL</string>
	<string name="FRIEND_END_GAME_CODE">RU5EX0dBTUU=</string>
	<string name="CONFIRMATION_CODE">Q09ORklSTUFUSU9O</string>

	<string name="FRIEND_END_GAME_CODE">RU5EX0dBTUU=</string>
	<string name="MORE_CODE">TU9SRQ==</string>
	<string name="NO_MORE_CODE">Tk9fTU9SRQ==</string>
	<string name="YES_MORE_CODE">WUVTX01PUkU</string>
	<string name="THANKS_CODE">VEhBTktTICEhIQ==</string>
	<string name="CANCEL_GAME_CODE">Q0FOQ0VMX0dBTUU=</string>
	*/

	static int ACK_CODE= 66249;
	static int ACCEPT_CODE= 71828;
	static int ALREADY_PLAYING_CODE= 28740;
	static int CANCEL_CODE= 18284;
	static int REQUEST_CODE= 59045;
	static int FILE_SIZE_CODE= 23536;
	static int TOLKING_CODE= 71352;
	static int INVITE_CODE= 70258;
	static int TOLK_GAME_PLAY_CODE=18786;


	//Omega 56714 32904 09783 87299 99686 62210 35555
	static final int BLOCKS_CODE=56714;
	static final int X_CODE=32904;
	static final int Y_CODE=97830;
	static final int ORIENT_CODE=87299;
	static final int TURN_CHANGE_CODE=99686;
	static final int GAME_OVER_CODE=62210;
	static final int FRIEND_END_GAME_CODE=35555;
	// Pi 14159 26535 89793 23846 26433 83279 50288
	static final int INITIALIZATION_CODE =14159;
	static final int GAME_PLAY_CODE =26535;
	static final int REQUEST_OPTION_CODE=89793;
	static final int CONFIRMATION_CODE=23846;
	static final int COMPETE_CODE = 26433;
	static final int COLLABORATE_CODE =83279;
	static final int DELETE_LINE_CODE =50288;

	//e 71828 18284 59045 23536 02874 71352 66249
	static int MORE_CODE =71828;
	static int YES_CODE =18284;
	static int NO_CODE =59045;
	static int THANKS_CODE =23536;
	static int LITE_VERSION_CODE=28740;

	//setprop persist.sys.language zh_CN;setprop persist.sys.country CN;stop;sleep 5;start
	//Golden Ratio 61803 39887 49894 84820 45868 34365 63811
	static int CHINA_CODE=61803;
	static int TAIWAN_CODE=39887;

	static int FOUR_MINUTES=240000;
	static int FIVE_MINUTES=300000;
	static int TEN_MINUTES=600000;
}

