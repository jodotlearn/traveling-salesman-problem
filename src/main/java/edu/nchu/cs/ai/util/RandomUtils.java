package edu.nchu.cs.ai.util;

public class RandomUtils {
	public static int randIntStartFromOne(int bound) {
		return (int) (Math.random()*bound);
	}
	public static double randDouble() {
		return Math.random();
	}
}
