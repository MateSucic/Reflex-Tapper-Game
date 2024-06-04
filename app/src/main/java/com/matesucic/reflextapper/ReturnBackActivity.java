package com.matesucic.reflextapper;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ReturnBackActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return_back);
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        TextView tvBase = findViewById(R.id.tvBase);
        TextView tvTime = findViewById(R.id.tvTime);
        TextView tvMissed = findViewById(R.id.tvMissed);
        TextView tvTotal = findViewById(R.id.tvTotal);
        Button btnBack = findViewById(R.id.btnBack);
        TextView tvCongrats = findViewById(R.id.tvCongrats);
        Bundle extras = getIntent().getExtras();
        long baseScore = 0;
        long timeScore = 0;
        long missedScore = 0;
        int counter=0;
        int numMissed=0;
        long totalScore=0;
        long oldHiScore=0;
        if (extras != null) {
            baseScore = extras.getLong("baseScore");
            timeScore = extras.getLong("timeScore");
            missedScore = extras.getLong("missedScore");
            counter = extras.getInt("counter");
            numMissed = extras.getInt("numMissed");
            totalScore = extras.getLong("totalScore");
            oldHiScore = extras.getLong("oldHiScore");
        }
        if(oldHiScore<totalScore) {
            tvCongrats.setVisibility(View.VISIBLE);

            vibe.vibrate(750);
        }
        tvBase.setText(String.format(getResources().getString(R.string.key_tvBaseScore)+" %s",baseScore));
        tvTime.setText(String.format(getResources().getString(R.string.key_tvTimeScore)+" %s (%ss)",timeScore,counter));
        tvMissed.setText(String.format(getResources().getString(R.string.key_tvMissedScore)+" %s (%s)",missedScore,numMissed));
        tvTotal.setText(String.format(getResources().getString(R.string.key_tvTotalScore)+" %s",totalScore));
        btnBack.setOnClickListener(view -> this.finish());
    }
}