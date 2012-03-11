package com.zyxyz.touchtest;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;

public class Ball
{
    static final float SPD_LEN_FCTR = 10.0f;
    static final float MAX_SPEED = 10.0f;
    static final float MIN_SPEED = 1.0f;
    static final float DEFLT_SPEED = 5.0f;
    static final int MAX_ARROW = (int)(SPD_LEN_FCTR*MAX_SPEED);
    static final int MIN_ARROW = (int)(SPD_LEN_FCTR*MIN_SPEED);
    static final int MAX_ARROW_SQ = MAX_ARROW*MAX_ARROW;
    static final int MIN_ARROW_SQ = MIN_ARROW*MIN_ARROW;
    static final float MASS_RAD_FCTR = 30.0f;
    static final int MAX_RAD = 90;
    static final int MIN_RAD = 30;
    static final int DEFLT_RAD = 60;
    static final float ARROW_HEAD = 10.0f;
    static final float ARROW_BASE = 5.0f;
    static final int TOUCH_PREC = 14;
    static final int TOUCH_PREC_SQ = TOUCH_PREC*TOUCH_PREC;
    float _x, _y;
    float _vx, _vy;
    float _init_speed;
    float _init_direction;
    int _rad;
    float _mass;
    boolean _vel_config;
    boolean _rad_config;
    private int _arrow_x, _arrow_y;
    private RectF _arc_rect = new RectF();

    public Ball(int rad, float vx, float vy)
    {
        _rad = rad;
        float root_m = _rad/MASS_RAD_FCTR;
        _mass = root_m*root_m;
        _vx = vx;
        _vy = vy;
        _arrow_x = (int)(_vx*SPD_LEN_FCTR);
        _arrow_y = (int)(_vy*SPD_LEN_FCTR);
    }

    public boolean includesPoint(int x, int y)
    {
        double dx = x - _x;
        double dy = y - _y;
        return ( dx*dx + dy*dy ) < _rad*_rad;
    }

    public void configureInit()
    {
        synchronized(this)
        {
            _vel_config = _rad_config = true;
        }
    }

    public void configure(int x, int y)
    {
        if ( _vel_config )
        {
            synchronized(this)
            {
                _arrow_x = (int)(x - _x);
                _arrow_y = (int)(y - _y);
                int arrow_sq = _arrow_x*_arrow_x + _arrow_y*_arrow_y;
                if ( arrow_sq > MAX_ARROW_SQ )
                {
                    double l = Math.sqrt(arrow_sq);
                    _arrow_x = (int)(_arrow_x/l*MAX_ARROW);
                    _arrow_y = (int)(_arrow_y/l*MAX_ARROW);
                }
                else if ( arrow_sq < MIN_ARROW_SQ )
                {
                    double l = Math.sqrt(arrow_sq);
                    _arrow_x = (int)(_arrow_x/l*MIN_ARROW);
                    _arrow_y = (int)(_arrow_y/l*MIN_ARROW);
                }
            }
            _vx = _arrow_x/SPD_LEN_FCTR;
            _vy = _arrow_y/SPD_LEN_FCTR;
        }
        else if ( _rad_config )
        {
            int dx = (int)(x - _x);
            int dy = (int)(y - _y);
            int l = (int)Math.sqrt(dx*dx + dy*dy);
            synchronized(this)
            {
                _rad = (int)Math.max(MIN_RAD, Math.min(l, MAX_RAD));
            }
            float root_m = _rad/MASS_RAD_FCTR;
            _mass = root_m*root_m;
        }
    }
    
    public boolean velChangeInit(int x, int y)
    {
        int arrow_end_x = (int)(_x + _arrow_x);
        int arrow_end_y = (int)(_y + _arrow_y);
        int dx = x - arrow_end_x;
        int dy = y - arrow_end_y;
        boolean result = (( dx*dx + dy*dy ) < TOUCH_PREC_SQ);
        if (result)
        {
            synchronized(this)
            {
                _rad_config = false;
            }
        }
        return result;
    }

    public boolean radChangeInit(int x, int y)
    {
        int dx = (int)(x - _x);
        int dy = (int)(y - _y);
        int l_sq = dx*dx + dy*dy;
        int touch_max_sq = (_rad + TOUCH_PREC/2)*(_rad + TOUCH_PREC/2);
        int touch_min_sq= (_rad - TOUCH_PREC/2)*(_rad - TOUCH_PREC/2);
        boolean result = ( l_sq < touch_max_sq ) && ( l_sq > touch_min_sq );
        if ( result )
        {
            synchronized(this)
            {
                _vel_config = false;
            }
        }
        return result;
    }

    public void configureEnd()
    {
        synchronized(this)
        {
            _vel_config = _rad_config = false;
        }
    }

    public void draw(Canvas canvas, Paint paint)
    {
        paint.setStyle(Style.FILL);
        paint.setColor(0xff0000ff);
        canvas.drawCircle((float)_x, (float)_y, _rad, paint);
    }

