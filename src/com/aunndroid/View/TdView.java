package com.aunndroid.View;


import com.aunndroid.Engine.BlockOps;
import com.aunndroid.Engine.Game;
import com.aunndroid.TetradsDropLite.R;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.Canvas;
//import android.hardware.Sensor;
//import android.hardware.SensorEvent;
//import android.hardware.SensorEventListener;
//import android.hardware.SensorManager;

public class TdView extends View { //implements SensorEventListener{

	private static final String TAG = "TetradsDrop : TdView :";
	private static final String VIEW_STATE="viewState";

	private Context mContext;
	private BlockOps mBlockOps;
	private Resources res;
	private Bitmap bmp1;
	private Bitmap bmp2;
	private Bitmap bmp3;
	private Bitmap bmp4;
	private Bitmap bmp5;
	private Bitmap bmp6;
	private Bitmap bmp7;

/*	private float mOrientationValues[] = new float[3];
	private float mAccX[]=new float[4];
	private float mAccY[]=new float[4];
	private float mAccZ[]=new float[4];
	private int dirX=0;
	private int dirY=0;
	private int dirZ=0;
*/
	private int screenWidth;
	private int screenHeight;
	private int blockLength=0;

	private int fieldHeight;
	private int fieldWidth;
	private int fieldX0;
	private int fieldY0;
	private int fieldX1;
	private int fieldY1;
	private int blockX0;
	private int blockY0;

	private final Rect dirtyRect=new Rect();
	private boolean draw=false;
	private boolean mSend=false;
	private boolean mCollaborate=false;
	private int width;
	private int height;
	private int mMotionEvent;
	private int origY;
	private int origBlock;
	private int origX;
//	private long lastSensorUpdate = -1;
	private int orgViewWidth;
	private int orgViewHeight;
	private static final int rLoopTimer=0;
	private static final boolean DEBUG=false;
	private static final boolean VERBOSE=false;
	private static final boolean TEMP=false;
	private static final boolean RMV=false;

	public TdView(Context context) {
		super(context);
		mContext=context;
		if(DEBUG)
			Log.d(TAG,"TdView(Context context)");
	}

	public TdView(Context context,AttributeSet attrs){ //This one is needed to be inflated from the XML file.
		super(context,attrs);
		mContext=context;
		if(DEBUG)
			Log.d(TAG,"TdView(Context context,AttributeSet attrs)");
		mBlockOps=BlockOps.getInstanceOf();
		setFocusable(true);
	}

	@Override
	protected Parcelable onSaveInstanceState(){
		Parcelable p=super.onSaveInstanceState();
		if(DEBUG)
			Log.d(TAG,"onSaveInstanceState");

		Bundle bundle=new Bundle();
		bundle.putParcelable(VIEW_STATE, p);
		return bundle;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state){
		if(DEBUG)
			Log.d(TAG,"onRestoreInstanceState");
		Bundle bundle=(Bundle)state;
		super.onRestoreInstanceState(bundle.getParcelable(VIEW_STATE)); //(Game)Activity will call onRestoreInstance() state for View too.
	}

	public void doHandlerPost(int r){
		switch(r){
		case rLoopTimer:
			break;
		default:
			break;
		}
	}

