import java.awt.List;
import java.util.ArrayList;

/*
 * @lc app=leetcode id=1203 lang=java
 *
 * [1203] Sort Items by Groups Respecting Dependencies
 */

// @lc code=start
class Solution {
    /**
     * 
     * 此处m应该表示的是组的编号
     * 
     * @param n
     * @param m
     * @param group
     * @param beforeItems
     * @return
     */
    public int[] sortItems(int n, int m, int[] group, List<List<Integer>> beforeItems) {
        for (int i = 0; i < group.length; i++) {
            if (group[i] == -1) {
                /*
                给项目打个临时的编号，方便后续排序
                 */
                group[i] = m;
                m++;
            }
        }

        /*
        将输入的图转化成常见的邻接表形式。
        注意此处的m可能已经不是初始传入的值（若都有归属则此时m不变）。
         */
        List<Integer>[] groupAdj = new ArrayList[m];
        List<Integer>[] itemAdj = new ArrayList[n];

        for(int i = 0; i < m; i++) {
            groupAdj[i] = new ArrayList<>();
        }
        for (int i = 0; i < n; i++) {
            itemAdj[i] = new ArrayList<>();
        }

        int[] groupsInDegree = new int[m];
        int[] itemsInDegree = new int[n];
        int len = group.length;
        for(int i = 0; i < len; i++) {
            int currentGroup = group[i];
            /*
            找到当前项目组的前置项目，放到邻接表中
             */
            for(int beforeItem : beforeItems.get(i)) {
                /*
                找到前缀项目的所属，确认
                 */
                int beforeGroup = group[beforeItem];
                if (beforeGroup != currentGroup) {
                    groupAdj[beforeGroup].add(currentGroup);
                    groupsInDegree[currentGroup]++;
                }
            }
        }
        /*
        邻接表表示图，把 beforeItem -> item（第i个节点）这个关系，
        在item行体现出来
        */
        for(int i = 0; i < n; i++) {
            for(Integer item : beforeItems.get(i)) {
                itemAdj[item].add(i);
                itemsInDegree[i]++;
            }
        }
        //得到组的拓扑排序
        List<Integer> groupsList = topologicalSort(groupAdj, groupsInDegree, m);
        if (groupsList.size() == 0) {
            return new int[0];
        }
        //得到项目的拓扑排序
        List<Integer> itemsList = topologicalSort(itemAdj, itemsInDegree, n);
        if (itemsList.size() == 0) {
            return new int[0];
        }

        /*
        根据项目的拓扑排序结果，项目到组的多对一关系，建立组到项目的一对多关系
         */
        Map<Integer, List<Integer>> groups2Items = new HashMap<>();
        for (Integer item : itemsList) {
            groups2Items.computeIfAbsent(group[item], key -> new ArrayList<>()).add(item);
        }
        /*

        把组的拓扑排序结果替换成为项目的拓扑排序结果
         */
        List<Integer> res = new ArrayList<>();
        for(Integer groupId : groupsList) {
            List<Integer> items = groups2Items.getOrDefault(groupId, new ArrayList<>());
            res.addAll(items);
        }

        return res.stream().mapToInt(Integer::valueOf).toArray();
    }


    private List<Integer> topologicalSort(List<Integer>[] adj, int[] inDegree, int n) {
        List<Integer> res = new ArrayList<>();
        Queue<Integer> queue = new LinkedList<>();

        //拓扑排序：第一步，先将入度为0的元素放入到队列中
        for (int i = 0; i < n; i++) {
            if (inDegree[i] == 0) {
                queue.offer(i);
            }
        }

        /*
        与BFS类似，利用队列实现树形的遍历，以便保证拓扑顺序。
        实际上拓扑排序的代码实现就是个模板，只是找下一个元素这里
        需要根据题目要求、数据结构灵活变化一下。
         */
        while(!queue.isEmpty()) {
            Integer front = queue.poll();
            res.add(front);
            for(int successor : adj[front]) {
                 /*
                由于移除了一个节点front，front所连着的
                相关节点入度也要减少1，如果入度变为0还
                要将其加入队列中，说明该节点是拓扑排序后
                的下一个节点。
                即利用入度来控制顺序。
                而此处的入度数组就是我们需要事先准备好的。
                */
                inDegree[successor]--;
                if (inDegree[successor] == 0) {
                    queue.offer(successor);
                }
            }
        }

        if (res.size() == n) {
            /*
            将所有元素都遍历完，说明拓扑排序完毕
             */
            return res;
        }

        return new ArrayList<>();
    }
}
// @lc code=end
