package com.zhaoia;

import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;

public class Anim {
	
    static public Animation trans( int w, long t, double f, long offset ){
        Animation anim = new TranslateAnimation((float)w, 0, 0, 0);
        anim.setDuration(t);
        anim.setInterpolator( new DecelerateInterpolator((float)f) );
        anim.setFillAfter(true);
        anim.setStartOffset(offset);
        return anim;
    }
    
    
    static public Animation alpha(double from, long t, double f, long offset){
    	Animation anim = new AlphaAnimation((float)from,1.0f);
    	anim.setDuration(t);
    	anim.setInterpolator( new AccelerateInterpolator((float)f) );
    	anim.setFillAfter(true);
        anim.setStartOffset(offset);
    	return anim;
    }
    
}