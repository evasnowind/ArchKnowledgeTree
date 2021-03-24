/*
 * @lc app=leetcode id=341 lang=java
 *
 * [341] Flatten Nested List Iterator
 */

// @lc code=start
/**
 * // This is the interface that allows for creating nested lists.
 * // You should not implement it, or speculate about its implementation
 * public interface NestedInteger {
 *
 *     // @return true if this NestedInteger holds a single integer, rather than a nested list.
 *     public boolean isInteger();
 *
 *     // @return the single integer that this NestedInteger holds, if it holds a single integer
 *     // Return null if this NestedInteger holds a nested list
 *     public Integer getInteger();
 *
 *     // @return the nested list that this NestedInteger holds, if it holds a nested list
 *     // Return empty list if this NestedInteger holds a single integer
 *     public List<NestedInteger> getList();
 * }
 */
public class NestedIterator implements Iterator<Integer> {

    private List<Integer> vals;
    private Iterator<Integer> cur;

    public NestedIterator(List<NestedInteger> nestedList) {
        vals = new ArrayList<>();

        dfs(nestedList);

        cur = vals.iterator();
    }

    private void dfs(List<NestedInteger> nestedList) {
        for(NestedInteger element : nestedList) {
            if (element.isInteger()) {
                vals.add(element.getInteger());
            } else {
                dfs(element.getList());
            }
        }
    }

    @Override
    public Integer next() {
        return cur.next();
    }

    @Override
    public boolean hasNext() {
        return cur.hasNext();
    }
}

/**
 * Your NestedIterator object will be instantiated and called as such:
 * NestedIterator i = new NestedIterator(nestedList);
 * while (i.hasNext()) v[f()] = i.next();
 */
// @lc code=end

