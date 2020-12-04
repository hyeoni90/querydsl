package com.study.querydsl.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.study.querydsl.entity.Member;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by rainalee on 2020-12-04.
 */
@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    void basicTest() {
        Member member = new Member("member1", 10);
        memberRepository.save(member);

        Member findMember = memberRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> resultAll = memberRepository.findAll();
        assertThat(resultAll).containsExactly(member);

        List<Member> resultAll2 = memberRepository.findByUsername("member1");
        assertThat(resultAll2).containsExactly(member);
    }

}