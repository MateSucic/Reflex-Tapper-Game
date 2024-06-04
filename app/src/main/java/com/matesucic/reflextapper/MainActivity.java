package com.matesucic.reflextapper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.atomic.AtomicInteger;


public class MainActivity extends AppCompatActivity {
    final byte circleNum = 6;
    final byte startingTime = 3;

    Handler mainHandler = new Handler();
    Context contextThis = this;
    public  int dp_to_px(int dp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
    public boolean hasNavBar(Context context) {
        Point realSize = new Point();
        Point screenSize = new Point();
        boolean hasNavBar = false;
        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        realSize.x = metrics.widthPixels;
        realSize.y = metrics.heightPixels;
        getWindowManager().getDefaultDisplay().getSize(screenSize);
        if (realSize.y != screenSize.y) {
            int difference = realSize.y - screenSize.y;
            int navBarHeight = 0;
            Resources resources = context.getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                navBarHeight = resources.getDimensionPixelSize(resourceId);
            }
            if (navBarHeight != 0) {
                if (difference == navBarHeight) {
                    hasNavBar = true;
                }
            }
        }
        return hasNavBar;

    }
    //Recreate app on resume app
    boolean firstTime=true;
    @Override
    public void onResume(){
        super.onResume();
        if(!firstTime) {
            shutdown=true;
            this.recreate();
        }
        firstTime=false;
    }

