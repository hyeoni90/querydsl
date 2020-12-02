package com.study.querydsl;

import static com.querydsl.jpa.JPAExpressions.select;
import static com.study.querydsl.entity.QMember.member;
import static com.study.querydsl.entity.QTeam.team;
import static org.assertj.core.api.Assertions.assertThat;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.dto.MemberDto;
import com.study.querydsl.dto.QMemberDto;
import com.study.querydsl.dto.UserDto;
import com.study.querydsl.entity.Member;
import com.study.querydsl.entity.QMember;
import com.study.querydsl.entity.Team;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by rainalee on 2020-11-26.
 */
@SpringBootTest
@Transactional
class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    // 영속성 테스트
    @PersistenceUnit
    EntityManagerFactory emf;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);

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
    }

    @Test
    void startJPQL() {
        String qlString = "select m from Member m "
            + "where m.username = :username";

        Member findMember = em.createQuery(qlString, Member.class)
            .setParameter("username", "member1")
            .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    @DisplayName("기본, static import 사용 (권장)")
    void startQuerydsl() {
        Member findMember = queryFactory
            .select(member)
            .from(member)
            .where(member.username.eq("member1"))
            .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    @DisplayName("alias 사용 ")
    void startQuerydslAlias() {
        // 같은 테이블 조인해야하는 경우, 아래와 같이 alias 해줘서 사용한다.
        QMember m1 = new QMember("m1");

        Member findMember = queryFactory
            .select(m1)
            .from(m1)
            .where(m1.username.eq("member1"))   // 파라미터 바인딩 처리
            .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void search() {
        // select().from() >> selectFrom()  으로 사용할 수 있다.
//            .select(member)
//            .from(member)

        Member findMember = queryFactory
            .selectFrom(member)
            .where(member.username.eq("member1")
                .and(member.age.eq(10)))
            .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void searchBetween() {
        Member findMember = queryFactory
            .selectFrom(member)
            .where(member.username.eq("member1")
                .and(member.age.between(10, 30)))
            .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void searchAndParam() {
        // where 조건 and > 동적 쿼리 짤 때 편리
        Member findMember = queryFactory
            .selectFrom(member)
            .where(
                member.username.eq("member1"),
                member.age.eq(10)
            )
            .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void resultFetch() {
//        List<Member> fetch = queryFactory
//            .selectFrom(member)
//            .fetch();
//
//        Member fetchOne = queryFactory
//            .selectFrom(member)
//            .fetchOne();
//
//        Member fetchFirst = queryFactory
//            .selectFrom(member)
//            .fetchFirst();

        // query 2 번 실행됨
        QueryResults<Member> results = queryFactory
            .selectFrom(member)
            .fetchResults();
        results.getTotal();
        List<Member> content = results.getResults();

    }

    @Test
    void total() {
        long count = queryFactory
            .selectFrom(member)
            .fetchCount();
    }

    /**
     * 회원 정렬 순서 1. 회원 나이 내림차순(DESC) 2. 회원 이름 올림차순(ASC) (단, 회원 이름이 없으면 마지막에 출력 nulls last)
     */
    @Test
    void sort() {
        // given
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        // when
        List<Member> result = queryFactory
            .selectFrom(member)
            .where(member.age.eq(100))
            .orderBy(member.age.desc(), member.username.asc().nullsLast())
            .fetch();

        // then
        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    void paging() {
        List<Member> result = queryFactory
            .selectFrom(member)
            .orderBy(member.username.desc())
            .offset(1)
            .limit(2)
            .fetch();

        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    void paging2() {
        // count, content query 2번 실행!
        QueryResults<Member> queryResults = queryFactory
            .selectFrom(member)
            .orderBy(member.username.desc())
            .offset(1)
            .limit(2)
            .fetchResults();

        assertThat(queryResults.getTotal()).isEqualTo(4);
        assertThat(queryResults.getLimit()).isEqualTo(2);
        assertThat(queryResults.getOffset()).isEqualTo(1);
        assertThat(queryResults.getResults().size()).isEqualTo(2);
    }

    /**
     * 실무에서 Tuple를 잘 사용하지 않고 DTO로 직접 매핑해오는 방법을 쓴다!
     */
    @Test
    void aggregation() {
        // given
        List<Tuple> result = queryFactory
            .select(
                member.count(),
                member.age.sum(),
                member.age.avg(),
                member.age.max(),
                member.age.min()
            )
            .from(member)
            .fetch();

        // when
        Tuple tuple = result.get(0);

        // then
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }


    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     */
    @Test
    @DisplayName("Querydsl join(), groupBy()")
    void groupBy() {
        List<Tuple> result = queryFactory
            .select(team.name, member.age.avg())
            .from(member)
            .join(member.team, team)
            .groupBy(team.name)
            .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15); // (10 + 20) / 2

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35); // (30 + 40) / 2
    }

    /**
     * 팀 A에 소속된 모든 회원 조회
     */
    @Test
    @DisplayName("Querydsl join()")
    void join() {
        // given, when
        List<Member> result = queryFactory
            .selectFrom(member)
            .join(member.team, team)
            .where(team.name.eq("teamA"))
            .fetch();

        // then
        assertThat(result)
            .extracting("username")
            .containsExactly("member1", "member2");
    }

    /**
     * 세타 조인 (연관관계가 없는 필드로 조인) => 외부 조인 불가능! but, hibernate 최신 버전에서는 on 사용해서 외부 조인 가능 쿼리 실행되는 것 확인해볼 것  cross
     * <p>
     * 회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    @DisplayName("Querydsl theta join()")
    void theta_join() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Member> result = queryFactory
            .select(member)
            .from(member, team)
            .where(member.username.eq(team.name))
            .fetch();

        assertThat(result)
            .extracting("username")
            .containsExactly("teamA", "teamB");
    }

    /**
     * ex) 회원과 팀을 조인하면서, 팀 이름이 TEAMA인 팀만, 조인, 회원은 모두 조회 - JPQL: select m, t from Member m left join m.team t on t.name =
     * 'teamA'
     */
    @Test
    @DisplayName("Querydsl join on")
    void join_on_filtering() {
        List<Tuple> result = queryFactory
            .select(member, team)
            .from(member)
            .leftJoin(member.team, team).on(team.name.eq("teamA"))
            .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    /**
     * 연관관게 없는 엔티티 외부 조인 회원의 이름이 팀 이름과 같은 대상 조인
     */
    @Test
    void join_on_no_relation() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = queryFactory
            .select(member, team)
            .from(member)
            .leftJoin(team).on(member.username.eq(team.name))
            .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @DisplayName("페치 조인 미적용")
    @Test
    void no_fetch_join() {
        // fetch join은 영속성 컨텍스트에 있는 것들을 깔끔하게 날리고 시작한다.
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(QMember.member)
                .where(QMember.member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 미적용 ").isFalse();
    }

    @DisplayName("페치 조인 적용")
    @Test
    void fetch_join() {
        // fetch join은 영속성 컨텍스트에 있는 것들을 깔끔하게 날리고 시작한다.
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(QMember.member)
                .join(member.team, team).fetchJoin()
                .where(QMember.member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 적용 ").isTrue();
    }

    /**
     * 나이가 가장 많은 회원 조회
     */
    @Test
    void sub_query() {

        // alias 시 사용
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result)
                .extracting("age")
                .containsExactly(40);
    }

    /**
     * 나이가 평균 이상인 회원
     * geo: >=
     */
    @Test
    void sub_query_goe() {

        // alias 시 사용
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
            .selectFrom(member)
            .where(member.age.goe(
                select(memberSub.age.avg())
                    .from(memberSub)
            ))
            .fetch();

        assertThat(result)
            .extracting("age")
            .containsExactly(30, 40);
    }

    /**
     * 나이가 평균 이상인 회원
     * geo: >=
     */
    @Test
    void sub_query_in() {

        // alias 시 사용
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
            .selectFrom(member)
            .where(member.age.in(
                select(memberSub.age)
                    .from(memberSub)
                    .where(memberSub.age.gt(10))        // 10살 초과
            ))
            .fetch();

        assertThat(result)
            .extracting("age")
            .containsExactly(20, 30, 40);
    }

    @Test
    void select_sub_query() {
        // alias 시 사용
        QMember memberSub = new QMember("memberSub");

        List<Tuple> result = queryFactory
            .select(member.username,
                select(memberSub.age.avg())
                    .from(memberSub))
            .from(member)
            .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    void select_case() {
        List<String> result = queryFactory
            .select(member.age
                .when(10).then("열살")
                .when(20).then("스무살")
                .otherwise("기타"))
            .from(member)
            .fetch();

    }

    @Test
    void select_case_1() {
        List<String> result = queryFactory
            .select(new CaseBuilder()
                .when(member.age.between(0, 20)).then("0~20살")
                .when(member.age.between(21, 30)).then("21~30살")
                .otherwise("기타"))
            .from(member)
            .fetch();
    }

    @Test
    void rank_path() {
        NumberExpression<Integer> rankPath = new CaseBuilder()
            .when(member.age.between(0, 20)).then(2)
            .when(member.age.between(21, 30)).then(1)
            .otherwise(3);

        List<Tuple> result = queryFactory
            .select(member.username, member.age, rankPath)
            .from(member)
            .orderBy(rankPath.desc())
            .fetch();

        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            Integer rank = tuple.get(rankPath);
            System.out.println("username = " + username + " age = " + age + " rank = " + rank);
        }
    }

    @Test
    void constant() {
        List<Tuple> result = queryFactory
            .select(member.username, Expressions.constant("A"))
            .from(member)
            .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    /**
     * select
     *      ((member0_.username||?)||cast(member0_.age as char)) as col_0_0_
     * from
     *      member member0_
     * where
     *      member0_.username=?
     */
    @Test
    void concat() {
        // ex) {username}_{age}
        List<String> result = queryFactory
            .select(member.username.concat("_").concat(member.age.stringValue())) // stringValue 문자로 변환
            .from(member)
            .where(member.username.eq("member1"))
            .fetch();

        for (String s : result) {
            System.out.println("s = " + s); // s = member1_10
        }
    }

    @Test
    void simple_projection() {
        List<String> result = queryFactory
            .select(member.username)
            .from(member)
            .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    /**
     * Tuple은 repository 계층에서만 사용하고,
     * repo 계층에서 service 계층에 던질 때에는 DTO를 사용하는 것이 좋다.
     */
    @Test
    void tuple_projection() {
        List<Tuple> result = queryFactory
            .select(member.username, member.age)
            .from(member)
            .fetch();

        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);

            System.out.println("username = " + username);
            System.out.println("age = " + age);
        }
    }

    /**
     * 순수 JPA 에서 DTO를 조회할 때는 new 명령어를 사용해야 함. (DTO의 패키지 이름을 다 적어줘야함...)
     * 생성자 방식만 지원!
     *
     * ex) com.study.querydsl.dto.MemberDto(m.username, m.age)
     */
    @Test
    void findDtoByJPQL() {
        List<MemberDto> result = em
            .createQuery("select new com.study.querydsl.dto.MemberDto(m.username, m.age) from Member m", MemberDto.class)
            .getResultList();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    /**
     * Querydsl 빈 생성(bean population) : 결과를 DTO 반환할 때 사용, 3가지 방법 지원.
     *  - 프로퍼티 접근
     *  - 필드 직접 접근
     *  - 생성자 사용
     *
     *  1) 프로퍼티 접근
     */
    @Test
    void findDtoByQuerydslSetter() {
        List<MemberDto> result = queryFactory
            .select(Projections.bean(MemberDto.class,
                member.username,
                member.age))
            .from(member)
            .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    /**
     * Querydsl 빈 생성(bean population) : 결과를 DTO 반환할 때 사용, 3가지 방법 지원.
     *  - 프로퍼티 접근
     *  - 필드 직접 접근
     *  - 생성자 사용
     *
     *  2) 필드 직접 접근
     */
    @Test
    void findDtoByField() {
        List<MemberDto> result = queryFactory
            .select(Projections.fields(MemberDto.class,
                member.username,
                member.age))
            .from(member)
            .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    /**
     * Querydsl 빈 생성(bean population) : 결과를 DTO 반환할 때 사용, 3가지 방법 지원.
     *  - 프로퍼티 접근
     *  - 필드 직접 접근
     *  - 생성자 사용
     *
     *  2) 생성자 사용
     */
    @Test
    void findDtoByConstructor() {
        List<MemberDto> result = queryFactory
            .select(Projections.constructor(MemberDto.class,
                member.username,
                member.age))
            .from(member)
            .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    void findUserDtoByField() {
        List<UserDto> result = queryFactory
            .select(Projections.fields(UserDto.class,
                member.username.as("name"),
                member.age))
            .from(member)
            .fetch();

        for (UserDto userDto : result) {
            System.out.println("memberDto = " + userDto);
        }
    }

    /**
     * 프로티나, 필드 접근 생성 방식에서 이름이 다를 때 해결방안
     * ExpressionUtils.as(source, alias) : 필드나 서브 쿼리에 별칭 적용
     * username.as("memberName"): 필드에 별칭 적용
     */
    @Test
    void findUserDto() {
        QMember memberSub = new QMember("memberSub");

        List<UserDto> result = queryFactory
            .select(Projections.fields(UserDto.class,
                member.username.as("name"),
                ExpressionUtils.as(
                    JPAExpressions
                        .select(memberSub.age.max())
                        .from(memberSub), "age")
            ))
            .from(member)
            .fetch();

        for (UserDto userDto : result) {
            System.out.println("memberDto = " + userDto);
        }
    }

    /**
     * constructor에 member.id 필드 추가시  >> 런타임 오류로 알게된다.
     */
    @Test
    void findUserDtoByConstuctor() {
        QMember memberSub = new QMember("memberSub");

        List<UserDto> result = queryFactory
            .select(Projections.constructor(UserDto.class,
                member.username,
                member.age))
            .from(member)
            .fetch();

        for (UserDto userDto : result) {
            System.out.println("memberDto = " + userDto);
        }
    }

    /**
     * 그에 비해 QMemberDto는 컴파일 오류로 알 수 있다!
     */
    @Test
    void findDtoByQueryProjection() {
        List<MemberDto> result = queryFactory
            .select(new QMemberDto(member.username, member.age))
            .from(member)
            .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    void dynamicQueryWithBooleanBuilder() {
        String usernameParam = "member1";
        Integer ageParam = 10;
//        Integer ageParam = null;  // -> null 일때는 and 조건에 포함되지 않도록 해서 and 조건에서 빠지게 됨

        List<Member> result = searchMember1(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {
        BooleanBuilder builder = new BooleanBuilder();
        if(usernameCond != null) {
            // builder 에 and 조건을 넣어준다.
            builder.and(member.username.eq(usernameCond));
        }

        if(ageCond != null) {
            builder.and(member.age.eq(ageCond));
        }

        return queryFactory
            .selectFrom(member)
            .where(builder)
            .fetch();
    }
}
