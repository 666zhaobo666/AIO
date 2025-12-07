package com.aio.module.fts.dto;

import lombok.Data;

@Data
public class GostDto {
    private int code;
    private String msg;
    private Object data;

    public static GostDto success(Object data) {
        GostDto dto = new GostDto();
        dto.setCode(0);
        dto.setMsg("OK");
        dto.setData(data);
        return dto;
    }

    public static GostDto error(String msg) {
        GostDto dto = new GostDto();
        dto.setCode(-1);
        dto.setMsg(msg);
        return dto;
    }
}
