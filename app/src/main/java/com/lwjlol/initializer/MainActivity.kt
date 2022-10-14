package com.lwjlol.initializer

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    fun TextView.appendd(s: String) {
        if (text.isNullOrBlank()) {
            text = s
        } else {
            text = this.text.toString() + "\n\n" + s;
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.btA).setOnClickListener {
            val textView = findViewById<TextView>(R.id.A)
            textView.text = ""
            Test.initializerA.init(this, debug = true, callback = object : Callback {
                override fun onInitializationStart(task: InitializeTask) {
                    textView.appendd("onInitializationStart, first is ${task::class.qualifiedName}")
                }

                override fun onTaskStart(task: InitializeTask) {
                    textView.appendd("onTaskStart ${task::class.qualifiedName}, dependency ${task.dependencies.toList()}")
                }

                override fun onTaskComplete(task: InitializeTask, timeConsuming: Long) {
                    textView.appendd("onTaskComplete ${task::class.qualifiedName}, dependency ${task.dependencies.toList()}, timeConsuming = $timeConsuming ms")
                }

                override fun onInitializationComplete(
                    lastTask: InitializeTask,
                    totalTimeConsuming: Long
                ) {
                    textView.appendd("onInitializationComplete, $totalTimeConsuming ms, lastTask is ${lastTask::class.qualifiedName}")
                }

            })
        }


        findViewById<View>(R.id.btB).setOnClickListener {
            val textView = findViewById<TextView>(R.id.A)
            textView.text = ""
            Test.initializerB.init(this, debug = true, callback = object : Callback {
                override fun onInitializationStart(task: InitializeTask) {
                    textView.appendd("onInitializationStart, first is ${task::class.qualifiedName}")
                }

                override fun onTaskStart(task: InitializeTask) {
                    textView.appendd("onTaskStart ${task::class.qualifiedName}, dependency ${task.dependencies.toList()}")
                }

                override fun onTaskComplete(task: InitializeTask, timeConsuming: Long) {
                    textView.appendd("onTaskComplete ${task::class.qualifiedName}, dependency ${task.dependencies.toList()}, timeConsuming = $timeConsuming ms")
                }

                override fun onInitializationComplete(
                    lastTask: InitializeTask,
                    totalTimeConsuming: Long
                ) {
                    textView.appendd("onInitializationComplete, $totalTimeConsuming ms, lastTask is ${lastTask::class.qualifiedName}")
                }

            })
        }


    }
}