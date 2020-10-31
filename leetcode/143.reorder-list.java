/*
 * @lc app=leetcode id=143 lang=java
 *
 * [143] Reorder List
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
    public void reorderList(ListNode head) {
        if (null == head || null == head.next) {
            return;
        }
        
        /*
        找到中点
        例如：
        1->2->3->4->5->6
        slow = 3
        fast = 5停止
        */
        ListNode p1 = head, p2 = head;
        while(null != p2.next && null != p2.next.next) {
            p1 = p1.next;
            p2 = p2.next.next;
        }

        
        //将中点之后的节点逆序
        ListNode dummy = new ListNode(0);
        dummy.next = null;
        p2 = p1.next;
        p1.next = null;
        ListNode tmp = null;
        
        while(null != p2) {
            tmp = p2.next;
            p2.next = dummy.next;
            dummy.next = p2;
            p2 = tmp;
        }
        
        //将中点之后的节点依次插入到前半部分的链表中(pre指向了后半条已逆序的链表)
        p1 = head;     
        p2 = dummy.next;
        while(null != p1 && null != p2) {
            tmp = p1.next;
            p1.next = p2;
            p1 = tmp;
            tmp = p2.next;
            p2.next = p1;
            p2 = tmp;
        }
    }
}
// @lc code=end

