package com.study.querydsl.repository;

import static org.springframework.util.StringUtils.hasText;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.dto.MemberSearchCondition;
import com.study.querydsl.dto.MemberTeamDto;
import com.study.querydsl.dto.QMemberTeamDto;
import com.study.querydsl.entity.Member;
import com.study.querydsl.entity.QMember;
import com.study.querydsl.entity.QTeam;
import java.util.List;
import javax.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;

/**
 * Created by hyeonahlee on 2020-12-04.
 */
public class MemberRepositoryImpl extends QuerydslRepositorySupport implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

//    public MemberRepositoryImpl(JPAQueryFactory queryFactory) {
//        this.queryFactory = queryFactory;
//    }

    /**
     * Creates a new {@link QuerydslRepositorySupport} instance for the given domain type.
     *
     */
    public MemberRepositoryImpl(EntityManager em) {
        super(Member.class);
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<MemberTeamDto> search(MemberSearchCondition condition) {

        // QuerydslRepositorySupport 상속 후 )
        // entityManager를 가져와서 쓸 수 도 있다.
        EntityManager entityManager = getEntityManager();

        List<MemberTeamDto> result = from(QMember.member)
            .leftJoin(QMember.member.team, QTeam.team)
            .where(
                usernameEq(condition.getUsername()),
                teamNameEq(condition.getTeamName()),
                ageGoe(condition.getAgeGoe()),
                ageLoe(condition.getAgeLoe())
            )
            .select(new QMemberTeamDto(
                QMember.member.id.as("memberId"),
                QMember.member.username,
                QMember.member.age,
                QTeam.team.id.as("teamId"),
                QTeam.team.name.as("teamName")))
            .fetch();

        // QuerydslRepositorySupport 상속 전 )
//        return queryFactory
//            .select(new QMemberTeamDto(
//                QMember.member.id.as("memberId"),
//                QMember.member.username,
//                QMember.member.age,
//                QTeam.team.id.as("teamId"),
//                QTeam.team.name.as("teamName")))
//            .from(QMember.member)
//            .leftJoin(QMember.member.team, QTeam.team)
//            .where(
//                usernameEq(condition.getUsername()),
//                teamNameEq(condition.getTeamName()),
//                ageGoe(condition.getAgeGoe()),
//                ageLoe(condition.getAgeLoe())
//            )
//            .fetch();
        return result;
    }

    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
        QueryResults<MemberTeamDto> results = queryFactory
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
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetchResults();

        List<MemberTeamDto> content = results.getResults();
        long total = results.getTotal();

        return new PageImpl<>(content, pageable, total);
    }

    public Page<MemberTeamDto> searchPageSimple2(MemberSearchCondition condition, Pageable pageable) {
        JPQLQuery<MemberTeamDto> jpaQuery = from(QMember.member)
            .leftJoin(QMember.member.team, QTeam.team)
            .where(
                usernameEq(condition.getUsername()),
                teamNameEq(condition.getTeamName()),
                ageGoe(condition.getAgeGoe()),
                ageLoe(condition.getAgeLoe())
            )
            .select(new QMemberTeamDto(
                QMember.member.id.as("memberId"),
                QMember.member.username,
                QMember.member.age,
                QTeam.team.id.as("teamId"),
                QTeam.team.name.as("teamName")));

        // offset, limit, sort 적용됨! , getQuerydsl() util 클래스 사용..
        JPQLQuery<MemberTeamDto> query = getQuerydsl().applyPagination(pageable, jpaQuery);

        List<MemberTeamDto> result = query.fetch();

        return new PageImpl<>(result);
    }

    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDto> content = queryFactory
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
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        // count query를 최적화 하고 싶다면 분리하라!
        // 또는 totalCount가 없다면 content 조회 안하도록 할 경우
        JPAQuery<Member> countQuery = queryFactory
            .select(QMember.member)
            .from(QMember.member)
            .leftJoin(QMember.member.team, QTeam.team)
            .where(usernameEq(condition.getUsername()),
                teamNameEq(condition.getTeamName()),
                ageGoe(condition.getAgeGoe()),
                ageLoe(condition.getAgeLoe())
            );

        // content size가 전체보다 작을 경우 count 쿼리를 날릴 필요가 없음!!!
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
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

}
