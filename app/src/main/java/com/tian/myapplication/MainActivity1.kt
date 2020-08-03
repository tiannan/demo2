package com.tian.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.lang.RuntimeException

class MainActivity1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        throw RuntimeException("hehe")
    }
}