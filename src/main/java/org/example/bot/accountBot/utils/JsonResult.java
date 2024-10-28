package org.example.bot.accountBot.utils;


import io.swagger.annotations.ApiModel;
import lombok.experimental.Accessors;

@ApiModel("JsonResult")
@Accessors(chain = true)
public class JsonResult {

    public int code=200;
    public String message="success";
    public Object data;

    public JsonResult(Object data) {
        this.data = data;
    }
    public JsonResult(String message) {
        this.message = message;
    }
    public JsonResult(Object data, String message) {
        this.data = data;
        this.message = message;
    }
    public JsonResult(Integer code) {
        this.code = code;
    }

    public JsonResult() {

    }
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }


}
