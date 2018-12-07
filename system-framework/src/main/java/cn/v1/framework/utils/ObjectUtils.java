package cn.v1.framework.utils;

import java.io.*;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

/**
 * @author wangrui
 */
public class ObjectUtils {

	public static boolean isNullOrEmptyString(Object o) {
		if(o == null)
			return true;
		if(o instanceof String) {
			String str = (String)o;
			if(str.length() == 0)
				return true;
		}
		return false;
	}
	
	/**
	 * 可以用于判断 Map,Collection,String,Array是否为空
	 * @param o
	 * @return
	 */
	@SuppressWarnings("all")
    public static boolean isEmpty(Object o)  {
        if(o == null) return true;

        if(o instanceof String) {
            if(((String)o).length() == 0){
                return true;
            }
        } else if(o instanceof Collection) {
            if(((Collection)o).isEmpty()){
                return true;
            }
        } else if(o.getClass().isArray()) {
            if(Array.getLength(o) == 0){
                return true;
            }
        } else if(o instanceof Map) {
            if(((Map)o).isEmpty()){
                return true;
            }
        }else {
            return false;
        }

        return false;
    }

	/**
	 * 可以用于判断 Map,Collection,String,Array是否不为空
	 * @param c
	 * @return
	 */
	public static boolean isNotEmpty(Object c) throws IllegalArgumentException {
		return !isEmpty(c);
	}

	/**
	 * 序列化对象
	 * @param object
	 * @return
	 */
	public static byte[] serialize(Object object) throws IOException {
		ObjectOutputStream oos = null;
		ByteArrayOutputStream baos = null;
		if (object != null) {
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			return baos.toByteArray();
		}
		return null;
	}

	/**
	 * 反序列化对象
	 * @param bytes
	 * @return
	 */
	public static Object deserialize(byte[] bytes)
			throws IOException, ClassNotFoundException {
		ByteArrayInputStream bais = null;
		if (bytes != null && bytes.length > 0) {
			bais = new ByteArrayInputStream(bytes);
			ObjectInputStream ois = new ObjectInputStream(bais);
			return ois.readObject();
		}
		return null;
	}

}
