package com.hachy.ttscoreboard

import androidx.core.view.GestureDetectorCompat
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.GestureDetector
import android.view.MotionEvent

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.abs


class MainActivity : AppCompatActivity() {

    enum class Counter {
        LSCORE, RSCORE, LGAME, RGAME
    }

    private var lscore = 0
    private var rscore = 0
    private var lgame = 0
    private var rgame = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val detectorLScore = GestureDetectorCompat(this, MyGestureListener(Counter.LSCORE))
        val detectorRScore = GestureDetectorCompat(this, MyGestureListener(Counter.RSCORE))
        val detectorLGame = GestureDetectorCompat(this, MyGestureListener(Counter.LGAME))
        val detectorRGame = GestureDetectorCompat(this, MyGestureListener(Counter.RGAME))

        lscore = savedInstanceState?.getInt(STATE_L_SCORE) ?: 0
        rscore = savedInstanceState?.getInt(STATE_R_SCORE) ?: 0
        lgame = savedInstanceState?.getInt(STATE_L_GAME) ?: 0
        rgame = savedInstanceState?.getInt(STATE_R_GAME) ?: 0

        showScore()
        showGame()

        scoreLeft.setOnTouchListener { v, motionEvent ->
            v.performClick()
            detectorLScore.onTouchEvent(motionEvent)
            true
        }

        scoreRight.setOnTouchListener { v, motionEvent ->
            v.performClick()
            detectorRScore.onTouchEvent(motionEvent)
            true
        }

        gameLeft.setOnTouchListener { v, motionEvent ->
            v.performClick()
            detectorLGame.onTouchEvent(motionEvent)
            true
        }

        gameRight.setOnTouchListener { v, motionEvent ->
            v.performClick()
            detectorRGame.onTouchEvent(motionEvent)
            true
        }

        changeEndsButton.setOnClickListener {
            val ls = lscore
            lscore = rscore
            rscore = ls
            val lg = lgame
            lgame = rgame
            rgame = lg
            showScore()
            showGame()
        }

        resetScoreButton.setOnClickListener {
            resetScore()
        }

        resetAllButton.setOnClickListener {
            resetScore()
            lgame = 0
            rgame = 0
            showGame()
        }

        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun resetScore() {
        lscore = 0
        rscore = 0
        showScore()
    }

    private fun showScore() {
        scoreLeft.text = lscore.toString()
        scoreRight.text = rscore.toString()
    }

    private fun showGame() {
        gameLeft.text = lgame.toString()
        gameRight.text = rgame.toString()
    }

    inner class MyGestureListener(private val detector: Counter) : GestureDetector.SimpleOnGestureListener() {

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (abs(e1.x - e2.x) > SWIPE_MAX_OFF_PATH) {
                return false
            }

            val distance = e1.y - e2.y
            val enoughSpeed = abs(velocityY) > SWIPE_THRESHOLD_VELOCITY

            return if (distance < -SWIPE_MIN_DISTANCE && enoughSpeed) {
                onSwipeDown()
                true
            } else {
                // do nothing
                false
            }
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            when (detector) {
                Counter.LSCORE -> {
                    lscore++
                    scoreLeft.text = lscore.toString()
                }
                Counter.RSCORE -> {
                    rscore++
                    scoreRight.text = rscore.toString()
                }
                Counter.LGAME -> {
                    lgame++
                    gameLeft.text = lgame.toString()
                }
                Counter.RGAME -> {
                    rgame++
                    gameRight.text = rgame.toString()
                }
            }
            return false
        }

        private fun onSwipeDown() {
            when (detector) {
                Counter.LSCORE -> if (lscore > 0) {
                    lscore--
                    scoreLeft.text = lscore.toString()
                }
                Counter.RSCORE -> if (rscore > 0) {
                    rscore--
                    scoreRight.text = rscore.toString()
                }
                Counter.LGAME -> if (lgame > 0) {
                    lgame--
                    gameLeft.text = lgame.toString()
                }
                Counter.RGAME -> if (rgame > 0) {
                    rgame--
                    gameRight.text = rgame.toString()
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_L_SCORE, lscore)
        outState.putInt(STATE_R_SCORE, rscore)
        outState.putInt(STATE_L_GAME, lgame)
        outState.putInt(STATE_R_GAME, rgame)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        lscore = savedInstanceState.getInt(STATE_L_SCORE)
        rscore = savedInstanceState.getInt(STATE_R_SCORE)
        lgame = savedInstanceState.getInt(STATE_L_GAME)
        rgame = savedInstanceState.getInt(STATE_R_GAME)
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
