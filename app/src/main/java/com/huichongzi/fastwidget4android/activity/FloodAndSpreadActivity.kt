package com.huichongzi.fastwidget4android.activity

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.os.Bundle
import android.view.View
import com.huichongzi.fastwidget4android.R
import kotlinx.android.synthetic.main.flood_and_spread_activity.*

/**
 * Created by hcui on 9/6/17.
 */
class FloodAndSpreadActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.flood_and_spread_activity)
        init()
    }

    fun init(){
        val height: Int = window.windowManager.defaultDisplay.height
        var floodWrapper = ViewWrapper(animation_content)
        var spreadWrapper = ViewWrapper(spread_view)
        var floodAnimation = ObjectAnimator.ofInt(floodWrapper, "height", height)
        floodAnimation.duration = 1000
        floodAnimation.start()
        floodAnimation.addListener(object: Animator.AnimatorListener{
            override fun onAnimationRepeat(p0: Animator?) {
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationStart(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
                ObjectAnimator.ofInt(spreadWrapper, "height", height).setDuration(1000).start()
            }
        })
    }

    class ViewWrapper (var mTarget: View){
        fun getHeight():Int{
            return mTarget.layoutParams.height
        }

        fun setHeight(height: Int){
            mTarget.layoutParams.height = height
            mTarget.requestLayout()
        }
    }
}