---
title: Express 라우트를 앱 팩토리로 분리해 테스트
category: node-express
tags: [express, test]
date: 2026-09-30
example: example/node-express/health-route
status: auto
---
`createApp()`이 app만 반환하게 하면 `listen(0)`으로 임의 포트에서 서버를 띄워 테스트할 수 있다.
헷갈린 점: `listen`은 server.ts에서만 호출해야 테스트가 포트 충돌을 일으키지 않는다.
