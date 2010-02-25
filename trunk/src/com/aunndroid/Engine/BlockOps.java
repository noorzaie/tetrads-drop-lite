/*
Copyright (c) 2007 Win Myo Htet <wmhtet@aunndroid.com>
          
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.
*/

package com.aunndroid.Engine;

import java.util.Random;
import android.util.Log;

public class BlockOps {

	private static final String TAG = "TetradsDrop : BlockOps : ";
	private Block[] aBlocks;

	private static final int FIELDWIDTH = 11;
	private static final int FIELDHEIGHT = 20;
	static int blocknum = -1, blockorient; /* which block */
	static int nextBlockNum=-1, nextBlockOrient;
	static int blockx, blocky; /* current location of block */
	private int[][] field=new int [FIELDHEIGHT] [FIELDWIDTH];
	private boolean playing;
	private boolean solidifying=false;
	static int[] blockcount = { 2, 1, 4, 4, 2, 2, 4 };
	private static final int pi=314159265;
	private static final int expE=27182818;
	private static final int goldenRatio=161803398;
	private static int constantCounter=0;
	private static long currTimeSeed;
	private static final boolean DEBUG=false;
	private static final boolean VERBOSE=false;
	private static final boolean TEMP=false;

	private static BlockOps singleton;
	private BlockOps(){
		initBlocks();
		initField();
	}

	public static BlockOps getInstanceOf(){
		if(DEBUG)
			Log.d(TAG,"getInstanceOf() : "+singleton);
		if(singleton==null)
			singleton=new BlockOps();
		return singleton;
	}

	public String getFieldPosition() {
		if(DEBUG)
			Log.d(TAG,"getFieldPosition");
		StringBuilder sb=new StringBuilder();;
		for(int i=0;i<FIELDHEIGHT ;i++){
			for(int j=0;j<FIELDWIDTH;j++)
				sb.append(field[i][j]);
		}
		return sb.toString();
	}

	public void updateFieldPosition(
			String fieldPosition) {
		if(DEBUG)
			Log.d(TAG,"updateFieldPosition");
		int index=0;
		for(int i=0;i<FIELDHEIGHT ;i++){
			for(int j=0;j<FIELDWIDTH;j++)
				field[i][j]=fieldPosition.charAt(index++);
		}
	}

	public void tetradDrawCurrentBlock ()	{
	    if (blocknum >= 0)
	        placeBlock ( blocknum, blockorient, blockx, blocky);
	}

	public void tetradMakeNextBlock() {
		nextBlockNum=randomNum(aBlocks.length);
		nextBlockOrient=randomNum(aBlocks[nextBlockNum].orientation);
	}

	public int tetradGetNextBlock() {
		return tetradMakeBlock(nextBlockNum,nextBlockOrient);
	}

	public int tetradMakeBlock() {
		if(VERBOSE)
			Log.d(TAG, "tetrad_makeblock()");
		int block=randomNum(aBlocks.length);
		int orient=randomNum(aBlocks[block].orientation);
		return tetradMakeBlock(block,orient);
	}

	public int tetradMakeBlock (int block, int orient)	{
		if(VERBOSE)
			Log.d(TAG, "tetradMakeBlock (int block, int orient)");
	    blocknum = block;
	    blockorient = orient;
	    blockx = FIELDWIDTH/2-2;
	    blocky = 0;

	    if (block >= 0 && blockObstructed ( blocknum, blockorient, blockx, blocky)>0){
	        /* player is dead */
//	        playerlost ();
	        blocknum = -1;
	        playing=false;
	        return 0;
	    }
	    else return 1;
	}

	/* returns -1 if block solidifies, 0 otherwise */
	public 	int tetradBlockDown ()	{
		if(VERBOSE)
			Log.d(TAG, "tetradBlockDown ()");
	    if (blocknum < 0) return 0;
	    /* move the block down one */
	    if (blockObstructed ( blocknum,blockorient, blockx, blocky+1)>0) {
	        /* cant move down */
//	    	tetradSolidify ();
	    	return -1;
	    }
	    else {
	        blocky ++;
	        return 0;
	    }
	}

	public 	void tetradBlockMove (int dir)	{
		if(DEBUG)
			Log.d(TAG,"tetradBlockMove :"+dir);
	    if (blocknum < 0) return;
	    if (blockObstructed ( blocknum,blockorient, blockx+dir, blocky)>0)
	    	/* do nothing */;
	    else blockx += dir;
	}

