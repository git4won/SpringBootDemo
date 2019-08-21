package com.example.jerseydemo.rest;

import java.util.List;

public class ListResponse<T> extends BaseResponse {

    private List<T> datas;

    public ListResponse() {
        super();
    }

    public ListResponse(List<T> datas) {
        super(true);
        this.datas = datas;
    }

    public List<T> getDatas() {
        return datas;
    }

    public void setDatas(List<T> datas) {
        this.datas = datas;
    }
}