	@Override
	protected void onDraw(Canvas canvas){
		if(VERBOSE)
			Log.d(TAG,"onDraw");

//		if(!settingSet)
//			init();
		//Draw the background
		Paint background=new Paint();
		background.setColor(getResources().getColor(R.color.pzl_background));
//		background.setStrokeWidth(4);
		canvas.drawRect(0,0,screenWidth,screenHeight,background);
//		canvas.drawRect(0,0,canvasLength,canvasLength,background);

		//Draw the Board
		//Define color for the grid lines
		Paint dark=new Paint();
		//dark.setColor(getResources().getColor(R.color.pzl_dark));
		dark.setColor(getResources().getColor(R.color.pzl_foreground));
		dark.setStrokeWidth(3);

		Paint hilite=new Paint();
		hilite.setColor(getResources().getColor(R.color.pzl_selected));
		hilite.setStrokeWidth(4);

		Paint border =new Paint();
		border.setColor(getResources().getColor(R.color.solid_royal_blue));
		border.setStrokeWidth(4);

		canvas.drawLine(4, fieldY0, fieldX1, fieldY0, border);	//Top
		canvas.drawLine(fieldX0, 4, fieldX0, fieldY1, border);	//Left
		canvas.drawLine(4, fieldY1, fieldX1+2, fieldY1, border);	//Bottom
		canvas.drawLine(fieldX1, 4, fieldX1, fieldY1+2, border);	//Right

		for(int x=0;x<11;x++ ){
			for(int y=0;y<20;y++){
				switch(mBlockOps.getField(x, y)){
				case 0:
					break;
				case 1:
					canvas.drawBitmap(bmp1, x*blockLength+blockX0,y*blockLength+blockY0, null);
					break;
				case 2:
					canvas.drawBitmap(bmp2, x*blockLength+blockX0,y*blockLength+blockY0, null);
					break;
				case 3:
					canvas.drawBitmap(bmp3, x*blockLength+blockX0,y*blockLength+blockY0, null);
					break;
				case 4:
					canvas.drawBitmap(bmp4, x*blockLength+blockX0,y*blockLength+blockY0, null);
					break;
				case 5:
					canvas.drawBitmap(bmp5, x*blockLength+blockX0,y*blockLength+blockY0, null);
					break;
				case 6:
					canvas.drawBitmap(bmp6, x*blockLength+blockX0,y*blockLength+blockY0,null);
					break;
				case 7:
					canvas.drawBitmap(bmp7, x*blockLength+blockX0,y*blockLength+blockY0, null);
					break;
				default:
					break;
				}
			}
		}

//		canvas.drawRect(dirtyRect, hilite);

		int yb0=BlockOps.getBlocky();
		int xb0=BlockOps.getBlockx();
		int blocknum=BlockOps.getBlocknum();
		int orient=BlockOps.getBlockorient();


		if(VERBOSE)
			Log.d(TAG,"doDraw xb0:"+xb0);

		for(int x=0;x<=4;x++ ){
			for(int y=0;y<=4;y++){
				switch(mBlockOps.getBlocksField(x, y,blocknum,orient)){
				case 0:
//					canvas.drawBitmap(bmp1, (xb0+x)*blockLength+blockX0,(yb0+y)*blockLength+blockY0, hilite);
					break;
				case 1:
					canvas.drawBitmap(bmp1, (xb0+x)*blockLength+blockX0,(yb0+y)*blockLength+blockY0, null);
					break;
				case 2:
					canvas.drawBitmap(bmp2, (xb0+x)*blockLength+blockX0,(yb0+y)*blockLength+blockY0, null);
					break;
				case 3:
					canvas.drawBitmap(bmp3, (xb0+x)*blockLength+blockX0,(yb0+y)*blockLength+blockY0, null);
					break;
				case 4:
					canvas.drawBitmap(bmp4, (xb0+x)*blockLength+blockX0,(yb0+y)*blockLength+blockY0, null);
					break;
				case 5:
					canvas.drawBitmap(bmp5, (xb0+x)*blockLength+blockX0,(yb0+y)*blockLength+blockY0, null);
					break;
				case 6:
					canvas.drawBitmap(bmp6, (xb0+x)*blockLength+blockX0,(yb0+y)*blockLength+blockY0, null);
					break;
				case 7:
					canvas.drawBitmap(bmp7, (xb0+x)*blockLength+blockX0,(yb0+y)*blockLength+blockY0, null);
					break;
				default:
					break;
				}
			}
		}
	}

