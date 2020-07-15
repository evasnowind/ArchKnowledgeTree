/*
 * @lc app=leetcode id=234 lang=java
 *
 * [234] Palindrome Linked List
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
    public boolean isPalindrome(ListNode head) {
        //1. 边界条件
        if (null == head) {
            return true;
        }
        
        if (null == head.next) {
            //单节点
            return true;
        }

        //2. 找到中间节点
        // 1 2
        // 1 2 3
        // 1 2 3 4
        // 1 2 3 4 5
        ListNode slow = head, fast = head;
        while(null != fast.next && null != fast.next.next) {
            slow = slow.next;
            fast = fast.next.next;
        }

        //3. 将中间节点之后的节点逆序
        ListNode rightHead = reverseList(slow.next);

        //4. 比较中间节点左、右（已逆序），看是否相等
        while(null != rightHead && rightHead.val == head.val) {
            rightHead = rightHead.next;
            head = head.next;
        }

        return rightHead == null;
    }

    private ListNode reverseList(ListNode head) {
        ListNode dummy = new ListNode();
        ListNode node = head;
        while(null != head) {
            node = head.next;
            head.next = dummy.next;
            dummy.next = head;
            head = node;
        }
        return dummy.next;
    }
}
// @lc code=end

