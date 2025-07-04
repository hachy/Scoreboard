package com.hachy.ttscoreboard

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.GestureDetector
import android.view.MotionEvent
import com.google.android.gms.ads.AdRequest

import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.hachy.ttscoreboard.databinding.ActivityMainBinding
import com.hachy.ttscoreboard.utils.GoogleMobileAdsConsentManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs


class MainActivity : AppCompatActivity() {
    enum class Counter {
        L_SCORE, R_SCORE, L_GAME, R_GAME
    }

    private var leftScore = 0
    private var rightScore = 0
    private var leftGame = 0
    private var rightGame = 0

    private lateinit var binding: ActivityMainBinding
    private lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager
    private var adView: AdView? = null

    private fun handleConsentAndInitAds() {
        googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(applicationContext)
        googleMobileAdsConsentManager.gatherConsent(this) { error ->
            if (error != null) {
                Log.d("Log for ConsentForm", "${error.errorCode}: ${error.message}")
            }

            if (googleMobileAdsConsentManager.canRequestAds) {
                initAdMobBanner()
            }

            if (googleMobileAdsConsentManager.isPrivacyOptionsRequired) {
                // Regenerate the options menu to include a privacy setting.
                invalidateOptionsMenu()
            }
        }

        if (googleMobileAdsConsentManager.canRequestAds) {
            initAdMobBanner()
        }
    }

