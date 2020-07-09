/*
 * @lc app=leetcode id=206 lang=java
 *
 * [206] Reverse Linked List
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
class Solution {
    public ListNode reverseList(ListNode head) {
        if (null == head) {
            return null;
        }

        ListNode guardNode = new ListNode(0);
        guardNode.next = null;
        
        ListNode curNode = head, tmpNode;
        while(null != curNode) {
            tmpNode = curNode.next;
            curNode.next = guardNode.next;
            guardNode.next = curNode;
            curNode = tmpNode;
        }

        return guardNode.next;
    }
}
// @lc code=end

