/*
 * @lc app=leetcode id=529 lang=java
 *
 * [529] Minesweeper
 */

// @lc code=start
class Solution {

    
    private int[] dirX = {0, 1, 0, -1, 1, 1, -1, -1};
    private int[] dirY = {1, 0, -1, 0, 1, -1, 1, -1};


    public char[][] updateBoard(char[][] board, int[] click) {
        int x = click[0], y = click[1];
        if (board[x][y] == 'M') {
            board[x][y] = 'X';
        } else {
//            dfs(board, x, y);
            bfs(board, x, y);
        }

        return board;
    }

    public void dfs(char[][] board, int x, int y) {

        int cnt = 0;
        for (int i = 0; i < 8; i++) {
            //扫描当前节点相邻的8个节点
            int tx = x + dirX[i];
            int ty = y + dirY[i];
            if (tx < 0 || tx >= board.length || ty < 0 || ty >= board[0].length) {
                continue;
            }
            //不用判断M，有M则表示当前游戏已结束。但需要统计相邻节点有多少个地雷
            if (board[tx][ty] == 'M') {
                cnt += 1;
            }
        }
        if (cnt > 0) {
            //表明一个至少与一个地雷相邻的空方块（'E'）被挖出，需要修改它为数字（'1'到'8'），表示相邻地雷的数量。直接改为数字即可。
            board[x][y] = (char) (cnt + '0');
        } else {
            //如果一个没有相邻地雷的空方块（'E'）被挖出，修改它为（'B'），并且所有和其相邻的未挖出方块都应该被递归地揭露。
            board[x][y] = 'B';
            for (int i = 0; i < 8; i++) {
                //递归搜索周边相邻的8个节点，继续检索、更新状态
                int tx = x + dirX[i];
                int ty = y + dirY[i];
                if (tx < 0 || tx >= board.length || ty < 0 || ty >= board[0].length || 'E' != board[tx][ty]) {
                    //非法位置，继续下一个节点即可。注意此处规则还要求只扫描E节点
                    continue;
                }
                dfs(board, tx, ty);
            }
        }
    }

    public void bfs(char[][] board, int sx, int sy) {

        Deque<int[]> queue = new LinkedList<>();
        boolean[][] visited = new boolean[board.length][board[0].length];

        queue.offer(new int[]{sx, sy});
        visited[sx][sy] = true;
        while(!queue.isEmpty()) {
            int[] p = queue.poll();

            int cnt = 0, x = p[0], y = p[1];
            for (int i = 0; i < 8; i++) {
                //递归搜索周边相邻的8个节点，继续检索、更新状态
                int tx = x + dirX[i];
                int ty = y + dirY[i];

                if (tx < 0 || tx >= board.length || ty < 0 || ty >= board[0].length) {
                    //非法位置，继续下一个节点即可。注意此处规则还要求只扫描E节点
                    continue;
                }

                //不用判断M，有M则表示当前游戏已结束。但需要统计相邻节点有多少个地雷
                if (board[tx][ty] == 'M') {
                    cnt += 1;
                }
            }

            if (cnt > 0) {
                //表明一个至少与一个地雷相邻的空方块（'E'）被挖出，需要修改它为数字（'1'到'8'），表示相邻地雷的数量。直接改为数字即可。
                board[x][y] = (char) (cnt + '0');
            } else {
                //如果一个没有相邻地雷的空方块（'E'）被挖出，修改它为（'B'），并且所有和其相邻的未挖出方块都应该被递归地揭露。
                board[x][y] = 'B';

                for (int i = 0; i < 8; i++) {
                    //扫描当前节点相邻的8个节点
                    int tx = x + dirX[i];
                    int ty = y + dirY[i];
                    if (tx < 0 || tx >= board.length || ty < 0 || ty >= board[0].length || 'E' != board[tx][ty] || visited[tx][ty]) {
                        continue;
                    }
                    queue.offer(new int[]{tx, ty});
                    visited[tx][ty] = true;
                }
            }
        }
    }
}
// @lc code=end

