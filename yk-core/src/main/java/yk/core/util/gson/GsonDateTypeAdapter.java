package yk.core.util.gson;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author 杨剑
 * @date 2018/10/25
 */
public class GsonDateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {

	private String dateFormat;
	private SimpleDateFormat format;

	public GsonDateTypeAdapter() {
		this(null);
	}

	public GsonDateTypeAdapter(String dateFormat) {
		this.dateFormat = dateFormat == null ? GsonUtil.DATE_PATTERN_DEFAULT : dateFormat;
		format = new SimpleDateFormat(this.dateFormat);
	}

	@Override
	public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
		return new JsonPrimitive(format.format(src));
	}

	@Override
	public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		if (json instanceof JsonPrimitive) {
			try {
				return format.parse(json.getAsString());
			} catch (ParseException e) {
				throw new JsonParseException("java.util.Date在JSON字符串中必须符合" + dateFormat + "格式。");
			}
		} else {
			throw new JsonParseException("java.util.Date在JSON字符串中必须符合" + dateFormat + "格式。");
		}
	}
}
