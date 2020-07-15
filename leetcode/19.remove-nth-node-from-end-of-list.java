/*
 * @lc app=leetcode id=19 lang=java
 *
 * [19] Remove Nth Node From End of List
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
    public ListNode removeNthFromEnd(ListNode head, int n) {
        if (null == head) {
            return null;
        }

        ListNode dummy = new ListNode();
        dummy.next = head;
        /*
        1 2 3 4 5
        n=2
        */
        ListNode slow = dummy, fast = dummy;
        for (int i = 0; i < n; i++) {
            fast = fast.next;
        }
        while(null != fast.next) {
            slow = slow.next;
            fast = fast.next;
        }

        ListNode p = slow.next;
        slow.next = p.next;
        p.next = null;
        p = null;
        return dummy.next;
    }
}
// @lc code=end

