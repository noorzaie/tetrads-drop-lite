package com.aunndroid.View;

import com.aunndroid.Engine.BlockOps;
import com.aunndroid.Engine.Game;
import com.aunndroid.TetradsDropLite.R;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;

public class SView extends View{

	private static final String TAG = "TetradsDrop : SView :";
	private static final String VIEW_STATE ="viewState";


	private BlockOps mBlockOps;
	private Bitmap bmp1;
	private Bitmap bmp2;
	private Bitmap bmp3;
	private Bitmap bmp4;
	private Bitmap bmp5;
	private Bitmap bmp6;
	private Bitmap bmp7;
//	private Bitmap drop;
	private Bitmap dPad;
	private int x0;
	private int y0;
	private int blockLength=0;
	private Resources res;

	private final Rect dirtyRect=new Rect();
	private Context context;
	private int width;
	private TdView mTdView;
	private boolean waitTimeUp=true;
//	private int screenOrientation;

	protected static final int WAIT_TIME=250;
	private static final boolean DEBUG=false;
	private static final boolean VERBOSE=false;
	private static final boolean TEMP=false;

	public SView(Context context) {
		super(context);
		if(DEBUG)
			Log.d(TAG,"SView(Context context)");
		this.context=context;
	}

	public SView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if(DEBUG)
			Log.d(TAG,"SView(Context context, AttributeSet attrs)");
		this.context=context;
		mBlockOps=BlockOps.getInstanceOf();
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

	void init(){
		if(DEBUG)
			Log.d(TAG,"init");
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
//		drop=BitmapFactory.decodeResource(res, R.drawable.drop);
		dPad=BitmapFactory.decodeResource(res, R.drawable.dpad);


		bmp1=Bitmap.createScaledBitmap(bmp1, blockLength, blockLength, true);
		bmp2=Bitmap.createScaledBitmap(bmp2, blockLength, blockLength, true);
		bmp3=Bitmap.createScaledBitmap(bmp3, blockLength, blockLength, true);
		bmp4=Bitmap.createScaledBitmap(bmp4, blockLength, blockLength, true);
		bmp5=Bitmap.createScaledBitmap(bmp5, blockLength, blockLength, true);
		bmp6=Bitmap.createScaledBitmap(bmp6, blockLength, blockLength, true);
		bmp7=Bitmap.createScaledBitmap(bmp7, blockLength, blockLength, true);
//		drop=Bitmap.createScaledBitmap(drop, 4*blockLength, 4*blockLength, true);
		dPad=Bitmap.createScaledBitmap(dPad, 4*blockLength, 4*blockLength, true);
//		settingSet=true;
	}

	@Override
	protected void onDraw(Canvas canvas){
		if(VERBOSE)
			Log.d(TAG,"onDraw");

		//Draw the background
		Paint background=new Paint();
		background.setColor(getResources().getColor(R.color.pzl_background));
		background.setStrokeWidth(4);
	//	canvas.drawLine(0, 0, 77, 276, background);

		int blocknum=BlockOps.getNextBlockNum();
		int orient=BlockOps.getNextBlockOrient();
/*		if(blocknum==0)
			orient=1;
		else
			orient=0;
*/
		if(blocknum!=-1)
		for(int y=0;y<4;y++){
			for(int x=0;x<4;x++){
				switch(mBlockOps.getBlocksField(x, y,blocknum,orient)){
				case 0:
					break;
				case 1:
					canvas.drawBitmap(bmp1, (x)*blockLength+x0,(y)*blockLength+y0, null);
					break;
				case 2:
					canvas.drawBitmap(bmp2, (x)*blockLength+x0,(y)*blockLength+y0, null);
					break;
				case 3:
					canvas.drawBitmap(bmp3, (x)*blockLength+x0,(y)*blockLength+y0, null);
					break;
				case 4:
					canvas.drawBitmap(bmp4, (x)*blockLength+x0,(y)*blockLength+y0, null);
					break;
				case 5:
					canvas.drawBitmap(bmp5, (x)*blockLength+x0,(y)*blockLength+y0, null);
					break;
				case 6:
					canvas.drawBitmap(bmp6, (x)*blockLength+x0,(y)*blockLength+y0, null);
					break;
				case 7:
					canvas.drawBitmap(bmp7, (x)*blockLength+x0,(y)*blockLength+y0, null);
					break;
				default:
					break;
				}
			}
		}
		canvas.drawBitmap(dPad, blockLength/2,(4.5f)*blockLength, null);	
	      // Define color and style for numbers
	      Paint foreground = new Paint(Paint.ANTI_ALIAS_FLAG);
	      foreground.setColor(getResources().getColor(
	            R.color.pzl_hilite));
	      foreground.setStyle(Style.FILL);
	      foreground.setTextSize(blockLength * 0.65f);
	      foreground.setTextScaleX(blockLength / blockLength);
	      foreground.setTextAlign(Paint.Align.LEFT);

		String score=res.getString(R.string.SCORE);
		canvas.drawText(score+((Game)this.context).getGameScore(),4 , 8*blockLength+y0,  foreground);
//		canvas.drawText(""+((Game)this.context).getGameScore(),4 , 9*blockLength+y0,  foreground);
		String line=res.getString(R.string.LINES)+((Game)this.context).getLineClear();
		canvas.drawText(line, 4, 9*blockLength +y0 , foreground);
//		canvas.drawText(""+((Game)this.context).getLineClear(), 2*blockLength+4, 11*blockLength +y0 , foreground);
		String level=res.getString(R.string.LEVEL)+((Game)this.context).getGameLevel();
		canvas.drawText(level, 4, 10*blockLength +y0 , foreground);
//		canvas.drawText(""+((Game)this.context).getGameLevel(), 2*blockLength+4, 13*blockLength +y0 , foreground);
	}

