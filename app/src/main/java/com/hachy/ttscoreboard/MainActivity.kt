package com.hachy.ttscoreboard

import android.support.v4.view.GestureDetectorCompat
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.GestureDetector
import android.view.MotionEvent

import com.google.android.gms.ads.AdRequest
import kotlinx.android.synthetic.main.activity_main.*


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

        val DetectorLScore = GestureDetectorCompat(this, MyGestureListener(Counter.LSCORE))
        val DetectorRScore = GestureDetectorCompat(this, MyGestureListener(Counter.RSCORE))
        val DetectorLGame = GestureDetectorCompat(this, MyGestureListener(Counter.LGAME))
        val DetectorRGame = GestureDetectorCompat(this, MyGestureListener(Counter.RGAME))

        if (savedInstanceState != null) {
            lscore = savedInstanceState.getInt(STATE_L_SCORE)
            rscore = savedInstanceState.getInt(STATE_R_SCORE)
            lgame = savedInstanceState.getInt(STATE_L_GAME)
            rgame = savedInstanceState.getInt(STATE_R_GAME)
        }

        showScore()
        showGame()

        scoreLeft.setOnTouchListener { _, motionEvent ->
            DetectorLScore.onTouchEvent(motionEvent)
            true
        }

        scoreRight.setOnTouchListener { _, motionEvent ->
            DetectorRScore.onTouchEvent(motionEvent)
            true
        }

        gameLeft.setOnTouchListener { _, motionEvent ->
            DetectorLGame.onTouchEvent(motionEvent)
            true
        }

        gameRight.setOnTouchListener { _, motionEvent ->
            DetectorRGame.onTouchEvent(motionEvent)
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

        //        AdRequest adRequest = new AdRequest.Builder().build(); // release用
        val adRequest = AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // すべてのエミュレータ
                .addTestDevice(resources.getString(R.string.test_device_id))  // テスト用携帯電話
                .build()
        adView.loadAd(adRequest)
    }

    fun resetScore() {
        lscore = 0
        rscore = 0
        showScore()
    }

    fun showScore() {
        scoreLeft.text = lscore.toString()
        scoreRight.text = rscore.toString()
    }

    fun showGame() {
        gameLeft.text = lgame.toString()
        gameRight.text = rgame.toString()
    }

    inner class MyGestureListener(private val detector: Counter) : GestureDetector.SimpleOnGestureListener() {

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (Math.abs(e1.x - e2.x) > SWIPE_MAX_OFF_PATH) {
                return false
            }

            val distance = e1.y - e2.y
            val enoughSpeed = Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY

            if (distance < -SWIPE_MIN_DISTANCE && enoughSpeed) {
                onSwipeDown()
                return true
            } else {
                // do nothing
                return false
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
        private val SWIPE_MIN_DISTANCE = 120
        private val SWIPE_MAX_OFF_PATH = 250
        private val SWIPE_THRESHOLD_VELOCITY = 200
        private val STATE_L_SCORE = "state_left_score"
        private val STATE_R_SCORE = "state_right_score"
        private val STATE_L_GAME = "state_left_game"
        private val STATE_R_GAME = "state_right_game"
    }
}
