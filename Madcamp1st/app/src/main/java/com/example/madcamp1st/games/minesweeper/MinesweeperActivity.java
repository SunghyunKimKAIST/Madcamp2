package com.example.madcamp1st.games.minesweeper;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.madcamp1st.R;

public class MinesweeperActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minesweeper);

        Button button = (Button) findViewById(R.id.resetButton_minesweeper);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MinesweeperEngine.getInstance().createGrid(getApplicationContext());
            }
        });

        MinesweeperEngine.getInstance().createGrid(this);
    }
}