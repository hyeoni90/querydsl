package com.study.querydsl.dto;

import lombok.Data;

/**
 * Created by rainalee on 2020-12-01.
 */
@Data
public class UserDto {

    private String name;
    private int age;

    public UserDto() {
    }

    public UserDto(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
