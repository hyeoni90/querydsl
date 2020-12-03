package com.study.querydsl.dto;

import lombok.Data;

/**
 * Created by hyeoahlee on 2020-12-03.
 */
@Data
public class MemberSearchCondition {

    private String username;
    private String teamName;
    private Integer ageGoe;
    private Integer ageLoe;
}