    private fun initAdMobBanner() {
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@MainActivity) {}
            runOnUiThread {
                loadBanner()
            }
        }
    }

    @SuppressLint("VisibleForTests")
    private fun loadBanner() {
        val adView = AdView(this)
        adView.adUnitId = resources.getString(R.string.banner_ad_unit_id_test)
        adView.setAdSize(getLandscapeAdaptiveAdSize)
        this.adView = adView

        binding.adViewContainer.removeAllViews()
        binding.adViewContainer.addView(adView)

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private val getLandscapeAdaptiveAdSize: AdSize
        get() {
            val adWidthPixels = calculateAdWidthPixels()
            val density = resources.displayMetrics.density
            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getLandscapeAnchoredAdaptiveBannerAdSize(this, adWidth)
        }

    @Suppress("DEPRECATION")
    private fun calculateAdWidthPixels(): Float {
        val width = binding.adViewContainer.width.toFloat()
        if (width > 0f) return width

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = windowManager.currentWindowMetrics.bounds
            bounds.width().toFloat()
        } else {
            val display = windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)
            outMetrics.widthPixels.toFloat()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        handleConsentAndInitAds()

        val detectorLScore = GestureDetector(this, MyGestureListener(Counter.L_SCORE))
        val detectorRScore = GestureDetector(this, MyGestureListener(Counter.R_SCORE))
        val detectorLGame = GestureDetector(this, MyGestureListener(Counter.L_GAME))
        val detectorRGame = GestureDetector(this, MyGestureListener(Counter.R_GAME))

        leftScore = savedInstanceState?.getInt(STATE_L_SCORE) ?: 0
        rightScore = savedInstanceState?.getInt(STATE_R_SCORE) ?: 0
        leftGame = savedInstanceState?.getInt(STATE_L_GAME) ?: 0
        rightGame = savedInstanceState?.getInt(STATE_R_GAME) ?: 0

        showScore()
        showGame()

        binding.scoreLeft.setOnTouchListener { v, motionEvent ->
            v.performClick()
            detectorLScore.onTouchEvent(motionEvent)
            true
        }

        binding.scoreRight.setOnTouchListener { v, motionEvent ->
            v.performClick()
            detectorRScore.onTouchEvent(motionEvent)
            true
        }

        binding.gameLeft.setOnTouchListener { v, motionEvent ->
            v.performClick()
            detectorLGame.onTouchEvent(motionEvent)
            true
        }

        binding.gameRight.setOnTouchListener { v, motionEvent ->
            v.performClick()
            detectorRGame.onTouchEvent(motionEvent)
            true
        }

        binding.changeEndsButton.setOnClickListener {
            leftScore = rightScore.also { rightScore = leftScore }
            leftGame = rightGame.also { rightGame = leftGame }
            showScore()
            showGame()
        }

        binding.resetScoreButton.setOnClickListener {
            resetScore()
        }

        binding.resetAllButton.setOnClickListener {
            resetScore()
            leftGame = 0
            rightGame = 0
            showGame()
        }
    }

    private fun resetScore() {
        leftScore = 0
        rightScore = 0
        showScore()
    }

    private fun showScore() {
        binding.scoreLeft.text = leftScore.toString()
        binding.scoreRight.text = rightScore.toString()
    }

    private fun showGame() {
        binding.gameLeft.text = leftGame.toString()
        binding.gameRight.text = rightGame.toString()
    }

    inner class MyGestureListener(private val detector: Counter) :
        GestureDetector.SimpleOnGestureListener() {

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 != null) {
                if (abs(e1.x - e2.x) > SWIPE_MAX_OFF_PATH) {
                    return false
                }

                val distance = e1.y - e2.y
                val enoughSpeed = abs(velocityY) > SWIPE_THRESHOLD_VELOCITY

                return if (distance < -SWIPE_MIN_DISTANCE && enoughSpeed) {
                    onSwipeDown()
                    true
                } else {
                    false
                }
            }
            return false
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            when (detector) {
                Counter.L_SCORE -> {
                    leftScore++
                    binding.scoreLeft.text = leftScore.toString()
                }

                Counter.R_SCORE -> {
                    rightScore++
                    binding.scoreRight.text = rightScore.toString()
                }

                Counter.L_GAME -> {
                    leftGame++
                    binding.gameLeft.text = leftGame.toString()
                }

                Counter.R_GAME -> {
                    rightGame++
                    binding.gameRight.text = rightGame.toString()
                }
            }
            return false
        }

        private fun onSwipeDown() {
            when (detector) {
                Counter.L_SCORE -> if (leftScore > 0) {
                    leftScore--
                    binding.scoreLeft.text = leftScore.toString()
                }

                Counter.R_SCORE -> if (rightScore > 0) {
                    rightScore--
                    binding.scoreRight.text = rightScore.toString()
                }

                Counter.L_GAME -> if (leftGame > 0) {
                    leftGame--
                    binding.gameLeft.text = leftGame.toString()
                }

                Counter.R_GAME -> if (rightGame > 0) {
                    rightGame--
                    binding.gameRight.text = rightGame.toString()
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_L_SCORE, leftScore)
        outState.putInt(STATE_R_SCORE, rightScore)
        outState.putInt(STATE_L_GAME, leftGame)
        outState.putInt(STATE_R_GAME, rightGame)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        leftScore = savedInstanceState.getInt(STATE_L_SCORE)
        rightScore = savedInstanceState.getInt(STATE_R_SCORE)
        leftGame = savedInstanceState.getInt(STATE_L_GAME)
        rightGame = savedInstanceState.getInt(STATE_R_GAME)
    }

    public override fun onPause() {
        adView?.pause()
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
        adView?.resume()
    }

    public override fun onDestroy() {
        adView?.destroy()
        super.onDestroy()
    }

    companion object {
        private const val SWIPE_MIN_DISTANCE = 120
        private const val SWIPE_MAX_OFF_PATH = 250
        private const val SWIPE_THRESHOLD_VELOCITY = 200
        private const val STATE_L_SCORE = "state_left_score"
        private const val STATE_R_SCORE = "state_right_score"
        private const val STATE_L_GAME = "state_left_game"
        private const val STATE_R_GAME = "state_right_game"
        fun getTestDeviceHashedId(context: Context):String{
            return context.getString(R.string.test_device_id)
        }
    }
}
