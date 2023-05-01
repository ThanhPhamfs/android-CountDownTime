package com.example.countdowntime

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.countdowntime.databinding.ActivityMainBinding
import com.example.countdowntime.models.CountDown

class MainActivity : AppCompatActivity() {
    private val TAG = "CountDownLog"
    private lateinit var statusReceiver: BroadcastReceiver
    private lateinit var timeReceiver: BroadcastReceiver
    private lateinit var binding: ActivityMainBinding

    private var isStopwatchRunning = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//
        binding.toggleButton.setOnClickListener {
            if (isStopwatchRunning) pauseStopwatch() else startStopwatch()
        }

        binding.resetImageView.setOnClickListener {
            resetStopwatch()
        }
//        object : CountDownTimer(30000, 1000) {
//            override fun onTick(millisUntilFinished: Long) {
////                mTextField.setText("seconds remaining: " + millisUntilFinished / 1000)
//                Toast.makeText(
//                    baseContext,
//                    "seconds remaining: " + millisUntilFinished / 1000,
//                    Toast.LENGTH_SHORT
//                ).show()
//
//            }
//            override fun onFinish() {
////                mTextField.setText("done!")
//                Toast.makeText(baseContext, "Done", Toast.LENGTH_SHORT).show()
//            }
//        }.start()
//        checkService()
    }
    override fun onStart() {
        super.onStart()

        // Moving the service to background when the app is visible
        moveToBackground()
        Log.d(TAG, "onStartActivity")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResumeActivity")

        getStopwatchStatus()

        // Receiving stopwatch status from service
        val statusFilter = IntentFilter()
        statusFilter.addAction(CountDownService.COUNTDOWN_TIMER_STATUS)
        statusReceiver = object : BroadcastReceiver() {
            @SuppressLint("SetTextI18n")
            override fun onReceive(p0: Context?, p1: Intent?) {
                val isRunning = p1?.getBooleanExtra(CountDownService.IS_COUNTDOWN_TIMER_RUNNING, false)!!
                isStopwatchRunning = isRunning
                val timeElapsed = p1.getIntExtra(CountDownService.TIME_ELAPSED, 0)

                updateLayout(isStopwatchRunning)
                updateStopwatchValue(timeElapsed)
            }
        }
        registerReceiver(statusReceiver, statusFilter)

        // Receiving time values from service
        val timeFilter = IntentFilter()
        timeFilter.addAction(CountDownService.COUNTDOWN_TIMER_TICK)
        timeReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                val timeElapsed = p1?.getIntExtra(CountDownService.TIME_ELAPSED, 0)!!
                updateStopwatchValue(timeElapsed)
            }
        }
        registerReceiver(timeReceiver, timeFilter)
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPauseActivity")

        unregisterReceiver(statusReceiver)
        unregisterReceiver(timeReceiver)

        // Moving the service to foreground when the app is in background / not visible
        moveToForeground()
    }

    @SuppressLint("SetTextI18n")
    private fun updateStopwatchValue(timeElapsed: Int) {
        val hours: Int = (timeElapsed / 60) / 60
        val minutes: Int = timeElapsed / 60
        val seconds: Int = timeElapsed % 60
        binding.stopwatchValueTextView.text =
            "${"%02d".format(hours)}:${"%02d".format(minutes)}:${"%02d".format(seconds)}"
    }

    private fun updateLayout(isStopwatchRunning: Boolean) {
        if (isStopwatchRunning) {
            binding.toggleButton.icon =
                ContextCompat.getDrawable(this, R.drawable.ic_pause)
            binding.resetImageView.visibility = View.INVISIBLE
        } else {
            binding.toggleButton.icon =
                ContextCompat.getDrawable(this, R.drawable.ic_play)
            binding.resetImageView.visibility = View.VISIBLE
        }
    }

    private fun getStopwatchStatus() {
        val stopwatchService = Intent(this, CountDownService::class.java)
        stopwatchService.putExtra(CountDownService.COUNTDOWN_TIMER_ACTION, CountDownService.GET_STATUS)
        startService(stopwatchService)
    }

    private fun startStopwatch() {
        Log.d(TAG, "Start")
        val stopwatchService = Intent(this, CountDownService::class.java)
        stopwatchService.putExtra(CountDownService.COUNTDOWN_TIMER_ACTION, CountDownService.START)
        startService(stopwatchService)
        checkService()
    }

    /**
     * Check service
     */
    private fun checkService() {
        Handler().postDelayed({
            val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            val services: List<ActivityManager.RunningServiceInfo> =
                manager.getRunningServices(Int.MAX_VALUE)
            Log.d(TAG, "${services.size}")
            for (serviceInfo in services) {
                val componentName = serviceInfo.service
                val serviceName = componentName.className
                Log.d(TAG, "$serviceName")
            }
        }, 5000)
    }

    private fun pauseStopwatch() {
        val stopwatchService = Intent(this, CountDownService::class.java)
        stopwatchService.putExtra(CountDownService.COUNTDOWN_TIMER_ACTION, CountDownService.PAUSE)
        startService(stopwatchService)
    }

    private fun resetStopwatch() {
        val stopwatchService = Intent(this, CountDownService::class.java)
        stopwatchService.putExtra(CountDownService.COUNTDOWN_TIMER_ACTION, CountDownService.RESET)
        startService(stopwatchService)
    }

    private fun moveToForeground() {
        val stopwatchService = Intent(this, CountDownService::class.java)
        stopwatchService.putExtra(
            CountDownService.COUNTDOWN_TIMER_ACTION,
            CountDownService.MOVE_TO_FOREGROUND
        )
        startService(stopwatchService)
    }

    private fun moveToBackground() {
        val stopwatchService = Intent(this, CountDownService::class.java)
        stopwatchService.putExtra(
            CountDownService.COUNTDOWN_TIMER_ACTION,
            CountDownService.MOVE_TO_BACKGROUND
        )
        startService(stopwatchService)
    }



}