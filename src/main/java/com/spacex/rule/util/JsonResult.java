package com.spacex.rule.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.spacex.rule.common.ErrorCodeEnum;

import java.util.Map;

public class JsonResult {
    private static final long serialVersionUID = 1L;

    public static final int SUCCESS = 0;
    public static final int FAILED = 1;

    private int code = 0;
    private String msg = "";
    private Object data = null;

    public JsonResult() {

    }

    public JsonResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public <T> T getData(Class<T> clazz) {
        if (this.getData() == null) {
            return null;
        }

        return JsonUtils.json2Object(JsonUtils.object2Json(this.getData()), clazz);
    }

    public <T> T getData(TypeReference<T> typeReference) {
        if (this.getData() == null) {
            return null;
        }

        return JsonUtils.json2Object(JsonUtils.object2Json(this.getData()), typeReference);
    }

    public boolean isSuccess() {
        return this.getCode() == SUCCESS;
    }

    public static JsonResult success(Object data) {
        JsonResult ret = new JsonResult(SUCCESS, "success");
        ret.setData(data);
        return ret;
    }

    public static JsonResult fail(int code, String msg) {
        return new JsonResult(code, msg);
    }


    public static JsonResult fail(ErrorCodeEnum codeEnum) {
        return new JsonResult(codeEnum.getCode(), codeEnum.getMsg());
    }


    public static JsonResult fail(Map<String,String> map) {
        JsonResult jsonResult = new JsonResult(ErrorCodeEnum.PARAM_ERROR.getCode(), "参数错误");

        jsonResult.setData(map);
        return jsonResult;
    }
}
