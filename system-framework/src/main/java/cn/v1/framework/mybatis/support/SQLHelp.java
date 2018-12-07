package cn.v1.framework.mybatis.support;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author wr
 */
public class SQLHelp {
    private static Logger logger = LoggerFactory.getLogger(SQLHelp.class);

    /**
     * 查询总纪录数
     *
     * @param countSQL 统计sql语句
     * @param mappedStatement mapped
     * @param parameterObject 参数
     * @param boundSql        boundSql
     * @return 总记录数
     * @throws SQLException sql查询错误
     */
    public static int getCount(final String countSQL, final Transaction transaction,
                               final MappedStatement mappedStatement, final Object parameterObject,
                               final BoundSql boundSql) throws SQLException {

        if (logger.isDebugEnabled()) {
            logger.debug("Total count SQL [{}], Parameters: {} ", countSQL, parameterObject);
        }


        Connection connection = null;
        PreparedStatement countStmt = null;
        ResultSet rs = null;
        int count = 0;
        try {
            connection = transaction.getConnection();
            countStmt = connection.prepareStatement(countSQL);
            DefaultParameterHandler handler = new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
            handler.setParameters(countStmt);

            rs = countStmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt(1);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Total count: {}", count);
            }

        } catch (Exception e) {
            logger.error("SQLHelp getCount error: ", e);
        }finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } finally {
                try {
                    if (countStmt != null) {
                        countStmt.close();
                    }
                }catch (Exception e){
                    logger.error("SQLHelp close countStmt error : ", e);
                }
            }
        }
        return count;
    }

}