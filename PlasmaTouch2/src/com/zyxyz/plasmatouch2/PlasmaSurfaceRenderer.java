package com.zyxyz.plasmatouch2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import java.util.Random;
//import android.util.Log;

public class PlasmaSurfaceRenderer implements GLSurfaceView.Renderer
{
    private FloatBuffer _rect_vb;
    private final String _vs_code[] = 
        {
            "attribute vec4 a_position;",
            "void main()",
            "{",
            "  gl_Position = a_position;",
            "}"
        };
    private final String _fs_code[] = 
        {
            "precision mediump float;",
            "uniform float u_r_freq;",
            "uniform float u_g_freq;",
            "uniform float u_b_freq;",
            "uniform float u_r_phase;",
            "uniform float u_g_phase;",
            "uniform float u_b_phase;",
            "uniform float u_shift;",
            "uniform float u_touch_x;",
            "uniform float u_touch_y;",
            "void main()",
            "{",
            "  float x = gl_FragCoord.x;",
            "  float y = gl_FragCoord.y;",
            "  vec2 xy = gl_FragCoord.xy;",
            "  vec2 xy_minus_center = xy - vec2(u_touch_x, u_touch_y);",
            "  float param = floor(512.0 + 128.0*sin(x/16.0) + 128.0*sin(y/32.0) +"+
                            "128.0*sin(length(xy_minus_center)/8.0) +"+
                            "128.0*sin(length(xy)/8.0))/4.0;",
            "  param = mod(param + u_shift, 256.0);",
            "  float r = 0.5 + 0.5*sin(param*u_r_freq + u_r_phase);",
            "  float g = 0.5 + 0.5*sin(param*u_g_freq + u_g_phase);",
            "  float b = 0.5 + 0.5*sin(param*u_b_freq + u_b_phase);",
            "  gl_FragColor = vec4(r, g, b, 1.0);",
            "}"
        };
    private int _width;
    private int _height;
    private float _u_r_freq;
    private float _u_g_freq;
    private float _u_b_freq;
    private float _u_r_phase;
    private float _u_g_phase;
    private float _u_b_phase;
    private float _u_shift;
    private float _u_touch_x;
    private float _u_touch_y;

    private int _program;
    private int _a_position_handle;
    private int _u_r_freq_handle;
    private int _u_g_freq_handle;
    private int _u_b_freq_handle;
    private int _u_r_phase_handle;
    private int _u_g_phase_handle;
    private int _u_b_phase_handle;
    private int _u_shift_handle;
    private int _u_touch_x_handle;
    private int _u_touch_y_handle;
    private boolean _touch_update = true;

    private void initShapes()
    {
        float[] rect_coords = {
                                1.0f, 1.0f, 0.0f,
                                -1.0f, 1.0f, 0.0f,
                                1.0f, -1.0f, 0.0f,
                                -1.0f, -1.0f,0.0f
                              };
        ByteBuffer bb = ByteBuffer.allocateDirect(rect_coords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        _rect_vb = bb.asFloatBuffer();
        _rect_vb.put(rect_coords);
        _rect_vb.position(0);
    }

    private void initParams()
    {
        Random rand = new Random();
        _u_r_freq = (float)(2*Math.PI/256.0*(rand.nextInt(5) + 1));
        _u_g_freq = (float)(2*Math.PI/256.0*(rand.nextInt(5) + 1));
        _u_b_freq = (float)(2*Math.PI/256.0*(rand.nextInt(5) + 1));
        _u_r_phase = (float)(Math.PI*(rand.nextInt(9) + 1));
        _u_g_phase = (float)(Math.PI*(rand.nextInt(9) + 1));
        _u_b_phase = (float)(Math.PI*(rand.nextInt(9) + 1));
        _u_touch_x = _width/2;
        _u_touch_y = _height/2;
        _u_shift = 0.0f;
    }

    private int loadShader(int type, String shader_code)
    {
        int shader = GLES20.glCreateShader(type);
    	GLES20.glShaderSource(shader, shader_code);
    	GLES20.glCompileShader(shader);
		final int[] compileStatus = new int[1];
		GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        //Check the compile status of your shaders
        //if you don't see anything on the screen
		//Log.d("compile status", "" + compileStatus[0]);
    	return shader;
    }

    public void setTouchCoords(float x, float y)
    {
        _u_touch_x = x;
        //Touch coords are in a coord. system with y origin at top-left
        //but, by default, the fragment shader uses a coord. system
        //with the y-origin at bottom left.
        _u_touch_y = _height - y;
        _touch_update = true;
    }

    public void onSurfaceCreated(GL10 unused, EGLConfig config)
    {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        StringBuilder sb = new StringBuilder(600);
        for (String str: _vs_code)
        {
            sb.append(str + "\n");
        }
        int vertex_shader = loadShader(GLES20.GL_VERTEX_SHADER, sb.toString());
        sb = new StringBuilder(600);
        for (String str: _fs_code)
        {
            sb.append(str + "\n");
        }
        int fragment_shader = loadShader(GLES20.GL_FRAGMENT_SHADER, sb.toString());
        _program = GLES20.glCreateProgram();
		GLES20.glAttachShader(_program, vertex_shader);
		GLES20.glAttachShader(_program, fragment_shader);
		GLES20.glLinkProgram(_program);
		GLES20.glUseProgram(_program);
        _a_position_handle = GLES20.glGetAttribLocation(_program, "a_position");
        _u_r_freq_handle = GLES20.glGetUniformLocation(_program, "u_r_freq");
        _u_g_freq_handle = GLES20.glGetUniformLocation(_program, "u_g_freq");
        _u_b_freq_handle = GLES20.glGetUniformLocation(_program, "u_b_freq");
        _u_r_phase_handle = GLES20.glGetUniformLocation(_program, "u_r_phase");
        _u_g_phase_handle = GLES20.glGetUniformLocation(_program, "u_g_phase");
        _u_b_phase_handle = GLES20.glGetUniformLocation(_program, "u_b_phase");
        _u_shift_handle = GLES20.glGetUniformLocation(_program, "u_shift");
        _u_touch_x_handle = GLES20.glGetUniformLocation(_program, "u_touch_x");
        _u_touch_y_handle = GLES20.glGetUniformLocation(_program, "u_touch_y");
		GLES20.glEnableVertexAttribArray(_a_position_handle);
    }

    public void onSurfaceChanged(GL10 unused, int width, int height)
    {
        _width = width;
        _height = height;
        initShapes();
        initParams();
        GLES20.glVertexAttribPointer(_a_position_handle, 3, GLES20.GL_FLOAT, false, 0, _rect_vb);
        GLES20.glUniform1f(_u_r_freq_handle, _u_r_freq);
        GLES20.glUniform1f(_u_g_freq_handle, _u_g_freq);
        GLES20.glUniform1f(_u_b_freq_handle, _u_b_freq);
        GLES20.glUniform1f(_u_r_phase_handle, _u_r_phase);
        GLES20.glUniform1f(_u_g_phase_handle, _u_g_phase);
        GLES20.glUniform1f(_u_b_phase_handle, _u_b_phase);
        GLES20.glViewport(0, 0, width, height);
    }

    public void onDrawFrame(GL10 unused)
    {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUniform1f(_u_shift_handle, _u_shift);
        if ( _touch_update )
        {
	        GLES20.glUniform1f(_u_touch_x_handle, _u_touch_x);
	        GLES20.glUniform1f(_u_touch_y_handle, _u_touch_y);
	        _touch_update = false;
        }
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        _u_shift = (float)((_u_shift + 4.0) % 1073741824);
    }
}

