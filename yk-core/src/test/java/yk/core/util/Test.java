package yk.core.util;

/**
 * @author 杨剑
 * @date 2018/10/25
 */
public class Test {

	public static void main(String[] args) throws Exception {
		long beginTms = System.currentTimeMillis(), endTms;
		Thread.sleep(100);
		endTms = System.currentTimeMillis();
		System.out.println(endTms - beginTms);
		beginTms = endTms;
		Thread.sleep(100);
		endTms = System.currentTimeMillis();
		System.out.println(endTms - beginTms);
	}
}
