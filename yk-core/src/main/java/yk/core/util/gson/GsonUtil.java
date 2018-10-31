package yk.core.util.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.Date;

/**
 * @author 杨剑
 * @date 2018/10/25
 */
public class GsonUtil {

	public static final String DATE_PATTERN_DEFAULT = "yyyy-MM-dd HH:mm:ss";

	/**
	 * 根据指定泛型将Json字符串转换为指定类型对象。
	 *
	 * @param json  Json字符串
	 * @param clazz 指定泛型
	 * @return 指定类型对象
	 */
	public static <T> T fromJson(String json, Class<T> clazz) {
		return fromJson(json, clazz, null);
	}

	/**
	 * 根据指定泛型和日期格式将Json字符串转换为目标类型对象。
	 *
	 * @param json        Json字符串
	 * @param clazz       指定泛型
	 * @param datePattern 指定日期格式，当目标类型对象中包含日期属性时，该参数不能为null
	 * @return 目标类型对象
	 */
	public static <T> T fromJson(String json, Class<T> clazz, String datePattern) {
		if (json == null || json.trim().length() < 1) {
			return null;
		}
		Gson gson = getGson(datePattern);
		return gson.fromJson(json, clazz);
	}

	/**
	 * 根据指定泛型将Json字符串转换为指定类型对象。
	 *
	 * @param json Json字符串
	 * @param type 指定泛型
	 * @return 指定类型对象
	 */
	public static <T> T fromJson(String json, Type type) {
		return (T) fromJson(json, type, null);
	}

	/**
	 * 根据指定泛型和日期格式将Json字符串转换为目标类型对象。
	 *
	 * @param json        Json字符串
	 * @param type        指定泛型
	 * @param datePattern 指定日期格式，当目标类型对象中包含日期属性时，该参数不能为null
	 * @return 目标类型对象
	 */
	public static <T> T fromJson(String json, Type type, String datePattern) {
		if (json == null || json.trim().length() < 1) {
			return null;
		}
		Gson gson = getGson(datePattern);
		return (T) gson.fromJson(json, type);
	}

	/**
	 * 将给定对象转换为Json字符串。
	 *
	 * @param src 要转换的对象
	 * @return 转换后的Json字符串
	 */
	public static String toJson(Object src) {
		return toJson(src, null, null);
	}

	/**
	 * 根据指定日期格式，将给定对象转换为Json字符串。
	 *
	 * @param src         要转换的对象
	 * @param datePattern 指定日期格式
	 * @return 转换后的Json字符串
	 */
	public static String toJson(Object src, String datePattern) {
		return toJson(src, null, datePattern);
	}

	/**
	 * 根据指定类型，将给定对象转换为Json字符串。
	 *
	 * @param src       要转换的对象
	 * @param typeOfSrc 指定类型
	 * @return 转换后的Json字符串
	 */
	public static String toJson(Object src, Type typeOfSrc) {
		return toJson(src, typeOfSrc, null);
	}

	/**
	 * 根据指定类型、指定日期格式，将给定对象转换为Json字符串。
	 *
	 * @param src         要转换的对象
	 * @param typeOfSrc   指定类型
	 * @param datePattern 指定日期格式
	 * @return 转换后的Json字符串
	 */
	public static String toJson(Object src, Type typeOfSrc, String datePattern) {
		if (src == null) {
			return null;
		}
		Type strType = typeOfSrc;
		if (strType == null) {
			strType = src.getClass();
		}
		Gson gson = getGson(datePattern);
		return gson.toJson(src, strType);
	}

	/**
	 * 根据指定日期格式获得Gson对象。
	 *
	 * @param datePattern 指定日期格式
	 * @return Gson对象
	 */
	private static Gson getGson(final String datePattern) {
		String strPattern = datePattern == null ? DATE_PATTERN_DEFAULT : datePattern;
		GsonBuilder builder = new GsonBuilder();
		builder.setDateFormat(strPattern);
		builder.registerTypeAdapter(Date.class, new GsonDateTypeAdapter(strPattern));
		builder.registerTypeAdapter(Timestamp.class, new GsonTimestampTypeAdapter());
		return builder.create();
	}
}
