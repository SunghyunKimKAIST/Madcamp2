package com.example.madcamp1st.games.minesweeper.views.grid;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.example.madcamp1st.games.minesweeper.MinesweeperEngine;

public class Grid extends GridView {
    public Grid(Context context, AttributeSet attrs) {
        super(context, attrs);

        MinesweeperEngine.getInstance().createGrid(context);

        setNumColumns(MinesweeperEngine.WIDTH);
        setAdapter(new GridAdapter());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private class GridAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return MinesweeperEngine.WIDTH * MinesweeperEngine.HEIGHT;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return MinesweeperEngine.getInstance().getCellAt(position);
        }
    }
}
