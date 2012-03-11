package com.zyxyz.touchtest;

import android.graphics.Canvas;
import android.graphics.Paint;

public class AnimThread extends Thread
{
    private AnimView _anim_view;
    boolean _running = false;
    private Paint _paint;
    private Ball[] _balls;
    private int _ball_num;
    private boolean[] _HITS;
    private boolean[] _hits;
    private final float _DELTA_T = 0.5f;

    interface Updater
    {
        void updateAndDisplay(Canvas canvas, Paint paint);
    }

    class Anim implements Updater
    {
        public void updateAndDisplay(Canvas canvas, Paint paint)
        {
            System.arraycopy(_HITS, 0, _hits, 0, _ball_num);
            for ( int i = 0; i < _ball_num; ++i )
            {
                if ( _hits[i] )
                    continue;
                Ball ball = _balls[i];
                if ( ball.hitsWall(_anim_view._width, _anim_view._height) )
                {
                    _hits[i] = true;
                    continue;
                }
                for ( int k = i + 1; k < _ball_num; ++k )
                {
                    if ( ! _hits[k] && ball.hitsBall(_balls[k]) )
                    {
                        _hits[i] = _hits[k] = true;
                        break;
                    }
                }
            }
            for ( Ball ball : _balls )
            {
                ball.displace(_DELTA_T);
                ball.draw(canvas, paint);
            }
        }
    }

    class NonAnim implements Updater
    {
        public void updateAndDisplay(Canvas canvas, Paint paint)
        {
            for ( Ball ball : _balls )
            {
                ball.draw(canvas, paint);
            }
            Ball sel_ball;
            if ( (sel_ball = _anim_view.getSelectedBall()) != null )
                sel_ball.drawConfig(canvas, paint);
        }
    }

    private Updater[] _updaters;
    public AnimThread(AnimView anim_view)
    {
        _anim_view = anim_view;
        _paint = new Paint();
        _paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        _balls = _anim_view._balls;
        _ball_num = _balls.length;
        _HITS = new boolean[_ball_num];
        _hits = new boolean[_ball_num];
        for ( int i = 0; i < _ball_num; ++i )
        {
            _HITS[i] = false;
        }
        _updaters = new Updater[2];
        _updaters[0] = new NonAnim();
        _updaters[1] = new Anim();
    }

    @Override
    public void run()
    {
        while (_running)
        {
            Canvas canvas = _anim_view.getHolder().lockCanvas();
            _paint.setColor(0xffffffff);
            canvas.drawPaint(_paint);
            _updaters[_anim_view._anim_on].updateAndDisplay(canvas, _paint);
            /*
            if ( _anim_view._anim_on )
            {
                updateAndDisplayBalls(canvas, _paint);
            }
            else
            {
                displayBalls(canvas, _paint);
            }
            */
			_anim_view.getHolder().unlockCanvasAndPost(canvas);
        }
    }

    /*
    private void updateAndDisplayBalls(Canvas canvas, Paint paint)
    {
        System.arraycopy(_HITS, 0, _hits, 0, _ball_num);
        for ( int i = 0; i < _ball_num; ++i )
        {
            if ( _hits[i] )
                continue;
            Ball ball = _balls[i];
            if ( ball.hitsWall(_anim_view._width, _anim_view._height) )
            {
                _hits[i] = true;
                continue;
            }
            for ( int k = i + 1; k < _ball_num; ++k )
            {
                if ( ! _hits[k] && ball.hitsBall(_balls[k]) )
                {
                    _hits[i] = _hits[k] = true;
                    break;
                }
            }
        }
        for ( Ball ball : _balls )
        {
            ball.displace(_DELTA_T);
            ball.draw(canvas, paint);
        }
    }

    private void displayBalls(Canvas canvas, Paint paint)
    {
        for ( Ball ball : _balls )
        {
            ball.draw(canvas, paint);
        }
        Ball sel_ball;
        if ( (sel_ball = _anim_view.getSelectedBall()) != null )
            sel_ball.drawConfig(canvas, paint);
    }
    */
}

