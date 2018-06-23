package com.heiku.panicbuy.util;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5Util {

    public static String md5(String src){
        return DigestUtils.md5Hex(src);
    }


    public static final String salt = "1a2b3c4d";

    public static String inputPassToFromPass(String inputPass){
        String str = "" + salt.charAt(0) +  salt.charAt(2) + inputPass + salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }

    public static String fromPassToDBPass(String fromPass, String salt){
        String str = "" + salt.charAt(0) +  salt.charAt(2) + fromPass + salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }


    public static String inputPassToDBPass(String intput, String saltDB){
        String fromPass = inputPassToFromPass(intput);
        String dbPass = fromPassToDBPass(fromPass, saltDB);

        return dbPass;
    }

    public static void main(String[] args) {
        System.out.println(inputPassToFromPass("123456"));
        System.out.println(fromPassToDBPass(inputPassToFromPass("123456"), "1a2b3c4d"));
    }
}
