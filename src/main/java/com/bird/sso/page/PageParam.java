package com.bird.sso.page;

import java.io.Serializable;

/**
 * @author 张朋
 * @version 1.0
 * @desc 分页参数传递工具类 .
 * @date 2020/6/5 10:45
 */
public class PageParam implements Serializable {


    /**
     * 默认为第一页.
     */
    public static final int DEFAULT_PAGE_NUM = 1;

    /**
     * 默认每页记录数(15).
     */
    public static final int DEFAULT_NUM_PER_PAGE = 15;

    /**
     * 最大每页记录数(100).
     */
    public static final int MAX_PAGE_SIZE = 100;

    private int pageNum = DEFAULT_PAGE_NUM; // 当前页数

    private int pageSize; // 每页记录数

    /**
     * 默认构造函数
     */
    private PageParam() {

    }

    public PageParam(int pageNum) {
        this(pageNum, DEFAULT_NUM_PER_PAGE);
    }

    /**
     * 带参数的构造函数
     *
     * @param pageNum
     * @param pageSize
     */
    public PageParam(int pageNum, int pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    /**
     * 当前页数
     */
    public int getPageNum() {
        return pageNum;
    }

    /**
     * 当前页数
     */
    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    /**
     * 每页记录数
     */
    public int getPageSize() {
        return pageSize > 0 ? pageSize : DEFAULT_NUM_PER_PAGE;
    }

    /**
     * 每页记录数
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }


}
