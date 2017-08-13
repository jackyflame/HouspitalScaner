package com.haozi.baselibrary.net.config;

public interface ErrorType {

    int ERROR_INVALID_USER = 401;
    int ERROR_AUTH = 403;
    int ERROR_SERVER_AUTH = 1001;
    int ERROR_SERVER = 1003;
    int ERROR_NETWORK = 1004;
    int ERROR_ALREADY_REGIST = 5001;
    int ERROR_UN_REGIST_YET = 5002;
    int ERROR_LIMITED_USER = 5003;
    int ERROR_TOMANYTRY_USER = 5004;

    int ERROR_NOSENDCOUNT_ERROR = 5005;//错误提示：套餐剩余次数不足

    int ERROR_RESPONSE_NULL = 9988;
    int ERROR_OTHER = 9999;
}
