package com.zyxyz.touchtest;

import android.content.Context;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.view.MotionEvent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import java.util.Random;

public class AnimView extends SurfaceView implements
    SurfaceHolder.Callback
{
    Ball[] _balls;
    private Ball _selected_ball;
    int _anim_on = 0;
    private AnimThread _anim_thread;
    int _width;
    int _height;
    private WakeLock _wakelock;

    public AnimView(Context context, Ball[] balls)
    {
        super(context);
        PowerManager power_mgr = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        _wakelock = power_mgr.newWakeLock(PowerManager.FULL_WAKE_LOCK, "TouchTest");
        _balls = balls;
        getHolder().addCallback(this);
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder sh, int format, int width, int height)
    {
        _anim_thread = new AnimThread(this);
        _width = width;
        _height = height;
        createBalls();
        _anim_thread._running = true;
        _anim_thread.start();
    }

    @Override
    public void surfaceCreated(SurfaceHolder sh)
    {
        _wakelock.acquire();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder sh)
    {
        _anim_thread._running = false;
        while (true)
        {
            try
            {
                _anim_thread.join();
                _wakelock.release();
                break;
            }
            catch (InterruptedException ie)
            {
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent me)
    {
        int action = me.getActionMasked();
        int ptr_indx = me.getActionIndex();
        //int ptr_id = me.getPointerId(ptr_indx);
        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
                if ( _anim_on == 1 )
                    _anim_on = 0;
                else
                {
                    if ( ! selectBall((int)me.getX(), (int)me.getY()) )
                    {
                        if ( _selected_ball != null )
                            unselectBall();
                        if ( ballsNotOverlapping() )
                            _anim_on = 1;
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if ( _selected_ball != null )
                {
                	int x = (int)me.getX(ptr_indx);
                	int y = (int)me.getY(ptr_indx);
                	if ( x < _width && x > 0 && y < _height && y > 0 )
                    {
                        _selected_ball._x = x;
                        _selected_ball._y = y;
                    }
                    unselectBall();
                }
                break;
            case MotionEvent.ACTION_UP:
                if ( _selected_ball != null )
                {
                	if ( !(_selected_ball._vel_config &&  _selected_ball._rad_config) )
	                    unselectBall();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if ( _selected_ball != null )
                	if ( !(_selected_ball._vel_config &&  _selected_ball._rad_config) )
	                    _selected_ball.configure((int)me.getX(), (int)me.getY());
                break;
        }
        return true;
    }

    private boolean selectBall(int x, int y)
    {
        if ( _selected_ball != null )
        {
            return ( _selected_ball.velChangeInit(x, y) ||
                    _selected_ball.radChangeInit(x,y) );
        }
        else
        {
            for (Ball ball : _balls)
            {
                if ( ball.includesPoint(x, y) )
                {
                    synchronized(this)
                    {
                        _selected_ball = ball;
                        _selected_ball.configureInit();
                    }
                    return true;
                }
            }
            return false;
        }
    }

    private void unselectBall()
    {
        synchronized(this)
        {
            _selected_ball.configureEnd();
            _selected_ball = null;
        }
    }

    private void createBalls()
    {
        int num_balls = _balls.length;
        Random random = new Random();
        for ( int i = 0; i < num_balls; ++i )
        {
            int rad = Ball.MIN_RAD + random.nextInt(Ball.MAX_RAD - Ball.MIN_RAD);
            float speed = Ball.MIN_SPEED + random.nextInt((int)(Ball.MAX_SPEED -
                                                            Ball.MIN_SPEED));
            int direction = random.nextInt(360);
            double angle = Math.toRadians(direction);
            float vx = (float)(speed*Math.cos(angle));
            float vy = (float)(speed*Math.sin(angle));
            Ball ball = _balls[i] = new Ball(rad, vx, vy);
            do
            {
                ball._x = ball._rad + random.nextInt(_width - 2*ball._rad);
                ball._y = ball._rad + random.nextInt(_height - 2*ball._rad);
            }
            while ( ball.overlappingWithBalls(_balls) );
        }
    }

    private boolean ballsNotOverlapping()
    {
        for ( Ball ball : _balls )
        {
            if ( ball.overlappingWithWalls(_width, _height) || ball.overlappingWithBalls(_balls) )
                return false;
        }
        return true;
    }

    public Ball getSelectedBall()
    {
        synchronized(this)
        {
            return _selected_ball;
        }
    }
}
