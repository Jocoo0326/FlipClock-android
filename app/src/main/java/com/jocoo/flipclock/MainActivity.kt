package com.jocoo.flipclock

import android.os.Bundle
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private var _mCurState: Lifecycle.State = Lifecycle.State.INITIALIZED
        set(value) {
            field = value
            mLifeCycleReg.currentState = _mCurState
        }
    private val mLifeCycleReg = LifecycleRegistry(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(MyGLSurfaceView(this))
//        setContentView(R.layout.activity_main)
        setContentView(PathEffectView(this))
//        val img = findViewById<ImageView>(R.id.img)
//        val tv = findViewById<TextView>(R.id.tv)
//        img.pivotX = 0f
//        tv.setOnClickListener {
//            img.pivotY = img.height.toFloat()
//            img.animate().rotationX(-90f).setListener(object : AnimatorListenerAdapter() {
//                override fun onAnimationEnd(animation: Animator?) {
//                    super.onAnimationEnd(animation)
////                    img.rotationX = 270f
//                    img.animate().rotationX(0f).setListener(null)
//                }
//            })
//            _mCurState = Lifecycle.State.CREATED
//            mLifeCycleReg.addObserver(object : LifecycleEventObserver {
//                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
//                    println("2 $event")
//                }
//            })
//        }
        mLifeCycleReg.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                println("1 $event")
            }
        })
        thread(name = "special thread") {
            testThreadAnnotation()
        }
    }

    @MainThread
    private fun testThreadAnnotation() {
        println("testThreadAnnotation ${Thread.currentThread()}")
    }
}