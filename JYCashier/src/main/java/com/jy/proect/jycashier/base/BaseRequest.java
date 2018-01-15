package com.jy.proect.jycashier.base;

import com.google.gson.Gson;

/**
 * Created by mango on 2018/1/12.
 */

public class BaseRequest {

    public String toJson() {
        return new Gson().toJson(this);
    }
}
