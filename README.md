# querydsl

## spec
- Spring Boot
- Gradle
- Spring Data JPA
- QueryDSL
- H2 DataBase

## querydsl 설정
1. build.gradle plugins에 querydsl, querydsl dependency 추가
```
 id 'com.ewerk.gradle.plugins.querydsl' version '1.0.10'
```

```
 implementation 'com.querydsl:querydsl-jpa'
```

2. build.gradle querydsl 설정 추가
```
//querydsl 추가 시작
def querydslDir = "$buildDir/generated/querydsl"

querydsl {
    jpa = true
    querydslSourcesDir = querydslDir
}

sourceSets {
    main.java.srcDir querydslDir
}

configurations {
    querydsl.extendsFrom compileClasspath }

compileQuerydsl {
    options.annotationProcessorPath = configurations.querydsl
}
```

3. `./gradlew clean compileQuerydsl` 실행
    - build > generated > querydsl 폴더 하위에 Q 객체 생성
    - build 안에 디렉토리 하위로 들어가기 때문에 별도의 gitignore 하지 않아도 됨!
    - Q타입은 컴파일 시점에 자동 생성! (버전관리에 포함되지 않는 것이 좋으며, 생성 위치를 build 폴더 아래 생성 / build 폴더 git에 포함 안됨)
    
4. QueryDSL library
- querydsl-apt: code generated QueryDSL
- querydsl-jpa: querydsl library 


## h2 Database


## 실행
cleanQuerydslSourcesDir > compileQuerydslJava

## QueryDsl

결과 조회
* fetch(): 리스트 조회, 데이터가 없으면 빈 리스트를 반환 한다.
* fetchOne(): 단건 조회 (결과 없으면 null, 결과 2 이상이면 NonUniqueResultException)
* fetchFirst(): limit(1).fetchOn() 
* fetchResults(): 페이징 정보 포함, total count 쿼리 추가 실행 => 성능 때문에 contents, total count 가 다를경우 쿼리를 나눠서 실행한다! 
* fetchCount(): count 쿼리로 변경해서 count 수 조회  

ON 절을 활용한 조인 (JPA 2.1 부터 지원)
- 조인 대상 필터링
    - 외부 조인이 아니라 내부조인 (inner join) 사용 시 where 절에서 필터링 하는 것과 기능이 동일하다
     inner join 경우 where 절로 해결하고, 정말 외부 조인이 필요한 경우에만 사용하자!
   ```java
   List<Tuple> result = queryFactory
       .select(member, team)
       .from(member)
       // 동일함.
       //.join(member.team, team).on(team.name.eq("teamA"))
       .join(member.team, team)
       .where(team.name.eq("teamA"))
       .fetch();
   ```
- 연관관계 없는 엔티티 외부 조인 (주로 많이 쓰임)

서브 쿼리
- JPAExpressions 사용
- JPA JPQL 서브쿼리의 한계점
    - from절의 서브쿼리(인라인 뷰)는 지원하지 않는다. Querydsl도 지원하지 않는다.
    - hibernate 구현체를 사용하면 select 절의 서브쿼리는 지원한다.
    - Querydsl hibernate 구현체를 사용하면 select절의 서브쿼리를 지원한다.
    
    - 해결방안)
         ```
        1. subquery를 join으로 변경한다. (가능 하거나 불가능할 떄도 있음)
        2. application에서 query를 2번 분리해서 실행
        3. nativeSQL을 사용한다.
        ```

## References
* QueryDSL Documentation[http://www.querydsl.com/static/querydsl/4.4.0/reference/html_single/]