package com.example.navcomposesample

import android.util.Log
import androidx.lifecycle.ViewModel

class GraphViewModel: ViewModel() {
    var count = 0
    init {
        Log.d("viewmodlel", "init")
    }
}