	public 	void tetradBlockRotate (int dir)	{
		if(DEBUG)
			Log.d(TAG,"tetrad_blockrotate (int dir)" + dir);
	    int neworient = blockorient + dir;
	    if (blocknum < 0) return;
	    if (neworient >= blockcount[blocknum]) neworient = 0;
	    if (neworient < 0) neworient = blockcount[blocknum] - 1;
	    switch (blockObstructed ( blocknum, neworient, blockx, blocky)){
	    case 1: return; /* cant rotate if obstructed by blocks */
	    case 2: {/* obstructed by sides - move block away if possible */
	            int[] shifts = {1, -1, 2, -2};
	            int i;
	            for (i = 0; i < 4; i ++) {
	                if (blockObstructed ( blocknum,neworient, blockx+shifts[i],blocky)==0){
	                    blockx += shifts[i];
	                    break;
	                }
	            }
	            return; /* unsuccessful */
	        }
	    }
	    blockorient = neworient;
	}

	public void tetradBlockDrop ()	{
		if(VERBOSE)
			Log.d(TAG,"tetradBlockDrop ()");
	    if (blocknum < 0) return;
	    while (tetradBlockDown () == 0);
	}

	public void tetradAddlines (int count, int type){
		if(TEMP)
			Log.d(TAG,"tetradAddlines :"+count);
	    int x, y, i;
	    for (i = 0; i < count; i ++) {
	        /* check top row */
	        for (x = 0; x < FIELDWIDTH; x ++) {
	            if (field[0][x]!=0) {
	                /* player is dead */
//	                playerlost ();
	                return;
	            }
	        }
	        /* move everything up one */
	        for (y = 0; y < FIELDHEIGHT-1; y ++) {
	            for (x = 0; x < FIELDWIDTH; x ++)
	                field[y][x] = field[y+1][x];
	        }
	        /* generate a random line with spaces in it */
	        switch (type) {
	        case 1: /* addline lines */
	            /* ### This is how the original  seems to do an add line */
	            for (x = 0; x < FIELDWIDTH; x ++)
	                field[FIELDHEIGHT-1][x] = randomNum(8);
	            field[FIELDHEIGHT-1][randomNum(FIELDWIDTH)] = 0;
	            /* ### Corrected by Pihvi */
	            break;
	        case 2: /* classicmode lines */
	            /* fill up the line */
	            for (x = 0; x < FIELDWIDTH; x ++)
	                field[FIELDHEIGHT-1][x] = randomNum(7) + 1;
	            /* add a single space */
	            field[FIELDHEIGHT-1][randomNum(FIELDWIDTH)] = 0;
	            break;
	        }
	    }
//	    Updatefield (field);
	}

	/* this function removes full lines */
	public 	int tetradRemovelines (){//char *specials)
	    if(DEBUG)
	    	Log.d(TAG,"tetrad_removelines() :");
	    int x, y, o, c = 0, i;
	    if (!playing) return 0;
//	    copyfield (field, fields[playernum]);
	    /* remove full lines */
	    for (y = 0; y < FIELDHEIGHT; y ++) {
	        o = 0;
	        /* count holes */
	        for (x = 0; x < FIELDWIDTH; x ++)
	            if (field[y][x] == 0) o ++;
	        if (o>0) continue; /* if holes */
	        /* no holes */
	        /* increment line count */
	        c ++;

	        /* grab specials */
	        /*
	        if (specials)
	            for (x = 0; x < FIELDWIDTH; x ++)
	                if (field[y][x] > 5)
	                    *specials++ = field[y][x];
	       */
	        /* move field down */
	        for (i = y-1; i >= 0; i --)
	            for (x = 0; x < FIELDWIDTH; x ++)
	                field[i+1][x] = field[i][x];
	        /* clear top line */
	        for (x = 0; x < FIELDWIDTH; x ++)
	            field[0][x] = 0;
	    }
//	    if (specials) *specials = 0; /* null terminate */
//    if (c) _updatefield (field);
	    return c;
	}

