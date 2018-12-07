package cn.v1.framework.page;

import java.io.Serializable;
import java.util.*;

/**
 * Created by wr on 2015/6/6.
 */
public class Page<E> implements Serializable {

    private static final long serialVersionUID = 1088834587229906334L;

    private int first=1;// 首页索引

    private String funcName = "page"; // 设置点击页码调用的js函数名称，默认为page，在一页有多个分页对象时使用。

    private String funcParam = ""; // 函数的附加参数，第三个参数值。

    private int length = 8;// 显示页面长度

    private int slider = 1;// 前后显示页面长度

    private String message = ""; // 设置提示消息，显示在“共n条”之后

    protected List<E> result = new ArrayList<E>();

    private Pagination pagination;

    public Page() {}

    public Page(Collection<? extends E> c) {
        if(c != null){
            result.addAll(c);
        }
    }


    public Page(Collection<? extends E> c, Pagination p) {
        if(c != null){
            result.addAll(c);
        }
        this.pagination = p;
    }

    public Page(Pagination p) {
        this.pagination = p;
    }


    /**
     * 得到分页器，通过Paginator可以得到总页数等值
     * @return pagination
     */
    public Pagination getPagination() {
        return this.pagination;
    }


    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public Iterator<E> iterator() {
        return (Iterator<E>) (result == null ? Collections.emptyList().iterator() : result.iterator());
    }

    public List<E> getResult() {
        return result;
    }

    public void setResult(List<E> result) {
        this.result = result;
    }

    public boolean hasContent(){
        return !result.isEmpty();
    }

    /**
     * 默认输出当前分页标签
     * <div class="page">${page}</div>
     */
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        if (pagination.isFirstPage()) {// 如果是首页
            sb.append("<li class=\"disabled\"><a href=\"javascript:\">&#171; 上一页</a></li>\n");
        } else {
            sb.append("<li><a href=\"javascript:\" onclick=\""+funcName+"("+pagination.getPrePage()+","+pagination.getLimit()+",'"+funcParam+"');\">&#171; 上一页</a></li>\n");
        }

        int begin = pagination.getPageNumber() - (length / 2);

        if (begin < first) {
            begin = first;
        }

        int end = begin + length - 1;

        if (end >=  pagination.getTotalPages()) {
            end =  pagination.getTotalPages();
            begin = end - length + 1;
            if (begin < first) {
                begin = first;
            }
        }

        if (begin > first) {
            int i = 0;
            for (i = first; i < first + slider && i < begin; i++) {
                sb.append("<li><a href=\"javascript:\" onclick=\""+funcName+"("+i+","+pagination.getLimit()+",'"+funcParam+"');\">"
                        + (i + 1 - first) + "</a></li>\n");
            }
            if (i < begin) {
                sb.append("<li class=\"disabled\"><a href=\"javascript:\">...</a></li>\n");
            }
        }

        for (int i = begin; i <= end; i++) {
            if (i == pagination.getPageNumber()) {
                sb.append("<li class=\"active\"><a href=\"javascript:\">" + (i + 1 - first)
                        + "</a></li>\n");
            } else {
                sb.append("<li><a href=\"javascript:\" onclick=\""+funcName+"("+i+","+pagination.getLimit()+",'"+funcParam+"');\">"
                        + (i + 1 - first) + "</a></li>\n");
            }
        }

        if ( pagination.getTotalPages() - end > slider) {
            sb.append("<li class=\"disabled\"><a href=\"javascript:\">...</a></li>\n");
            end =  pagination.getTotalPages() - slider;
        }

        for (int i = end + 1; i <=  pagination.getTotalPages(); i++) {
            sb.append("<li><a href=\"javascript:\" onclick=\""+funcName+"("+i+","+pagination.getLimit()+",'"+funcParam+"');\">"
                    + (i + 1 - first) + "</a></li>\n");
        }

        if (pagination.getPageNumber() == pagination.getTotalPages()) {
            sb.append("<li class=\"disabled\"><a href=\"javascript:\">下一页 &#187;</a></li>\n");
        } else {
            sb.append("<li><a href=\"javascript:\" onclick=\""+funcName+"("+pagination.getNextPage()+","+pagination.getLimit()+",'"+funcParam+"');\">"
                    + "下一页 &#187;</a></li>\n");
        }

        sb.append("<li class=\"disabled controls\"><a href=\"javascript:\">当前 ");
        sb.append("<input type=\"text\" value=\""+pagination.getPageNumber()+"\" onkeypress=\"var e=window.event||event;var c=e.keyCode||e.which;if(c==13)");
        sb.append(funcName+"(this.value,"+pagination.getLimit()+",'"+funcParam+"');\" onclick=\"this.select();\"/> / ");
        sb.append("<input type=\"text\" value=\""+pagination.getLimit()+"\" onkeypress=\"var e=window.event||event;var c=e.keyCode||e.which;if(c==13)");
        sb.append(funcName+"("+pagination.getPageNumber()+",this.value,'"+funcParam+"');\" onclick=\"this.select();\"/> 条，");
        sb.append("共 " + pagination.getTotalCount() + " 条"+(message!=null?message:"")+"</a></li>\n");

        sb.insert(0,"<ul>\n").append("</ul>\n");

        sb.append("<div style=\"clear:both;\"></div>");

//		sb.insert(0,"<div class=\"page\">\n").append("</div>\n");

        return sb.toString();
    }
}
