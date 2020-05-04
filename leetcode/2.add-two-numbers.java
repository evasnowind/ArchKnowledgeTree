/*
 * @lc app=leetcode id=2 lang=java
 *
 * [2] Add Two Numbers
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
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        if(null == l1 && null == l2) {
            return new ListNode(0);
        }

        if(null == l1) {
            return l2;
        }
        if(null == l2) {
            return l1;
        }

        ListNode result = new ListNode(0);
        ListNode curNode = result;
        int curVal = 0, carry = 0;
        while(carry != 0 || null != l1 || null != l2) {
            curVal = carry;

            if (null != l1) {
                curVal = curVal + l1.val;
                l1 = l1.next;
            }
            if (null != l2) {
                curVal = curVal + l2.val;
                l2 = l2.next;
            }
            
            carry = curVal / 10;
            ListNode newNode = new ListNode(curVal % 10);
            curNode.next = newNode;
            curNode = newNode;
        }

        return result.next;
    }
}
// @lc code=end

