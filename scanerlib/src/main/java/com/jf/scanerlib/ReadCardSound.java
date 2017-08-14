package com.jf.scanerlib;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

public class ReadCardSound {
    private static final String TAG = "ReadCardSound";
    public static Context mContext;
	    
    public static final int NORMAL         = 0;
    public static final int SUSPECT        = 1;
    public static final int INSPECT        = 2;

    private static final int NUM_SOUNDS           = 3;
    private ReadCardSoundPlayer[] mReadCardSoundPlayers;

    public ReadCardSound(Context context) {
    	mContext = context;
    }

    public void playSound(int soundId) {
        if (mReadCardSoundPlayers == null) {
        	mReadCardSoundPlayers = new ReadCardSoundPlayer[NUM_SOUNDS];
        }
        if (mReadCardSoundPlayers[soundId] == null) {
        	mReadCardSoundPlayers[soundId] = new ReadCardSoundPlayer(soundId);
        }
        mReadCardSoundPlayers[soundId].play();
    }
    
    public void release() {
        if (mReadCardSoundPlayers != null) {
            for (ReadCardSoundPlayer rcsp: mReadCardSoundPlayers) {
                if (rcsp != null) {
                	rcsp.release();
                }
            }
            mReadCardSoundPlayers = null;
        }
    }

    private static class ReadCardSoundPlayer implements Runnable {
		private int mSoundId;
		private MediaPlayer mPlayer;
		private Thread mThread;
		private boolean mExit;
		private int mPlayCount;

		@Override
		public void run() {
			switch (mSoundId) {
			case NORMAL:
				mPlayer = MediaPlayer.create(mContext, R.raw.normal);
				break;
			case SUSPECT:
				mPlayer = MediaPlayer.create(mContext, R.raw.suspect);
				break;
			case INSPECT:
				mPlayer = MediaPlayer.create(mContext, R.raw.inspect);
				break;
			default:
				Log.e(TAG, "Unknown sound " + mSoundId + " requested.");
				return;
			}
	        
			/*
			try {
				mPlayer.setLooping(false);
				mPlayer.prepare();
			} catch(IOException e) {
				Log.e(TAG, "Error setting up sound " + mSoundId, e);
				return;
			} catch(IllegalStateException e){
				Log.e(TAG, "Error setting up sound " + mSoundId, e);
				return;
			}*/

			while(true) {
	        	try {
	        		synchronized (this) {
	        			while(true) {
	        				if (mExit) {
	        					return;
	        				} else if (mPlayCount <= 0) {
	        					wait();
	        				} else {
	        					mPlayCount--;
	        					break;
	        				}
	        			}
	        		}
	        		mPlayer.start();
	        	} catch (Exception e) {
	        		Log.e(TAG, "Error playing sound " + mSoundId, e);
	        	}
			}
		}

		public ReadCardSoundPlayer(int soundId) {
			mSoundId = soundId;
		}

		public void play() {
			if (mThread == null) {
	        	mThread = new Thread(this);
	        	mThread.start();
	        }
	        
			synchronized (this) {
	        	mPlayCount++;
	        	notifyAll();
	        }
	    }

	    public void release() {
	    	if (mThread != null) {
	    		synchronized (this) {
	    			mExit = true;
	    			notifyAll();
	    		}
	    		try {
	    			mThread.join();
	    		} catch (InterruptedException e) {
	    		}
	    		mThread = null;
	    	}
	    
	    	if (mPlayer != null) {
	        	mPlayer.release();
	        	mPlayer = null;
	        }
	    }

	    @Override
	    protected void finalize() {
	    	release();
	    }
	}
}