    public void drawConfig(Canvas canvas, Paint paint)
    {
        synchronized(this)
        {
            if (_vel_config)
            {
                drawArrow(_x, _y, _x + _arrow_x, _y + _arrow_y, canvas, paint);
            }
        }
        synchronized(this)
        {
            if (_rad_config)
            {
                _arc_rect.left = _x - _rad;
                _arc_rect.right = _x + _rad;
                _arc_rect.top = _y - _rad;
                _arc_rect.bottom = _y + _rad;
                paint.setStyle(Style.STROKE);
                paint.setStrokeWidth(TOUCH_PREC);
                paint.setColor(0x80ff0000);
                canvas.drawArc(_arc_rect, 0.0f, 360.0f, false, paint);
            }
        }
    }

    private void drawArrow(float x1, float y1, float x2, float y2, Canvas canvas, Paint paint)
    {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float l = (float)Math.sqrt(dx*dx + dy*dy);
        float unit_x = dx/l;
        float unit_y = dy/l;
        float perp_x = - unit_y;
        float perp_y = unit_x;
        float base_pt_x = x2 - ARROW_HEAD*unit_x;
        float base_pt_y = y2 - ARROW_HEAD*unit_y;
        float left_pt_x = base_pt_x - ARROW_BASE*perp_x;
        float left_pt_y = base_pt_y - ARROW_BASE*perp_y;
        float right_pt_x = base_pt_x + ARROW_BASE*perp_x;
        float right_pt_y = base_pt_y + ARROW_BASE*perp_y;
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(4.0f);
        paint.setColor(0xffffff00);
        float[] pts = { x1, y1, x2, y2,
                      left_pt_x, left_pt_y, x2, y2,
                      x2, y2, right_pt_x, right_pt_y
                    };
        canvas.drawLines(pts, paint);
    }

    public void displace(float dt)
    {
        _x += _vx*dt;
        _y += _vy*dt;
    }

    public boolean hitsWall(float right, float bottom)
    {
        boolean ret_value = false;
        if ( ( _x < _rad && _vx < 0.0f ) ||
            ( _x > (right - _rad) && _vx > 0.0f ))
        {
            ret_value = true;
            _vx *= -1.0f;
        }

        if ( ( _y < _rad && _vy < 0.0f ) ||
            ( _y > (bottom - _rad) && _vy > 0.0f ))
        {
            ret_value = true;
            _vy *= -1.0f;
        }

        return ret_value;
    }

    public boolean hitsBall(Ball ball)
    {
        if (! overlappingWithBall(ball))
            return false;
        float dx = ball._x - _x;
        float dy = ball._y - _y;
        float r_sq = dx*dx + dy*dy;
        float v_dot_r_by_r_sq = (_vx * dx + _vy * dy)/r_sq;
        float v_nx = dx*v_dot_r_by_r_sq;
        float v_ny = dy*v_dot_r_by_r_sq;
        float bv_dot_r_by_r_sq = (ball._vx * dx + ball._vy * dy)/r_sq;
        float bv_nx = dx*bv_dot_r_by_r_sq;
        float bv_ny = dy*bv_dot_r_by_r_sq;
        float vx_diff = bv_nx - v_nx;
        float vy_diff = bv_ny - v_ny;
        if ( (vx_diff*dx + vy_diff*dy) > 0.0f )
            return false;
        float v_tx = _vx - v_nx;
        float v_ty = _vy - v_ny;
        float bv_tx = ball._vx - bv_nx;
        float bv_ty = ball._vy - bv_ny;
        float mass_sum = _mass + ball._mass;
        float mass_diff = _mass - ball._mass;
        float new_v_nx = (2.0f*ball._mass*bv_nx + mass_diff*v_nx)/mass_sum;
        float new_v_ny = (2.0f*ball._mass*bv_ny + mass_diff*v_ny)/mass_sum;
        float new_bv_nx = (2.0f*_mass*v_nx - mass_diff*bv_nx)/mass_sum;
        float new_bv_ny = (2.0f*_mass*v_ny - mass_diff*bv_ny)/mass_sum;
        _vx = new_v_nx + v_tx;
        _vy = new_v_ny + v_ty;
        ball._vx = new_bv_nx + bv_tx;
        ball._vy = new_bv_ny + bv_ty;
        return true;
    }

    public boolean overlappingWithWalls(float right, float bottom)
    {
        if ( _x > _rad && _y > _rad && _x < ( right - _rad ) &&
                _y < ( bottom - _rad ) )
            return false;
        return true;
    }

    public boolean overlappingWithBall(Ball ball)
    {
        float rad_sum = _rad + ball._rad;
        float rad_sum_sq = rad_sum * rad_sum;
        float dx = _x - ball._x;
        float dy = _y - ball._y;
        if ( (dx*dx + dy*dy) < rad_sum_sq )
            return true;
        return false;
    }

    public boolean overlappingWithBalls(Ball[] balls)
    {
        for (Ball ball : balls)
        {
            if ( ball == null || ball == this )
                continue;
            if ( overlappingWithBall(ball) )
                return true;
        }
        return false;
    }
}
