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


## References
* QueryDSL Documentation[http://www.querydsl.com/static/querydsl/4.4.0/reference/html_single/]