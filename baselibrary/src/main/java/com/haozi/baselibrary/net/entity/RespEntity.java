package com.haozi.baselibrary.net.entity;

import com.alibaba.fastjson.annotation.JSONField;

public class RespEntity<T> extends RespBaseEntity {

    @JSONField(name = "jsonStr")
    private String jsonStr;
    @JSONField(name = "obj")
    private T rstdata;

    public String getJsonStr() {
        return jsonStr;
    }

    public void setJsonStr(String jsonStr) {
        this.jsonStr = jsonStr;
    }

    public T getRstdata() {
        return rstdata;
    }

    public void setRstdata(T rstdata) {
        this.rstdata = rstdata;
    }
}