	public void tetradSolidify (){
		if(VERBOSE)
			Log.d(TAG, "tetrad_solidify (): ");
		solidifying=true;
//	    copyfield (field, fields[playernum]);
	    if (blocknum < 0) return;
	    if (blockObstructed ( blocknum, blockorient, blockx, blocky)>0) {
	        /* move block up until we get a free spot */
	        for (blocky --; blocky >= 0; blocky --)
	            if (blockObstructed ( blocknum, blockorient, blockx, blocky)==0){
	                placeBlock ( blocknum, blockorient, blockx, blocky);
	                break;
	            }
	        if (blocky < 0) {
	            /* no space - player has lost */
//	            playerlost ();

	            blocknum = -1;
	            return;
	        }
	    }
	    else {
	        placeBlock ( blocknum, blockorient, blockx, blocky);
	    }
	    blocknum = -1;
	    solidifying=false;
	}

	public int blockObstructed (int block, int orient, int bx, int by)	{
		if(VERBOSE)
			Log.d(TAG, "blockObstructed");
	    int x, y, side = 0;
	    for (y = 0; y < 4; y ++)
	        for (x = 0; x < 4; x ++)
	            if (aBlocks[block].block[orient][y][x]>0) {
	                switch (obstructed ( bx+x, by+y)) {
	                case 0:
	                	continue;
	                case 1:
	                	return 1;
	                case 2:
	                	side = 2;
	                }
	            }
	    return side;
	}

	public int obstructed ( int x, int y)	{
	    if (x < 0) return 2;
	    if (x >= FIELDWIDTH) return 2;
	    if (y < 0) return 1;
	    if (y >= FIELDHEIGHT) return 1;
	    if (field[y][x]>0) return 1;
	    return 0;
	}

	public 	void placeBlock ( int block, int orient, int bx, int by)	{
	    int x, y;
	    for (y = 0; y < 4; y ++)
	        for (x = 0; x < 4; x ++) {
	            if (aBlocks[block].block[orient][y][x]>0)
	                field[y+by][x+bx] = aBlocks[block].block[orient][y][x];
	        }
	}

	public 	int tetradRandomOrient (int block)	{
	    return randomNum (this.aBlocks[block].orientation);
	}

	/**
	 *  returns a random number in the range 0 to n-1 --
	 * Note both n==0 and n==1 always return 0 */
	private int randomNum(int num) {
		long currTime=System.currentTimeMillis();
			if(DEBUG)
				Log.d(TAG,"randomNum constantCounter :"+constantCounter);
			switch(constantCounter){
			case 0:
				currTimeSeed=currTime+pi;
				constantCounter++;
				break;
			case 1:
				currTimeSeed=currTime+goldenRatio;
				constantCounter++;
				break;
			case 2:
				currTimeSeed=currTime+expE;
				constantCounter++;
				break;
			default:
				currTimeSeed=currTime;
				constantCounter=0;
				break;
			}
		Random randomizer = new Random(currTimeSeed);
		return randomizer.nextInt(num);
	}

	public int getBlocksField(int x, int y, int blocknum, int orient) {
		if(blocknum<0)
			return 0;
		if(y==4||x==4){
			return 0;
		}
		else{
			return this.aBlocks[blocknum].block[orient][y][x];
		}
	}

	public static int getBlocknum() {
		if(VERBOSE)
			Log.d(TAG,"getBlocknum :"+blocknum);
		return blocknum;
	}

	public static void setBlocknum(int blocknum) {
    	if(VERBOSE)
    		Log.d(TAG,"setBlocknum :"+ blocknum );
		BlockOps.blocknum = blocknum;
	}

	public static int getBlockorient() {
		return blockorient;
	}

	public static void setBlockorient(int blockorient) {
		BlockOps.blockorient = blockorient;
	}

	public static int getBlockx() {
		return blockx;
	}

	public static void setBlockx(int blockx) {
		BlockOps.blockx = blockx;
	}

	public static void setBlockxOrigin(){
	    blockx = FIELDWIDTH/2-2;
	}

	public static int getBlocky() {
		return blocky;
	}

	public static void setBlocky(int blocky) {
		BlockOps.blocky = blocky;
	}

	public boolean isSolidifying() {
		return solidifying;
	}

	public void setSolidifying(boolean solidifying) {
		this.solidifying = solidifying;
	}

	public int getField(int x,int y){
		return field[y][x];
	}

	public boolean isPlaying() {
		return playing;
	}

	public void setPlaying(boolean playing) {
		this.playing = playing;
	}

	public static int getNextBlockNum() {
		return nextBlockNum;
	}

	public static void setNextBlockNum(int nextBlockNum) {
		BlockOps.nextBlockNum = nextBlockNum;
	}

