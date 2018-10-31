package yk.core.base;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.DateConverter;

import java.io.Serializable;
import java.util.TimeZone;

/**
 * @author 杨剑
 * @date 2018/10/25
 */
public class BaseObject implements Serializable {

	@Override
	public String toString() {
		XStream xStream = new XStream();
		xStream.registerConverter(new DateConverter("yyyy-MM-dd HH:mm:ss.sss", null, TimeZone.getTimeZone("GMT+8")));
		return xStream.toXML(this);
	}
}
