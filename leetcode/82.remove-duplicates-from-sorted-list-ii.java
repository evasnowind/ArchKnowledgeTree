import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/*
 * @lc app=leetcode id=82 lang=java
 *
 * [82] Remove Duplicates from Sorted List II
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
     * 思路1：
     * 1、利用额外的hashmap，统计每个值的出现次数，
     * 2、创建一个hashset，遍历hashmap、将其中统计次数大于1
     * 的值写入到hashset中。
     * 3、再次遍历链表，若值在hashset中则删除该节点
     * 
     * 思路2：
     * 1、使用3个指针p0/p1/p2，一起往前遍历
     * p0 指向相同元素起始的前驱节点
     * p1 指向相同元素的第一个节点
     * p2 指向相同元素的下一个节点
     *
     * 
     * @param head
     * @return
     */
    public ListNode deleteDuplicates(ListNode head) {
        if (null == head || head.next == null) {
            return head;
        }

        ListNode dummy = new ListNode(0);
        dummy.next = head;
    
        ListNode p0 = dummy, p1 = head, p2 = head.next;
        while(null != p2) {
            //找到当前重复元素
            if (p1.val == p2.val) {
                while(null != p2.next && p2.val == p1.val) {
                    p2 = p2.next;
                }

                /*
                说明当前p2还没有遍历结束，后面还有节点。
                那么此时需要删除[p1, p2)这部分。
                */
                if (p2.val != p1.val) {
                    p0.next = p2;
                    p1 = p2;
                    p2 = p2.next;
                } else {
                    /*
                    已遍历完整个链表，并且[p1,p2]都是重复元素，
                    直接删除后面所有节点，让p0指向null即可。
                    */
                    p0.next = null;
                    break;
                }
            } else {
                /*
                3个指针都挪动到下一个节点，继续扫描
                */
                p0 = p0.next;
                p1 = p1.next;
                p2 = p2.next;
            }
        }

        return dummy.next;
    }
}
// @lc code=end

