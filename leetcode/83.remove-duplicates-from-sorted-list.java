/*
 * @lc app=leetcode id=83 lang=java
 *
 * [83] Remove Duplicates from Sorted List
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
    public ListNode deleteDuplicates(ListNode head) {
        if (null == head || head.next == null) {
            return head;
        }

        ListNode dummy = new ListNode(0);
        dummy.next = head;
        ListNode pre = head, cur = head.next;
        while(null != cur) {
            while(null != cur && cur.val == pre.val) {
                cur = cur.next;
            }
            if (null == cur) {
                //后面的元素都相同，则直接将前驱节点指向null即可
                pre.next = null;
                break;
            } else {
                //cur已指向了下一个不同的元素，则pre指向该元素、跳过重复元素，然后更新pre cur指针
                pre.next = cur;
                pre = cur;
                cur = cur.next;
            }
        }
        return dummy.next;
    }
}
// @lc code=end

