/*
 * @lc app=leetcode id=733 lang=java
 *
 * [733] Flood Fill
 */

// @lc code=start
class Solution {

    private int[] dx = {1, 0, 0, -1};
    private int[] dy = {0, 1, -1, 0};

    public int[][] floodFill(int[][] image, int sr, int sc, int newColor) {
        if (image[sr][sc] == newColor) {
            return image;
        }

        int curColor = image[sr][sc];
        int row = image.length, col = image[0].length;
        Deque<int[]> queue = new LinkedList<>();
        queue.offer(new int[]{sr, sc});
        image[sr][sc] = newColor;
        while(!queue.isEmpty()) {
            /*
            由于此处只需要遍历所有节点、不需要像按层遍历二叉树那样分层输出，因此不用事先统计出当前层
            节点的个数、也不用将当前层遍历结束后再进入下次while循环，直接开始当前节点遍历操作、然后进入
            下次while循环即可。
            */
            int[] cur = queue.poll();
            int cx = cur[0], cy = cur[1];
            for(int i = 0; i < 4; i++) {
                int nextX = cx + dx[i];
                int nextY = cy + dy[i];

                if (nextX >= 0 && nextX < row && nextY >= 0 && nextY < col && image[nextX][nextY] == curColor) {
                    //需要检查该节点有效、且与之前保存的节点是联通的
                    queue.offer(new int[]{nextX, nextY});
                    image[nextX][nextY] = newColor;
                }    
            }
        }
        
        return image;
    }
}
// @lc code=end

