/*
 * @lc app=leetcode id=130 lang=java
 *
 * [130] Surrounded Regions
 */

// @lc code=start
class Solution {
    public void solve(char[][] board) {
        if (null == board || board.length == 0) {
            return;
        }

        int row = board.length;
        int column = board[0].length;
        char markCh = '#';
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                boolean isEdgePoint = i == 0 || j == 0 || i == row - 1 || j == column - 1;
                if (isEdgePoint && board[i][j] == 'O') {
                    bfs(board, i, j, markCh);
                }
            }
        }

        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                if (board[i][j] == 'O') {
                    board[i][j] = 'X';
                }
                if (board[i][j] == markCh) {
                    //将标记的位置替换回o
                    board[i][j] = 'O';
                }
            }
        }
    }

    private void bfs(char[][] board, int row, int column, char markCh) {
        if (row < 0 || row >= board.length || column < 0 || column >= board[0].length 
                || board[row][column] == 'X' || board[row][column] == markCh) {
            //越界，或是不需要遍历（X），或是已经遍历过(#)
            return;
        }
        //边界点，或是与边界上o相连的点，标记成markCh
        board[row][column] = markCh;
        //bfs遍历周边节点，逐步扩展
        bfs(board, row + 1, column, markCh);
        bfs(board, row - 1, column, markCh);
        bfs(board, row, column + 1, markCh);
        bfs(board, row, column - 1, markCh);
    }
}
// @lc code=end

