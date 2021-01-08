package com.example.madcamp1st.games;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.madcamp1st.R;
import com.example.madcamp1st.games.breakout.BreakoutActivity;
import com.example.madcamp1st.games.minesweeper.MinesweeperActivity;
import com.example.madcamp1st.games.snake.SnakeActivity;

public class Fragment_Games extends Fragment {
    static final String[] LIST_MENU = {"Snake game", "Minesweeper", "Breakout"} ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_games,container,false);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, LIST_MENU);

        ListView listview = view.findViewById(R.id.listView_games);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                Toast.makeText(getContext(), LIST_MENU[position] + " will start.", Toast.LENGTH_SHORT).show();

                Intent intent;
                switch (position) {
                    case 0:
                        intent = new Intent(getActivity(), SnakeActivity.class);
                        startActivity(intent);
                        break;
                    case 1:
                        intent = new Intent(getActivity(), MinesweeperActivity.class);
                        startActivity(intent);
                        break;
                    case 2:
                        intent = new Intent(getActivity(), BreakoutActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        });

        return view;
    }
}