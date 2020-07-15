/*
 * @lc app=leetcode id=61 lang=java
 *
 * [61] Rotate List
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
    /**
     * k=2
     * 1->2->3->4->5->NULL
     * 结果：4->5->1->2->3->NULL
     * 
     * @param head
     * @param k
     * @return
     */
    public ListNode rotateRight(ListNode head, int k) {
        if (null == head || 0 == k) {
            return head;
        }
        
        /*
        先计算出链表长度，然后找到第k个位置，将链表再接上即可。
        */   
        ListNode dummy = new ListNode();
        dummy.next = head;
        ListNode p1, p2;
        //1. 计算长度，同时保留最后一个节点的指针，以便后续反转操作时接上链表头
        int len = 0;
        p1 = dummy;
        while(null != p1.next) {
            len += 1;
            p1 = p1.next;
        }

        //2. 算出k
        k = k % len;

        //3. 指针找到第k个位置
        p2 = dummy;
        for (int i = 0; i < len - k; i++) {
            p2 = p2.next;
        }

        //4. 反转
        p1.next = dummy.next;
        dummy.next = p2.next;
        p2.next = null;
        return dummy.next;
    }
}
// @lc code=end

