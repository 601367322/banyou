package com.quanliren.quan_one.api.base;

import android.content.Context;

import com.loopj.android.http.RequestParams;
import com.quanliren.quan_one.application.AppClass;
import com.quanliren.quan_one.util.LogUtil;
import com.quanliren.quan_one.util.Util;

import java.io.Serializable;

/**
 * Created by Shen on 2015/5/30.
 */
public abstract class BaseApi implements Serializable {

    public Context context;
    public AppClass ac;
    private RequestParams params;

    public int currentPage = 0;

    public BaseApi(Context context) {
        this.context = context;
        if (this.context == null) {
            LogUtil.d("context null");
            this.context = AppClass.getContext();
        }
        ac = (AppClass) this.context.getApplicationContext();
        params = Util.getRequestParams(this.context);
    }

    public abstract String getUrl();

    public RequestParams getParams() {
        return params;
    }

    public void setPage(int currentPage) {
        params.put("p", currentPage);
        this.currentPage = currentPage;
    }

    public void initParam(Object... obj) {
    }

}