    long oldHiScore;
    public void createStartCircles(int minSize, int maxSize, String[] colors)
    {   FrameLayout startFrame = findViewById(R.id.frameStart);
        DisplayMetrics dMetrics = getResources().getDisplayMetrics();
        byte maxAllowed = (byte) (7*440/dMetrics.densityDpi);
        byte minAllowed = (byte) (maxAllowed-1);
        byte circleNum = (byte) (Math.random()*(maxAllowed-minAllowed+1)+minAllowed);
        byte max = (byte) maxSize, min = (byte) minSize;
        int[] x_pos = new int[circleNum];
        int[] y_pos = new int[circleNum];
        int[] size = new int[circleNum];
        int dpWidth = dMetrics.widthPixels;
        int dpHeight = dMetrics.heightPixels;
        LinearLayout startUi = findViewById(R.id.linearStartUI);
        TextView tvHiScore = findViewById(R.id.tvHiScore);
        startUi.post(() -> {
            SharedPreferences hiScoreStored = getSharedPreferences("highScore", 0);
            tvHiScore.setText(String.format(getResources().getString(R.string.key_tvHiScore)+" %s",hiScoreStored.getString("highScore","0")));
            int freeSpace = (dpHeight-startUi.getHeight())/2-dp_to_px(80);
            //loop circle creation
            for(int i=0;i<circleNum;i++) {
                boolean isNear;
                int minDistance;
                //Repeat when a circle inside of greeting sign or near circles
                do {
                    isNear=false;
                    //Randomize size of circle
                    size[i] = dp_to_px((int) (Math.random() * (max - min + 1) + min));
                    //Randomize position of circle
                    x_pos[i] = (int) (Math.random() * (dpWidth - size[i] + 1) + 0);
                    y_pos[i] = (int) (Math.random() * (dpHeight - size[i] + 1) + 0);
                    for (int j = 0; j < i; j++) {
                        minDistance = Math.max(size[i], size[j]);
                        if (Math.abs(x_pos[i] - x_pos[j]) < minDistance && Math.abs(y_pos[i] - y_pos[j]) < minDistance) {
                            isNear = true;
                            break;
                        }
                    }
                } while (y_pos[i] >= freeSpace && y_pos[i] <= dpHeight - freeSpace || isNear);


                //Create and edit background circles
                ImageView circle = new ImageView(contextThis);

                circle.setImageResource(R.drawable.round);
                int colorAmount = 0;
                for(int j=0;j<colors.length;j++)
                {
                    if(colors[j]==null)
                        break;
                    else
                        colorAmount++;
                }
                int ranColor = (int) (Math.random()*colorAmount);
                circle.setColorFilter(Color.parseColor(colors[ranColor]), PorterDuff.Mode.SRC_ATOP);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size[i], size[i]);
                params.leftMargin = x_pos[i];
                params.topMargin = y_pos[i];
                circle.setLayoutParams(params);
                startFrame.addView(circle);
            }
        });

    }

    volatile boolean shutdown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if(hasNavBar(this))
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        //Check if app is in light/dark mode to set circle colors
        int maxColorNum = 100;
        String[] colors = new String[maxColorNum];
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                String[] colorsDark = new String[]{"#88bae3", "#fff799", "#bd8cbf", "#a3d39c", "#fdc689"};
                System.arraycopy(colorsDark, 0, colors, 0, colorsDark.length);
                break;

            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                String[] colorsLight = new String[]{"#D4AC0D", "#28B463", "#D68910", "#17A589", "#2471A3", "#A93226"};
                System.arraycopy(colorsLight, 0, colors, 0, colorsLight.length);
                break;
        }
        int minCircleSize = 50;
        int maxCircleSize = 100;
        createStartCircles(minCircleSize, maxCircleSize, colors);
        Button btnStart = findViewById(R.id.btnStart);
        FrameLayout startFrame = findViewById(R.id.frameStart);
        FrameLayout playFrame = findViewById(R.id.framePlaying);
        btnStart.setOnClickListener(view -> {
            //Remove the start screen
            vibe.vibrate(100);
            startFrame.setVisibility(View.GONE);
            //Add the playing screen
            playFrame.setVisibility(View.VISIBLE);
            startCirclesThread(colors);
            startTimerThread();
            startCounterThread();
            startBackgroundClickThread();
    });
    }

    public void startCirclesThread(String[] colors) {
        circlesThread thread = new circlesThread(colors);
        new Thread(thread).start();
    }
    public void startTimerThread() {
        timerThread timerThread = new timerThread();
        new Thread(timerThread).start();
    }
    public void startCounterThread() {
        countThread countThread = new countThread();
        new Thread(countThread).start();
    }
    public void startBackgroundClickThread() {
        clickThread bckgrndThread = new clickThread();
        new Thread(bckgrndThread).start();
    }
    int cdTimer, counter, numMissed;
    long totalScore, baseScore, missedScore, timeScore;
    int childrenCount=0;
    ImageView[] circles = new ImageView[circleNum];
    class circlesThread implements Runnable {
        String[] colors;
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        circlesThread(String[] colorArr)
        {
            colors=colorArr;
        }
        @Override
        public void run() {
            DisplayMetrics dMetrics = getResources().getDisplayMetrics();
            FrameLayout playFrame = findViewById(R.id.framePlaying);

            AtomicInteger i= new AtomicInteger();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (childrenCount < circleNum && cdTimer > 0 && !shutdown){
                for(int j=0;j<circles.length;j++)
                {
                    if(circles[j] == null) {
                        i.set(j);
                        break;
                    }
                }
                int finalI = i.get();
                int max = 140, min = 80;
                int[] x_pos = new int[circleNum];
                int[] y_pos = new int[circleNum];
                int[] size = new int[circleNum];

                int dpWidth = dMetrics.widthPixels;
                int dpHeight = dMetrics.heightPixels;

                TextView tvTimer = findViewById(R.id.tvTimer);
                TextView tvCounter = findViewById(R.id.tvScore);

                //Randomize size of circle
                size[finalI] = dp_to_px((int) (Math.random() * (max - min + 1) + min));
                //Randomize position of circle
                x_pos[finalI] = (int) (Math.random() * (dpWidth - size[finalI] + 1) + 0);
                y_pos[finalI] = (int) (Math.random() * (dpHeight - size[finalI]-tvTimer.getHeight() - tvCounter.getHeight()  + 1) + tvCounter.getHeight());
                mainHandler.post(() -> {
                    circles[finalI] = new ImageView(contextThis);
                    circles[finalI].setImageResource(R.drawable.round);
                    int colorAmount = 0;
                    for (int j = 0; j < colors.length; j++) {
                        if (colors[j] != null)
                            colorAmount++;
                    }
                    int ranColor = (int) (Math.random() * colorAmount);
                    circles[finalI].setColorFilter(Color.parseColor(colors[ranColor]), PorterDuff.Mode.SRC_ATOP);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size[finalI], size[finalI]);
                    params.leftMargin = x_pos[finalI];
                    params.topMargin = y_pos[finalI];
                    circles[finalI].setLayoutParams(params);
                    playFrame.addView(circles[finalI]);
                    int startTime = (int) (System.nanoTime()/Math.pow(10,6));
                    ImageView circle = circles[finalI];
                    circle.setOnClickListener(view -> {
                        if(childrenCount < circleNum && cdTimer > 0)
                        {
                            vibe.vibrate(50);
                            circle.setOnClickListener(null);
                            playFrame.removeView(circle);
                            circles[finalI] = null;
                            int endTime = (int) (System.nanoTime() / Math.pow(10, 6));
                            int clickTime = endTime - startTime;
                            baseScore += Math.round(359.8428 * Math.pow(100, -clickTime / 700d));
                            tvCounter.setText(String.format("%s", baseScore));
                            if (cdTimer < startingTime) {
                                cdTimer++;
                                tvTimer.setText(String.format("%s", cdTimer));
                            }
                        }
                    });
                });
                childrenCount=0;
                for (int j = 0; j<circles.length; j++)
                    if(circles[j]!=null)
                        childrenCount++;
                try {
                    Thread.sleep((long) (1148.698*Math.pow(2, -counter/50.0)));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    class timerThread implements Runnable {
        TextView tvTimer = findViewById(R.id.tvTimer);
        @Override
        public void run() {
            cdTimer = startingTime;
            mainHandler.post(() -> tvTimer.setText(String.format("%s",cdTimer)));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            do {
                int finalCdTimer = cdTimer;
                mainHandler.post(() -> tvTimer.setText(String.format("%s",finalCdTimer)));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                cdTimer--;
            }while(cdTimer>0 && childrenCount<circleNum&& !shutdown);
            mainHandler.post(() -> {
                tvTimer.setText(String.format("%s", cdTimer));
                if(childrenCount>=circleNum)
                    tvTimer.setText(getResources().getString(R.string.key_cricleNumExceeded));
                else if(cdTimer<=0)
                    tvTimer.setText(getResources().getString(R.string.key_timerEnded));
            });
            if(!shutdown) {
                cdTimer = 0;
                SharedPreferences hiScoreStored = getSharedPreferences("highScore", 0);
                long highScore = Integer.parseInt(hiScoreStored.getString("highScore", "0"));
                SharedPreferences.Editor editHiScore = hiScoreStored.edit();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(!shutdown) {
                    timeScore = counter * 5L;
                    totalScore = baseScore + timeScore + missedScore;
                    if (totalScore < 0)
                        totalScore = 0;
                    oldHiScore= highScore;
                    if (highScore < totalScore)
                        editHiScore.putString("highScore", String.valueOf(totalScore)).apply();
                    Intent intent = new Intent(contextThis, ReturnBackActivity.class);
                    intent.putExtra("baseScore", baseScore);
                    intent.putExtra("timeScore", timeScore);
                    intent.putExtra("missedScore", missedScore);
                    intent.putExtra("counter", counter);
                    intent.putExtra("numMissed", numMissed);
                    intent.putExtra("totalScore", totalScore);
                    intent.putExtra("oldHiScore", oldHiScore);
                    startActivity(intent);
                }
            }
        }
    }

    class countThread implements Runnable {
        TextView tvScore = findViewById(R.id.tvScore);
        @Override
        public void run() {
            mainHandler.post(() -> tvScore.setText(String.format("%s",totalScore)));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            do {
                counter++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }while(cdTimer>0 && !shutdown);
        }
    }

    class clickThread implements Runnable{
        FrameLayout playFrame = findViewById(R.id.framePlaying);
        @Override
        public void run() {
            mainHandler.post(() -> playFrame.setOnClickListener(view -> {
                if(cdTimer > 0 && !shutdown) {
                    numMissed++;
                    missedScore -= 30;
                }
                else
                    playFrame.setOnClickListener(null);
            }));

        }
    }
}