	void init(){
		if(DEBUG)
			Log.d(TAG,"init : ");
		 screenWidth=getWidth();
		 screenHeight=getHeight();
//		 canvasLength=Math.max(screenWidth, screenHeight);

//		if(TEMP)
//			Log.d(TAG,"blockLength :"+blockLength+" fieldHeight :"+fieldHeight+" fieldWidth :"+fieldWidth);

		bmp1=BitmapFactory.decodeResource(res, R.drawable.cyan1);
		bmp2=BitmapFactory.decodeResource(res, R.drawable.yello2);
		bmp3=BitmapFactory.decodeResource(res, R.drawable.darkblue3);
		bmp4=BitmapFactory.decodeResource(res, R.drawable.orange4);
		bmp5=BitmapFactory.decodeResource(res, R.drawable.red5);
		bmp6=BitmapFactory.decodeResource(res, R.drawable.green6);
		bmp7=BitmapFactory.decodeResource(res, R.drawable.violet7);

		bmp1=Bitmap.createScaledBitmap(bmp1, blockLength, blockLength, true);
		bmp2=Bitmap.createScaledBitmap(bmp2, blockLength, blockLength, true);
		bmp3=Bitmap.createScaledBitmap(bmp3, blockLength, blockLength, true);
		bmp4=Bitmap.createScaledBitmap(bmp4, blockLength, blockLength, true);
		bmp5=Bitmap.createScaledBitmap(bmp5, blockLength, blockLength, true);
		bmp6=Bitmap.createScaledBitmap(bmp6, blockLength, blockLength, true);
		bmp7=Bitmap.createScaledBitmap(bmp7, blockLength, blockLength, true);
//		settingSet=true;
	}

	@Override
	protected void onSizeChanged(int w,int h,int oldw,int oldh){
		if(DEBUG)
			Log.d(TAG,"onSizeChanged w:"+w+" h:"+h+" oldw:"+oldw+" oldh:"+oldh);
		int viewHeight=Math.max(w,h);
		blockLength=(viewHeight-12)/20;
		calculateField(blockLength);
		init();
		super.onSizeChanged(w,h,oldw,oldh);
//		if(TEMP)			Log.d(TAG,"onSizeChanged W:"+ (fieldX1+2) +" H:"+(fieldY1+2)+" bl:"+blockLength);
	}

