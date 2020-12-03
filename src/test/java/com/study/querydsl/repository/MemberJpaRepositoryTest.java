package com.study.querydsl.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.study.querydsl.entity.Member;
import java.util.List;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by rainalee on 2020-12-03.
 */
@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    void basicTest() {
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        Member findMember = memberJpaRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> resultAll = memberJpaRepository.findAll();
        assertThat(resultAll).containsExactly(member);

        List<Member> resultAll2 = memberJpaRepository.findByUsername("member1");
        assertThat(resultAll2).containsExactly(member);
    }

    @Test
    void basicQuerydslTest() {
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        Member findMember = memberJpaRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> resultAll = memberJpaRepository.findAllWithQuerydsl();
        assertThat(resultAll).containsExactly(member);

        List<Member> resultAll2 = memberJpaRepository.findByUsernameWithQuerydsl("member1");
        assertThat(resultAll2).containsExactly(member);
    }
}