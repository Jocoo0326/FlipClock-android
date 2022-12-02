package com.jocoo.flipclock

import android.content.Context
import android.opengl.EGL14.EGL_CONTEXT_CLIENT_VERSION
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.os.SystemClock
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.opengles.GL10

class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {

    init {
        setEGLContextFactory(object : EGLContextFactory {
            override fun createContext(
                egl: EGL10,
                eglDisplay: EGLDisplay?,
                eglConfig: EGLConfig?
            ): EGLContext {
                return egl.eglCreateContext(
                    eglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT,
                    intArrayOf(EGL_CONTEXT_CLIENT_VERSION, 3, EGL10.EGL_NONE)
                )
            }

            override fun destroyContext(p0: EGL10, p1: EGLDisplay, p2: EGLContext) {
                p0.eglDestroyContext(p1, p2)
            }
        })
        setRenderer(MyRenderer())
        renderMode = RENDERMODE_CONTINUOUSLY
    }


}

class MyRenderer : GLSurfaceView.Renderer {
    private var timeUniform: Int = 0
    private lateinit var vertexByteBuffer: FloatBuffer
    private var program: Int = 0
    val vertSrc = """
        attribute vec2 position;
        void main() {
            gl_Position = vec4(position, 0, 1);
        }
    """.trimIndent()

    //    val fragSrc = """
//        precision mediump float;
//        uniform vec2 resolution;
//        void main() {
//          vec2 uv = gl_FragCoord.xy/resolution;
//          uv = uv * 2.0 - 1.0;
//          if (resolution.x >= resolution.y) {
//            float ratio = resolution.x/resolution.y;
//            uv.x *= ratio;
//          } else {
//            float ratio = resolution.y/resolution.x;
//            uv.y *= ratio;
//          }
//          vec3 col = vec3(1.0);
//          float dis = 1.0 - length(uv);
//          float thickness = 0.00001 * min(resolution.x, resolution.y);
//          thickness = 0.006;
//          col = vec3(smoothstep(-thickness, 0.0, dis));
//          col *= vec3(uv, 1.0);
//          gl_FragColor = vec4(col, 1.0);
//        }
//
//    """.trimIndent()
    val fragSrc = """
        precision mediump float;
        uniform vec2 resolution;
        uniform float time;
        void main()	{
          vec2 uv = gl_FragCoord.xy/resolution;
          // uv = uv * 2.0 - 1.0;
          vec2 p = - 1.0 + 2.0 * uv;
          float a = time * 40.0;
          float d, e, f, g = 1.0 / 40.0 ,h ,i ,r ,q;

          e = 400.0 * ( p.x * 0.5 + 0.5 );
          f = 400.0 * ( p.y * 0.5 + 0.5 );
          i = 200.0 + sin( e * g + a / 150.0 ) * 20.0;
          d = 200.0 + cos( f * g / 2.0 ) * 18.0 + cos( e * g ) * 7.0;
          r = sqrt( pow( abs( i - e ), 2.0 ) + pow( abs( d - f ), 2.0 ) );
          q = f / r;
          e = ( r * cos( q ) ) - a / 2.0;
          f = ( r * sin( q ) ) - a / 2.0;
          d = sin( e * g ) * 176.0 + sin( e * g ) * 164.0 + r;
          h = ( ( f + d ) + a / 2.0 ) * g;
          i = cos( h + r * p.x / 1.3 ) * ( e + e + a ) + cos( q * g * 6.0 ) * ( r + h / 3.0 );
          h = sin( f * g ) * 144.0 - sin( e * g ) * 212.0 * p.x;
          h = ( h + ( f - e ) * q + sin( r - ( a + h ) / 7.0 ) * 10.0 + i / 4.0 ) * g;
          i += cos( h * 2.3 * sin( a / 350.0 - q ) ) * 184.0 * sin( q - ( r * 4.3 + a / 12.0 ) * g ) + tan( r * g + h ) * 184.0 * cos( r * g + h );
          i = mod( i / 5.6, 256.0 ) / 64.0;
          if ( i < 0.0 ) i += 4.0;
          if ( i >= 2.0 ) i = 4.0 - i;
          d = r / 350.0;
          d += sin( d * d * 8.0 ) * 0.52;
          f = ( sin( a * g ) + 1.0 ) / 2.0;
          gl_FragColor = vec4( vec3( f * i / 1.6, i / 2.0 + d / 13.0, i ) * d * p.x + vec3( i / 1.3 + d / 8.0, i / 2.0 + d / 18.0, i ) * d * ( 1.0 - p.x ), 1.0 );
        }
    """.trimIndent()

    val vertxBuffer = floatArrayOf(
        -1.0f, 1.0f,
        -1.0f, -1.0f,
        1.0f, -1.0f,
        -1.0f, 1.0f,
        1.0f, -1.0f,
        1.0f, 1.0f,
    )

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
//        Log.i("MyRenderer", "onSurfaceChanged: ${GLES30.glGetString(GLES30.GL_VERSION)}")
        Log.i("MyRenderer", "onSurfaceChanged: ${getGLSLVersion()}, width: $width, height: $height")
//        clear()

        // create program
        val vs = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER)
        GLES30.glShaderSource(vs, vertSrc)
        GLES30.glCompileShader(vs)
        checkError()
        val fs = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER)
        GLES30.glShaderSource(fs, fragSrc)
        GLES30.glCompileShader(fs)
        checkError()
        program = GLES30.glCreateProgram()
        GLES30.glAttachShader(program, vs)
        GLES30.glAttachShader(program, fs)
        GLES30.glLinkProgram(program)
        checkError()

        vertexByteBuffer = ByteBuffer.allocateDirect(vertxBuffer.size * Float.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexByteBuffer.put(vertxBuffer)
            .position(0)
        val vbo = IntArray(1)
        GLES30.glGenBuffers(1, vbo, 0)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0])
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER,
            vertexByteBuffer.capacity() * 4,
            vertexByteBuffer,
            GLES30.GL_STATIC_DRAW
        )
        checkError()
        GLES30.glUseProgram(program)
        val resUniform = GLES30.glGetUniformLocation(program, "resolution")
        GLES30.glUniform2f(resUniform, width.toFloat(), height.toFloat())
        timeUniform = GLES30.glGetUniformLocation(program, "time")
    }

    var time: Float = 0.0f
    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        GLES30.glUseProgram(program)
        Log.i("MyRenderer", "onDrawFrame: $time")
        time += 0.03f
        if (time > 60.0f) time = 0.0f
        GLES30.glUniform1f(timeUniform, time)
        val posAttribLocation = GLES30.glGetAttribLocation(program, "position")
        GLES30.glEnableVertexAttribArray(posAttribLocation)
        GLES30.glVertexAttribPointer(
            posAttribLocation, 2, GLES30.GL_FLOAT, false,
            2 * 4, 0
        )
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6)
        GLES30.glDisableVertexAttribArray(posAttribLocation)
    }

    private fun checkError() {
        val error = GLES30.glGetError();
        if (error != GLES30.GL_NO_ERROR) {
            Log.e("MyRenderer", "GL error $error", RuntimeException())
        }
    }

    external fun getGLSLVersion(): String
    external fun clear()
}
