package com.example.madcamp1st.diary;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.madcamp1st.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class EditDiaryActivity extends AppCompatActivity {

    private Button dateButton;
    Button saveButton;
    RatingBar ratingBar;
    EditText editText;
    RadioGroup radioGroup;
    Calendar calendar = Calendar.getInstance();

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_diary);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        dateButton = findViewById(R.id.date_button);
        radioGroup = findViewById(R.id.weather_button);
        ratingBar = findViewById(R.id.daily_rating);
        editText = findViewById(R.id.daily_comment);
        saveButton = findViewById(R.id.save_diary);

        Intent i = getIntent();
        int request = i.getIntExtra("request_code", 2);
//            actionBar.setTitle("My diary");
        long date = i.getLongExtra("date",0);
        int weather_id = i.getIntExtra("weather",-1);
        float rate = i.getFloatExtra("rate",0);

        calendar.setTimeInMillis(date);
        dateButton.setText(dateFormat.format(calendar.getTime()));
        ratingBar.setRating(rate);
        radioGroup.check(weather_id);
        editText.setText(i.getStringExtra("content"));

        dateButton.setClickable(false);
        for (int n = 0; n < radioGroup.getChildCount(); n++) {
            radioGroup.getChildAt(n).setClickable(false);
//                radioGroup.getChildAt(n).setEnabled(false);
        }
        ratingBar.setIsIndicator(true);
        editText.setEnabled(false);
//        saveButton.setVisibility(View.INVISIBLE);
        saveButton.setEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.diary_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_edit:
                System.out.println("=====edit button pressed=====");
                activateComponents();
                break;
        }
        return true;
    }

    private void activateComponents() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (radioGroup.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(EditDiaryActivity.this, "날씨를 선택해 주세요!", Toast.LENGTH_SHORT).show();
                    return;
                } else if (editText.getText().length() == 0) {
                    Toast.makeText(EditDiaryActivity.this, "오늘의 한 줄을 입력해 주세요!", Toast.LENGTH_SHORT).show();
                    return;
                } else if (ratingBar.getRating() == 0) {
                    Toast.makeText(EditDiaryActivity.this, "오늘을 평가해 주세요!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent i = new Intent();
                Bundle bundle = new Bundle();
                i.putExtra("date", calendar.getTimeInMillis());
//                    i.putExtra("date", calendar.getTime());
                i.putExtra("weather", radioGroup.getCheckedRadioButtonId());
                i.putExtra("comment", editText.getText().toString());
                i.putExtra("rating", ratingBar.getRating());
                setResult(RESULT_OK, i);
                finish();
            }
        });

        for (int n = 0; n < radioGroup.getChildCount(); n++) {
            radioGroup.getChildAt(n).setClickable(true);
//                radioGroup.getChildAt(n).setEnabled(false);
        }
        ratingBar.setIsIndicator(false);
        editText.setEnabled(true);
//        saveButton.setVisibility(View.VISIBLE);
        saveButton.setEnabled(true);
    }
}
