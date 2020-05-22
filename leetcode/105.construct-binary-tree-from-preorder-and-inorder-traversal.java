/*
 * @lc app=leetcode id=105 lang=java
 *
 * [105] Construct Binary Tree from Preorder and Inorder Traversal
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
	public TreeNode buildTree(int[] preorder, int[] inorder) {
        return buildSubTree(0, 0, preorder.length-1, preorder, inorder);
    }


	public TreeNode buildSubTree(int preStart, int inStart, int inEnd, int[] preorder, int[] inorder){
        if (preStart > preorder.length - 1 || inStart > inEnd) {
			return null;
		}

		TreeNode rootNode = new TreeNode(preorder[preStart]);
		int inIndex = 0;
		for (int i = inStart; i <= inEnd; i++) {
			if (inorder[i] == preorder[preStart]) {
				inIndex = i;
				break;
			}
		}

		rootNode.left = buildSubTree(preStart + 1, inStart, inIndex - 1, preorder, inorder);
		rootNode.right = buildSubTree(preStart + (inIndex - inStart + 1), inIndex + 1, inEnd, preorder, inorder);
		return rootNode;
	}

}
// @lc code=end

