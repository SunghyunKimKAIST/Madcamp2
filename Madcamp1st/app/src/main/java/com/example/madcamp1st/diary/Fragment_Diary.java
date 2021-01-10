package com.example.madcamp1st.diary;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.madcamp1st.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Fragment_Diary extends Fragment {

    private View view;

    public Fragment_Diary() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        view = inflater.inflate(R.layout.fragment_diary, container, false);

        CalendarView calendarView = view.findViewById(R.id.diary_calendar);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                // TODO: Get comment and rating from server
            }
        });

        RatingBar ratingBar = view.findViewById(R.id.average_rating);
        // TODO: Get average rating from server
        ratingBar.setRating(0);
        TextView averageInfo = view.findViewById(R.id.star_info);
        averageInfo.setText(String.format("Average Rating = %.2f", 0.));
        TextView selectedComment = view.findViewById(R.id.content_view);
        selectedComment.setText("Failed to load from server.");
        selectedComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedComment.getText().length() > 0) {
                    Intent i = new Intent(getContext(), EditDiaryActivity.class);
                    i.putExtra("date", 0);
                    i.putExtra("weather", 0);
                    i.putExtra("comment", selectedComment.getText().toString());
                    i.putExtra("rating", ratingBar.getRating());
                    startActivity(i);
                }
            }
        });

        FloatingActionButton addButton = view.findViewById(R.id.add_diary);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), NewDiaryActivity.class);
                startActivity(i);
            }
        });

        return view;
    }
}
