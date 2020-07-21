/*
 * @lc app=leetcode id=384 lang=java
 *
 * [384] Shuffle an Array
 */

// @lc code=start
class Solution {

    int[] original;
    int[] array;
    Random random;

    public Solution(int[] array) {
        if (array == null) {
            throw new IllegalArgumentException();
        }
        original = array.clone();
        this.array = array;
        random = new Random();
    }

    public int[] reset() {
        return original;
    }

    public int[] shuffle() {
        for (int i = 1; i < array.length; i++) {
            int rand = random.nextInt(i + 1); // random int from [0, i+1) exclusive. Same as [0, i] inclusive
            swap(array, i, rand);
        }
        return array;
    }

    private void swap(int[] array, int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
}

/**
 * Your Solution object will be instantiated and called as such:
 * Solution obj = new Solution(nums);
 * int[] param_1 = obj.reset();
 * int[] param_2 = obj.shuffle();
 */
// @lc code=end

