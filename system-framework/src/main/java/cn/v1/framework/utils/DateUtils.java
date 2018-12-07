/**
 * Create by gen
 */
package cn.v1.framework.utils;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 日期工具类, 继承org.apache.commons.lang.time.DateUtils类
 * @author ThinkGem
 * @version 2014-4-15
 */
public class DateUtils extends org.apache.commons.lang3.time.DateUtils {

	private static final String API_HOLIDAY_URL_3RD_PARTY = "http://tool.bitefu.net/jiari/";
	private static final long TIME_SECOND_IN_MILLIS = 1000L;
	private static final long TIME_MINUTE_IN_MILLIS = 60000L;
	private static final long TIME_HOUR_IN_MILLIS = 3600000L;
	private static final long TIME_DAY_IN_MILLIS = 86400000L;
	private static final String[] PARSE_PATTERNS = new String[]{"yyyy-MM-dd", "yyyyMMdd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM", "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM", "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};
	private static final ThreadLocal<SimpleDateFormat> UTC_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
		protected SimpleDateFormat initialValue() {
			SimpleDateFormat formatter = new SimpleDateFormat();
			formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
			return formatter;
		}
	};

	public DateUtils() {
	}

	public static String format(Date date, String pattern) {
		return DateFormatUtils.format(date, pattern);
	}

	public static String format(long timeMillis, String pattern) {
		return DateFormatUtils.format(new Date(timeMillis), pattern);
	}

	public static String formatDate(Date date) {
		return format(date, "yyyy-MM-dd");
	}

	public static String formatDateShortTime(long timeMillis) {
		long day = timeMillis / 86400000L;
		long hour = timeMillis % 86400000L / 3600000L;
		long min = timeMillis % 3600000L / 60000L;
		return (day > 0L ? day + "," : "") + hour + ":" + min;
	}

	public static String formatDateTime(Date date) {
		return format(date, "yyyy-MM-dd HH:mm:ss");
	}

	public static String formatDateTime(long timeMillis) {
		long day = timeMillis / 86400000L;
		long hour = timeMillis % 86400000L / 3600000L;
		long min = timeMillis % 3600000L / 60000L;
		long s = timeMillis % 60000L / 1000L;
		long sss = timeMillis % 1000L;
		return (day > 0L ? day + "," : "") + hour + ":" + min + ":" + s + "." + sss;
	}

	public static String getDate() {
		return getNow("yyyy-MM-dd");
	}

	public static String getDateTime() {
		return format(new Date(), "yyyy-MM-dd HH:mm:ss");
	}

	public static String getDayOfMonth() {
		return format(new Date(), "dd");
	}

	public static double getDistanceOfTwoDate(Date before, Date after) {
		long beforeTime = before.getTime();
		long afterTime = after.getTime();
		return (double)((afterTime - beforeTime) / 86400000L);
	}

	public static long getDistanceOfSeconds(Date before, Date after) {
		long beforeTime = before.getTime();
		long afterTime = after.getTime();
		return (afterTime - beforeTime) / 1000L;
	}

	public static Date getFirstDayInMonth(Date date) {
		Calendar cale = Calendar.getInstance();
		cale.setTime(date);
		cale.set(5, cale.getActualMinimum(5));
		Date newDate = cale.getTime();
		return parseDate(DateFormatUtils.format(newDate, "yyyy-MM-dd") + " 00:00:00");
	}

	public static Date getLastDayInMonth(Date date) {
		Calendar cale = Calendar.getInstance();
		cale.setTime(date);
		cale.set(5, cale.getActualMaximum(5));
		Date newDate = cale.getTime();
		return parseDate(DateFormatUtils.format(newDate, "yyyy-MM-dd") + " 23:59:59");
	}

	public static String getMonth() {
		return format(new Date(), "MM");
	}

	public static Date[] getMonthDays(Date date) {
		Calendar cale = Calendar.getInstance();
		cale.setTime(date);
		int today = cale.get(5);
		int days = cale.getActualMaximum(5);
		long millis = cale.getTimeInMillis();
		Date[] dates = new Date[days];

		for(int i = 1; i <= days; ++i) {
			long sub = (long)(today - i) * 86400000L;
			dates[i - 1] = new Date(millis - sub);
		}

		cale = null;
		return dates;
	}

	public static String getNow(String pattern) {
		return format(new Date(), pattern);
	}

	public static String getTime() {
		return format(new Date(), "HH:mm:ss");
	}

	public static String getTwoHour(String st1, String st2) {
		String[] kk = null;
		String[] jj = null;
		kk = st1.split(":");
		jj = st2.split(":");
		if (Integer.parseInt(kk[0]) < Integer.parseInt(jj[0])) {
			return "0";
		} else {
			double y = Double.parseDouble(kk[0]) + Double.parseDouble(kk[1]) / 60.0D;
			double u = Double.parseDouble(jj[0]) + Double.parseDouble(jj[1]) / 60.0D;
			return y - u > 0.0D ? y - u + "" : "0";
		}
	}

