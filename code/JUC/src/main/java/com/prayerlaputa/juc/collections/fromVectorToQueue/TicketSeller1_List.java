/**
 * ��N�Ż�Ʊ��ÿ��Ʊ����һ�����
 * ͬʱ��10�����ڶ�����Ʊ
 * ��дһ��ģ�����
 * 
 * ��������ĳ�����ܻ������Щ���⣿
 * �ظ����ۣ��������ۣ�
 * 
 * 
 * @author ��ʿ��
 */
package com.prayerlaputa.juc.collections.fromVectorToQueue;

import java.util.ArrayList;
import java.util.List;

public class TicketSeller1_List {
	static List<String> tickets = new ArrayList<>();
	
	static {
		for(int i=0; i<10000; i++) tickets.add("Ʊ��ţ�" + i);
	}


	public static void main(String[] args) {
		/*
		�ᱨ�쳣��
		Exception in thread "Thread-4" java.lang.ArrayIndexOutOfBoundsException: -1
	at java.util.ArrayList.remove(ArrayList.java:505)
	at com.prayerlaputa.juc.collections.fromVectorToQueue.TicketSeller1.lambda$main$0(TicketSeller1.java:28)
	at java.lang.Thread.run(Thread.java:748)

		ԭ��
		���̷߳��ʣ�tickets����û��֤�̰߳�ȫ����ʵ������ģ�ⳬ��
		 */
		for(int i=0; i<10; i++) {
			new Thread(()->{
				while(tickets.size() > 0) {
					System.out.println("������--" + tickets.remove(0));
				}
			}).start();
		}
	}
}
