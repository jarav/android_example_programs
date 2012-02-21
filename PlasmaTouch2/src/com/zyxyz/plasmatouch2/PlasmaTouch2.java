package com.zyxyz.plasmatouch2;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.util.DisplayMetrics;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.view.Gravity;

public class PlasmaTouch2 extends Activity
{
    private GLSurfaceView _gl_view;
    private WakeLock _wakelock;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

        _gl_view = new PlasmaSurfaceView(this);

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
        //For tablet screens, do not use the full screen.
        //Instead use a smaller region.
        //I find that this makes the animation smoother
        //on the Notion Ink Adam.
        if (metrics.heightPixels > 800)
        {
            LinearLayout ll_root = new LinearLayout(this);
            ll_root.setOrientation(LinearLayout.VERTICAL);
            ll_root.setGravity(Gravity.CENTER);
            ll_root.setLayoutParams(new LinearLayout.LayoutParams(
                        LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
            ll_root.addView(_gl_view, new LinearLayout.LayoutParams( 512, 512));
            setContentView(ll_root);
        }
        else
            setContentView(_gl_view);

        PowerManager power_mgr = (PowerManager)getSystemService(Context.POWER_SERVICE);
        _wakelock = power_mgr.newWakeLock(PowerManager.FULL_WAKE_LOCK, "PlasmaTouch2");
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        _gl_view.onPause();
        _wakelock.release();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        _gl_view.onResume();
        _wakelock.acquire();
    }
}

class PlasmaSurfaceView extends GLSurfaceView
{
	private PlasmaSurfaceRenderer _plasma_renderer;
    public PlasmaSurfaceView(Context context)
    {
        super(context);
        setEGLContextClientVersion(2);
        _plasma_renderer = new PlasmaSurfaceRenderer();
        setRenderer(_plasma_renderer);
    }
    
    @Override
    public boolean onTouchEvent(final MotionEvent e)
    {
        queueEvent(new Runnable()
                {
                    public void run()
                    {
                        _plasma_renderer.setTouchCoords(e.getX(), e.getY());
                    }
                }
                );
        return true;
    }
}
