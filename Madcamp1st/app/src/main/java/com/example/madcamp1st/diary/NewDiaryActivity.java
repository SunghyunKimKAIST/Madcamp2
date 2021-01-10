package com.example.madcamp1st.diary;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Toast;

import com.example.madcamp1st.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class NewDiaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_diary);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Button dateButton = findViewById(R.id.date_button);

        Button saveButton = findViewById(R.id.save_diary);
        RatingBar ratingBar = findViewById(R.id.daily_rating);
        EditText editText = findViewById(R.id.daily_comment);
        RadioGroup radioGroup = findViewById(R.id.weather_button);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");

        Intent i = getIntent();
        int request = i.getIntExtra("request_code", 2);
        actionBar.setTitle("New diary");
        Calendar calendar = Calendar.getInstance();

        dateButton.setText(dateFormat.format(calendar.getTime()));
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(NewDiaryActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(year, month, dayOfMonth);
                        dateButton.setText(dateFormat.format(calendar.getTime()));
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));

                dialog.show();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO: 해당 날짜에 이미 등록된 일기가 있는지 중복체크

                if (radioGroup.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(NewDiaryActivity.this, "날씨를 선택해 주세요!", Toast.LENGTH_SHORT).show();
                    return;
                } else if (editText.getText().length() == 0) {
                    Toast.makeText(NewDiaryActivity.this, "오늘의 한 줄을 입력해 주세요!", Toast.LENGTH_SHORT).show();
                    return;
                } else if (ratingBar.getRating() == 0) {
                    Toast.makeText(NewDiaryActivity.this, "오늘을 평가해 주세요!", Toast.LENGTH_SHORT).show();
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
    }
}