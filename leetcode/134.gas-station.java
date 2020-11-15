/*
 * @lc app=leetcode id=134 lang=java
 *
 * [134] Gas Station
 */

// @lc code=start
class Solution {
    public int canCompleteCircuit(int[] gas, int[] cost) {
        

        int length = gas.length;
        for(int i = 0; i < length; i++) {
            int start = i;
            //起始油量
            int remain = gas[i];
            if (remain < cost[i]) {
                continue;
            }
            remain = remain - cost[i];
            boolean hasSuccess = true;
            for(int j = 1; j < length; j++) {
                int idx = (i + j) % length;
                remain = remain + gas[idx] - cost[idx];
                if (remain < 0) {
                    //本次迭代失败，跳出，尝试从下一个节点作为起始节点
                    hasSuccess = false;
                    break;
                }
            }
            if (hasSuccess) {
                return start;
            }
        }

        return -1;
    }
}
// @lc code=end

