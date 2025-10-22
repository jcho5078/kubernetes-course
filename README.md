# 📦 Kubernetes Kustomization 실습: ConfigMap & Secret 관리 자동화

환경 변수(ConfigMap)와 민감 정보(Secret)를 효율적으로 관리하기 위해 Kustomization의 Generator 기능을 활용한 실습 과정.

# 🌟 Kustomization 사용 이유

Kubernetes 환경에서 애플리케이션의 설정(ConfigMap)이나 민감 정보(Secret)가 변경될 때마다, 해당 리소스를 참조하는 Deployment를 수동으로 재배포(Rollout)해야 하는 비효율적인 문제 방지 위함.

Immutability 보장: 설정 파일(.env 등)의 내용이 변경되면, Kustomize는 리소스 이름에 고유한 해시 값을 자동으로 추가하여 새로운 ConfigMap/Secret을 생성함.

자동 롤아웃 (Automatic Rollout): 새로 생성된 리소스를 참조하도록 Deployment YAML을 자동으로 수정하여, 변경 사항이 감지될 때마다 Deployment가 자동으로 재시작(Rollout)되도록 함.

# 🗂️ 프로젝트 디렉토리 구조

````
/base : 베이스 설정 소스
 
/overlays/dev : base 소스 기반으로 개발 환경설정으로 덧씌움. 개발환경일시 해당 경로에서 Kustomization 적용
````

## 🚀 실습 과정

### 1. Base Deployment 정의

- user-deployment.yaml 파일은 ConfigMap (app-config)과 Secret (spring-db-secret)의 원본 이름을 참조하도록 작성함.

- Kustomize가 이 이름을 자동으로 해시된 이름('설정이름-해시값')으로 변경할 것이며, secret env는 base64로 인코딩하여 저장할 예정.


### 2. 환경 변수 파일 정의

환경 변수와 민감 정보는 .env 파일 포맷으로 관리하여 Generator의 소스로 활용합니다.

- .config.env (ConfigMap Source)
````
APP_MODE=prod
APP_VERSION=v1.0
````

- .db-secret.env (Secret Source)
````
DB_USER=db_admin
DB_PASS=secure_pass123
````

### 3. Kustomization 설정 (Generator 정의)

kustomization.yaml 파일을 작성하여 Generator를 정의하고, Deployment YAML을 resources에 추가합니다.
````
# kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

# [1] Kustomize가 처리할 기본 리소스 목록
resources:
- user-deployment.yaml

# [2] ConfigMap Generator: .config.env 파일을 읽어 ConfigMap 생성
configMapGenerator:
- name: app-config
  envs:
    - .config.env

# [3] Secret Generator: .db-secret.env 파일을 읽어 Secret 생성
secretGenerator:
- name: spring-db-secret
  envs:
    - .db-secret.env
````