package com.awbeci.ssb.core.support;

/**
 * Created by zhangwei on 2018/8/13.
 */
public class SimpleResponse {
    public SimpleResponse(Object content){
        this.content = content;
    }

    private Object content;

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
}
