/*
 * @lc app=leetcode id=109 lang=java
 *
 * [109] Convert Sorted List to Binary Search Tree
 */

// @lc code=start
/**
 * Definition for singly-linked list.
 * public class ListNode {
 *     int val;
 *     ListNode next;
 *     ListNode() {}
 *     ListNode(int val) { this.val = val; }
 *     ListNode(int val, ListNode next) { this.val = val; this.next = next; }
 * }
 */
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
    public TreeNode sortedListToBST(ListNode head) {
        if (null == head) {
            return null;
        }

        return buildTree(head, null);
    }

    private TreeNode buildTree(ListNode left, ListNode right) {
        if (left == right) {
            return null;
        }
        
        ListNode medianNode = getMedianNode(left, right);
        TreeNode rootNode = new TreeNode(medianNode.val);
        
        rootNode.left = buildTree(left, medianNode);
        rootNode.right = buildTree(medianNode.next, right);
        return rootNode;
    }

    private ListNode getMedianNode(ListNode left, ListNode right) {
        ListNode fast = left;
        ListNode slow = left;
        while(right != fast && right != fast.next) {
            /*
            利用递归创建子树时，利用快慢指针找到链表重点，但由于每次我们需要找的是一个区间，
            而不是整个链表，因此不能用fast != null作为判断条件，而是当前区间最后一个节点
            */
            fast = fast.next;
            fast = fast.next;
            slow = slow.next;
        }

        return slow;
    }
}
// @lc code=end

