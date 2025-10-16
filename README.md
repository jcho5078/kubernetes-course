# 🚀 2단계(2주차): 쿠버네티스 운영 실습 — HPA, ConfigMap/Secret, VolumeMount, Service 노출

> **목표**
> - 리소스 요청/제한 설정과 HPA로 오토스케일링 실습
> - ConfigMap/Secret 환경 설정 주입 (env/volume)
> - ConfigMap 변경 반영(rollout restart)과 검증
> - Service: ClusterIP ↔ LoadBalancer 전환 및 접근
> - 부하 생성기(load-generator)로 실제 동작 확인

> **전제 조건**
> - 1단계(1주차) 완료: `spring-app` Deployment(3 replicas), `spring-app-service`, Docker Hub 이미지(`username/spring-app:v3` 등)
> - `kubectl`, `minikube`, `docker` 사용 가능
> - 네임스페이스는 `default` 기준

---

## 🧭 전체 구조 개요

Client(브라우저)
│ ┌── Pod(spring-app-)
LoadBalancer ── Service ── Pod(spring-app-)
│ └── Pod(spring-app-*)

## 0. 현재 상태 점검 (권장)

```bash
kubectl get nodes
kubectl get deploy spring-app -o wide
kubectl get pods -l app=spring-app -o wide
kubectl get svc spring-app-service -o wide
```

## 1. 리소스 요청/제한 설정 + 재배포

deployment.yaml 컨테이너에 추가
````
resources:
  requests:
    cpu: "100m"
    memory: "256Mi"
  limits:
    cpu: "500m"
    memory: "512Mi"
````

적용/확인
````
kubectl apply -f deployment.yaml
kubectl rollout status deployment/spring-app --timeout=120s
kubectl describe pod -l app=spring-app | grep -i -E "Limits|Requests" -n
````
## 2. metrics-server 활성화(HPA용)
````
minikube addons enable metrics-server
kubectl -n kube-system get deploy metrics-server
````

Available 되면 다음 단계 진행

## 3. HPA 생성 및 부하 테스트
3-1. HPA 생성 (CPU 50% 기준, 3~10개)
````
kubectl autoscale deployment spring-app --cpu-percent=50 --min=3 --max=10
kubectl get hpa
````

### 3-2. 부하 생성기 Pod로 진입
````
kubectl run -it load-generator --rm --image=busybox -- /bin/sh
````

컨테이너 내부에서 지속 요청

while true; do wget -q -O- http://spring-app-service:8080 > /dev/null; done


별도 터미널에서 관찰
````
kubectl get hpa -w
kubectl get pods -l app=spring-app -w
````

부하 종료
````
exit
````

(남아있으면)
````
kubectl delete pod load-generator
````
## 4. ConfigMap/Secret 생성(환경변수 주입)
### 4-1. ConfigMap

configmap.yaml
````
apiVersion: v1
kind: ConfigMap
metadata:
  name: spring-config
data:
  APP_MODE: "dev"
  APP_VERSION: "v4-env"
````

적용

kubectl apply -f configmap.yaml

### 4-2. Secret

secret.yaml
````
apiVersion: v1
kind: Secret
metadata:
  name: spring-secret
type: Opaque
data:
  DB_USER: c3ByaW5ndXNlcg==    # springuser
  DB_PASS: c3ByaW5ncGFzcw==    # springpass
````

적용

kubectl apply -f secret.yaml

### 4-3. Deployment에 env 주입

deployment.yaml 발췌
````
env:
  - name: APP_MODE
    valueFrom:
      configMapKeyRef:
        name: spring-config
        key: APP_MODE
  - name: APP_VERSION
    valueFrom:
      configMapKeyRef:
        name: spring-config
        key: APP_VERSION
  - name: DB_USER
    valueFrom:
      secretKeyRef:
        name: spring-secret
        key: DB_USER
  - name: DB_PASS
    valueFrom:
      secretKeyRef:
        name: spring-secret
        key: DB_PASS
````

적용/재시작/확인

kubectl apply -f deployment.yaml
kubectl rollout restart deployment/spring-app
kubectl rollout status deployment/spring-app --timeout=120s
kubectl exec -it $(kubectl get pod -l app=spring-app -o name | head -n1) -- printenv | egrep "APP_|DB_"

