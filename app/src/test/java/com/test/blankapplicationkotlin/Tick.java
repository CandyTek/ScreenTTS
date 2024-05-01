package com.test.blankapplicationkotlin;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** Log 记时工具 */
@SuppressWarnings("unused")
public class Tick {
	private final Map<String,Long> startTime = new HashMap<>();
	private final Map<String,Long> endTime = new HashMap<>();
	private final ArrayList<String> labels = new ArrayList<>();

	/** 添加标签并开始计时 */
	public void add(String label) {
		startTime.put(label,System.currentTimeMillis());
		labels.add(label);
	}

	/** 结束计时并计算耗时 */
	public void tick(String label) {
		if (!startTime.containsKey(label)) {
			startTime.put(label,System.currentTimeMillis());
		} else {
			endTime.put(label,System.currentTimeMillis());
		}
	}

	/** 打印所有标签和耗时结果 */
	public void msgAll() {
		StringBuilder result = new StringBuilder("耗时结果：\n\n");
		for (String label : labels) {
			Long startTime = this.startTime.get(label);
			Long endTime = this.endTime.get(label);
			if (startTime != null && endTime != null) {
				long duration = endTime - startTime;
				result.append(label).append(": ").append(duration).append(" 毫秒\n");
			}
		}
		System.out.println(result);
		// Log.w("Tick",result.toString());

	}


}
