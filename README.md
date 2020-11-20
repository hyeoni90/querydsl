# querydsl

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

3. `./gradlew compileQuerydsl` 실행
    build > generated > querydsl 폴더 하위에 Q 객체 생성
    build 안에 디렉토리 하위로 들어가기 때문에 별도의 gitignore 하지 않아도 됨!