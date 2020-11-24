/*
 * @lc app=leetcode id=222 lang=java
 *
 * [222] Count Complete Tree Nodes
 */

// @lc code=start
/**
 * Definition for a binary tree node.
 * public class TreeNode {
 *     int val;
 *     TreeNode left;
 *     TreeNode right;
 *     TreeNode() {}
 *     TreeNode(int val) { this.val = val; }
 *     TreeNode(int val, TreeNode left, TreeNode right) {
 *         this.val = val;
 *         this.left = left;
 *         this.right = right;
 *     }
 * }
 */
class Solution {
    // public int countNodes(TreeNode root) {
    //     if (null == root) {
    //         return 0;
    //     }

    //     Deque<TreeNode> queue = new LinkedList<>();
    //     queue.offer(root);
    //     int cnt = 0;
    //     while(!queue.isEmpty()) {
            
    //         int level = queue.size();
    //         cnt += level;
            
    //         for(int i = 0; i < level; i++) {
    //             TreeNode cur = queue.poll();
    //             if (null != cur.left) {
    //                 queue.offer(cur.left);
    //             }
    //             if (null != cur.right) {
    //                 queue.offer(cur.right);
    //             }
    //         }

    //     }
    //     return cnt;
    // }


    private int countLevel(TreeNode root) {
        int level = 0;
        while(null != root) {
            level+=1;
            root = root.left;
        }
        return level;
    }

    public int countNodes(TreeNode root) {
        if (null == root) {
            return 0;
        }

        int left = countLevel(root.left);
        int right = countLevel(root.right);
        if (left == right) {
            /*
            由于我们统计深度时，是利用完全二叉树性质，只遍历了左节点，
            那么对于右子树，可能出现只差一个叶子就是满二叉树、但深度仍与
            左子树相等的情况。
            因此，当left=right是，只能说明左子树一定是满二叉树，仍需要
            统计右子树。而左子树是满二叉树时，整个左子树节点个数是2^left-1。
            加上根节点，刚好就是2^left，剩下只需要递归统计右子树即可。
            */
            return countNodes(root.right) + (1<<left);
        } else {
            /*
            当左右子树深度不等，那么说明倒数第一层没满、但倒数第二层肯定慢了。
            我们此时拿到的右子树就是满二叉树、且深度就是right，则右子树个数2^right-1，
            加上根，仍是2^right，再继续递归统计左子树的个数即可。
            */
            return countNodes(root.left) + (1<<right);
        }
    }
}
// @lc code=end

