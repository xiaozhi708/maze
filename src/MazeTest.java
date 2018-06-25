import org.junit.Test;

public class MazeTest {
    private static Maze myMaze = new Maze();
    private int[][] maze = {{0, 0, 0, 1, 0, 1, 0, 0},
            {0, 1, 0, 1, 1, 1, 1, 0},
            {0, 1, 1, 0, 1, 0, 1, 0},
            {0, 1, 0, 0, 0, 0, 0, 0},
            {0, 1, 0, 1, 0, 0, 0, 0},
            {0, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 1},
            {0, 1, 1, 1, 0, 1, 0, 0}};                                       //迷宫，0代码普通格子，1代表石块，2代表入口，3代表出口

    @Test
    public void drawMaze() throws Exception {
        myMaze.drawMaze(maze);
    }
}