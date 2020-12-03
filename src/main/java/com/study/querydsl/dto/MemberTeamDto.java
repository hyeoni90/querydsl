package com.study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

/**
 * Created by hyeonahlee on 2020-12-03.
 */
@Data
public class MemberTeamDto {

    private Long id;
    private String username;
    private int age;
    private Long teamId;
    private String teamName;

    @QueryProjection
    public MemberTeamDto(Long id, String username, int age, Long teamId, String teamName) {
        this.id = id;
        this.username = username;
        this.age = age;
        this.teamId = teamId;
        this.teamName = teamName;
    }
}
