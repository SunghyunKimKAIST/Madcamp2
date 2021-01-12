package com.example.madcamp1st.diary;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Toast;

import com.example.madcamp1st.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NewDiaryActivity extends AppCompatActivity {

    private final String DB_URL = "http://192.249.18.163:1234/";
    private DiaryService diaryService;

    int weather = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_diary);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        diaryService = new Retrofit.Builder()
                .baseUrl(DB_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(DiaryService.class);

        Button dateButton = findViewById(R.id.date_button);
        Button saveButton = findViewById(R.id.save_diary);
        RatingBar ratingBar = findViewById(R.id.daily_rating);
        EditText editText = findViewById(R.id.daily_comment);
        RadioGroup radioGroup = findViewById(R.id.weather_button);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");

        Intent i = getIntent();
        actionBar.setTitle("New diary");
        Calendar calendar = Calendar.getInstance();
        // Remove time from calendar
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

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

                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.sunny:
                        weather = 0;
                        break;
                    case R.id.overcast:
                        weather = 1;
                        break;
                    case R.id.cloudy:
                        weather = 2;
                        break;
                    case R.id.rainy:
                        weather = 3;
                        break;
                    case R.id.snowy:
                        weather = 4;
                        break;
                }
                String calToStr = String.format("%s-%s-%s", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
                diaryService.getPage(calToStr).enqueue(new Callback<Page>() {
                    @Override
                    public void onResponse(Call<Page> call, Response<Page> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "선택한 날짜에 이미 작성한 일기가 있습니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            Page page = new Page(calendar.getTime(), weather, ratingBar.getRating(), editText.getText().toString());

                            Intent i = new Intent();
                            i.putExtra("page", page);
                            setResult(RESULT_OK, i);
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<Page> call, Throwable t) {
                        Toast.makeText(getApplicationContext(), "서버에 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }
}
