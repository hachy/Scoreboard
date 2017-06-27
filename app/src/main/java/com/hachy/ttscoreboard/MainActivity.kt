package com.hachy.ttscoreboard

import android.support.v4.view.GestureDetectorCompat
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView


class MainActivity : AppCompatActivity() {
    private var mDetectorLScore: GestureDetectorCompat? = null
    private var mDetectorRScore: GestureDetectorCompat? = null
    private var mDetectorLGame: GestureDetectorCompat? = null
    private var mDetectorRGame: GestureDetectorCompat? = null
    private var lscore = 0
    private var rscore = 0
    private var lgame = 0
    private var rgame = 0
    private var leftScore: TextView? = null
    private var rightScore: TextView? = null
    private var leftGame: TextView? = null
    private var rightGame: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mDetectorLScore = GestureDetectorCompat(this, MyGestureListener("leftScore"))
        mDetectorRScore = GestureDetectorCompat(this, MyGestureListener("rightScore"))
        mDetectorLGame = GestureDetectorCompat(this, MyGestureListener("leftGame"))
        mDetectorRGame = GestureDetectorCompat(this, MyGestureListener("rightGame"))

        leftScore = findViewById(R.id.scoreLeft) as TextView
        rightScore = findViewById(R.id.scoreRight) as TextView
        leftGame = findViewById(R.id.gameLeft) as TextView
        rightGame = findViewById(R.id.gameRight) as TextView

        val changeEndsButton = findViewById(R.id.changeEndsButton) as Button
        val resetScoreButton = findViewById(R.id.resetScoreButton) as Button
        val resetAllButton = findViewById(R.id.resetAllButton) as Button

        if (savedInstanceState != null) {
            lscore = savedInstanceState.getInt(STATE_L_SCORE)
            rscore = savedInstanceState.getInt(STATE_R_SCORE)
            lgame = savedInstanceState.getInt(STATE_L_GAME)
            rgame = savedInstanceState.getInt(STATE_R_GAME)
        }

        showScore()
        showGame()

        leftScore!!.setOnTouchListener { view, motionEvent ->
            mDetectorLScore!!.onTouchEvent(motionEvent)
            true
        }
        rightScore!!.setOnTouchListener(rightScoreListener)
        leftGame!!.setOnTouchListener(leftGameListener)
        rightGame!!.setOnTouchListener(rightGameListener)

        changeEndsButton.setOnClickListener(changeEndsListener)
        resetScoreButton.setOnClickListener(resetScoreListener)
        resetAllButton.setOnClickListener(resetAllListener)

        val mAdView = this.findViewById(R.id.adView) as AdView
        //        AdRequest adRequest = new AdRequest.Builder().build(); // release用
        val adRequest = AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // すべてのエミュレータ
                .addTestDevice(resources.getString(R.string.test_device_id))  // テスト用携帯電話
                .build()
        mAdView.loadAd(adRequest)
    }

    internal var leftScoreListener: View.OnTouchListener = View.OnTouchListener { view, motionEvent ->
        mDetectorLScore!!.onTouchEvent(motionEvent)
        true
    }

    internal var rightScoreListener: View.OnTouchListener = View.OnTouchListener { view, motionEvent ->
        mDetectorRScore!!.onTouchEvent(motionEvent)
        true
    }

    internal var leftGameListener: View.OnTouchListener = View.OnTouchListener { view, motionEvent ->
        mDetectorLGame!!.onTouchEvent(motionEvent)
        true
    }

    internal var rightGameListener: View.OnTouchListener = View.OnTouchListener { view, motionEvent ->
        mDetectorRGame!!.onTouchEvent(motionEvent)
        true
    }

    internal var changeEndsListener: View.OnClickListener = View.OnClickListener {
        val ls = lscore
        lscore = rscore
        rscore = ls
        val lg = lgame
        lgame = rgame
        rgame = lg
        showScore()
        showGame()
    }

    internal var resetScoreListener: View.OnClickListener = View.OnClickListener { resetScore() }

    internal var resetAllListener: View.OnClickListener = View.OnClickListener {
        resetScore()
        lgame = 0
        rgame = 0
        showGame()
    }

    fun resetScore() {
        lscore = 0
        rscore = 0
        showScore()
    }

    protected fun showScore() {
        leftScore!!.text = lscore.toString()
        rightScore!!.text = rscore.toString()
    }

    protected fun showGame() {
        leftGame!!.text = lgame.toString()
        rightGame!!.text = rgame.toString()
    }

    inner class MyGestureListener private constructor(private val detector: String) : GestureDetector.SimpleOnGestureListener() {

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
                "leftScore" -> {
                    lscore++
                    leftScore!!.text = lscore.toString()
                }
                "rightScore" -> {
                    rscore++
                    rightScore!!.text = rscore.toString()
                }
                "leftGame" -> {
                    lgame++
                    leftGame!!.text = lgame.toString()
                }
                "rightGame" -> {
                    rgame++
                    rightGame!!.text = rgame.toString()
                }
            }
            return false
        }

        private fun onSwipeDown() {
            when (detector) {
                "leftScore" -> if (lscore > 0) {
                    lscore--
                    leftScore!!.text = lscore.toString()
                }
                "rightScore" -> if (rscore > 0) {
                    rscore--
                    rightScore!!.text = rscore.toString()
                }
                "leftGame" -> if (lgame > 0) {
                    lgame--
                    leftGame!!.text = lgame.toString()
                }
                "rightGame" -> if (rgame > 0) {
                    rgame--
                    rightGame!!.text = rgame.toString()
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
