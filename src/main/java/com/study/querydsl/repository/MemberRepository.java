package com.study.querydsl.repository;

import com.study.querydsl.entity.Member;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by hyeonahlee on 2020-12-04.
 */
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    List<Member> findByUsername(String username);
}
