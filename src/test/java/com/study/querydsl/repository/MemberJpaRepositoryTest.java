package com.study.querydsl.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.study.querydsl.dto.MemberSearchCondition;
import com.study.querydsl.dto.MemberTeamDto;
import com.study.querydsl.entity.Member;
import com.study.querydsl.entity.Team;
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

    @Test
    void searchTest() {
        // given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        // when
        List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(condition);

        // then
        assertThat(result).extracting("username").containsExactly("member4");
    }

    @Test
    void searchTest2() {
        // given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setTeamName("teamB");

        // when
        List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(condition);

        // then
        assertThat(result).extracting("username").containsExactly("member3", "member4");
    }
}