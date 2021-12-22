package com.huichongzi.fastwidget4android.activity


import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.annotation.ArrayRes
import android.support.annotation.LayoutRes
import android.support.annotation.Nullable
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import com.huichongzi.fastwidget4android.R
import com.huichongzi.fastwidget4android.utils.DisplayUtils


class SpinnerActivity : Activity() {
    var adapter : SpinnerAdapter<CharSequence>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.spinner_activity)
        val spinner = findViewById<Spinner>(R.id.spinner)
        adapter = SpinnerAdapter.createFromResource(this, R.array.grade, R.layout.spinner_layout)
        adapter?.setDropDownViewResource(R.layout.spinner_item)
        spinner.setBackgroundColor(0x0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            spinner.setPopupBackgroundResource(R.drawable.spinner_bg)
            spinner.dropDownVerticalOffset = DisplayUtils.dip2px(this, 0f)
        }
        spinner.setAdapter(adapter)
        spinner.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                adapter?.setSelectedPostion(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })
    }

    class SpinnerAdapter<T>(context: Context, resource: Int, objects: Array<T>) :
        ArrayAdapter<T>(context, resource, objects) {
        private var selectedPostion = 0
        fun setSelectedPostion(selectedPostion: Int) {
            this.selectedPostion = selectedPostion
        }

        override fun getDropDownView(
            position: Int,
            @Nullable convertView: View?,
            parent: ViewGroup
        ): View {
            val view: View = super.getDropDownView(position, convertView, parent)
            val textView = view as TextView
            if (selectedPostion == position) {
                textView.setTextColor(-0xc8c8bf)
                textView.paint.isFakeBoldText = true
            } else {
                textView.setTextColor(-0x929293)
                textView.paint.isFakeBoldText = false
            }
            return view
        }

        companion object {
            fun createFromResource(
                context: Context,
                @ArrayRes textArrayResId: Int, @LayoutRes textViewResId: Int
            ): SpinnerAdapter<CharSequence> {
                val strings: Array<CharSequence> =
                    context.getResources().getTextArray(textArrayResId)
                return SpinnerAdapter(context, textViewResId, strings)
            }
        }
    }
}