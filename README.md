## GCP 기반 MSA 아키텍처의 설문 관리 서비스 <WaveForm> - 개선 프로젝트

- 기간 : 2023년 10월 2일 ~ 2024년 4월 (6 개월)
- GitHub: https://github.com/tomy8964/Swave_Survey_Update
- 기술 스택 : Java, SpringBoot, Spring Cloud Gateway, Spring Security, Gradle, JUnit5, Mockito, queryDsl, Docker, Kubernetes, GitHub, Jenkins, MySQL, MySQLOpretor, Prometheus, Grafana, Elastic, Kibana, Fluentd, GCP, Redis, ArgoCD, Slack, nGrinder
- 개요 : WaveForm은 설문 생성·관리와 응답 수집 및 분석을 제공하는 MSA 기반 서비스로, 사용자가 빠르게 설문 리스트를 조회하고 질문을 탐색할 수 있어야 했습니다.
    
    하지만 nGrinder로 핵심 설문 조회 API에 대해 3,000 Vuser 부하 테스트를 수행해 본 결과, TPS가 단 64.4에 불과해 실서비스 운영 기준(>500 TPS)에 크게 못 미치는 것으로 확인됐습니다. 
    
    이에 따라 팀원들과 진행했던 WaveForm 프로젝트를 개선하는 프로젝트를 혼자서 시작하게 되었습니다. 최종적으로 **TPS를 14배 (60.4 -> 841.4)** 향상 시켰으며 **테스트 커버리지 100%를** 목표로 **147**개 이상의 테스트 코드를 작성하고 객체지향적 리팩토링을 달성하였습니다.
    

### TPS 60.4에서 841.4까지 진짜 병목을 해결한 성능 개선

**문제 상황 및 분석**

서비스의 핵심인 설문 조회 API에 대해 nGrinder로 성능 테스트 시 Spring Cloud Gateway 인스턴스(replica=1)가 반복적으로 다운되는 현상이 발생했습니다. 

Prometheus와 Grafana를 통해 Gateway Pod와 클러스터 노드의 CPU·메모리·네트워크 I/O를 1분 단위로 모니터링했고, 부하 테스트가 시작되자마자 인스턴스 하나가 CPU 70% 이상을 지속적으로 사용하다 최대 부하 시 95%를 돌파했으며, HTTP 5xx 오류율도 15%까지 급등하는 현상을 실시간으로 확인했습니다. 

하지만 단순히  Gateway의 replica 수만 늘려서는 진짜 병목을 해결할 수 없다고 판단했습니다. 트래픽이 Gateway를 거쳐 백엔드까지 전달되는 과정 어딘가에 근본 병목이 숨어 있을 것이라 판단했습니다. 

**성능 개선 과정**

