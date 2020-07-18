import java.util.ArrayList;
import java.util.Collections;

/*
 * @lc app=leetcode id=118 lang=java
 *
 * [118] Pascal's Triangle
 */

// @lc code=start
class Solution {
    public List<List<Integer>> generate(int numRows) {
        List<List<Integer>> res = new ArrayList<List<Integer>>();
        if (numRows == 0) {
            return res;
        }

        ArrayList<Integer> row = new ArrayList<>();
        for (int i = 0; i < numRows; i++) {
            row.add(0, 1);
            for(int j = 1; j < row.size() - 1; j++) {
                row.set(j, row.get(j) + row.get(j+1));
            }
            res.add(new ArrayList<>(row));
        }

        return res;
    }
}
// @lc code=end

