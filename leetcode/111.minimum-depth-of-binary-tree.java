/*
 * @lc app=leetcode id=111 lang=java
 *
 * [111] Minimum Depth of Binary Tree
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
    public int minDepth(TreeNode root) {
        if (null == root) {
			return 0;
		}
		if (null == root.left && null == root.right) {
			return 1;
		}

		Deque<TreeNode> queue = new LinkedList<>();
		queue.offer(root);
		int curDepth = 1;
		while (!queue.isEmpty()) {
			int size = queue.size();

			boolean findNull = false;
			for (int i = 0; i < size; i++) {
				TreeNode node = queue.poll();
				if (null == node.left && null == node.right) {
					findNull = true;
					break;
				}

				if (null != node.left) {
					queue.offer(node.left);
				}
				if (null != node.right) {
					queue.offer(node.right);
				}
			}

			if (findNull) {
				//说明已经找到叶子节点，发现了最短深度对应的路径
				break;
			}
			curDepth += 1;
		}

		return curDepth;
    }
}
// @lc code=end

