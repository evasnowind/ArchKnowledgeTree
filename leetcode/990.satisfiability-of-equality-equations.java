/*
 * @lc app=leetcode id=990 lang=java
 *
 * [990] Satisfiability of Equality Equations
 */

// @lc code=start
class Solution {
    /**
     * 解析参见：https://leetcode-cn.com/problems/satisfiability-of-equality-equations/solution/shou-hui-tu-jie-shou-xie-unionfind-bing-cha-ji-bu-/
     * @param equations
     * @return
     */
    public boolean equationsPossible(String[] equations) {
        int charArrLen = 26;
        int[] root = new int[charArrLen];
        int[] ranks = new int[charArrLen];

        init(root, ranks, charArrLen);

        for (String str : equations) {
            if (str.charAt(1) == '=') {
                union(root, ranks, str.charAt(0) - 'a', str.charAt(3) - 'a');
            }
        }

        for (String str : equations) {
            if (str.charAt(1) == '!' && findRoot(root, str.charAt(0) - 'a') == findRoot(root, str.charAt(3) - 'a')) {
                return false;
            }
        }
        
        return true;
    }
    
    private void init(int[] root, int[] ranks, int len) {
        for (int i = 0; i < len; i++) {
            root[i] = -1;
            ranks[i] = 0;
        }
    }

    private int findRoot(int[] root, int x) {
        int xRoot = x;
        while(root[xRoot] != -1) {
            xRoot = root[xRoot];
        }
        return xRoot;
    }

    private void union(int[] root, int[] ranks, int x, int y) {
        int xRoot = findRoot(root, x);
        int yRoot = findRoot(root, y);
        if (xRoot == yRoot) {
            //两个节点在一棵树上
            return;
        }

        int xRank = ranks[xRoot];
        int yRank = ranks[yRoot];
        if (xRank < yRank) {
            //谁的高度大，谁就作为合并之后树的根
            root[xRoot] = yRoot;
        } else if (xRank > yRank) {
            root[yRoot] = xRoot;
        } else {
            root[yRoot] = xRoot;
            ranks[xRoot] += 1;
        }
    }
}
// @lc code=end

