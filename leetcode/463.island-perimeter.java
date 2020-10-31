/*
 * @lc app=leetcode id=463 lang=java
 *
 * [463] Island Perimeter
 */

// @lc code=start
class Solution {

    
    private static final int[] xStep = new int[]{0, 0, 1, -1};
    private static final int[] yStep = new int[]{1, -1, 0, 0};


    public int islandPerimeter(int[][] grid) {
        int rowLen = grid.length, colLen = grid[0].length;
        int res = 0;
        for (int i = 0; i < rowLen; i++) {
            for (int j = 0; j < colLen; j++) {
                if (grid[i][j] == 1) {
                    /*
                    当前已经是岛屿的前提下，找到地图边界，或是
                    发现已经走出岛屿，则说明找到一条边。
                    */
                    int cnt = 0;
                    for(int k = 0; k < 4; k++) {
                        int tx = i + xStep[k];
                        int ty = j + yStep[k];
                        if (tx < 0 || tx >= rowLen 
                            || ty < 0 || ty >= colLen
                            || grid[tx][ty] == 0) {
                                cnt += 1;
                            }
                    }
                    res += cnt;
                }
            }
        }    
        return res;    
    }
}
// @lc code=end

