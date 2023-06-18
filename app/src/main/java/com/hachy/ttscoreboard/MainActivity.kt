package com.hachy.ttscoreboard

import android.annotation.SuppressLint
import android.os.Build
import androidx.core.view.GestureDetectorCompat
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity
import android.view.GestureDetector
import android.view.MotionEvent

import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.hachy.ttscoreboard.databinding.ActivityMainBinding
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
    private lateinit var adView: AdManagerAdView
    private var initialLayoutComplete = false

    @Suppress("DEPRECATION")
    private val adSize: AdSize
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics = windowManager.currentWindowMetrics
                val bounds = windowMetrics.bounds
                var adWidthPixels = binding.adViewContainer.width.toFloat()
                if (adWidthPixels == 0f) {
                    adWidthPixels = bounds.width().toFloat()
                }
                val density = resources.displayMetrics.density
                val adWidth = (adWidthPixels / density).toInt()

                return AdSize.getLandscapeAnchoredAdaptiveBannerAdSize(this, adWidth)
            } else {
                val display = windowManager.defaultDisplay
                val outMetrics = DisplayMetrics()
                display.getMetrics(outMetrics)
                val density = outMetrics.density
                var adWidthPixels = binding.adViewContainer.width.toFloat()
                if (adWidthPixels == 0f) {
                    adWidthPixels = outMetrics.widthPixels.toFloat()
                }
                val adWidth = (adWidthPixels / density).toInt()
                return AdSize.getLandscapeAnchoredAdaptiveBannerAdSize(this, adWidth)
            }
        }

    @SuppressLint("VisibleForTests")
    private fun loadBanner() {
        adView.adUnitId = resources.getString(R.string.banner_ad_unit_id_test)
        adView.setAdSizes(adSize, AdSize.BANNER)
        val adRequest = AdManagerAdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val detectorLScore = GestureDetectorCompat(this, MyGestureListener(Counter.L_SCORE))
        val detectorRScore = GestureDetectorCompat(this, MyGestureListener(Counter.R_SCORE))
        val detectorLGame = GestureDetectorCompat(this, MyGestureListener(Counter.L_GAME))
        val detectorRGame = GestureDetectorCompat(this, MyGestureListener(Counter.R_GAME))

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

        MobileAds.initialize(this) {}

        adView = AdManagerAdView(this)
        binding.adViewContainer.addView(adView)
        binding.adViewContainer.viewTreeObserver.addOnGlobalLayoutListener {
            if (!initialLayoutComplete) {
                initialLayoutComplete = true
                loadBanner()
            }
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

    companion object {
        private const val SWIPE_MIN_DISTANCE = 120
        private const val SWIPE_MAX_OFF_PATH = 250
        private const val SWIPE_THRESHOLD_VELOCITY = 200
        private const val STATE_L_SCORE = "state_left_score"
        private const val STATE_R_SCORE = "state_right_score"
        private const val STATE_L_GAME = "state_left_game"
        private const val STATE_R_GAME = "state_right_game"
    }
}
