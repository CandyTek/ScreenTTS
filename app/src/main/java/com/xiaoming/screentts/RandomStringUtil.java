package com.xiaoming.screentts;

import java.util.Random;

/** 随机字符串工具类 */
@SuppressWarnings("unused")
public class RandomStringUtil {
	private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

	/** 生成指定数量的随机文本 */
	public static String generateRandomString(int length) {
		StringBuilder sb = new StringBuilder(length);
		Random random = new Random();

		for (int i = 0; i < length; i++) {
			int index = random.nextInt(CHARACTERS.length());
			char randomChar = CHARACTERS.charAt(index);
			sb.append(randomChar);
		}
		return sb.toString();
	}
	
}