1. **DB 최적화**
    
    먼저 설문 조회 쿼리를 분석해보니, N+1 문제가 발생하고 있었습니다. 실제로 간단한 설문 하나를 조회할 때만 해도 6회 이상의 쿼리가 실행되고 있었습니다.  
    
    이를 해결하기 위해 설문 조회 로직을 두 단계로 재구성했습니다. 
    
    먼저 QueryDSL의 **fetch join**을 적용하여, 설문을 조회할 때 일대일 관계인 것들 뿐만 아니라 일대다 관계인 질문 리스트까지 한 번의 조인으로 처리하도록 쿼리를 수정했습니다. 
    
    그 다음 질문과 일대다 관계인 선택지 리스트는 별도 fetch join으로 한 번에 조회하고, 그 외 일대다 관계는 default_batch_fetch_size 설정에 따라 IN절로 묶어 로딩하도록 했습니다. 
    
    그 결과, 원래는 설문 조회 1회, 질문 조회 1회, 선택지 조회 2회(2개의 선택지가 있는 예시), 그 외 일대다 관계 조회 2회로 총 6회 실행되던 쿼리가 설문+일대일+질문 조회 1회, 선택지 조회 1회, 그 외 일대다 관계 조회 1회로 총 **6회 → 3회**로 줄어들었습니다.
    
    - https://velog.io/@tomy8964/Spring-QueryDsl-DB-%EC%BF%BC%EB%A6%AC-%EC%8B%9C-%EB%B0%9C%EC%83%9D%ED%95%98%EB%8A%94-N1-%EB%AC%B8%EC%A0%9C-%ED%95%B4%EA%B2%B0-%EB%B0%8F-%EC%84%B1%EB%8A%A5-%EA%B0%9C%EC%84%A0
    
    다음으로 서비스의 요구사항인 is_deleted 플래그를 통해 삭제 데이터를 관리하고 있었습니다. 이에 따라 질문 테이블에서 survey_document_id와 is_deleted에 각각 단일 인덱스를 설정해두었으나, 실제 MySQL 옵티마이저는 survey_document_id 인덱스만 사용하고 is_deleted는 후처리로만 적용해 수백 건을 스캔하는 비효율이 드러났습니다. 
    
    이를 해결하기 위해 두 컬럼을 한 번에 처리하는 복합 인덱스 (survey_document_id, is_deleted)를 설계·적용했고, 실행 계획 재검증 결과 스캔 대상이 300건에서 52건으로 줄어들며 평균 응답 속도가 약 0.15ms에서 0.05ms로 3배 이상 개선되었습니다. 또한 나머지 테이블에 대해서도 복합 인덱스를 적용하였습니다.
    
    - https://velog.io/@tomy8964/%EB%B3%B5%ED%95%A9-%EC%9D%B8%EB%8D%B1%EC%8A%A4-%EC%84%A4%EA%B3%84%EB%A1%9C-%EC%A7%88%EB%AC%B8-%EC%A1%B0%ED%9A%8C-%EC%84%B1%EB%8A%A5-%EA%B0%9C%EC%84%A0%ED%95%98%EA%B8%B0
    
    이 개선만으로 서비스 전체 TPS는 64.4에서 약 200까지 상승했습니다.
    
2. **Redis 글로벌 캐시 도입**
    
    자주 조회되는 설문 데이터를 Redis에 캐싱해 DB 조회 횟수를 대폭 줄였습니다. 캐싱 적용 후 TPS는 200에서 500까지 추가로 높아졌습니다.
    
    - https://velog.io/@tomy8964/Spring-Redis-Kubenetes-ArgoCD-Redis-cache%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%9C-%EC%A1%B0%ED%9A%8C-%EC%84%B1%EB%8A%A5-%EA%B0%9C%EC%84%A0
3. **Gateway 확장 및 오토스케일링**
    
    Kubernetes에서 Gateway replica를 1→3으로 확장하고, CPU 사용률 70% 초과 시 자동으로 Pod를 증설하는 Horizontal Pod Autoscaler(HPA)를 도입했습니다. 
    
    그 결과 TPS는 500→800 이상으로 크게 증가했고, 필요시 최대 5개까지 자동 확장되도록 설정해 안정성을 강화했습니다.
    
4. **MySQL InnoDB Cluster를 통한 읽기 처리량 확대 및 고가용성**
    
    설문 조회·응답 데이터를 저장하는 MySQL 단일 인스턴스는 노드 장애 시 전체 서비스 가용성에 치명적이기 때문에, MySQL Operator를 활용해 Kubernetes 위에 InnoDB Cluster를 구축했습니다. 
    
    MySQL Router를 통해 설문 조회와 같은 빈번한 읽기 요청을 여러 리더 노드로 자동 라우팅함으로써 읽기 처리량을 늘렸습니다.
    

**추가 고려**

현재 데이터 테이블 구조가 일대다 관계가 중첩되어 있어 조회 시 여러 번의 조인과 batch_fetch가 발생하는 구조적 한계가 남아 있습니다. 

이를 완화하기 위해 역정규화를 적용하여 최소한의 조인으로 데이터를 조회하는 방법도 고려해보고 있습니다. 또한, 쓰기 모델과 읽기 모델을 분리하여 읽기 전용 최적화된 스키마나 테이블을 분리하는 방법도 도입할 예정입니다.

### 테스트 커버리지 100%를 목표로 147개 이상의 테스트 코드를 작성

기능 단위로는 Controller→Service→Repository까지 각 계층별로 단위 테스트를 세분화했고, WebClient나 외부 API 연동 부분은 MockWebServer를 활용해 실제 HTTP 호출 시나리오를 그대로 재현했습니다. 

