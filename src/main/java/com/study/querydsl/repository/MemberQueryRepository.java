package com.study.querydsl.repository;

import static org.springframework.util.StringUtils.hasText;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.dto.MemberSearchCondition;
import com.study.querydsl.dto.MemberTeamDto;
import com.study.querydsl.dto.QMemberTeamDto;
import com.study.querydsl.entity.QMember;
import com.study.querydsl.entity.QTeam;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * Created by hyeonahlee on 2020-12-04.
 *
 * 기본적으로는 Custom (참고, MemberRepositoryCustom) 을 만들어서 하는 것이 기본,
 * but, Custom에 의존해서 개발하는 것도 좋은 설계는 아니기에,
 * 쿼리가 복잡하거나 재 사용성이 적고, 특정 기능 혹은 화면에 에 특화된 기능이라면,
 * 별도의 조회용 repository로 분리해서 사용하는 것도 아키텍처 면에서 유연한 방법이다.
 */
@Repository
public class MemberQueryRepository {

    private final JPAQueryFactory queryFactory;

    public MemberQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

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

}
