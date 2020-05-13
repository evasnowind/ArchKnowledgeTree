/*
 * @lc app=leetcode id=155 lang=java
 *
 * [155] Min Stack
 */

// @lc code=start
class MinStack {

    Stack<Integer> minStack = new Stack<Integer>();
	Stack<Integer> stack = new Stack<Integer>();
	
	/** initialize your data structure here. */
    public MinStack() {
    }
    
    public void push(int x) {
    	stack.push(x);
    	
    	if(minStack.isEmpty()){
    		minStack.push(x);
    	} else {
    		if(minStack.peek() >= x){
    			minStack.push(x);
    		}
    	}
    }
    
    public void pop() {
    	int top = stack.pop();
    	if(!minStack.isEmpty() && top == minStack.peek()) {
    		minStack.pop();
    	}
    }
    
    public int top() {
    	if(!stack.isEmpty()){
    		return stack.peek();
    	}
        return Integer.MAX_VALUE;
    }
    
    public int getMin() {
    	if(!minStack.isEmpty()){
    		return minStack.peek();
    	}
        return Integer.MAX_VALUE;
    }
}

/**
 * Your MinStack object will be instantiated and called as such:
 * MinStack obj = new MinStack();
 * obj.push(x);
 * obj.pop();
 * int param_3 = obj.top();
 * int param_4 = obj.getMin();
 */
// @lc code=end

