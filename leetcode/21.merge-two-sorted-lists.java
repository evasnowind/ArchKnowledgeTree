/*
 * @lc app=leetcode id=21 lang=java
 *
 * [21] Merge Two Sorted Lists
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
    public ListNode mergeTwoLists(ListNode l1, ListNode l2) {
        ListNode head = new ListNode(0);
        head.next = null;
        ListNode curNode = head;
        
        while(l1 != null || l2 != null) {
            if (l1 != null && l2 != null) {
                if (l1.val <= l2.val) {
                    curNode.next = l1;
                    curNode = curNode.next;
                    l1 = l1.next;
                } else {
                    curNode.next = l2;
                    curNode = curNode.next;
                    l2 = l2.next;
                }
            } else if (l1 == null) {
                curNode.next = l2;
                curNode = curNode.next;
                l2 = l2.next;
            } else {
                curNode.next = l1;
                curNode = curNode.next;
                l1 = l1.next;
            }
        }

        return head.next;
    }
}
// @lc code=end

