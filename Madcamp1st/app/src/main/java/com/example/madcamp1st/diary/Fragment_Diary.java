package com.example.madcamp1st.diary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.madcamp1st.R;
import com.facebook.Profile;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Fragment_Diary extends Fragment {

    private View view;

    private final String DB_URL = "http://192.249.18.163:1234/";
    private DiaryService diaryService;
    private List<Page> internalPages;
    private Page selectedPage;

    private int REQUEST_CREATE_PAGE = 0;
    private int REQUEST_EDIT_PAGE = 1;

    CalendarView calendarView;
    TextView averageInfo;
    RatingBar ratingBar;
    TextView selectedComment;

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

        diaryService = new Retrofit.Builder()
                .baseUrl(DB_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(DiaryService.class);

        diaryService.getAllPages().enqueue(new Callback<List<Page>>() {
            @Override
            public void onResponse(Call<List<Page>> call, Response<List<Page>> response) {
                if (response.isSuccessful()) {
                    internalPages = response.body();
                } else {
                    Toast.makeText(getContext(), "getAllPages: DB에서 일기를 불러오는데 실패했습니다.\nHTTP status code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Page>> call, Throwable t) {
                Toast.makeText(getContext(), "getAllPages: DB에서 일기를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        calendarView = view.findViewById(R.id.diary_calendar);
        ratingBar = view.findViewById(R.id.average_rating);
        selectedComment = view.findViewById(R.id.content_view);

        // Get average rating from server
        averageInfo = view.findViewById(R.id.star_info);
        averageInfo.setText(String.format("Average Rating = %.2f", 0.));
        updateAverageRating();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(calendarView.getDate());
        updateAverageRating();
        updateCommentAndRating(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));

//        ratingBar.setRating(0);
//        selectedComment.setText("");
        selectedComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedComment.getText().length() > 0) {
                    Intent i = new Intent(getContext(), EditDiaryActivity.class);
                    i.putExtra("page", selectedPage);
                    startActivityForResult(i, REQUEST_EDIT_PAGE);
                }
            }
        });

        calendarView.setMaxDate(Calendar.getInstance().getTimeInMillis());
        // Get a page of selected date from server
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                // Get comment and rating from server
                updateCommentAndRating(year, month+1, dayOfMonth);
            }
        });

        FloatingActionButton addButton = view.findViewById(R.id.add_diary);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), NewDiaryActivity.class);
                startActivityForResult(i, REQUEST_CREATE_PAGE);
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Page page;
        int action;
        if (requestCode == REQUEST_CREATE_PAGE && resultCode == Activity.RESULT_OK) {
            page = (Page) data.getSerializableExtra("page");
            diaryService.createPage(page).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (!response.isSuccessful()) {
                        Toast.makeText(getContext(), "createPage: 새로운 페이지를 추가하는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(getContext(), "createPage: DB와 연결하는데 실패했습니다", Toast.LENGTH_SHORT).show();
                }
            });
        } else if (requestCode == REQUEST_EDIT_PAGE && resultCode == Activity.RESULT_OK) {
            page = (Page) data.getSerializableExtra("page");
            action = data.getIntExtra("action", -1);
            if (action == 0) {
                diaryService.updatePage(Profile.getCurrentProfile().getId(), page.date, page).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (!response.isSuccessful()) {
                            Toast.makeText(getContext(), "editPage: 페이지를 수정하는데 실패했습니다.\nHTTP status code: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(getContext(), "editPage: DB와 연결하는데 실패했습니다", Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (action == 1) {
                diaryService.deletePage(Profile.getCurrentProfile().getId(), page.date).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (!response.isSuccessful()) {
                            Toast.makeText(getContext(), "deletePage: 페이지를 삭제하는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(getContext(), "deletePage: DB와 연결하는데 실패했습니다", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(calendarView.getDate());
        updateAverageRating();
//        updateCommentAndRating(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
    }

    private void updateAverageRating() {
        diaryService.getAverageRating(Profile.getCurrentProfile().getId()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String res = response.body().string();
                        averageInfo.setText(String.format("Average Rating = %.2f", Float.valueOf(res)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    ratingBar.setRating(0);
                    Toast.makeText(getContext(), "getAverage: DB에서 값을 불러오는데 실패했습니다.\nHTTP status code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "getAverage: DB에서 값을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCommentAndRating(int year, int month, int dayOfMonth) {
        diaryService.getPage(Profile.getCurrentProfile().getId(), String.format("%s-%s-%s", year, month, dayOfMonth)).enqueue(new Callback<Page>() {
            @Override
            public void onResponse(Call<Page> call, Response<Page> response) {
                if (response.isSuccessful()) {
                    selectedPage = response.body();
                    ratingBar.setVisibility(RatingBar.VISIBLE);
                    ratingBar.setRating(selectedPage.getRating());
                    selectedComment.setText(selectedPage.getComment());
                } else {
                    ratingBar.setVisibility(RatingBar.INVISIBLE);
                    ratingBar.setRating(0);
                    selectedComment.setText("");
    //                Toast.makeText(getContext(), "getPage: DB에서 페이지를 불러오는데 실패했습니다.\nHTTP status code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Page> call, Throwable t) {
                Toast.makeText(getContext(), "getPage: DB에서 페이지를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
