
Callable Runnable + ret
Future �洢ִ�н����Ż�����Ľ�����첽����
FutureTask ����> Future + Runnable �ӿڶ��߳�
CompletableFuture --> ������Future�Ľ����������������Խ��������ϴ���


�̳߳ؾܾ����ԣ�Ĭ���ṩ��4�֣������Զ���
����ע��˵��Ҫ��ȷ��






һ��˼���⣺

���ԣ������ṩһ�����ӷ��񣬶��ķ�����˺ܶ࣬10���ˣ���ô�Ż���
������Ŀ��
1���ܵķ������ַ�����Ե������
2��ÿ�����������ö��У���������һ�����߳�����
3����������

FixedThread
���Բ���ִ��

# ���У�Concurrency�� VS ����(Paralism)

# ThreadPolExecutorԴ�����  

Worker��
    ʵ����Runnable��AQS�ӿ�
    ��������״̬���������·�װ
    ���߳��������worker��������Ҫ����AQS

submit

execute
    1 core queue noncore

addWorker
    counter+1
    addWorker
    start
    
    