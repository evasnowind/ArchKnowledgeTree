import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/*
 * @lc app=leetcode id=66 lang=java
 *
 * [66] Plus One
 */

// @lc code=start
class Solution {
    public int[] plusOne(int[] digits) {
        if (null == digits) {
            return null;
        }

        //先把1加上，然后看是否需要进位，若不用进位，直接返回即可
        digits[digits.length - 1] = digits[digits.length - 1] + 1;
        if (digits[digits.length - 1] < 10) {
            return digits;
        }

        /*
        若需要进位，最坏情况，可能导致数组所有数字都需要进位，因此需要走大整数相加的流程。
        简单起见，重新分配一个数组来保存结果
        */
        LinkedList<Integer> res = new LinkedList<>();

        int carry = digits[digits.length - 1] / 10, sum = 0;
        res.add(digits[digits.length - 1] % 10);

        for (int i = digits.length - 2; i >= 0; i--) {
            sum = carry + digits[i];
            carry = sum / 10;
            res.addFirst(sum % 10);
        }
        
        if (carry > 0) {
            res.addFirst(carry);
        }
        
        int[] resArray = new int[res.size()];

        Iterator<Integer> itr = res.iterator();
        int i = 0;
        while(itr.hasNext()) {
            resArray[i] = itr.next();
            i ++;
        }
        
        return resArray;
    }

    /*
    leetcode 国际站给出的解法，针对加1这个场景，比大整数相加的思路更加简化
public int[] plusOne(int[] digits) {
    for (int i = digits.length - 1; i >=0; i--) {
        if (digits[i] != 9) {
            digits[i]++;
            break;
        } else {
            digits[i] = 0;
        }
    }
    if (digits[0] == 0) {
        int[] res = new int[digits.length+1];
        res[0] = 1;
        return res;
    }
    return digits;
}
    */
}
// @lc code=end

