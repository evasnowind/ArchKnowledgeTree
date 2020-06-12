/*
 * @lc app=leetcode id=297 lang=java
 *
 * [297] Serialize and Deserialize Binary Tree
 */

// @lc code=start
/**
 * Definition for a binary tree node.
 * public class TreeNode {
 *     int val;
 *     TreeNode left;
 *     TreeNode right;
 *     TreeNode(int x) { val = x; }
 * }
 */
public class Codec {

    // Encodes a tree to a single string.
    public String serialize(TreeNode root) {
        if (null == root) {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            TreeNode node = queue.poll();
            if (null == node) {
                builder.append("n ");
                continue;
            }
            builder.append(node.val)
                    .append(" ");
            queue.add(node.left);
            queue.add(node.right);
        }
        return builder.toString();
    }

    // Decodes your encoded data to tree.
    public TreeNode deserialize(String data) {
        if ("" == data) {
            return null;
        }

        Queue<TreeNode> queue = new LinkedList<>();
        String[] nodeStr = data.split(" ");
        TreeNode root = new TreeNode(0);
        root.val = Integer.parseInt(nodeStr[0]);
        queue.add(root);

        for(int idx = 1; idx < nodeStr.length; idx ++) {
            TreeNode parent = queue.poll();
            if (!nodeStr[idx].equals("n")) {
                TreeNode leftNode = new TreeNode(Integer.parseInt(nodeStr[idx]));
                parent.left = leftNode;
                queue.add(leftNode);
            }
            idx += 1;
            if (!nodeStr[idx].equals("n")) {
                TreeNode rightNode = new TreeNode(Integer.parseInt(nodeStr[idx]));
                parent.right = rightNode;
                queue.add(rightNode);
            }
        }

        return root;
    }
}

// Your Codec object will be instantiated and called as such:
// Codec codec = new Codec();
// codec.deserialize(codec.serialize(root));
// @lc code=end

