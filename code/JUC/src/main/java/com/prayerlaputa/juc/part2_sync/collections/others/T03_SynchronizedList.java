package com.prayerlaputa.juc.part2_sync.collections.others;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class T03_SynchronizedList {
	public static void main(String[] args) {
		List<String> strs = new ArrayList<>();
		List<String> strsSync = Collections.synchronizedList(strs);
	}
}
