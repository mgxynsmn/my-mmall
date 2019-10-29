package com.mmall.common;

/**
 * 常量类
 */
public class Const {
    public static final String CURRENT_USER = "currentUser";
    public static final String USERNAME = "username";
    public static final String EMAIL = "email";

    public interface Role{
        int ROLE_ADMIN = 1;
        int ROLE_CUSTOMER = 0;
    }
}