@WebMvcTest, @DataJpaTest, @SpringBootTest 등을 상황에 맞게 조합하면서 “이 테스트가 진짜 검증해야 할 행동이 무엇인가”를 매번 고민했고, Mockito의 when/thenReturn vs. 실제 레포지토리 사용 여부를 철저히 기준화해 테스트의 신뢰성과 유지보수성을 모두 확보했습니다.

- https://velog.io/@tomy8964/Spring-MVC-%ED%8C%A8%ED%84%B4%EC%9D%98-%EB%B0%B1%EC%97%94%EB%93%9C-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-%ED%85%8C%EC%8A%A4%ED%8A%B8-%EB%B0%A9%EB%B2%95

### 설문 응답 동시성 문제 해결

동일 설문에 대한 중복 응답 요청이 대량으로 들어올 때 DB 트랜잭션만으로는 성능 저하와 잠재적 데드락 위험을 완전히 해소할 수 없다는 것을 확인했습니다. 

처음에는 낙관적 락(버전 컬럼)이나 데이터베이스의 비관적 락(SELECT … FOR UPDATE)을 고려했지만, 이들은 요청 지연이 길어질수록 DB 커넥션을 장시간 점유하게 되어 확장성 측면에서 한계가 있었습니다. 

반면 Redis 분산락은 **가벼운 I/O**, **유연한 만료 설정**, **클러스터·Sentinel 모드 지원** 등 MSA 환경에 최적화된 특성을 지니고 있었기에, “락 획득 대기”와 “자동 만료”를 통해 한 번에 오직 하나의 인스턴스만 핵심 로직을 실행하게끔 설계했습니다.

Redis 클라이언트를 선택할 때도 단순 RedisTemplate 대신 **RedissonClient**를 도입한 이유는, **tryLock(waitTime, leaseTime)** API가 제공하는 명시적 대기·만료 타이밍 제어와 **auto-renewal** 기능 덕분에 “서비스가 예기치 않게 중단되더라도 락이 자동 해제된다”는 보장이 있었기 때문입니다. 또한 Spring AOP로 락 로직을 `@DistributedLock(key = "survey:#{#dto.id}")` 한 줄로 추상화하면서, 비즈니스 코드는 락 획득·해제 스레드 안전성에 대해 전혀 신경 쓰지 않아도 되도록 분리했습니다.

- https://velog.io/@tomy8964/MSA-%EC%95%84%ED%82%A4%ED%85%8D%EC%B2%98%EC%97%90%EC%84%9C-Redis%EB%A5%BC-%ED%99%9C%EC%9A%A9%ED%95%9C-%EB%B6%84%EC%82%B0%EB%9D%BD-%EC%A0%81%EC%9A%A9%EC%9D%84-Spring-AOP%EB%A5%BC-%EC%82%AC%EC%9A%A9%ED%95%B4-%EC%9E%AC%EC%82%AC%EC%9A%A9%EC%84%B1-%EB%86%92%EA%B2%8C-%EC%A0%81%EC%9A%A9%ED%95%98%EB%8A%94-%EB%B0%A9%EB%B2%95

### 왜 Filter에서 JWT 인증을 처리했는가?

JWT 인증 로직을 Spring MVC의 인터셉터가 아니라 서블릿 컨테이너 단계에서 실행되는 **Filter**로 구현한 것은, “모든 요청에 대해 인증이 가장 먼저, 그리고 일관되게 처리되어야 한다”는 본질적인 요구에서 출발했습니다. 

처음에는 Spring MVC 인터셉터나 AOP 기반 어노테이션 방식도 고려했습니다. 인터셉터는 컨트롤러 호출 전후에 로직을 삽입할 수 있지만, DispatcherServlet 바깥의 정적 리소스나 프록시된 엔드포인트 요청은 보호하지 못한다는 한계가 있었습니다. 

AOP 어노테이션은 비즈니스 코드와 분리된 관심사 분리에 유리하지만, HTTP 요청 처리 흐름의 가장 초기 단계에서 실행된다는 보장은 없었습니다. 

