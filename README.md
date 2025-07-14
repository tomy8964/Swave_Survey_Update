# SWAVE Project - WaveForm 개선 프로젝트

- 개요 : 카카오엔터프라이즈 SW 아카데미에서 팀원들과 진행했던 설문 관리 서비스 프로젝트의 성능을 개선하기 위해 혼자서 시작한 프로젝트
- **문제 상황 및 분석**
    - nGrinder로 성능 측정 결과 TPS 64.4로, 실서비스 수준에 미치지 못함을 확인
    - 초기에는 구체적 원인을 알지 못해, 캐싱, 인덱스, 쿼리 효율화 등 여러 방면에서 성능 개선 시도를 병행
- **성능 개선 과정**
    1. **글로벌 캐시 적용**
        - 설문 조회가 빈번하게 발생함을 고려하여, **Redis Cluster**를 도입해 글로벌 캐시를 구현
        - 설문 ID를 키로 자주 조회되는 데이터를 캐시에 저장, DB 부하를 크게 줄임
    2. **복합 인덱스 설계**
        - SQL 실행 계획 분석 과정에서, 삭제된 데이터(`is_deleted`)를 효율적으로 걸러내는 쿼리가 없었음을 발견
        - **is_deleted + survey_document_id**에 복합 인덱스를 추가하여, 설문의 질문(일대다 관계(설문-질문)) 조회 시 빠른 조회가 가능하도록 설계
    3. **N+1 쿼리 해소**
        - 기존에는 각 설문에 대해 질문을 반복 조회하는 `N+1` 문제가 발생
        - **QueryDSL의 fetch join**과 **batch size** 설정으로, 한 번의 쿼리로 필요한 데이터를 모두 불러오게 개선
    4. **Gateway 인스턴스 확장**
        - 성능 저하의 근본 원인은 **Spring Gateway의 replicas가 1**로 고정되어 트래픽이 몰리면 단일 인스턴스가 모두 감당해야 했던 점
        - Kubernetes 환경에서 Gateway replica 수를 확장, 수천 명의 동시 접속 부하를 안정적으로 분산 처리할 수 있게 수정
- **최종 결과**
    - 위의 개선 과정으로 **TPS 64.4 → 841.4**로, 약 **14배** 성능을 향상
    - 서비스의 구조적 문제를 근본적으로 해결하며, 실제 서비스 수준의 안정성과 확장성을 경험

## Architecture
<img width="1025" height="782" alt="Image" src="https://github.com/user-attachments/assets/e4a62cf4-c5b5-4cef-bcb1-b7c613f65b59" />

## Blog

- [[Spring + Redis] Redis cache를 이용한 조회 성능 개선](https://velog.io/@tomy8964/Spring-Redis-Kubenetes-ArgoCD-Redis-cache%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%9C-%EC%A1%B0%ED%9A%8C-%EC%84%B1%EB%8A%A5-%EA%B0%9C%EC%84%A0)
- [복합 인덱스 설계로 설문 조회 성능 개선하기](https://velog.io/@tomy8964/%EB%B3%B5%ED%95%A9-%EC%9D%B8%EB%8D%B1%EC%8A%A4-%EC%84%A4%EA%B3%84%EB%A1%9C-%EC%A7%88%EB%AC%B8-%EC%A1%B0%ED%9A%8C-%EC%84%B1%EB%8A%A5-%EA%B0%9C%EC%84%A0%ED%95%98%EA%B8%B0)
- [[Spring + QueryDsl] DB 쿼리 시 발생하는 N+1 문제 해결 및 성능 개선](https://velog.io/@tomy8964/Spring-QueryDsl-DB-%EC%BF%BC%EB%A6%AC-%EC%8B%9C-%EB%B0%9C%EC%83%9D%ED%95%98%EB%8A%94-N1-%EB%AC%B8%EC%A0%9C-%ED%95%B4%EA%B2%B0-%EB%B0%8F-%EC%84%B1%EB%8A%A5-%EA%B0%9C%EC%84%A0)
- [[Spring] MSA 아키텍처에서 Redis를 활용한 분산락 적용을 Spring AOP를 활용해 재사용성 높게 적용하는 방법](https://velog.io/@tomy8964/MSA-%EC%95%84%ED%82%A4%ED%85%8D%EC%B2%98%EC%97%90%EC%84%9C-Redis%EB%A5%BC-%ED%99%9C%EC%9A%A9%ED%95%9C-%EB%B6%84%EC%82%B0%EB%9D%BD-%EC%A0%81%EC%9A%A9%EC%9D%84-Spring-AOP%EB%A5%BC-%EC%82%AC%EC%9A%A9%ED%95%B4-%EC%9E%AC%EC%82%AC%EC%9A%A9%EC%84%B1-%EB%86%92%EA%B2%8C-%EC%A0%81%EC%9A%A9%ED%95%98%EB%8A%94-%EB%B0%A9%EB%B2%95)
- [[Spring] 테스트 커버리지 100%를 목표로 MSA 아키텍처의 백엔드 프로젝트 테스트 방법](https://velog.io/@tomy8964/Spring-MVC-%ED%8C%A8%ED%84%B4%EC%9D%98-%EB%B0%B1%EC%97%94%EB%93%9C-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-%ED%85%8C%EC%8A%A4%ED%8A%B8-%EB%B0%A9%EB%B2%95)
- [[Spring] OAuth Kakao, Git, Google 로그인 다형성을 활용하여 객체 지향적으로 리팩토링 하기](https://velog.io/@tomy8964/Spring-OAuthService-%EB%8B%A4%ED%98%95%EC%84%B1%EC%9D%84-%ED%99%9C%EC%9A%A9%ED%95%98%EC%97%AC-%EA%B0%9D%EC%B2%B4-%EC%A7%80%ED%96%A5%EC%A0%81%EC%9C%BC%EB%A1%9C-%EB%A6%AC%ED%8C%A9%ED%86%A0%EB%A7%81-%ED%95%98%EA%B8%B0)
- [[Spring] 왜 Filter에서 JWT 인증을 처리하는가?](https://velog.io/@tomy8964/Spring-%EC%99%9C-Filter%EC%97%90%EC%84%9C-JWT-%EC%9D%B8%EC%A6%9D%EC%9D%84-%EC%B2%98%EB%A6%AC%ED%95%98%EB%8A%94%EA%B0%80)
- [[Spring] REST API 통신 시 사용해야 할 HTTP 클라이언트 RestTemplate vs WebClient](https://velog.io/@tomy8964/Spring-REST-API-%ED%86%B5%EC%8B%A0-%EC%8B%9C-%EC%82%AC%EC%9A%A9%ED%95%B4%EC%95%BC-%ED%95%A0-HTTP-%ED%81%B4%EB%9D%BC%EC%9D%B4%EC%96%B8%ED%8A%B8-RestTemplate-vs-WebClient)
