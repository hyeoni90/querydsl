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

## References
* QueryDSL Documentation[http://www.querydsl.com/static/querydsl/4.4.0/reference/html_single/]