package yk.core.util.gson;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.sql.Timestamp;

/**
 * @author 杨剑
 * @date 2018/10/25
 */
public class GsonTimestampTypeAdapter implements JsonSerializer<Timestamp>, JsonDeserializer<Timestamp> {

	@Override
	public JsonElement serialize(Timestamp src, Type typeOfSrc, JsonSerializationContext context) {
		return new JsonPrimitive(src.getTime());
	}

	@Override
	public Timestamp deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		if (json instanceof JsonPrimitive) {
			return new Timestamp(json.getAsJsonPrimitive().getAsLong());
		} else {
			throw new JsonParseException("java.sql.Timestamp格式转换失败。");
		}
	}
}
