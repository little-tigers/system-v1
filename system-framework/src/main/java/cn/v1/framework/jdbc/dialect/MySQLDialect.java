package cn.v1.framework.jdbc.dialect;

/**
 * @author wr
 */
public class MySQLDialect extends Dialect {

    public boolean supportsLimitOffset() {
        return true;
    }

    public boolean supportsLimit() {
        return true;
    }

    public String getLimitString(String sql, int offset, String offsetPlaceholder, int limit, String limitPlaceholder, boolean isPageParameter) {
        StringBuffer buffer = new StringBuffer().append(sql);

        if(isPageParameter){
            if(offset >0){
                buffer.append(" limit ?, ?");
                setPageParameter(offsetPlaceholder, Integer.valueOf(offset));
                setPageParameter(limitPlaceholder, Integer.valueOf(limit));
            }else{
                buffer.append(" limit ?");
                setPageParameter(limitPlaceholder, Integer.valueOf(limit));
            }
        }else{
            if (offset > 0) {
                buffer.append(" limit ").append(offsetPlaceholder).append( ",").append(limitPlaceholder);
            } else {
                buffer.append(" limit ").append(limitPlaceholder);
            }

        }
        return buffer.toString();
    }

}
