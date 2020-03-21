package com.gelostech.dankmemes.utils

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.gelostech.dankmemes.R
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method


/**
 * A square relative layout
 */
class SquareLayout : RelativeLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.SquareLayout)

        for (i in 0..a.indexCount) {
            when (val attr = a.getIndex(i)) {
                R.styleable.SquareLayout_onLongClick -> {
                    if (context.isRestricted) throw IllegalStateException("On long press cannot be used on restricted context")

                    val handlerName: String? = a.getString(attr)
                    var handler: Method?

                    handlerName?.let {
                        setOnLongClickListener {
                            var result = false

                            try {
                                handler = getContext().javaClass.getMethod(handlerName, View::class.java)
                            } catch (e: NoSuchMethodException) {
                                val id = id
                                val idText = if (id === View.NO_ID) "" else " with id '" + getContext().resources.getResourceEntryName(
                                        id) + "'"
                                throw java.lang.IllegalStateException("Could not find a method " +
                                        handlerName.toString() + "(View) in the activity "
                                        + getContext().javaClass.toString() + " for onKeyLongPress handler" + " on view " + javaClass.toString() + idText, e)
                            }

                            result = try {
                                handler?.invoke(getContext(), this::class.java)
                                true
                            } catch (e: IllegalAccessException) {
                                throw java.lang.IllegalStateException("Could not execute non "
                                        + "public method of the activity", e)
                            } catch (e: InvocationTargetException) {
                                throw java.lang.IllegalStateException("Could not execute "
                                        + "method of the activity", e)
                            } catch (e: Exception) {
                                false
                            }

                            return@setOnLongClickListener result
                        }
                    }
                }
            }
        }

        a.recycle()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

}
