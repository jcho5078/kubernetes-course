# 🚀 1단계: 쿠버네티스 기본 학습 및 실습
 
- 쿠버네티스 실습
- branch별 course 실습

---

## 📚 학습 개요

### 코스별 각 branch checkout 후 실습


---

## ⚙️ Step 1. Minikube 클러스터 설치 및 실행

````
minikube start --driver=docker
kubectl get nodes
````

✅ 노드가 Ready 상태면 성공.

## 🧱 Step 2. 각 코스별 Docker 이미지 빌드 및 푸시
1️⃣ 로컬 빌드
docker build -t spring-app:v1 .
docker run -p 8080:8080 spring-app:v1

2️⃣ Docker Hub 푸시
docker tag spring-app:v1 username/spring-app:v1
docker push username/spring-app:v1