5. ConfigMap을 파일(Volume)로 주입 (Spring Boot 자동 인식)
5-1. ConfigMap(파일형)

configmap-volume.yaml
````
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config-yaml
data:
  application.yaml: |
    server:
      port: 8080
    spring:
      application:
        mode: dev
        version: v4-config-vol
````

적용
````
kubectl apply -f configmap-volume.yaml
````
5-2. Deployment에 Volume/VolumeMount 추가

주의: mountPath: / 금지! 반드시 /config/로 마운트

deployment.yaml 발췌
````
spec:
  template:
    spec:
      volumes:
        - name: app-config-volume
          configMap:
            name: app-config-yaml
      containers:
        - name: spring-app
          image: username/spring-app:v3
          volumeMounts:
            - name: app-config-volume
              mountPath: /config/
````

적용/재시작/로그 확인
````
kubectl apply -f deployment.yaml
kubectl rollout restart deployment/spring-app
kubectl logs -l app=spring-app --tail=100
````

컨테이너 내부 확인
````
kubectl exec -it $(kubectl get pod -l app=spring-app -o name | head -n1) -- sh -lc "ls -l /config && sed -n '1,50p' /config/application.yaml"
````
6. Service: ClusterIP ↔ LoadBalancer 전환과 접근
6-1. ClusterIP (내부 통신 전용)

service.yaml
````
apiVersion: v1
kind: Service
metadata:
  name: spring-app-service
spec:
  selector:
    app: spring-app
  type: ClusterIP
  ports:
    - port: 8080
      targetPort: 8080
````

적용/내부 접근 테스트
````
kubectl apply -f service.yaml
kubectl get svc spring-app-service -o wide
kubectl run -it curltest --rm --image=curlimages/curl -- curl -sI http://spring-app-service:8080
````
6-2. LoadBalancer (Minikube 외부 접근용)

service.yaml 변경
````
spec:
  type: LoadBalancer
  ports:
    - port: 8080
      targetPort: 8080
````

적용/확인

kubectl apply -f service.yaml
kubectl get svc spring-app-service


외부 접근 (선택 1: 터널)
````
minikube tunnel
kubectl get svc spring-app-service
````
# 브라우저: http://<EXTERNAL-IP>:8080


외부 접근 (선택 2: 간편 URL)
````
minikube service spring-app-service --url
````
# 브라우저: 출력된 http://127.0.0.1:6xxxx

7. ConfigMap 변경 반영(rollout restart)
````
kubectl edit configmap app-config-yaml
````

# 값 수정 후 저장
````
kubectl rollout restart deployment/spring-app
kubectl rollout status deployment/spring-app --timeout=120s
kubectl logs -l app=spring-app --tail=100
````
8. 롤링 업데이트 / 롤백 (이미지 버전)
````
kubectl set image deployment/spring-app spring-app=username/spring-app:v4
kubectl rollout status deployment/spring-app
kubectl rollout history deployment/spring-app
kubectl rollout undo deployment/spring-app --to-revision=2
````
9. 트러블슈팅(빠른 점검 명령)
# 서비스/엔드포인트/셀렉터
````
kubectl get svc spring-app-service -o wide
kubectl describe svc spring-app-service
kubectl get endpoints spring-app-service
````
# Pod 상태/로그
````
kubectl get pods -l app=spring-app -o wide
kubectl logs -l app=spring-app --tail=100
````
# 내부 연결 테스트
````
kubectl run -it curltest --rm --image=curlimages/curl -- curl -sI http://spring-app-service:8080
````
# HPA/메트릭
````
kubectl get hpa
kubectl top pod -l app=spring-app
````

# LoadBalancer 접근 (Minikube)
````
minikube tunnel
minikube service spring-app-service --url
````

자주 발생하는 문제 요약

LoadBalancer인데 브라우저 접속 불가 → minikube tunnel 미실행/종료

내부 Pod 접근 불가 → 내부 테스트는 ClusterIP로

ConfigMap VolumeMount 후 서버 미기동 → mountPath: / 사용(❌), /config로 수정(✅)

AlreadyExists: pods "load-generator" → kubectl delete pod load-generator 후 --rm 사용

✅ 최종 확인
````
minikube tunnel
kubectl get svc spring-app-service
````
# 또는
````
minikube service spring-app-service --url
````


브라우저 접속 → ConfigMap 값(예: v4-config-vol) 반영 확인 시 성공