	/** @see android.view.View#measure(int, int)
	 *
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if(DEBUG)
			Log.d(TAG,"onMeasure W:"+ widthMeasureSpec +" H:"+heightMeasureSpec);
//		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int viewHeight=Math.max(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
		int viewWidth=Math.min(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
		if(TEMP)				Log.d(TAG,"onMeasure vW:"+ viewWidth +" vH:"+viewHeight+" bl:"+blockLength);

		if(DEBUG)
			Log.d(TAG,"onMeasure orgViewWidth:"+ orgViewWidth +" viewWidth:"+viewWidth+" orgViewHeight:"+orgViewHeight+" viewHeight:"+viewHeight);
		if(orgViewWidth!=viewWidth){
			orgViewWidth=viewWidth;
		}
		if(orgViewHeight!=viewHeight){
			orgViewHeight=viewHeight;
		}
		calculateView(viewWidth);
		setMeasuredDimension(width,height);

//		if(TEMP)				Log.d(TAG,"onMeasure W:"+ width +" H:"+height+" bl:"+blockLength);
	}

	/**
	 * calculateView is important and is confusing because onMeasure is called repeatedly when the screen get rotated.
	 * onMeasure is always called twice for View creation. onConfigChange access View two time thus triggering onMeasure
	 * 4 times with different view width. calculateView must provide all these calls with consistent value.
	 * */
	private void calculateView(int viewWidth) {
		if(DEBUG)
			Log.d(TAG,"calculateView :");
		res=this.getResources();
		int screenOrientation=res.getConfiguration().orientation;

		switch (screenOrientation){
		case Configuration.ORIENTATION_PORTRAIT:
			if(blockLength!=0){
				int tempWidth=blockLength*16+12;
//				if(TEMP)Log.d(TAG,"inside ORIENTATION_PORTRAIT same vW:"+viewWidth+" tW:"+tempWidth);
				if(tempWidth<viewWidth){
					if(tempWidth*2>viewWidth){
						blockLength=(viewWidth-12)/16;
//						if(TEMP)	Log.d(TAG,"inside ORIENTATION_PORTRAIT 16 <<< vW:"+viewWidth+" tW:"+tempWidth+" bl:"+blockLength);
					}
				}else{
					blockLength=(viewWidth-12)/11;
//					if(TEMP)	Log.d(TAG,"inside ORIENTATION_PORTRAIT 11 >>> vW:"+viewWidth+" tW:"+tempWidth+" bl:"+blockLength);
				}
			}else{
				blockLength=(viewWidth-12)/16;
//				if(TEMP)	Log.d(TAG,"inside ORIENTATION_PORTRAIT BLOCK==0 16 bl:"+blockLength);
			}
//			if(TEMP)				Log.d(TAG,"calculateView PortraitvW:"+viewWidth+" bl:"+blockLength);
			break;
		case Configuration.ORIENTATION_LANDSCAPE:
			if(blockLength!=0){
				int tempWidth=blockLength*20+12;
//				if(TEMP)Log.d(TAG,"inside ORIENTATION_LANDSCAPE same vW:"+viewWidth+" tW:"+tempWidth);
				int diff;
				if(viewWidth<tempWidth){
					diff=tempWidth-viewWidth;
					/**
					 * This is the case where onConfigChange is being called and the measurement are still from portrait
					 * even though the orientation is changed to Landscape. diff will be bigger than blockLength*1.5 thus
					 * it is calculated as portrait mode
					 * */
					if(diff>blockLength*1.5&&tempWidth*2>viewWidth){//diff>blockLength*1.5){
					//	blockLength=(viewWidth-12)/20;
//						if(TEMP)Log.d(TAG,"calculateView Landscape bl:"+blockLength+" diff:"+diff);
						blockLength=(viewWidth-12)/11;
					}
					/**
					*Now the measurement are in Landscape mode and diff will be less than blockLength*1.5, calculation is
					*done for landscape mode.
					*/
					else{
						blockLength=(viewWidth-12)/20;
//						if(TEMP)Log.d(TAG,"calculateView Landscape vW:"+viewWidth+" bl:"+blockLength);
					}

				}
				/**
				 * I have not seen this condition triggered. Initially I thought that a few extra pixels at the bottom
				 *  might cause a problem and this condition has been set up.
				 * */
				else{
					diff=viewWidth-tempWidth;
//					if(TEMP)Log.d(TAG,"inside ORIENTATION_LANDSCAPE diff:"+diff);
					if(diff<blockLength*1.5){
					//	blockLength=(viewWidth-12)/11;
					}
				}
			}else{
				/**
				 * The very beginning landscape setup calculation.
				 * */
				blockLength=(viewWidth-12)/20;
//				if(TEMP)Log.d(TAG,"calculateView Landscape vW:"+viewWidth+" bl:"+blockLength);
			}
			break;
		case Configuration.ORIENTATION_SQUARE:
			if(blockLength!=0){
				int tempWidth=blockLength*20+12;
				if(viewWidth-tempWidth<blockLength){
//					if(TEMP)Log.d(TAG,"inside ORIENTATION_LANDSCAPE same bl:"+blockLength);
				//	blockLength=(viewWidth-12)/11;
				}
			}else{
				blockLength=(viewWidth-12)/20;
			}
			if(VERBOSE)				Log.d(TAG,"calculateView SQUARE");
			break;
		case Configuration.ORIENTATION_UNDEFINED:
			if(blockLength!=0){
				int tempWidth=blockLength*20+12;
				if(viewWidth-tempWidth<blockLength){
//					if(TEMP)Log.d(TAG,"inside ORIENTATION_LANDSCAPE same bl:"+blockLength);
				//	blockLength=(viewWidth-12)/11;
				}
			}else{
				blockLength=(viewWidth-12)/20;
			}
			if(VERBOSE)				Log.d(TAG,"calculateView Undefined");
			break;
		default:
			if(blockLength!=0){
				int tempWidth=blockLength*20+12;
				if(viewWidth-tempWidth<blockLength){
//					if(TEMP)Log.d(TAG,"inside ORIENTATION_LANDSCAPE same bl:"+blockLength);
				//	blockLength=(viewWidth-12)/11;
				}
			}else{
				blockLength=(viewWidth-12)/20;
			}
			break;
		}
		calculateField(blockLength);
		width=fieldX1+2;
		height=fieldY1+2;
	}

