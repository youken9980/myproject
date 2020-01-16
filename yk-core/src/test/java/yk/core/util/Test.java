package yk.core.util;

/**
 * @author 杨剑
 * @date 2018/10/25
 */
public class Test {

	public static void main(String[] args) throws Exception {
		long beginTime, endTime = System.currentTimeMillis();

		beginTime = endTime;
		endTime = System.currentTimeMillis();
		System.out.println(String.format("total cost : %sms", endTime - beginTime));
	}
}
