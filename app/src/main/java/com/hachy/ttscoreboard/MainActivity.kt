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
    enum class Counter { L_SCORE, R_SCORE, L_GAME, R_GAME }

    data class ScoreState(var score: Int = 0, var game: Int = 0)

    private val left = ScoreState()
    private val right = ScoreState()

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
        setContentView(binding.root)

        handleConsentAndInitAds()

        restoreState(savedInstanceState)

        setupGesture(binding.scoreLeft, Counter.L_SCORE)
        setupGesture(binding.scoreRight, Counter.R_SCORE)
        setupGesture(binding.gameLeft, Counter.L_GAME)
        setupGesture(binding.gameRight, Counter.R_GAME)

        binding.changeEndsButton.setOnClickListener { swapEnds() }
        binding.resetScoreButton.setOnClickListener { resetScore() }
        binding.resetAllButton.setOnClickListener { resetAll() }

        updateUI()
    }

    private fun restoreState(savedInstanceState: Bundle?) {
        left.score = savedInstanceState?.getInt(STATE_L_SCORE) ?: 0
        right.score = savedInstanceState?.getInt(STATE_R_SCORE) ?: 0
        left.game = savedInstanceState?.getInt(STATE_L_GAME) ?: 0
        right.game = savedInstanceState?.getInt(STATE_R_GAME) ?: 0
    }

    private fun setupGesture(view: android.view.View, counter: Counter) {
        val detector = GestureDetector(this, MyGestureListener(counter))
        view.setOnTouchListener { v, event ->
            v.performClick()
            detector.onTouchEvent(event)
            true
        }
    }

    private fun swapEnds() {
        left.score = right.score.also { right.score = left.score }
        left.game = right.game.also { right.game = left.game }
        updateUI()
    }

    private fun resetScore() {
        left.score = 0
        right.score = 0
        updateUI()
    }

    private fun resetAll() {
        resetScore()
        left.game = 0
        right.game = 0
        updateUI()
    }

    private fun updateUI() {
        binding.scoreLeft.text = left.score.toString()
        binding.scoreRight.text = right.score.toString()
        binding.gameLeft.text = left.game.toString()
        binding.gameRight.text = right.game.toString()
    }

    inner class MyGestureListener(private val detector: Counter) : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
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
                Counter.L_SCORE -> left.score++
                Counter.R_SCORE -> right.score++
                Counter.L_GAME -> left.game++
                Counter.R_GAME -> right.game++
            }
            updateUI()
            return false
        }

        private fun onSwipeDown() {
            when (detector) {
                Counter.L_SCORE -> if (left.score > 0) left.score--
                Counter.R_SCORE -> if (right.score > 0) right.score--
                Counter.L_GAME -> if (left.game > 0) left.game--
                Counter.R_GAME -> if (right.game > 0) right.game--
            }
            updateUI()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_L_SCORE, left.score)
        outState.putInt(STATE_R_SCORE, right.score)
        outState.putInt(STATE_L_GAME, left.game)
        outState.putInt(STATE_R_GAME, right.game)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        restoreState(savedInstanceState)
        updateUI()
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
        fun getTestDeviceHashedId(context: Context): String {
            return context.getString(R.string.test_device_id)
        }
    }
}
