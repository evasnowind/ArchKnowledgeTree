/*
 * @lc app=leetcode id=147 lang=java
 *
 * [147] Insertion Sort List
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
    public ListNode insertionSortList(ListNode head) {
        if (null == head || null == head.next) {
            return head;
        }
        /*
        使用dummy节点，每次从dummy开始遍历。
        每次通过只比较pre.next cur.next，来保证一致能找到待交换节点的替换位置。

         */
        ListNode dummy = new ListNode(0);
        dummy.next = head;
        ListNode lastNode = head;
        ListNode cur = head.next;
        while(null != cur) {
            if (cur.val >= lastNode.val) {
                lastNode = lastNode.next;
            } else {
                ListNode pre = dummy;
                /*
                由于已经知道cur要小于lastNode，必然在当前有序节点的中间位置，
                因此pre在遍历、找cur插入位置时不用判空
                 */
                while(pre.next.val <= cur.val) {
                    pre = pre.next;
                }
                lastNode.next = cur.next;
                cur.next = pre.next;
                pre.next = cur;
            }
            cur = lastNode.next;
        }
        return dummy.next;
    }
}
// @lc code=end

