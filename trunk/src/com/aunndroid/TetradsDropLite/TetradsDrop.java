/*
Copyright (c) 2007 Win Myo Htet <wmhtet@aunndroid.com>
          
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.
*/

package com.aunndroid.TetradsDropLite;


import java.util.Locale;

import com.aunndroid.Engine.Game;
import com.aunndroid.Tolking.ConstantInterface;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class TetradsDrop extends Activity implements OnClickListener, ConstantInterface{
    private static final String TAG = "TetradsDrop : TetradsDrop";
    private static final boolean DEBUG=false;
	private static final boolean TEMP=false;
	private static int screenDimension=-1;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Eula.show(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        View playButton = this.findViewById(R.id.play_button);
        playButton.setOnClickListener(this);
        View aboutButton=this.findViewById(R.id.about_button);
        aboutButton.setOnClickListener(this);
        View exitButton=this.findViewById(R.id.exit_button);
        exitButton.setOnClickListener(this);

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

		switch(screenDimension){
		case QVGA:
			setImage(min,max-20,4);
			break;
		case HVGA:
			setImage(min,max-20,1);
			break;
		case WVGA854:
			setImage(min,max-20,1);
			break;
		case WVGA800:
			setImage(min,max-20,1);
			break;
		default:
			setImage(min,max-20,1);
			break;
		}
    }

    
    private void setImage(int min, int max, int i) {
    	if(DEBUG)
    		Log.d(TAG,"setImage "+min+" "+max+" "+i);
		Resources res=this.getResources();
		ImageView imageViewBg=(ImageView) this.findViewById(R.id.ImageViewBG);
		Bitmap bg=BitmapFactory.decodeResource(res, R.drawable.bgp);
		bg=Bitmap.createScaledBitmap(bg, min, max, true);
		imageViewBg.setImageBitmap(bg);
		Bitmap qr=null;
		String country=this.getResources().getConfiguration().locale.getCountry();
		ImageView qrImageView=(ImageView) this.findViewById(R.id.ImageViewQR);

		if(i!=1){
			ImageView aunnImageView=(ImageView) this.findViewById(R.id.ImageViewAunn);
			Bitmap aunn=BitmapFactory.decodeResource(res, R.drawable.aunn);
			aunn=Bitmap.createScaledBitmap(aunn, min/i, min/i, true);
			aunnImageView.setImageBitmap(aunn);

			if(country.equalsIgnoreCase(Locale.CHINA.getCountry())){
				qr=BitmapFactory.decodeResource(res, R.drawable.wwwaunndroidcom);
			}else{
				qr=BitmapFactory.decodeResource(res, R.drawable.qrcode);
			}
			qr=Bitmap.createScaledBitmap(qr, min/i, min/i, true);
			qrImageView.setImageBitmap(qr);
		}else{
			if(country.equalsIgnoreCase(Locale.CHINA.getCountry())){
				qr=BitmapFactory.decodeResource(res, R.drawable.wwwaunndroidcom);
			}else{
				qr=BitmapFactory.decodeResource(res, R.drawable.qrcode);
			}
		//	qr=Bitmap.createScaledBitmap(qr, min/i, min/i, true);
			qrImageView.setImageBitmap(qr);
			
		}
		
	}

	public void onClick(View v){
    	switch (v.getId()){
    	case R.id.play_button:
    		startTetradsDrop();
    		break;
    	case R.id.about_button:
    		startAboutDialog();
    		break;
    	case R.id.exit_button:
//    		Log.d(TAG,"Before finish()");
    		finish();
    		System.gc();
    		android.os.Process.killProcess(android.os.Process.myPid());
//    		Log.d(TAG,"After finish()");
    		break;
    	}
    }
    private void startTetradsDrop(){
    	if(DEBUG)
    		Log.d(TAG, "At startTetradsDrop");
    	Intent intent=new Intent(this,Game.class);
    	startActivity(intent);
    }

    private void startAboutDialog(){
    	new AlertDialog.Builder(this).setTitle(R.string.about_label).setMessage(R.string.about_text).show();
    }

	public static void setScreenDimension(int screenDimension_) {
		screenDimension = screenDimension_;
	}

	public static int getScreenDimension() {
		return screenDimension;
	}
}
