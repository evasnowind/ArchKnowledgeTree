





```sequence
Title: Raft日志复制
client -> leader: 请求
leader -> leader: 追加到本地log，\nuncommitted
leader -> follower 1: AppendEntries
leader -> follower 2: AppendEntries
leader -> follower 3: AppendEntries
follower 1 -> follower 1: 记录日志
follower 1 --> leader: ACK
follower 2 -> follower 2: 记录日志
follower 2 --> leader: ACK
leader -> leader: 收到多数ACK，本地log\ncommitted, log应用到\n本地状态机
leader --> client: 返回结果
follower 3 -> follower 3: 记录日志
follower 3 --> leader: ACK
```

