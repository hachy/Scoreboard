package com.hachy.ttscoreboard;

import android.support.v4.view.GestureDetectorCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


public class MainActivity extends AppCompatActivity {
    private GestureDetectorCompat mDetectorLScore;
    private GestureDetectorCompat mDetectorRScore;
    private GestureDetectorCompat mDetectorLGame;
    private GestureDetectorCompat mDetectorRGame;
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private int lscore = 0;
    private int rscore = 0;
    private int lgame = 0;
    private int rgame = 0;
    private TextView leftScore;
    private TextView rightScore;
    private TextView leftGame;
    private TextView rightGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDetectorLScore = new GestureDetectorCompat(this, new MyGestureListener("leftScore"));
        mDetectorRScore = new GestureDetectorCompat(this, new MyGestureListener("rightScore"));
        mDetectorLGame = new GestureDetectorCompat(this, new MyGestureListener("leftGame"));
        mDetectorRGame = new GestureDetectorCompat(this, new MyGestureListener("rightGame"));

        leftScore = (TextView) findViewById(R.id.scoreLeft);
        rightScore = (TextView) findViewById(R.id.scoreRight);
        leftGame = (TextView) findViewById(R.id.gameLeft);
        rightGame = (TextView) findViewById(R.id.gameRight);

        Button changeEndsButton = (Button) findViewById(R.id.changeEndsButton);
        Button resetScoreButton = (Button) findViewById(R.id.resetScoreButton);
        Button resetAllButton = (Button) findViewById(R.id.resetAllButton);

        showScore();
        showGame();

        leftScore.setOnTouchListener(leftScoreListener);
        rightScore.setOnTouchListener(rightScoreListener);
        leftGame.setOnTouchListener(leftGameListener);
        rightGame.setOnTouchListener(rightGameListener);

        changeEndsButton.setOnClickListener(changeEndsListener);
        resetScoreButton.setOnClickListener(resetScoreListener);
        resetAllButton.setOnClickListener(resetAllListener);

        AdView mAdView = (AdView) this.findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build(); // release用
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // すべてのエミュレータ
                .addTestDevice(getResources().getString(R.string.test_device_id))  // テスト用携帯電話
                .build();
        mAdView.loadAd(adRequest);
    }

    View.OnTouchListener leftScoreListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mDetectorLScore.onTouchEvent(motionEvent);
            return true;
        }
    };

    View.OnTouchListener rightScoreListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mDetectorRScore.onTouchEvent(motionEvent);
            return true;
        }
    };

    View.OnTouchListener leftGameListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mDetectorLGame.onTouchEvent(motionEvent);
            return true;
        }
    };

    View.OnTouchListener rightGameListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mDetectorRGame.onTouchEvent(motionEvent);
            return true;
        }
    };

    View.OnClickListener changeEndsListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int ls = lscore;
            lscore = rscore;
            rscore = ls;
            int lg = lgame;
            lgame = rgame;
            rgame = lg;
            showScore();
            showGame();
        }
    };

    View.OnClickListener resetScoreListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            resetScore();
        }
    };

    View.OnClickListener resetAllListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            resetScore();
            lgame = 0;
            rgame = 0;
            showGame();
        }
    };

    public void resetScore() {
        lscore = 0;
        rscore = 0;
        showScore();
    }

    protected void showScore() {
        leftScore.setText(String.valueOf(lscore));
        rightScore.setText(String.valueOf(rscore));
    }

    protected void showGame() {
        leftGame.setText(String.valueOf(lgame));
        rightGame.setText(String.valueOf(rgame));
    }

    public class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private String detector;

        public MyGestureListener(String detector) {
            this.detector = detector;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH) {
                return false;
            }

            final float distance = e1.getY() - e2.getY();
            final boolean enoughSpeed = Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY;

            if (distance < -SWIPE_MIN_DISTANCE && enoughSpeed) {
                onSwipeDown();
                return true;
            } else {
                // do nothing
                return false;
            }
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            switch (detector) {
                case "leftScore":
                    lscore++;
                    leftScore.setText(String.valueOf(lscore));
                    break;
                case "rightScore":
                    rscore++;
                    rightScore.setText(String.valueOf(rscore));
                    break;
                case "leftGame":
                    lgame++;
                    leftGame.setText(String.valueOf(lgame));
                    break;
                case "rightGame":
                    rgame++;
                    rightGame.setText(String.valueOf(rgame));
                    break;
            }
            return false;
        }

        protected void onSwipeDown() {
            switch (detector) {
                case "leftScore":
                    if (lscore > 0) {
                        lscore--;
                        leftScore.setText(String.valueOf(lscore));
                    }
                    break;
                case "rightScore":
                    if (rscore > 0) {
                        rscore--;
                        rightScore.setText(String.valueOf(rscore));
                    }
                    break;
                case "leftGame":
                    if (lgame > 0) {
                        lgame--;
                        leftGame.setText(String.valueOf(lgame));
                    }
                    break;
                case "rightGame":
                    if (rgame > 0) {
                        rgame--;
                        rightGame.setText(String.valueOf(rgame));
                    }
                    break;
            }
        }
    }

}
