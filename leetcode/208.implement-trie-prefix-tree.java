/*
 * @lc app=leetcode id=208 lang=java
 *
 * [208] Implement Trie (Prefix Tree)
 */

// @lc code=start
class Trie {

    
    private TrieNode root;

    /** Initialize your data structure here. */
    public Trie() {
        root = new TrieNode();
    }


    /** Inserts a word into the trie.
     * 插入字符串时，有两种情况：
     * 1、当前处理的字符已经在Trie树中，直接挪动指针，处理下一个字符即可
     * 2、当前处理的字符不在Trie树中，需要先在当前层中创建该字符对应节点，然后再挪动指针，继续处理下个字符
     * */
    public void insert(String word) {
        TrieNode node = root;
        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);
            if (!node.containKey(ch)) {
                node.put(ch, new TrieNode());
            }
            node = node.get(ch);
        }
        /*
        这一步给isEnd置为true很重要，说明这个Trie树中包含这个单独，这个节点不一定是叶子节点。
         */
        node.setEnd(true);
    }

    /** Returns if the word is in the trie.
     * 搜索一个word是否在Trie树中，有以下几种情况：
     * 1、在当前节点中找不到：说明该节点肯定不在Trie树中，直接返回false
     * 2、当前节点中找到，需要细分：
     *  2.1 已经是word最后一个字符 && 当前节点是最后一个节点
     *      说明该word存在，返回true
     *
     *  2.2 不是最后一个字符 && 当前节点不是最后一个节点
     *      说明还在比较过程中，需要挪动指针，比较下一个字符
     *
     *  2.3  已经是word最后一个字符 && 当前节点不是最后一个节点
     *      word不在树中，返回false
     *
     *  2.4 不是最后一个字符 && 当前节点是最后一个节点
     *      word不在树中，返回false
     *
     * 进一步简化：
     * 使用一个工具方法，获得该word所能对应的node节点，若该节点不为null && isEnd = true，说明该word存在
     * */
    public boolean search(String word) {
        TrieNode node = searchPrefix(word);
        return null != node && node.isEnd();
    }

    private TrieNode searchPrefix(String word) {
        TrieNode node = root;
        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);
            if (node.containKey(ch)) {
                node = node.get(ch);
            } else {
                return null;
            }
        }
        return node;
    }

    /** Returns if there is any word in the trie that starts with the given prefix.
     *
     * */
    public boolean startsWith(String prefix) {
        TrieNode node = searchPrefix(prefix);
        return null != node;
    }

}

class TrieNode {
    
    private boolean isEnd;
    private TrieNode[] links;
    private static final int NODE_LINKS_NUM = 26;

    public TrieNode() {
        links = new TrieNode[NODE_LINKS_NUM];
    }

    public boolean containKey(char ch) {
        return links[ch - 'a'] != null;
    }

    public TrieNode get(char ch) {
        return links[ch - 'a'];
    }

    public void put(char ch, TrieNode node) {
        links[ch - 'a'] = node;
    }

    public void setEnd(boolean isEnd) {
        this.isEnd = isEnd;
    }

    public boolean isEnd() {
        return isEnd;
    }
}

/**
 * Your Trie object will be instantiated and called as such:
 * Trie obj = new Trie();
 * obj.insert(word);
 * boolean param_2 = obj.search(word);
 * boolean param_3 = obj.startsWith(prefix);
 */
// @lc code=end

