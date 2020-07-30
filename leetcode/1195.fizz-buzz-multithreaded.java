/*
 * @lc app=leetcode id=1195 lang=java
 *
 * [1195] Fizz Buzz Multithreaded
 */

// @lc code=start
class FizzBuzz {
    private int n;
    private int i = 1;  //从1开始数
    public FizzBuzz(int n) {
        this.n = n;
    }

    /*
    https://leetcode-cn.com/problems/fizz-buzz-multithreaded/solution/99shi-jian-suan-fa-by-weitongbai/
     */
    // printFizz.run() outputs "fizz".
    public void fizz(Runnable printFizz) throws InterruptedException {
        synchronized(this){
            while(i <= n){
                if(!(i%5 != 0 && i%3 == 0)) wait();
                if(!(i%5 != 0 && i%3 == 0)) continue;
                if(i > n) break;
                printFizz.run();
                i++;
                notifyAll();
            }
        }
    }

    // printBuzz.run() outputs "buzz".
    public void buzz(Runnable printBuzz) throws InterruptedException {
        synchronized(this){
            while(i <= n){
                if(!(i%5 == 0 && i%3 != 0)) wait();
                if(!(i%5 == 0 && i%3 != 0)) continue;
                if(i > n) break;
                printBuzz.run();
                i++;
                notifyAll();
            }
        }
    }

    // printFizzBuzz.run() outputs "fizzbuzz".
    public void fizzbuzz(Runnable printFizzBuzz) throws InterruptedException {
        synchronized(this){
            while(i <= n){
                if(!(i%3 == 0 && i%5 == 0)) wait();
                if(!(i%3 == 0 && i%5 == 0)) continue;
                if(i > n) break;
                printFizzBuzz.run();
                i++;
                notifyAll();
            }
        }
    }

    // printNumber.accept(x) outputs "x", where x is an integer.
    public void number(IntConsumer printNumber) throws InterruptedException {
        synchronized(this){
            while(i <= n){
                if(i%5 == 0 || i%3 == 0) wait();
                if(i%5 == 0 || i%3 == 0) continue;
                if(i > n) break;
                printNumber.accept(i);
                i++;
                notifyAll();
            }
        }
    }
}
// @lc code=end

