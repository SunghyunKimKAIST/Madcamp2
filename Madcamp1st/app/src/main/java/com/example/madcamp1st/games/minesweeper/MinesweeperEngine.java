package com.example.madcamp1st.games.minesweeper;

import android.content.Context;
import android.widget.Toast;

import com.example.madcamp1st.games.minesweeper.util.Generator;
import com.example.madcamp1st.games.minesweeper.views.grid.Cell;

public class MinesweeperEngine {
    private static MinesweeperEngine instance;

    public static final int BOMB_NUMBER = 30;
    public static final int WIDTH = 10;
    public static final int HEIGHT = 16;

    private Context context;

    private final Cell[][] MinesweeperGrid = new Cell[WIDTH][HEIGHT];

    public static MinesweeperEngine getInstance() {
        if(instance == null) {
            instance = new MinesweeperEngine();
        }
        return instance;
    }

    private MinesweeperEngine() { }

    public void createGrid(Context context) {
        this.context = context;

        Toast.makeText(context, "There are " + BOMB_NUMBER + " Bombs!", Toast.LENGTH_SHORT).show();

        int [][] GeneratedGrid = Generator.generate(BOMB_NUMBER, WIDTH,HEIGHT);
        setGrid(context, GeneratedGrid);
    }

    private void setGrid(final Context context , final int[][] grid) {
        for (int x = 0; x < WIDTH; x++){
            for (int y = 0; y < HEIGHT; y++){
                if (MinesweeperGrid[x][y] == null)
                    MinesweeperGrid[x][y] = new Cell(context, x, y);

                MinesweeperGrid[x][y].setValue(grid[x][y]);
                MinesweeperGrid[x][y].invalidate();
            }
        }
    }

    public Cell getCellAt(int position) {
        int x = position % WIDTH;
        int y = position / WIDTH;

        return MinesweeperGrid[x][y];
    }

    public Cell getCellAt(int x, int y) {
        return MinesweeperGrid[x][y];
    }

    public void click (int x, int y) {
        if (x >= 0 && y >= 0 && x < WIDTH && y < HEIGHT && !getCellAt(x, y).isClicked() && !getCellAt(x,y).isFlagged()) {
            getCellAt(x,y).setClicked();

            if (getCellAt(x,y).getValue() == 0) {
                for (int xt = -1; xt <= 1; xt++) {
                    for (int yt = -1; yt <= 1; yt++) {
//                        if (xt != yt) {
                        if((xt != 0)||(yt != 0) ) click(x + xt, y + yt);
//                        }
                    }
                }
            }
            if (getCellAt(x, y).isBomb())
                onGameLost();
        }

        checkEnd();
    }

    private void checkEnd(){
        int bombNotFound = BOMB_NUMBER;
        int notRevealed = WIDTH * HEIGHT;

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (getCellAt(x,y).isRevealed() || getCellAt(x,y).isFlagged())
                    notRevealed--;

                if (getCellAt(x,y).isFlagged() && getCellAt(x,y).isBomb())
                    bombNotFound--;
            }
        }

        if (bombNotFound == 0 && notRevealed == 0)
            Toast.makeText(context, "Game won!", Toast.LENGTH_SHORT).show();
    }

    public void flag(int x, int y) {
        boolean isFlagged = getCellAt(x, y).isFlagged();
        getCellAt(x,y).setFlagged(!isFlagged);
        getCellAt(x,y).invalidate();
        checkEnd();
    }

    private void onGameLost() {
        Toast.makeText(context, "Game lost! Push the Reset Button.", Toast.LENGTH_SHORT).show();

        for (int x = 0; x < WIDTH; x++)
            for (int y = 0; y < HEIGHT; y++)
                getCellAt(x,y).setRevealed();
    }
}
