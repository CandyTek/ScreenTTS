package com.test.blankapplicationkotlin;

import org.junit.Test;

import java.util.regex.Pattern;

public class ExampleUnitTest {

	private int count = 0;
	private final Tick tick = new Tick();
	private final String string = "uih45fe1541f47e5d5f4e58455s6d5f415微信fe15f5";

	@Test
	public void test_match1() {
		final String regex = "^\\d\\d微信$";

		final Pattern pattern = Pattern.compile(regex);

		tick.add("test1");
		for (int i = 0;i < 100000000;i++) {
			if (pattern.matcher(string).matches()) {
				count++;
			}
		}
		tick.tick("test1");
		tick.msgAll();
		System.out.println(count);
	}

	@Test
	public void test_match2() {
		final String regex = "\\d\\d微信";

		final Pattern pattern = Pattern.compile(regex);

		tick.add("test1");
		for (int i = 0;i < 100000000;i++) {
			if (pattern.matcher(string).matches()) {
				count++;
			}
		}
		tick.tick("test1");
		tick.msgAll();
		System.out.println(count);
	}
}