	public static int getNextBlockOrient() {
		return nextBlockOrient;
	}

	public static void setNextBlockOrient(int nextBlockOrient) {
		BlockOps.nextBlockOrient = nextBlockOrient;
	}

	public 	void initField(){
		for(int i=0;i<FIELDHEIGHT ;i++){
			for(int j=0;j<FIELDWIDTH;j++)
				field[i][j]=0;
		}
	}

	public void initBlocks(){
		if(DEBUG)
			Log.d(TAG,"initBlocks()");

		aBlocks=new Block[7];
		aBlocks[0]=new Block();
		aBlocks[0].block=new int[][][]
		     {
			    {
			        {1,1,1,1},
			        {0,0,0,0},
			        {0,0,0,0},
			        {0,0,0,0}
			    }, {
			        {0,0,1,0},
			        {0,0,1,0},
			        {0,0,1,0},
			        {0,0,1,0}
			    }
			};
		aBlocks[0].orientation=aBlocks[0].block.length;

		aBlocks[1]=new Block();
		aBlocks[1].block=new int[][][]{
			    {
			        {0,2,2,0},
			        {0,2,2,0},
			        {0,0,0,0},
			        {0,0,0,0}
			    }
			};
		aBlocks[1].orientation=aBlocks[1].block.length;

		aBlocks[2]=new Block();
		aBlocks[2].block=new int[][][]{
			    {
			        {0,0,3,0},
			        {0,0,3,0},
			        {0,3,3,0},
			        {0,0,0,0}
			    }, {
			        {0,3,0,0},
			        {0,3,3,3},
			        {0,0,0,0},
			        {0,0,0,0}
			    }, {
			        {0,3,3,0},
			        {0,3,0,0},
			        {0,3,0,0},
			        {0,0,0,0}
			    }, {
			        {0,3,3,3},
			        {0,0,0,3},
			        {0,0,0,0},
			        {0,0,0,0}
			    }
			};
		aBlocks[2].orientation=aBlocks[2].block.length;

		aBlocks[3]=new Block();
		aBlocks[3].block=new int[][][]{
			    {
			        {0,4,0,0},
			        {0,4,0,0},
			        {0,4,4,0},
			        {0,0,0,0}
			    }, {
			        {0,4,4,4},
			        {0,4,0,0},
			        {0,0,0,0},
			        {0,0,0,0}
			    }, {
			        {0,4,4,0},
			        {0,0,4,0},
			        {0,0,4,0},
			        {0,0,0,0}
			    }, {
			        {0,0,0,4},
			        {0,4,4,4},
			        {0,0,0,0},
			        {0,0,0,0}
			    }
			};
		aBlocks[3].orientation=aBlocks[3].block.length;

		aBlocks[4]=new Block();
		aBlocks[4].block=new int[][][]{
			    {
			        {0,0,5,0},
			        {0,5,5,0},
			        {0,5,0,0},
			        {0,0,0,0}
			    }, {
			        {0,5,5,0},
			        {0,0,5,5},
			        {0,0,0,0},
			        {0,0,0,0}
			    }
			};
		aBlocks[4].orientation=aBlocks[4].block.length;

		aBlocks[5]=new Block();
		aBlocks[5].block=new int[][][]{
			    {
			        {0,6,0,0},
			        {0,6,6,0},
			        {0,0,6,0},
			        {0,0,0,0}
			    }, {
			        {0,0,6,6},
			        {0,6,6,0},
			        {0,0,0,0},
			        {0,0,0,0}
			    }
			};
		aBlocks[5].orientation=aBlocks[5].block.length;

		aBlocks[6]=new Block();
		aBlocks[6].block=new int[][][]{
			    {
			        {0,0,7,0},
			        {0,7,7,0},
			        {0,0,7,0},
			        {0,0,0,0}
			    }, {
			        {0,0,7,0},
			        {0,7,7,7},
			        {0,0,0,0},
			        {0,0,0,0}
			    }, {
			        {0,7,0,0},
			        {0,7,7,0},
			        {0,7,0,0},
			        {0,0,0,0}
			    }, {
			        {0,7,7,7},
			        {0,0,7,0},
			        {0,0,0,0},
			        {0,0,0,0}
			    }
			};
		aBlocks[6].orientation=aBlocks[6].block.length;
	}

	class Block {
		private int[][][] block;
		private int orientation;
		Block(){}
	}

}