	@Override
	protected void onSizeChanged(int w,int h,int oldw,int oldh){
		if(DEBUG)
			Log.d(TAG,"onSizeChanged");
//		int viewWidth=Math.min(w,h);
	//	if(TEMP) Log.d(TAG,"onSizeChanged W:"+ (viewWidth) +" H:"+((2*viewWidth)+(viewWidth/5))+" bl:"+blockLength);
		super.onSizeChanged(w,h,oldw,oldh);
	}


	private void calculateView(int viewWidth) {
		res=this.getResources();
		int screenOrientation=res.getConfiguration().orientation;
		switch (screenOrientation){
		case Configuration.ORIENTATION_PORTRAIT:
			if(blockLength!=0){
				int tempWidth=blockLength*5;
				if(tempWidth==viewWidth){
//					if(TEMP)Log.d(TAG,"inside ORIENTATION_PORTRAIT 5 bl:"+blockLength);
				}else if(tempWidth<viewWidth){
					blockLength=(viewWidth)/5;
				}else if(tempWidth>viewWidth){
					blockLength=(viewWidth)/5;
				}
			}else{
				blockLength=(viewWidth)/5;
			}
//			if(TEMP)Log.d(TAG,"inside ORIENTATION_PORTRAIT 5 vW:"+viewWidth+" bl:"+blockLength);
			break;
		case Configuration.ORIENTATION_LANDSCAPE:
			if(blockLength!=0){
				int tempWidth=(blockLength*5);
				if(tempWidth==viewWidth){
//					if(TEMP)Log.d(TAG,"inside ORIENTATION_LANDSCAPE same vW:"+viewWidth+" tW:"+tempWidth+" bl:"+blockLength);
				}else if(tempWidth<viewWidth){
					blockLength=(viewWidth-12)/20;
//					blockLength=(viewWidth)/5;
//					if(TEMP)Log.d(TAG,"inside ORIENTATION_LANDSCAPE bl:"+blockLength);
//					if(TEMP)Log.d(TAG,"inside ORIENTATION_LANDSCAPE <<<< vW:"+viewWidth+" tW:"+tempWidth+" bl:"+blockLength);
				}else if(tempWidth>viewWidth){
					blockLength=viewWidth/5;
//					if(TEMP)Log.d(TAG,"inside ORIENTATION_LANDSCAPE >>>> vW:"+viewWidth+" tW:"+tempWidth+" bl:"+blockLength);
				}
			}else{
				blockLength=(viewWidth-12)/20;
			}
//			if(TEMP)Log.d(TAG,"inside ORIENTATION_LANDSCAPE vW:"+viewWidth+" bl:"+blockLength);
			break;
		case Configuration.ORIENTATION_SQUARE:
			if(blockLength!=0){
				int tempWidth=(blockLength+12)/6;
				if(tempWidth==viewWidth){
//					if(TEMP)Log.d(TAG,"inside ORIENTATION_SQUARE5 bl:"+blockLength);
				}
			}else{
				blockLength=(viewWidth-12)/20;
			}
			break;
		case Configuration.ORIENTATION_UNDEFINED:
			if(blockLength!=0){
				int tempWidth=(blockLength+12)/6;
				if(tempWidth==viewWidth){
//					if(TEMP)Log.d(TAG,"inside ORIENTATION_UNDEFINED 5 bl:"+blockLength);
				}
			}else{
				blockLength=(viewWidth-12)/20;
			}
			break;
		default:
			if(blockLength!=0){
				int tempWidth=(blockLength+12)/6;
				if(tempWidth==viewWidth){
//					if(TEMP)Log.d(TAG,"inside ORIENTATION_ default bl:"+blockLength);
				}
			}else{
				blockLength=(viewWidth-12)/20;
			}
			break;
		}
		x0=0;
		y0=blockLength;
		init();
		width=5*blockLength;
		//			blockLength=0;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if(DEBUG)
			Log.d(TAG,"onMeasure W:"+ widthMeasureSpec +" H:"+heightMeasureSpec);

//		int viewHeight=Math.max(measureWidth(widthMeasureSpec),	measureHeight(heightMeasureSpec));
		int viewWidth=Math.min(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));

//		if(TEMP)				Log.d(TAG,"onMeasure vW:"+ viewWidth +" vH:"+viewHeight+" bl:"+blockLength);
		//	  	calculateBlockLength(viewWidth,0);
//		if(TEMP)Log.d(TAG,"onMeasure BEFORE W:"+ viewWidth +" viewHeight:"+ viewHeight+" bl:"+ blockLength);
		calculateView(viewWidth);
		viewWidth=width;
		setMeasuredDimension(viewWidth,(2*viewWidth)+(viewWidth/4));

//		if(TEMP)Log.d(TAG,"onMeasure AFTER W:"+ viewWidth +" H:"+((2*viewWidth)+(viewWidth/4))+ " bl:"+ blockLength);
	  }


	  private int measureWidth(int measureSpec) {
	      int result = 0;
	      int specMode = MeasureSpec.getMode(measureSpec);
	      int specSize = MeasureSpec.getSize(measureSpec);

	      if (specMode == MeasureSpec.EXACTLY) {
	          // We were told how big to be
	          result = specSize;
	      } else {
	          // Measure the text
//	          result = (int) mTextPaint.measureText(mText) + getPaddingLeft()+ getPaddingRight();
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

//	      mAscent = (int) mTextPaint.ascent();
	      if (specMode == MeasureSpec.EXACTLY) {
	          // We were told how big to be
	          result = specSize;
	      } else {
	          // Measure the text (beware: ascent is a negative number)
//	          result = (int) (-mAscent + mTextPaint.descent()) + getPaddingTop()+ getPaddingBottom();
	          if (specMode == MeasureSpec.AT_MOST) {
	              // Respect AT_MOST value if that was what is called for by measureSpec
	              result = Math.min(result, specSize);
	          }
	      }
	      return result;
	  }

	public void invalidateBlockArea() {
		dirtyRect.set(x0, y0, x0+6*blockLength, y0+6*blockLength);
		invalidate(dirtyRect);
	}
	

	@Override
	public boolean onTouchEvent(MotionEvent event){
		//	Log.d(TAG," onTouchEvent ");

//		if(event.getAction() != MotionEvent.ACTION_DOWN)
//			return super.onTouchEvent(event);

		int x=(int)(event.getX());
		int y=(int)(event.getY());
		if(waitTimeUp){
			waitTimeUp=false;
			calculate_dPad(x,y);
			new Thread(new Runnable(){
				public void run(){
					try {
						Thread.sleep(WAIT_TIME);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					waitTimeUp=true;
					}}).start();
		}
		return true;
	}

	private void calculate_dPad(int rawX, int rawY ) {
		if(TEMP)Log.d(TAG,"calculate_dPad blockLength:"+blockLength+" rawX:"+rawX+" rawY:"+rawY);
		if(TEMP)Log.d(TAG,"calculate_dPad subtractedrawX:"+(rawX-blockLength/2)+" subtractedrawY:"+(rawY-(4.5f)*blockLength));
		float x=(rawX-blockLength/2)/blockLength;
		float y=(rawY-(4.5f)*blockLength)/blockLength;
		if(TEMP)Log.d(TAG,"calculate_dPad x:"+x+" y:"+y);
		if(y<1){
			if((1<=x&&x<=3)||(x<=1&&y<x)||(x>=3&&(x-3)<y))
				mTdView.dPadPressed(KeyEvent.KEYCODE_DPAD_UP);
			else if(x<1&&y>=x)
				mTdView.dPadPressed(KeyEvent.KEYCODE_DPAD_LEFT);
			else if(x>=3&&(x-3)>=y)
				mTdView.dPadPressed(KeyEvent.KEYCODE_DPAD_RIGHT);
		}else if(y<=3){// y==1 and y==3 is in this middle section where things are a bit clear
			if(x>1&&x<3)
				mTdView.dPadPressed(KeyEvent.KEYCODE_DPAD_CENTER);
			else if (x<=1)
				mTdView.dPadPressed(KeyEvent.KEYCODE_DPAD_LEFT);
			else if(x>=3)
				mTdView.dPadPressed(KeyEvent.KEYCODE_DPAD_RIGHT);			
		}else{
			if((1<=x&&x<=3)||(x<1&&x<(y-3))||(x>=3&&(x-3)>(y-3)))
				mTdView.dPadPressed(KeyEvent.KEYCODE_DPAD_DOWN);
			else if(x<1&&x>=y-3)
				mTdView.dPadPressed(KeyEvent.KEYCODE_DPAD_LEFT);
			else if(x>=3&&(x-3)<=(y-3))
				mTdView.dPadPressed(KeyEvent.KEYCODE_DPAD_RIGHT);				
		}
	}

	public void setTdView(TdView mTdView) {
		this.mTdView=mTdView;
	}
}
