package com.zyxyz.touchtest;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.view.Gravity;

public class Touchtest extends Activity
{
    /** Called when the activity is first created. */
    static final int NUM_BALLS = 3;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Ball[] balls = new Ball[NUM_BALLS];
        AnimView anim_view = new AnimView(this, balls);

        DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
        //For tablet screens, do not use the full screen.
        //Instead use a smaller region.
        //I find that this makes the animation smoother
        //on the Notion Ink Adam.
        if (metrics.heightPixels > 800)//we are using the portrait mode always
        {
            LinearLayout ll_root = new LinearLayout(this);
            ll_root.setOrientation(LinearLayout.VERTICAL);
            ll_root.setGravity(Gravity.CENTER);
            ll_root.setLayoutParams(new LinearLayout.LayoutParams(
                        LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
            int sz = (metrics.widthPixels < 512) ? metrics.widthPixels : 512;
            ll_root.addView(anim_view, new LinearLayout.LayoutParams( sz, sz));
            setContentView(ll_root);
        }
        else
            setContentView(anim_view);
    }
}
