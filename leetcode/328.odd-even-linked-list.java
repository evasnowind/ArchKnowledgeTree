/*
 * @lc app=leetcode id=328 lang=java
 *
 * [328] Odd Even Linked List
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
     * 总体思路：采用双指针的思想，维护多个指针，
     * 保存奇数链表的尾节点oddTail、偶数链表尾节点evenTail，
     * 偶数链表头节点evenHead。
     * oddTail evenHead两个节点刚好就是新的奇数节点需要插入位置
     * 的前驱、后继节点。
     * 而evenTail其实是用来保存当前链表已遍历到的节点位置。
     * 因为遇到奇数节点，我们需要将该节点断开、让evenTail保存下个节点、
     * 同时将该奇数节点插入到oddTail后面的位置、然后挪动oddTail。
     * 如果是遍历到偶数节点，由于已经在链表后半部分，从evenHead到该节点已经
     * 保证了是偶数链表节点，不用做任何操作。
     */
    public ListNode oddEvenList(ListNode head) {
        if (null == head || null == head.next) {
            return head;
        }
        
        ListNode oddTail = head, evenHead = head.next, evenTail = evenHead;
        boolean isOdd = true;
        while(null != evenTail.next) {
            ListNode node = evenTail.next;
            if (isOdd) {
                /*
                当前节点node是奇数节点.
                我们将奇数节点放到evenHead oddTail之间
                */
                evenTail.next = node.next;
                node.next = evenHead;
                oddTail.next = node;
                oddTail = node;

                isOdd = false;
            } else {
                //当前节点是偶数

                isOdd = true;
                evenTail = evenTail.next;
            }
        }

        return head;
    }

}
// @lc code=end

