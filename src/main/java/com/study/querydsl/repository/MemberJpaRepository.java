package com.study.querydsl.repository;

import static org.springframework.util.StringUtils.hasText;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.dto.MemberSearchCondition;
import com.study.querydsl.dto.MemberTeamDto;
import com.study.querydsl.dto.QMemberTeamDto;
import com.study.querydsl.entity.Member;
import com.study.querydsl.entity.QMember;
import com.study.querydsl.entity.QTeam;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import org.springframework.stereotype.Repository;

/**
 * Created by rainalee on 2020-12-03.
 */
@Repository
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public MemberJpaRepository(EntityManager em, JPAQueryFactory queryFactory) {
        this.em = em;
        this.queryFactory = queryFactory;
    }

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(Long id) {
        Member findMember = em.find(Member.class, id);
        return Optional.ofNullable(findMember);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
            .getResultList();
    }

    public List<Member> findAllWithQuerydsl() {
        return queryFactory
            .selectFrom(QMember.member)
            .fetch();
    }

    public List<Member> findByUsername(String username) {
        return em.createQuery("select m from Member m where m.username = :username", Member.class)
            .setParameter("username", username)
            .getResultList();
    }

    public List<Member> findByUsernameWithQuerydsl(String username) {
        return queryFactory
            .selectFrom(QMember.member)
            .where(QMember.member.username.eq(username))
            .fetch();
    }

    // BooleanBuilder 활용
    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition) {

        BooleanBuilder builder = new BooleanBuilder();
        if (hasText(condition.getUsername())) {
            builder.and(QMember.member.username.eq(condition.getUsername()));
        }

        if (hasText(condition.getTeamName())) {
            builder.and(QTeam.team.name.eq(condition.getTeamName()));
        }

        if (condition.getAgeGoe() != null) {
            builder.and(QMember.member.age.goe(condition.getAgeGoe()));
        }

        if (condition.getAgeLoe() != null) {
            builder.and(QMember.member.age.loe(condition.getAgeLoe()));
        }

        return queryFactory
            .select(new QMemberTeamDto(
                QMember.member.id.as("memberId"),
                QMember.member.username,
                QMember.member.age,
                QTeam.team.id.as("teamId"),
                QTeam.team.name.as("teamName")))
            .from(QMember.member)
            .leftJoin(QMember.member.team, QTeam.team)
            .where(builder)
            .fetch();
    }

    // where 활용, BooleanExpression 활용
    public List<MemberTeamDto> search(MemberSearchCondition condition) {
        return queryFactory
            .select(new QMemberTeamDto(
                QMember.member.id.as("memberId"),
                QMember.member.username,
                QMember.member.age,
                QTeam.team.id.as("teamId"),
                QTeam.team.name.as("teamName")))
            .from(QMember.member)
            .leftJoin(QMember.member.team, QTeam.team)
            .where(
                usernameEq(condition.getUsername()),
                teamNameEq(condition.getTeamName()),
                ageGoe(condition.getAgeGoe()),
                ageLoe(condition.getAgeLoe())
            )
            .fetch();
    }

    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? QMember.member.username.eq(username) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? QTeam.team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? QMember.member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? QMember.member.age.loe(ageLoe) : null;
    }


    // where 파라미터 활용, ageBetween 메서드로 조합 가능!
    public List<Member> searchMember(MemberSearchCondition condition) {
        return queryFactory
            .selectFrom(QMember.member)
            .from(QMember.member)
            .leftJoin(QMember.member.team, QTeam.team)
            .where(
                usernameEq(condition.getUsername()),
                teamNameEq(condition.getTeamName()),
                ageBetween(condition.getAgeLoe(), condition.getAgeGoe())
            )
            .fetch();
    }

    private BooleanExpression ageBetween(int ageLoe, int ageGoe) {
        return ageGoe(ageLoe).and(ageGoe(ageGoe));
    }
}
