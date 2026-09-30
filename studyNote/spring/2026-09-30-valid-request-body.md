---
title: @Valid로 요청 본문 검증
category: spring
tags: [validation, rest]
date: 2026-09-30
example: example/spring-boot/rest-validation
status: auto
---
`@Valid @RequestBody`와 `@NotBlank`만으로 잘못된 요청을 400으로 거절할 수 있다.
헷갈린 점: `spring-boot-starter-validation` 의존성이 없으면 검증이 조용히 무시된다.
