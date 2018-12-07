package cn.v1.framework.holder;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurablePropertyResolver;

/**
 * Created by wangrui on 2017/6/11.
 */
public class PropertyPlaceHolder extends PropertySourcesPlaceholderConfigurer {

    private static ConfigurablePropertyResolver propertyResolver;

    @Override
    protected void processProperties(
            ConfigurableListableBeanFactory beanFactoryToProcess,
            final ConfigurablePropertyResolver propertyResolver) throws BeansException {
        super.processProperties(beanFactoryToProcess, propertyResolver);
        this.propertyResolver = propertyResolver;
    }

    public static String getProperty(String key) {
        String value = propertyResolver.getProperty(key);
        if (isNullOrEmptyString(value)) {
            return "";
        } else {
            return value;
        }
    }

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

    public static String getRequiredString(String key) {
        return propertyResolver.getRequiredProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return propertyResolver.getProperty(key, defaultValue);
    }

    public static Integer getInteger(String key) {
        if (isNullOrEmptyString(getProperty(key))) {
            return null;
        }
        return Integer.parseInt(getRequiredString(key));
    }

    public static int getInt(String key, int defaultValue) {
        if (isNullOrEmptyString(getProperty(key))) {
            return defaultValue;
        }
        return Integer.parseInt(getRequiredString(key));
    }

    public static int getRequiredInt(String key) {
        return Integer.parseInt(getRequiredString(key));
    }

    public static Long getLong(String key) {
        if (isNullOrEmptyString(getProperty(key))) {
            return null;
        }
        return Long.parseLong(getRequiredString(key));
    }

    public static long getLong(String key, long defaultValue) {
        if (isNullOrEmptyString(getProperty(key))) {
            return defaultValue;
        }
        return Long.parseLong(getRequiredString(key));
    }

    public static Long getRequiredLong(String key) {
        return Long.parseLong(getRequiredString(key));
    }

    public static Boolean getBoolean(String key) {
        if (isNullOrEmptyString(getProperty(key))) {
            return null;
        }
        return Boolean.parseBoolean(getRequiredString(key));
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        if (isNullOrEmptyString(getProperty(key))) {
            return defaultValue;
        }
        return Boolean.parseBoolean(getRequiredString(key));
    }

    public static boolean getRequiredBoolean(String key) {
        return Boolean.parseBoolean(getRequiredString(key));
    }

    public static Float getFloat(String key) {
        if (isNullOrEmptyString(getProperty(key))) {
            return null;
        }
        return Float.parseFloat(getRequiredString(key));
    }

    public static float getFloat(String key, float defaultValue) {
        if (isNullOrEmptyString(getProperty(key))) {
            return defaultValue;
        }
        return Float.parseFloat(getRequiredString(key));
    }

    public static Float getRequiredFloat(String key) {
        return Float.parseFloat(getRequiredString(key));
    }

    public static Double getDouble(String key) {
        if (isNullOrEmptyString(getProperty(key))) {
            return null;
        }
        return Double.parseDouble(getRequiredString(key));
    }

    public static double getDouble(String key, double defaultValue) {
        if (isNullOrEmptyString(getProperty(key))) {
            return defaultValue;
        }
        return Double.parseDouble(getRequiredString(key));
    }

    public static Double getRequiredDouble(String key) {
        return Double.parseDouble(getRequiredString(key));
    }

}
