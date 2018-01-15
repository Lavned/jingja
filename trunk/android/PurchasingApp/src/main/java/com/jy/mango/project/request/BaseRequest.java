package com.jy.mango.project.request;

import com.google.gson.Gson;

/**
 * Created by mango on 2017/10/30.
 */

public class BaseRequest {

    public String toJson() {
        return new Gson().toJson(this);
    }
}
