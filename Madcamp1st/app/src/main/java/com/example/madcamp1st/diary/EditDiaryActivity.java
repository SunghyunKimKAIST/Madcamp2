package com.example.madcamp1st.diary;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

    Page page;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");
    private int[] weatherRadio = {
            R.id.sunny, R.id.overcast, R.id.cloudy, R.id.rainy, R.id.snowy
    };

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

        actionBar.setTitle("My diary");
        Intent i = getIntent();
        page = (Page) i.getSerializableExtra("page");

        calendar.setTime(page.date);
        dateButton.setText(dateFormat.format(calendar.getTime()));
        ratingBar.setRating(page.rating);
        radioGroup.check(weatherRadio[page.weather]);
        editText.setText(page.comment);

        dateButton.setClickable(false);
        for (int n = 0; n < radioGroup.getChildCount(); n++) {
            radioGroup.getChildAt(n).setClickable(false);
//            radioGroup.getChildAt(n).setEnabled(false);
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
            case R.id.menu_delete:
                Intent i = new Intent();
                i.putExtra("page", page);
                setResult(RESULT_CANCELED, i);
                finish();
                break;
        }
        return true;
    }

    private void activateComponents() {
        for (int n = 0; n < radioGroup.getChildCount(); n++) {
            radioGroup.getChildAt(n).setClickable(true);
//            radioGroup.getChildAt(n).setEnabled(false);
        }
        ratingBar.setIsIndicator(false);
        editText.setEnabled(true);

//        saveButton.setVisibility(View.VISIBLE);
        saveButton.setEnabled(true);
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

                page.comment = editText.getText().toString();
                page.rating = ratingBar.getRating();

                Intent i = new Intent();
                i.putExtra("page", page);
                setResult(RESULT_OK, i);
                finish();
            }
        });
    }
}
