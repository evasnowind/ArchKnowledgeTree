/**
 * ��N�Ż�Ʊ��ÿ��Ʊ����һ�����
 * ͬʱ��10�����ڶ�����Ʊ
 * ��дһ��ģ�����
 * 
 * ��������ĳ�����ܻ������Щ���⣿
 *  
 * ʹ��Vector����Collections.synchronizedXXX
 * ����һ�£������ܽ��������
 * 
 * @author ��ʿ��
 */
package com.prayerlaputa.juc.collections.fromVectorToQueue;

import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class TicketSeller2_Vector {
	static Vector<String> tickets = new Vector<>();
	
	
	static {
		for(int i=0; i<1000; i++) tickets.add("Ʊ ��ţ�" + i);
	}
	
	public static void main(String[] args) {
		
		for(int i=0; i<10; i++) {
			new Thread(()->{
				while(tickets.size() > 0) {
					/*
					�Իᱨ�쳣��
					Exception in thread "Thread-6" Exception in thread "Thread-5" Exception in thread "Thread-2" Exception in thread "Thread-8" Exception in thread "Thread-0" Exception in thread "Thread-7" Exception in thread "Thread-4" Exception in thread "Thread-1" Exception in thread "Thread-9" java.lang.ArrayIndexOutOfBoundsException: Array index out of range: 0
	at java.util.Vector.remove(Vector.java:831)
	at com.prayerlaputa.juc.collections.fromVectorToQueue.TicketSeller2.lambda$main$0(TicketSeller2.java:39)
	at java.lang.Thread.run(Thread.java:748)
java.lang.ArrayIndexOutOfBoundsException: Array index out of range: 0
	at java.util.Vector.remove(Vector.java:831)
	at com.prayerlaputa.juc.collections.fromVectorToQueue.TicketSeller2.lambda$main$0(TicketSeller2.java:39)
	at java.lang.Thread.run(Thread.java:748)

					ԭ����Ȼtickets����Vector����֤�����������̰߳�ȫ�ģ�������
					while(tickets.size() > 0)  �� tickets.remove(0) ������������û�б�֤ԭ���ԡ�
					����A��B�����̵߳���tickets.size()ʱ��tickets�պ�ֻʣ��1��Ԫ�أ����ǡ�����
					 */
					try {
						TimeUnit.MILLISECONDS.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					
					System.out.println("������--" + tickets.remove(0));
				}
			}).start();
		}
	}
}