	private void calculateField(int blockLength) {
		fieldHeight=blockLength*20;
		fieldWidth=blockLength*11;
		fieldX0=6;
		fieldY0=6;
		fieldX1=fieldWidth+fieldX0+4;
		fieldY1=fieldHeight+fieldY0+4;
		blockX0=fieldX0+2;
		blockY0=fieldY0+2;
	}
// -2-4-6
	private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text
//            result = (int) mTextPaint.measureText(mText) + getPaddingLeft()+ getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    /**
     * Determines the height of this view
     * @param measureSpec A measureSpec packed into an int
     * @return The height of the view, honoring constraints from measureSpec
     */
    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

//        mAscent = (int) mTextPaint.ascent();
        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text (beware: ascent is a negative number)
//            result = (int) (-mAscent + mTextPaint.descent()) + getPaddingTop()+ getPaddingBottom();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
    	if(DEBUG)
    		Log.d(TAG, "onKeyDown: keycode=" + keyCode+", event="+event);
    	if(!dPadPressed(keyCode))
    		super.onKeyDown(keyCode, event);
    	return true;
    }

    public boolean dPadPressed(int keyCode){
    	if(!mBlockOps.isSolidifying())
    		switch(keyCode){
    		case KeyEvent.KEYCODE_DPAD_UP:
    			if(TEMP)Log.d(TAG,"KEYCODE_DPAD_UP");
    			if(!mCollaborate){
    				if(((Game)mContext).ismRunning()){
	    				mBlockOps.tetradBlockRotate(-1);
	    				setRectForInvalidate(0,0);
    				}
    			}
    			else if(mSend){
    				//x=0,y=1,orient=2
    				((Game)mContext).sendXYOrient(2,-1);
    				mBlockOps.tetradBlockRotate(-1);
    				setRectForInvalidate(0,0);
    			}
    			break;
    		case KeyEvent.KEYCODE_DPAD_DOWN:
    			if(TEMP)Log.d(TAG,"KEYCODE_DPAD_DOWN");
    			if(!mCollaborate){
    				if(((Game)mContext).ismRunning()){
	    				mBlockOps.tetradBlockDown();
	    				setRectForInvalidate(0,1);
    				}
    			}
    			else if(mSend){
    				//x=0,y=1,orient=2
    				((Game)mContext).sendXYOrient(1,1);
    				mBlockOps.tetradBlockDown();
    				setRectForInvalidate(0,1);
    			}
    			break;
    		case KeyEvent.KEYCODE_DPAD_LEFT:
    			if(TEMP)Log.d(TAG,"KEYCODE_DPAD_LEFT");
    			if(!mCollaborate){
    				if(((Game)mContext).ismRunning()){
	    				mBlockOps.tetradBlockMove(-1);
	    				setRectForInvalidate(-1,0);
    				}
    			}
    			else if(mSend){
    				//x=0,y=1,orient=2
    				((Game)mContext).sendXYOrient(0,-1);
    				mBlockOps.tetradBlockMove(-1);
    				setRectForInvalidate(-1,0);
    			}
    			break;
    		case KeyEvent.KEYCODE_DPAD_RIGHT:
    			if(TEMP)Log.d(TAG,"KEYCODE_DPAD_RIGHT");
    			if(!mCollaborate){
    				if(((Game)mContext).ismRunning()){
	    				mBlockOps.tetradBlockMove(1);
	    				setRectForInvalidate(1,0);
	    			}
    			}
    			else if(mSend){
    				//x=0,y=1,orient=2
    				((Game)mContext).sendXYOrient(0,1);
    				mBlockOps.tetradBlockMove(1);
    				setRectForInvalidate(1,0);
    			}
    			break;
    		case KeyEvent.KEYCODE_DPAD_CENTER:
    			if(TEMP)Log.d(TAG,"KEYCODE_DPAD_CENTER");
    			if(!mCollaborate){
    				if(((Game)mContext).ismRunning()){
	    				mBlockOps.tetradBlockRotate(1);
	    				setRectForInvalidate(0,0);
    				}
    			}
    			else if(mSend){//x=0,y=1,orient=2
    				((Game)mContext).sendXYOrient(2,1);
    				mBlockOps.tetradBlockRotate(1);
    				setRectForInvalidate(0,0);
    			}
    			break;
/*
			case KeyEvent.KEYCODE_MENU:
				((Game)mContext).chatShow();
				break;
*/
    		default:
    			return false;
    		}
    	return true;
    }

	@Override
	public boolean onTouchEvent(MotionEvent event){
		if(TEMP)
			Log.d(TAG,"onTouchEvent");

        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
            	if(isCoordinateInRange(event.getX(), event.getY())){
            		mMotionEvent=MotionEvent.ACTION_DOWN;
            		origBlock=BlockOps.getBlocknum();
            	}
                origY=BlockOps.getBlocky();
                origX=BlockOps.getBlockx();
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
            	if(mMotionEvent==MotionEvent.ACTION_DOWN){
     //       		mMotionEvent=MotionEvent.ACTION_MOVE;
            		if(origBlock==BlockOps.getBlocknum())
            			blockMove(event.getX(), event.getY());
            		return true;
//            	mTileView.dragTile(event.getX(), event.getY());
            	}
            	return false;
            }
            case MotionEvent.ACTION_UP: {
            	if(Math.abs(BlockOps.getBlocky()-origY)<=1&& Math.abs(origX-BlockOps.getBlockx())<=1)
            		if(origBlock==BlockOps.getBlocknum())
            			rotateBlock(event.getX());
            	mMotionEvent=MotionEvent.ACTION_UP;
            	return true;
            }
            }
        return false;
	}

	private void blockMove(float x, float y) {
		if(DEBUG)
			Log.d(TAG,"blockMove x:"+x+" y:"+y);
		int yb1=(int) ((BlockOps.getBlocky()-1)*blockLength+blockY0)+blockLength*6;
		int xb0=(BlockOps.getBlockx())*blockLength+blockX0;
		int xb1=xb0+blockLength*6;
		if(TEMP)
			Log.d(TAG,"blockMove x:"+x+" xb0:"+xb0+" y:"+y+" yb1:"+yb1);

		if(y>yb1){
			if(!mCollaborate){
				if(((Game)mContext).ismRunning()){
					mBlockOps.tetradBlockDown();
					setRectForInvalidate(0,1);
				}
			}
			else if(mSend){//x=0,y=1,orient=2
				((Game)mContext).sendXYOrient(1,1);
				mBlockOps.tetradBlockDown();
				setRectForInvalidate(0,1);
			}
		}
		if(x>xb1){
			if(!mCollaborate){
				if(((Game)mContext).ismRunning()){
					mBlockOps.tetradBlockMove(1);
					setRectForInvalidate(1,0);
				}
			}
			else if(mSend){//x=0,y=1,orient=2
				((Game)mContext).sendXYOrient(0,1);
				mBlockOps.tetradBlockMove(1);
				setRectForInvalidate(1,0);
			}
		}else if(x<=xb0){
			if(!mCollaborate){
				if(((Game)mContext).ismRunning()){
					mBlockOps.tetradBlockMove(-1);
					setRectForInvalidate(-1,0);
				}
			}
			else if(mSend){//x=0,y=1,orient=2
				((Game)mContext).sendXYOrient(0,-1);
				mBlockOps.tetradBlockMove(-1);
				setRectForInvalidate(-1,0);
			}
		}else if(x<=blockX0){
			if(!mCollaborate){
				if(((Game)mContext).ismRunning()){
					mBlockOps.tetradBlockMove(-1);
					setRectForInvalidate(-1,0);
				}
			}
			else if(mSend){//x=0,y=1,orient=2
				((Game)mContext).sendXYOrient(0,-1);
				mBlockOps.tetradBlockMove(-1);
				setRectForInvalidate(-1,0);
			}
		}
	}

	private void rotateBlock(float x) {
		if(DEBUG)
			Log.d(TAG,"rotateBlock x:"+x);
		int xMid=blockX0+blockLength*3;
		if(TEMP)
			Log.d(TAG,"rotateBlock x:"+x+" xMid:"+xMid);
		if(x<=xMid){
			if(!mCollaborate){
				if(((Game)mContext).ismRunning()){
					mBlockOps.tetradBlockRotate(-1);
				}
			}
			else if(mSend){//x=0,y=1,orient=2
				((Game)mContext).sendXYOrient(2,-1);
				mBlockOps.tetradBlockRotate(-1);
			}
		}else if(x>xMid){
			if(!mCollaborate){
				if(((Game)mContext).ismRunning()){
					mBlockOps.tetradBlockRotate(1);
				}
			}
			else if(mSend){//x=0,y=1,orient=2
				((Game)mContext).sendXYOrient(2,1);
				mBlockOps.tetradBlockRotate(1);
			}
		}
		setRectForInvalidate(0,0);
	}

	private boolean isCoordinateInRange(float x, float y) {
		int yb0=(int) ((BlockOps.getBlocky()-1)*blockLength+blockY0);
		int xb0=(BlockOps.getBlockx()-1)*blockLength+blockX0;

		if(TEMP)
			Log.d(TAG,"isCoordinateInRange x:"+x+" xb0:"+xb0+" y:"+y+" yb0:"+yb0);

		if(xb0<x && x<xb0+blockLength*6){
			if(yb0<y && y<yb0+blockLength*5)
				return true;
		}
		return false;
	}

	public void updateSelXY(int XY) {
		if(DEBUG)
			Log.d(TAG,"updateSelXY");
	}

	public BlockOps getmBlockOps() {
		return mBlockOps;
	}

	public void setmBlockOps(BlockOps mBlockOps) {
		this.mBlockOps = mBlockOps;
	}

    public boolean isDraw() {
		return draw;
	}

	public void setDraw(boolean draw) {
		this.draw = draw;
	}

	public void setRectForInvalidate(int x, int y) {
		int yb0=(BlockOps.getBlocky()-y-1)*blockLength+blockY0;
		int xb0=BlockOps.getBlockx();

//		dirtyRect.set(0, yb0, 20*blockLength, yb0+6*blockLength);
//		invalidate(dirtyRect);

		if(xb0<=2){
			xb0=blockX0;
			dirtyRect.set(xb0, yb0, xb0+10*blockLength, yb0+6*blockLength);
			invalidate(dirtyRect);
		}else if(x>=0){
			xb0=(xb0-x-1)*blockLength+blockX0;
			dirtyRect.set(xb0, yb0, xb0+10*blockLength, yb0+6*blockLength);
			invalidate(dirtyRect);
		}else{
			if(RMV)
				Log.d(TAG," setRectForInvalidate xb0:"+xb0+" :"+(xb0-x+1));
			xb0=(xb0-1)*blockLength+blockX0;
			dirtyRect.set(xb0, yb0, xb0+10*blockLength, yb0+6*blockLength);
			invalidate(dirtyRect);
		}

	}