	public static String getWeek() {
		return format(new Date(), "E");
	}

	public static String getWeekOfYear() {
		Calendar c = Calendar.getInstance(Locale.CHINA);
		String week = Integer.toString(c.get(3));
		if (week.length() == 1) {
			week = "0" + week;
		}

		String year = Integer.toString(c.get(1));
		return year + week;
	}

	public static String getYear() {
		return format(new Date(), "yyyy");
	}

	public static int isHoliday(String httpArg) {
		String httpUrl = "http://tool.bitefu.net/jiari/?d=" + httpArg;
		BufferedReader reader = null;
		int result = 0;
		StringBuffer sbf = new StringBuffer();

		try {
			URL url = new URL(httpUrl);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();
			InputStream is = connection.getInputStream();
			reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String strRead = null;

			while((strRead = reader.readLine()) != null) {
				sbf.append(strRead);
			}

			reader.close();
			result = Integer.valueOf(sbf.toString());
		} catch (Exception var9) {
			var9.printStackTrace();
		}

		return result;
	}

	public static boolean isLeapYear(Date date) {
		GregorianCalendar gc = (GregorianCalendar)Calendar.getInstance();
		gc.setTime(date);
		int year = gc.get(1);
		if (year % 400 == 0) {
			return true;
		} else if (year % 4 == 0) {
			return year % 100 != 0;
		} else {
			return false;
		}
	}

	public static boolean isSameWeek(Date date1, Date date2) {
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.setTime(date1);
		cal2.setTime(date2);
		int subYear = cal1.get(1) - cal2.get(1);
		if (0 == subYear) {
			if (cal1.get(3) == cal2.get(3)) {
				return true;
			}
		} else if (1 == subYear && 11 == cal2.get(2)) {
			if (cal1.get(3) == cal2.get(3)) {
				return true;
			}
		} else if (-1 == subYear && 11 == cal1.get(2) && cal1.get(3) == cal2.get(3)) {
			return true;
		}

		return false;
	}

	public static void main(String[] args) throws ParseException {
	}

	public static Date parseDate(Object str) {
		return str == null ? null : parseDate(str.toString());
	}

	public static Date parseDate(String str) {
		try {
			return parseDate(str, PARSE_PATTERNS);
		} catch (Exception var2) {
			return null;
		}
	}

	public static Date parseDateTime(String str) throws ParseException {
		return parseDate(str, new String[]{"yyyy-MM-dd HH:mm:ss"});
	}

	public static long pastDays(Date date) {
		long t = System.currentTimeMillis() - date.getTime();
		return t / 86400000L;
	}

	public static long pastDays(Date start, Date end) {
		long t = end.getTime() - start.getTime();
		return t / 86400000L;
	}

	public static long pastDays(String date) {
		long d = 0L;

		try {
			d = pastDays(parseDate(date));
		} catch (Exception var4) {
			var4.printStackTrace();
		}

		return d;
	}

	public static long pastDays(Date date, String pattern) {
		return pastDays(format(date, pattern));
	}

	public static long pastHour(Date date) {
		long t = System.currentTimeMillis() - date.getTime();
		return t / 3600000L;
	}

	public static long pastHour(Date start, Date end) {
		long t = end.getTime() - start.getTime();
		return t / 3600000L;
	}

	public static long pastMinutes(Date date) {
		long t = System.currentTimeMillis() - date.getTime();
		return t / 60000L;
	}

	public static long pastMinutes(Date start, Date end) {
		long t = end.getTime() - start.getTime();
		return t / 60000L;
	}

	public static long pastSeconds(Date date) {
		long t = System.currentTimeMillis() - date.getTime();
		return t / 1000L;
	}

	public static long pastSeconds(Date start, Date end) {
		long t = end.getTime() - start.getTime();
		return t / 1000L;
	}

	public static String utc2Local(String utcTime, String utcTimePatten, String localTimePatten) {
		SimpleDateFormat formatter = (SimpleDateFormat)UTC_FORMATTER.get();
		formatter.applyPattern(utcTimePatten);
		Date gpsUTCDate = null;

		try {
			gpsUTCDate = formatter.parse(utcTime);
		} catch (ParseException var6) {
			var6.printStackTrace();
			return null;
		}

		return format(gpsUTCDate, localTimePatten);
	}

	/**
	 * 获取指定时间的那天 00:00:00.000 的时间
	 */
	public static Date getDayBeginTime(final Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	/**
	 * 获取指定时间的那天 23:59:59.999 的时间
	 */
	public static Date getDayEndTime(final Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 59);
		c.set(Calendar.MILLISECOND, 999);
		return c.getTime();
	}
}
