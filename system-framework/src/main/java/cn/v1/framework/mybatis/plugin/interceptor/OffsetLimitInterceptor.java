package cn.v1.framework.mybatis.plugin.interceptor;


import cn.v1.framework.jdbc.dialect.Dialect;
import cn.v1.framework.jdbc.parser.SqlParser;
import cn.v1.framework.mybatis.support.SQLHelp;
import cn.v1.framework.mybatis.util.PropertiesUtil;
import cn.v1.framework.page.OrderBy;
import cn.v1.framework.page.PageBounds;
import cn.v1.framework.page.PageList;
import cn.v1.framework.page.Pagination;
import com.google.common.collect.Maps;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.mapping.MappedStatement.Builder;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.reflection.wrapper.ObjectWrapper;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;


/**
 * 为MyBatis提供基于方言(Dialect)的分页查询的插件
 *
 * 将拦截Executor.query()方法实现分页方言的插入.
 * @author wr
 *
 */

@Intercepts({@Signature(
		type= Executor.class,
		method = "query",
		args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
public class OffsetLimitInterceptor implements Interceptor {
	private static Logger logger = LoggerFactory.getLogger(OffsetLimitInterceptor.class);
	static int MAPPED_STATEMENT_INDEX = 0;
	static int PARAMETER_INDEX = 1;
	static int ROWBOUNDS_INDEX = 2;

	static ExecutorService pool;
	boolean asyncTotalCount = false;
	String dialectClass = null;
	static final SqlParser sqlParser = new SqlParser();


	private String getPageSQL(String sql, PageBounds customRowBounds, RowBounds rowBounds, Dialect dialect){
		String pageSQL = sql;
		if(customRowBounds.getOrders() != null && !customRowBounds.getOrders().isEmpty()){
			pageSQL = getSortString(sql, customRowBounds.getOrders());
		}
		if(rowBounds.getOffset() != RowBounds.NO_ROW_OFFSET
				|| rowBounds.getLimit() != RowBounds.NO_ROW_LIMIT){
			pageSQL = dialect.getLimitString(pageSQL, rowBounds.getOffset(), "__offset", rowBounds.getLimit(), "__limit", true);
		}
		return pageSQL;
	}

	public Object intercept(final Invocation invocation) throws Throwable {
		final Executor executor = (Executor) invocation.getTarget();
		final Object[] queryArgs = invocation.getArgs();
		final MappedStatement ms = (MappedStatement)queryArgs[MAPPED_STATEMENT_INDEX];
		final Object parameter = queryArgs[PARAMETER_INDEX];
		final RowBounds rowBounds = (RowBounds)queryArgs[ROWBOUNDS_INDEX];
		final PageBounds customRowBounds = new PageBounds(rowBounds);



		if(customRowBounds.getOffset() == RowBounds.NO_ROW_OFFSET
				&& customRowBounds.getLimit() == RowBounds.NO_ROW_LIMIT
				&& customRowBounds.getOrders().isEmpty()){
			return invocation.proceed();
		}

		Dialect dialect = null;
		List<ParameterMapping> parameterMappings = null;
		Map<String, Object> pageParameters = null;

		try {
			Class clazz = Class.forName(dialectClass);
			Constructor constructor = clazz.getConstructor();
			dialect = (Dialect)constructor.newInstance();
		} catch (Exception e) {
			throw new ClassNotFoundException("Cannot create dialect instance: "+dialectClass, e);
		}

		final BoundSql boundSql = ms.getBoundSql(parameter);

		// 构建分页sql语句
		StringBuffer bufferSql = new StringBuffer(boundSql.getSql().trim());
		if(bufferSql.lastIndexOf(";") == bufferSql.length()-1){
			bufferSql.deleteCharAt(bufferSql.length()-1);
		}
		String sql = bufferSql.toString();

		String pageSQL = getPageSQL(sql, customRowBounds, rowBounds, dialect);


		//构建 总记录sql
		final String countSQL = sqlParser.getSmartCountSql(sql);
//        countSQL = getCountString(sql);

		if(logger.isDebugEnabled()){
			logger.debug("\npageSQL: [{}]\n countSQL: [{}]" , pageSQL, countSQL);
		}


		// 为mybatis构建 parameterMappings pageParameters 参数
		parameterMappings = new ArrayList(boundSql.getParameterMappings());
		pageParameters = Maps.newHashMap();
		if(parameterMappings != null) {
			Configuration configuration = ms.getConfiguration();
			MetaObject metaObject = configuration.newMetaObject(parameter);
			TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
			ObjectWrapper wrapper = metaObject.getObjectWrapper();
			for (ParameterMapping parameterMapping : parameterMappings){
				if (parameterMapping.getMode() != ParameterMode.OUT) {
					Object value;
					String propertyName = parameterMapping.getProperty();
					PropertyTokenizer prop = new PropertyTokenizer(propertyName);
					if (parameter == null) {
						value = null;
					} else if (typeHandlerRegistry.hasTypeHandler(parameter.getClass())) {
						value = parameter;
					} else if (boundSql.hasAdditionalParameter(propertyName)) {
						value = boundSql.getAdditionalParameter(propertyName);
					} else if (propertyName.startsWith(ForEachSqlNode.ITEM_PREFIX) && boundSql.hasAdditionalParameter(prop.getName())) {
						value = wrapper.get(prop);
						if (value != null) {
							value = configuration.newMetaObject(value).getValue(propertyName.substring(prop.getName().length()));
						}
					} else {
						value = metaObject == null ? null : metaObject.getValue(propertyName);
					}
					pageParameters.put(parameterMapping.getProperty(), value);
				}
			}
		}

		List<Dialect.PageParameter> params = dialect.getPageParameter();
		for(Dialect.PageParameter param : params){
			ParameterMapping parameterMapping = new ParameterMapping.Builder(ms.getConfiguration(), param.getName(), param.getValue().getClass()).build();
			parameterMappings.add(parameterMapping);
			pageParameters.put(param.getName(), param.getValue());
		}

		//
		queryArgs[MAPPED_STATEMENT_INDEX] = copyFromNewSql(ms, boundSql, pageSQL, parameterMappings, pageParameters);
		queryArgs[PARAMETER_INDEX] = pageParameters;
		queryArgs[ROWBOUNDS_INDEX] = new RowBounds(RowBounds.NO_ROW_OFFSET, RowBounds.NO_ROW_LIMIT);

		Boolean async = customRowBounds.getAsyncTotalCount() == null ? asyncTotalCount : customRowBounds.getAsyncTotalCount();
		Future<List> listFuture = call(new Callable<List>() {
			public List call() throws Exception {
				return (List)invocation.proceed();
			}
		}, async);


		if(customRowBounds.isContainsTotalCount()){
			Callable<Pagination> countTask = new Callable() {
				public Object call() throws Exception {
					Integer count;
					Cache cache = ms.getCache();
					if(cache != null && ms.isUseCache() && ms.getConfiguration().isCacheEnabled()){
						CacheKey cacheKey = executor.createCacheKey(ms,parameter,new PageBounds(),copyFromBoundSql(ms,boundSql, countSQL, boundSql.getParameterMappings(), boundSql.getParameterObject()));
						count = (Integer)cache.getObject(cacheKey);
						if(count == null){
							count = SQLHelp.getCount(countSQL, executor.getTransaction() , ms, parameter, boundSql);
							cache.putObject(cacheKey, count);
						}
					}else{
						count = SQLHelp.getCount(countSQL, executor.getTransaction(), ms, parameter, boundSql);
					}
					return new Pagination(customRowBounds.getPageNumber(), customRowBounds.getLimit(), count);
				}
			};
			Future<Pagination> countFuture = call(countTask, async);
			return new PageList(listFuture.get(),countFuture.get());
		}

		return listFuture.get();
	}

	private <T> Future<T> call(Callable callable, boolean async){
		if(async){
			return pool.submit(callable);
		}else{
			FutureTask<T> future = new FutureTask(callable);
			future.run();
			return future;
		}
	}

	private MappedStatement copyFromNewSql(MappedStatement ms, BoundSql boundSql,
                                           String sql, List<ParameterMapping> parameterMappings, Object parameter){
		BoundSql newBoundSql = copyFromBoundSql(ms, boundSql, sql, parameterMappings, parameter);
		return copyFromMappedStatement(ms, new BoundSqlSqlSource(newBoundSql));
	}

	private BoundSql copyFromBoundSql(MappedStatement ms, BoundSql boundSql,
                                      String sql, List<ParameterMapping> parameterMappings, Object parameter) {
		BoundSql newBoundSql = new BoundSql(ms.getConfiguration(),sql, parameterMappings, parameter);
		for (ParameterMapping mapping : boundSql.getParameterMappings()) {
			String prop = mapping.getProperty();
			if (boundSql.hasAdditionalParameter(prop)) {
				newBoundSql.setAdditionalParameter(prop, boundSql.getAdditionalParameter(prop));
			}
		}
		return newBoundSql;
	}

	//see: MapperBuilderAssistant
	private MappedStatement copyFromMappedStatement(MappedStatement ms, SqlSource newSqlSource) {
		Builder builder = new Builder(ms.getConfiguration(),ms.getId(),newSqlSource,ms.getSqlCommandType());

		builder.resource(ms.getResource());
		builder.fetchSize(ms.getFetchSize());
		builder.statementType(ms.getStatementType());
		builder.keyGenerator(ms.getKeyGenerator());
		if(ms.getKeyProperties() != null && ms.getKeyProperties().length !=0){
			StringBuffer keyProperties = new StringBuffer();
			for(String keyProperty : ms.getKeyProperties()){
				keyProperties.append(keyProperty).append(",");
			}
			keyProperties.delete(keyProperties.length()-1, keyProperties.length());
			builder.keyProperty(keyProperties.toString());
			logger.debug("copyFromMappedStatement keyProperties: [{}]" , keyProperties.toString());
		}



		//setStatementTimeout()
		builder.timeout(ms.getTimeout());

		//setStatementResultMap()
		builder.parameterMap(ms.getParameterMap());

		//setStatementResultMap()
		builder.resultMaps(ms.getResultMaps());
		builder.resultSetType(ms.getResultSetType());

		//setStatementCache()
		builder.cache(ms.getCache());
		builder.flushCacheRequired(ms.isFlushCacheRequired());
		builder.useCache(ms.isUseCache());

		return builder.build();
	}

	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	public void setProperties(Properties properties) {
		PropertiesUtil propertiesHelper = new PropertiesUtil(properties);
		String dialectClass = propertiesHelper.getRequiredString("dialectClass");
		setDialectClass(dialectClass);

		setAsyncTotalCount(propertiesHelper.getBoolean("asyncTotalCount",false));

		setPoolMaxSize(propertiesHelper.getInt("poolMaxSize",0));

	}

	public static class BoundSqlSqlSource implements SqlSource {
		BoundSql boundSql;
		public BoundSqlSqlSource(BoundSql boundSql) {
			this.boundSql = boundSql;
		}
		public BoundSql getBoundSql(Object parameterObject) {
			return boundSql;
		}
	}

	public void setDialectClass(String dialectClass) {
		logger.debug("dialectClass: {} ", dialectClass);
		this.dialectClass = dialectClass;
	}

	public void setAsyncTotalCount(boolean asyncTotalCount) {
		logger.debug("asyncTotalCount: {} ", asyncTotalCount);
		this.asyncTotalCount = asyncTotalCount;
	}

	public void setPoolMaxSize(int poolMaxSize) {

		if(poolMaxSize > 0){
			logger.debug("poolMaxSize: {} ", poolMaxSize);
			pool = Executors.newFixedThreadPool(poolMaxSize);
		}else{
			pool = Executors.newCachedThreadPool();
		}
	}



	/**
	 * 将sql转换为带排序的SQL
	 * @param sql SQL语句
	 * @return 总记录数的sql
	 */
	protected String getSortString(String sql, List<OrderBy> orders){
		if(orders == null || orders.isEmpty()){
			return sql;
		}

//        StringBuffer buffer = new StringBuffer("select * from (").append(sql).append(") temp_order order by ");
		StringBuffer buffer = new StringBuffer(sql).append(" ORDER BY ");
		for(OrderBy order : orders){
			if(order != null){
				buffer.append(order.toString())
						.append(", ");
			}

		}
		buffer.delete(buffer.length()-2, buffer.length());
		return buffer.toString();
	}

	public static void shutdownNow(){
		pool.shutdownNow();
	}

	//    /**
//     * 将sql转换为总记录数SQL
//     * @param sql SQL语句
//     * @return 总记录数的sql
//     */
//    protected String getCountString(String sql){
//
//
//        return "select count(1) from (" + sql + ") tmp_count";
//    }
}