/*	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if(!((Game)(mContext)).ismRunning())
			return;
		int type=event.sensor.getType();
		switch(type){
		case Sensor.TYPE_ORIENTATION:
			for (int i=0 ; i<3 ; i++) {
				mOrientationValues[i]=event.values[i];
			}
			Log.d(TAG,"Azimuth:"+mOrientationValues[0]);
			Log.d(TAG,"Pitch:"+mOrientationValues[1]);
			Log.d(TAG,"Roll:"+mOrientationValues[2]);
			break;
		case Sensor.TYPE_ACCELEROMETER:
			Log.d(TAG,": :");
			Log.d(TAG,"ACCELEROMETER X:"+event.values[0]);
			Log.d(TAG,"ACCELEROMETER Y:"+event.values[1]);
			Log.d(TAG,"ACCELEROMETER Z:"+event.values[2]);
			for(int i=0;i<3;i++){
				mAccX[3-i]=mAccX[3-i-1];
				mAccY[3-i]=mAccY[3-i-1];
				mAccZ[3-i]=mAccZ[3-i-1];
			}
			mAccX[0]=event.values[0];
			mAccY[0]=event.values[1];
			mAccZ[0]=event.values[2];
//			Log.d(TAG," "+mAccX[0]+" "+mAccX[1]+" "+mAccX[2]+" "+mAccX[3]+" "+" ");
			int motionX=0;
			int motionY=0;
			int motionZ=0;
			for(int i=0;i<3;i++){
				if(mAccX[i+1]>mAccX[i])
					motionX++;
				else if(mAccX[i+1]<mAccX[i])
					motionX--;

				if(mAccY[i+1]>mAccY[i])
					motionY++;
				else if (mAccY[i+1]<mAccY[i])
					motionY--;

				float diffZ=mAccZ[i+1]-mAccZ[i];
				if(Math.abs(diffZ)>=1)
					if(diffZ>=0)
						motionZ++;
					else
						motionZ--;
			}

//			Log.d(TAG,"Motion X:"+motionX+" Y:"+motionY+" Z:"+motionZ);
//			Log.d(TAG,"Dir X:"+dirX+" Y:"+dirY+" Z:"+dirZ);

			if(Math.abs(motionZ)>=2)
				if(motionZ>0)
					dirZ++;
				else
					dirZ--;

			if(Math.abs(dirZ)>=2){
				if(dirZ>0){
					Log.d(TAG,"Rotate Right");
	    			if(!mCollaborate){
	    				if(((Game)mContext).ismRunning()){
		    				mBlockOps.tetradBlockRotate(1);
		    				setRectForInvalidate(0,0);
	    				}
	    			}
	    			else if(mSend){//x=0,y=1,orient=2
	    				((Game)mContext).sendXYOrient(2,1);
	    				mBlockOps.tetradBlockRotate(1);
	    				setRectForInvalidate(0,0);
	    			}
	    			dirZ=1;
				}
				else{
					Log.d(TAG,"Rotate Left");
	    			if(!mCollaborate){
	    				if(((Game)mContext).ismRunning()){
		    				mBlockOps.tetradBlockRotate(-1);
		    				setRectForInvalidate(0,0);
	    				}
	    			}
	    			else if(mSend){
	    				//x=0,y=1,orient=2
	    				((Game)mContext).sendXYOrient(2,-1);
	    				mBlockOps.tetradBlockRotate(-1);
	    				setRectForInvalidate(0,0);
	    			}
	    			dirZ=-1;
				}
			//	dirZ=0;
				break;
			}

			long curTime = System.currentTimeMillis();
			// only allow one update every 50ms, otherwise updates
			// come way too fast
			if (!(lastSensorUpdate == -1 || (curTime - lastSensorUpdate) > 75)) {
				return;

			}
				lastSensorUpdate = curTime;

			if(Math.abs(motionX)>=1)
				if(motionX>0)
					dirX++;
				else
					dirX--;

			if(Math.abs(dirX)>=7&&Math.abs(dirX)<11){
				if(dirX>0){
					Log.d(TAG,"Moving Right");
	    			if(!mCollaborate){
	    				if(((Game)mContext).ismRunning()){
		    				mBlockOps.tetradBlockMove(1);
		    				setRectForInvalidate(1,0);
		    			}
	    			}
	    			else if(mSend){
	    				//x=0,y=1,orient=2
	    				((Game)mContext).sendXYOrient(0,1);
	    				mBlockOps.tetradBlockMove(1);
	    				setRectForInvalidate(1,0);
	    			}
	    			dirX=5;
				}
				else{
					Log.d(TAG,"Moving Left");
	    			if(!mCollaborate){
	    				if(((Game)mContext).ismRunning()){
		    				mBlockOps.tetradBlockMove(-1);
		    				setRectForInvalidate(-1,0);
	    				}
	    			}
	    			else if(mSend){
	    				//x=0,y=1,orient=2
	    				((Game)mContext).sendXYOrient(0,-1);
	    				mBlockOps.tetradBlockMove(-1);
	    				setRectForInvalidate(-1,0);
	    			}
	    			dirX=-5;
				}
			//	dirX=0;
				break;
			}

			if(Math.abs(motionY)>=1)
				if(motionY>0)
					dirY++;
				else
					dirY--;

			if(Math.abs(dirY)>=11){
				if(dirY>0){
					Log.d(TAG,"Moving up");
					dirY=0;
				}
				else{
					Log.d(TAG,"Moving down");
	    			if(!mCollaborate){
	    				if(((Game)mContext).ismRunning()){
		    				mBlockOps.tetradBlockDown();
		    				setRectForInvalidate(0,1);
	    				}
	    			}
	    			else if(mSend){
	    				//x=0,y=1,orient=2
	    				((Game)mContext).sendXYOrient(1,1);
	    				mBlockOps.tetradBlockDown();
	    				setRectForInvalidate(0,1);
	    			}
	    			dirY=-3;
				}
		//		dirY=0;
				break;
			}
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			break;
		default :
			break;
		}
	}
*/
	public void setSent(boolean b) {
		mSend=b;	}

	public int getFieldHeight() {
		return fieldHeight;
	}

	public void setFieldHeight(int fieldHeight) {
		this.fieldHeight = fieldHeight;
	}

	public int getFieldWidth() {
		return fieldWidth;
	}

	public void setFieldWidth(int fieldWidth) {
		this.fieldWidth = fieldWidth;
	}

	public boolean ismCollaborate() {
		return mCollaborate;
	}

	public void setmCollaborate(boolean mCollaborate) {
		this.mCollaborate = mCollaborate;
	}
}
