# 🚀 1단계: 쿠버네티스 기본 학습 및 실습

> **목표:**  
> Spring Boot 애플리케이션을 Docker 이미지로 빌드하고,  
> Minikube(Kubernetes) 환경에서 Deployment + LoadBalancer Service로 배포하여  
> 실제 접속 가능한 형태로 구동시키는 것.

---

## 📚 학습 개요

- 쿠버네티스 기본 개념 이해
- Minikube 환경 설치 및 클러스터 구성
- Spring Boot 앱 Docker 이미지 생성
- Deployment / Service 생성 및 배포
- LoadBalancer를 통한 외부 접속 실습
- 롤링 업데이트 / 롤백 실습

---

## 🧩 실습 구성도

[Local Browser]
│
▼
[LoadBalancer Service]
│
▼
[Kubernetes Cluster]
└─ Deployment (ReplicaSet → Pods)
├─ spring-app (Pod-1)
├─ spring-app (Pod-2)
└─ spring-app (Pod-3)


---

## ⚙️ Step 1. Minikube 클러스터 설치 및 실행

minikube start --driver=docker
kubectl get nodes


✅ 노드가 Ready 상태면 성공.

## 🧱 Step 2. Docker 이미지 빌드 및 푸시
1️⃣ 로컬 빌드
docker build -t spring-app:v1 .
docker run -p 8080:8080 spring-app:v1

2️⃣ Docker Hub 푸시
docker tag spring-app:v1 username/spring-app:v1
docker push username/spring-app:v1


⚠️ Kubernetes는 레지스트리에서 이미지를 Pull하므로 docker push 필수.

## ☸️ Step 3. Deployment 생성

deployment.yaml

apiVersion: apps/v1
kind: Deployment
metadata:
  name: spring-app
spec:
  replicas: 3
  selector:
    matchLabels:
      app: spring-app
  template:
    metadata:
      labels:
        app: spring-app
    spec:
      containers:
        - name: spring-app
          image: username/spring-app:v3
          ports:
            - containerPort: 8080

kubectl apply -f deployment.yaml
kubectl get pods


✅ 모든 Pod 상태가 Running이면 성공.

## 🌐 Step 4. LoadBalancer Service 생성

service.yaml

apiVersion: v1
kind: Service
metadata:
  name: spring-app-service
spec:
  selector:
    app: spring-app
  type: LoadBalancer
  ports:
    - port: 80
      targetPort: 8080

kubectl apply -f service.yaml

## 🚇 Step 5. 외부 접속 (Minikube Tunnel)
minikube tunnel
kubectl get svc spring-app-service


EXTERNAL-IP이 <pending> → 192.168.xx.xx 등으로 바뀌면 성공

# 접속 확인
curl http://192.168.49.2
# or 브라우저에서 http://192.168.49.2

## 🔄 Step 6. 롤링 업데이트 & 롤백
이미지 업데이트
kubectl set image deployment/spring-app spring-app=username/spring-app:v3
kubectl rollout status deployment/spring-app

롤백
kubectl rollout undo deployment/spring-app


⚠️ 동일 태그(v3)를 덮어쓰면 롤백해도 내용은 동일할 수 있음.
실제 버전별로는 v1, v2, v3처럼 고유 태그를 써야 정확히 관리 가능.

## 🔍 Step 7. 상태 점검 명령어 요약
목적	명령어
Deployment 상태 확인	kubectl get deploy spring-app -o wide
Pod 목록 및 노드 위치	kubectl get pods -o wide
Service 상태	kubectl get svc spring-app-service
상세 이벤트 로그	kubectl describe deployment spring-app
Pod 로그 보기	kubectl logs -f <pod-name>
리비전 이력 확인	kubectl rollout history deployment/spring-app
## 🧠 Step 8. 네트워크 및 포트 원리 요약

Pod → 컨테이너가 실제 애플리케이션을 실행

Service → Pod를 묶어 내부 DNS 이름으로 접근 (spring-app-service:80)

LoadBalancer → 외부 IP 할당 (Minikube는 tunnel로 흉내냄)

클라이언트 요청 흐름

브라우저 → LoadBalancer → Service → Pod 중 하나


TCP 세션 단위 라우팅: 연결이 새로 만들어질 때마다 Pod가 바뀔 수 있음.

✅ 결과 확인

배포 성공 시 브라우저에 표시:

Hello v3: 192.168.49.1-50123


RemoteAddr = 클라이언트 IP
RemotePort = 클라이언트 임시 포트(매 요청마다 변경)

🧾 실습 요약
단계	내용
1	Minikube 설치 및 클러스터 생성
2	Docker 이미지 빌드 및 푸시
3	Deployment 생성 (3 replica)
4	LoadBalancer Service 생성
5	Minikube Tunnel로 외부 노출
6	롤링 업데이트 및 롤백 실습
7	Pod/Service 상태 점검
8	네트워크 라우팅 및 포트 동작 이해