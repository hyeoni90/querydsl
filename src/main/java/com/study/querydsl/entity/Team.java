package com.study.querydsl.entity;

import javax.persistence.*;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hyeonahlee on 2020-11-22.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "name"})
public class Team {

    @Id
    @GeneratedValue
    @Column(name = "team_id")
    private Long id;

    private String name;

    @OneToMany(mappedBy = "team")
    private List<Member> members = new ArrayList<>();

    public Team(String name) {
        this.name = name;
    }
}
