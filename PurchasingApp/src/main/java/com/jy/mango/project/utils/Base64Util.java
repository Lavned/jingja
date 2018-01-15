package com.jy.mango.project.utils;

import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;

/**
 * Created by mango on 2017/10/30.
 */

public class Base64Util {


    public static String Base(String str) {
        byte[] encodeBase64 = null;
        try {
            encodeBase64 = Base64.encodeBase64(str.getBytes("UTF-8"));
            System.out.println("RESULT: " + new String(encodeBase64));
            return new String(encodeBase64);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new String(encodeBase64);
    }
}
