package com.luciasoft.browserjavatokotlin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView

internal class MyInputDialog(
    context: Context,
    title: String = "",
    subTitle: String = "",
    doSomething: DoSomethingWithResult? = null
)
{
    private var builder: AlertDialog.Builder

    internal interface DoSomethingWithResult
    {
        fun doSomething(result: String)
    }

    init
    {
        builder = AlertDialog.Builder(context)
        @SuppressLint("InflateParams") val dialogLayout =
            LayoutInflater.from(context).inflate(R.layout.input_dialog, null)
        builder.setView(dialogLayout)
        val inputText = dialogLayout.findViewById<View>(R.id.inputText) as EditText
        (dialogLayout.findViewById<View>(R.id.title) as TextView).text = title
        (dialogLayout.findViewById<View>(R.id.subTitle) as TextView).text = subTitle
        builder.setPositiveButton(
            "OK", if (doSomething == null) null else
            DialogInterface.OnClickListener { dialog, which -> doSomething.doSomething(inputText.text.toString()) })
        builder.setNegativeButton("Cancel", null)
    }

    fun show()
    {
        builder.show()
    }
}