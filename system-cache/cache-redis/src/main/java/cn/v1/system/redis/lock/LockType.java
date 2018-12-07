package cn.v1.system.redis.lock;

import java.util.concurrent.TimeUnit;

public enum LockType {
	// 统一命名规则：锁使用的 ID 放在锁名字的第一个单词，避免使用了不同的 ID 生成锁
	CUSTOMER_PAYING("支付事务"),
	CUSTOMER_ORDERING("订单事务"),
	CUSTOMER_ON_DEPOSIT("押金事务"),
	ENTITY_GET_QR_CODE("获取实体二维码事务"),
	PHONE_REGISTING("注册事务"),
	TRADE_DEALING("交易事务"),
	TERMINAL_CMD("控车指令", 30),
	VEHICLE_UNDER_CONTROL("客户端控车", 2),
	CUSTOMER_RECEIVING_COUPONS("订单分享优惠券"),
	TASK_DISPATCHING("调度任务"),
	DISPATCHER_CREATING_TASK("调度任务创建事务"),
	DISPATCHER_CLAIMING_TASK("调度任务领取事务"),
	ACCESS_TOKEN("获取accessToken事务"),
	JSAPI_TICKET("获取jsapiTicket事务"),
	CREDIT_TOKEN("获取creditToken事务"),
	CAR_UPDATING_STATUS("CAR_UPDATING_STATUS", 3, TimeUnit.SECONDS),
	EVNET_HANDLING("EVNET_HANDLING"),
	JOB_SCHEDULING("JOB_SCHEDULING", 30, TimeUnit.MINUTES);

	private static final int DEFAULT_LOCK_EXPIRE_MINUTE = 3;

	private final String title;
	private final int expireSeconds;
	private String expireStr;

	private String hint;

	LockType(String title) {
		this(title, DEFAULT_LOCK_EXPIRE_MINUTE, TimeUnit.MINUTES);
	}

	LockType(String title, int expireSeconds) {
		this.title = title;
		this.expireSeconds = expireSeconds;
		this.expireStr = expireSeconds + " 秒";
		this.hint = "有处理中的" + title + "，请 " + expireStr + "后重试。如反复出现，请联系客服。";
	}

	/**
	 * 根据实际需要设置超时时间，单位暂时只支持 分 和 秒，如果需要增加更多单位，请修改单位判断
	 * @param title
	 * @param expire
	 * @param unit
	 */
	LockType(String title, int expire, TimeUnit unit) {
		this(title, (int) unit.toSeconds(expire));
		if (unit.equals(TimeUnit.MINUTES)) {
			this.expireStr = expire + " 分";
			this.hint = "有处理中的" + title + "，请 " + expireStr
					+ "后重试。如反复出现，请联系客服。";
		}
	}

	public int getExpireSeconds() {
		return expireSeconds;
	}

	/**
	 * 返回该锁设定的超时时间的字符串，方便自行拼接提示语
	 * @return
	 */
	public String getExpireStr() {
		return expireStr;
	}

	public String getHint() {
		return hint;
	}

	public String getTitle() {
		return title;
	}
}
