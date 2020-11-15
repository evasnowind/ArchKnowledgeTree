/*
 * @lc app=leetcode id=148 lang=java
 *
 * [148] Sort List
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
    public ListNode sortList(ListNode head) {

        if (null == head || null == head.next) {
            return head;
        }

        /*
        通过快慢指针的思想，找到中点。
        注意，我们只要找到近似中点即可，不用太过考虑是否多了一个节点、少了一个节点，
        此处找中点只是为了将链表分成两部分。
        */
        ListNode dummy = new ListNode(0);
        dummy.next = head;
        ListNode left = dummy, right = dummy;
        while(null != right.next && null != right.next.next) {
            left = left.next;
            right = right.next.next;
        }

        /*
        分成两部分.left指向前半部分最后一个节点，right指向末尾。用left
        */
        ListNode head2 = left.next;
        left.next = null;
        
        /*
        递归下降，一直到单个节点或是null，此时就是有序的了，接下来就该合并有序的部分
        */
        left = sortList(dummy.next);
        right = sortList(head2);

        /*
        此处我自己写的时候，有两种思路：
        1、while(null != left || null != right)
        2、while(null != left && null != right) 
        明显2更方便，因为只要有一个为空，剩下的另一个非空的链表已然排好序、直接接上
        即可。如下所示。
         */
        dummy.next = null;
        ListNode tail = dummy;
        while(null != left && null != right) {
            if (left.val < right.val) {
                tail.next = left;
                left = left.next;
            } else {
                tail.next = right;
                right = right.next;
            }
            tail = tail.next;
        }

        tail.next = null == left ? right : left;
        
        return dummy.next;

    }
}
// @lc code=end

