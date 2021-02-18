import java.util.Map;

/*
 * @lc app=leetcode id=947 lang=java
 *
 * [947] Most Stones Removed with Same Row or Column
 */

// @lc code=start
class Solution {
    /**
     * 需要转换下思路。
     * 可以将输入看成一个连通图，那么同行/同列的节点就
     * 在一个连通分量中，一个连通分量中的节点按规则删除时
     * 到最后可以只剩下一个节点。因此目标可以转换成：
     * 能移除的最大个数=所有节点个数-所有连通分量个数
     * 
     * @param stones
     * @return
     */
    public int removeStones(int[][] stones) {
        UnionFind unionFind = new UnionFind();
        
        for(int[] stone : stones) {
            //此处x+10001是为了与y区分开
            unionFind.union(stone[0] + 10001, stone[1]);
        }

        return stones.length - unionFind.getCount();

    }

    class UnionFind {
        Map<Integer, Integer> parent;
        int count;

        public UnionFind() {
            parent = new HashMap<>();
            count = 0;
        }

        public int getCount() {
            return count;
        }

        public int find(int x) {
            if (!parent.containsKey(x)) {
                parent.put(x, x);
                count++;
            }

            if (x != parent.get(x)) {
                //递归方式找到x所在连通分量最终的根
                parent.put(x, find(parent.get(x)));
            }
            
            return parent.get(x);
        }

        public void union(int x, int y) {
            int rootX = find(x);
            int rootY = find(y);
            if (rootX == rootY) {
                return;
            }

            parent.put(rootX, rootY);
            count--;
        }
    }
}
// @lc code=end