반면 필터는 “서블릿 레벨에서 무조건 실행”되고, Spring Security가 제공하는 보안 체인과 일체화되어 있기 때문에, 인증 로직을 가장 명확하고 안전하게 배치할 수 있었습니다. 

이처럼 “어떤 요청을, 언제, 어떻게 검증해야 하는지”부터 “실패 시 예외를 어떻게 일관되게 처리할지”까지 치밀하게 고민한 끝에, 필터 기반 JWT 인증이 최적의 선택임을 확신할 수 있었습니다.

- https://velog.io/@tomy8964/Spring-%EC%99%9C-Filter%EC%97%90%EC%84%9C-JWT-%EC%9D%B8%EC%A6%9D%EC%9D%84-%EC%B2%98%EB%A6%AC%ED%95%98%EB%8A%94%EA%B0%80

### 다형성 기반 OAuthService 리팩토링으로 확장성·유지보수성 확보

OAuth 통합 로직을 리팩토링하면서 가장 먼저 고민한 것은 “새로운 OAuth 공급자가 추가될 때마다 if/switch나 Enum 값을 수정해야 하는 비효율”이었습니다. 

처음엔 애노테이션이나 리플렉션을 활용한 동적 매핑도 고려했지만, 이 방법은 복잡도를 높이고 디버깅이 어려워지는 단점이 있었습니다. 결국 “공급자별 토큰 파싱·프로필 매핑 책임을 Provider 인터페이스 구현체에 위임하고, ProviderList라는 일종의 레지스트리에서 필요한 구현체를 찾아 쓰는 방식”을 선택했습니다. 

이 설계는 다음 세 가지를 보장합니다. 

첫째, 단일 책임 원칙(SRP)을 지켜 OAuthService는 오직 HTTP 호출과 흐름 제어만 담당하며, 토큰 문자열 변환·JSON 파싱·Profile 클래스 선택은 각 구현체가 알아서 처리합니다. 

둘째, 개방‑폐쇄 원칙(OCP)을 충족해 새로운 공급자 추가 시 코드 수정 없이 구현체와 등록만으로 바로 지원됩니다. 

셋째, 의존성 역전 원칙(DIP)을 따르면서 테스트도 훨씬 간단해졌습니다. 

이 과정을 통해 “단순히 기능이 동작하게 하는 수준을 넘어, 확장성과 유지보수성을 미리 예측해 아키텍처를 설계하는 능력”을 길렀으며, 지금 다시 만든다면 Spring의 DI 컨테이너를 활용해 Provider 구현체들을 자동 탐색하고, 레지스트리 클래스를 완전히 제거해 더욱 가볍고 선언적인 구조로 진화시킬 것 같습니다.

- https://velog.io/@tomy8964/Spring-OAuthService-%EB%8B%A4%ED%98%95%EC%84%B1%EC%9D%84-%ED%99%9C%EC%9A%A9%ED%95%98%EC%97%AC-%EA%B0%9D%EC%B2%B4-%EC%A7%80%ED%96%A5%EC%A0%81%EC%9C%BC%EB%A1%9C-%EB%A6%AC%ED%8C%A9%ED%86%A0%EB%A7%81-%ED%95%98%EA%B8%B0

### **성과 및 결과**

성능 튜닝을 통해,  TPS를 64.4에서 841.4로 14배 이상 향상 시키고, Redis 캐시와 InnoDB Cluster를 활용해 읽기 처리량을 확장하면서도 일관성을 지킬 수 있었습니다. 

또한 코드 레벨에서는 QueryDSL fetch join, Hibernate batch_fetch, 복합 인덱스 적용 등 다양한 최적화 기법을 적용하고, 테스트 커버리지를 100% 달성하기 위해 147건의 단위·통합 테스트를 작성하며 객체 지향적 리팩토링을 완성했습니다. 

이 과정에서 “표면적인 스케일 아웃이 아닌, 어디에 진짜 병목이 있는지 찾아내고 해결한다”는 경험을 쌓았고, MSA 환경에서 안정성과 성능을 동시에 만족시키는 설계·구현 역량을 크게 강화할 수 있었습니다.

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
