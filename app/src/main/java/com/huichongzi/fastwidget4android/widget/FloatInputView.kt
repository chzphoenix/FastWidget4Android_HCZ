package com.huichongzi.fastwidget4android.widget

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.Gravity
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import com.huichongzi.fastwidget4android.R


class FloatInputView : FrameLayout {

    var activity : Activity? = null
    lateinit var content : View
    lateinit var edit : EditText

    var rootListener = ViewTreeObserver.OnGlobalLayoutListener {
        var rect = Rect()
        //这里用R.id.content，而不用rootView（DecorView）的原因是rootView还包含虚拟导航栏区域，这就需要判断导航栏是否显示，否则计算键盘高度会出现偏差
        //但是android系统目前还没有官方的api去判断导航栏是否显示，网上的方法都不完全保险。
        //而R.id.content区域则是不包含虚拟导航栏区域的（如果隐藏了虚拟导航栏，这个就与rootView区域一致），所以可以忽略这个问题
        var contentView = rootView.findViewById<View>(android.R.id.content)
        contentView.getWindowVisibleDisplayFrame(rect)  //获取窗口的显示区域

        if(lastBottom == rect.bottom)
            return@OnGlobalLayoutListener

        //这里使用bottom而不是height来比较，是因为height还需要考虑全屏与否的通知栏问题。
        //如果显示区域的bottom小于窗口的实际bottom，说明键盘弹出
        if(rect.bottom < contentView.bottom){
            visibility = View.VISIBLE
            //因为content初始位置在最底部，虽然showInput中设置了setY()，但是这个函数实际上也是设置translationY，没有改变初始位置，只改变了显示位置
            //这时候键盘弹出，想要显示在键盘上面就必须将content向上移动键盘的高度。而contentView.bottom - rect.bottom就是键盘高度。
            translationY = -(contentView.bottom - rect.bottom).toFloat()
        }
        else{
            visibility = View.INVISIBLE
        }
        lastBottom = rect.bottom
    }
    var lastBottom = 0

    constructor(context: Context) : super(context){
        init()
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){
        init()
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        init()
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes){
        init()
    }

    fun init(){
        setOnClickListener {
            hideInput()
        }
        setContent(R.layout.float_input_default_layout, R.id.edit)
        initDefaultLayout()
        visibility = View.INVISIBLE
    }

    private fun initDefaultLayout(){
        findViewById<View>(R.id.send).setOnClickListener {
            hideInput()
        }
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        //延时一下，防止还没计算完布局导致键盘显示出来
        postDelayed({
            rootView.findViewById<View>(android.R.id.content).viewTreeObserver.addOnGlobalLayoutListener(rootListener)
        }, 500)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        rootView.findViewById<View>(android.R.id.content).viewTreeObserver.removeOnGlobalLayoutListener(rootListener)
    }

    fun setContent(@LayoutRes layoutId : Int, @IdRes editId : Int){
        removeAllViews()
        content = LayoutInflater.from(context).inflate(layoutId, null)
        var params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        params.gravity = Gravity.BOTTOM or Gravity.CENTER
        addView(content, params)
        content.isClickable = true

        edit = content.findViewById(editId)
    }

    fun showInput(){
        edit.requestFocus()
        y = -resources.displayMetrics.heightPixels.toFloat() //先移到屏幕顶部，这样键盘弹起时不会遮挡住输入框，就不会重新调整布局。否则会出现布局上推，内部布局缩小等情况
        val inputManager: InputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.showSoftInput(edit, 0)
    }

    fun hideInput(){
        val inputManager: InputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(edit.windowToken, 0)
    }
}