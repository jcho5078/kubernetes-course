# 🚀 3단계: 쿠버네티스 운영 실습 — Ingress를 통한 내부 서비스 통신

> **목표**
> - Ingress를 통한 내부통신 실습

---

## 0. 이미지 빌드 및 도커 허브에 push

```bash
docker build -t username/user-app:lastest ./user-app/.
docker build -t username/product-app:lastest  ./product-app/.

docker push username/user-app:lastest
docker push username/product-app:lastest 
```

user-app-deploy.yaml, product-app-deploy.yaml에 이미지 적용

적용/확인
````
kubectl apply -f user-app-deployment.yaml
kubectl describe pod -l app=user-app | grep -i -E "Limits|Requests" -n

kubectl apply -f product-app-deployment.yaml
kubectl describe pod -l app=product-app | grep -i -E "Limits|Requests" -n

kubectl rollout restart user-app
kubectl rollout restart product-app
````
## 2. 내부통신 debug용 pod 생성 : debug-net
````
kubectl apply -f ./debug-pod.yaml
````

### 2-2. (내부통신 테스트용 : 배포 전부 끝난 후) debug-net pod 접속 후 내부 통신 테스트
````
kubectl get pod
kubectl exec -it debug-net -- bash

# 1. DNS 확인 (Service 이름이 IP로 변환되는지)
nslookup product-app-service

# 2. HTTP 요청 확인 (통신이 성공하는지)
# Service Port 80으로 요청해야 합니다.
curl -v http://product-app-service:80/product
````


## 3. ingress 적용
3-1. HPA 생성 (CPU 50% 기준, 3~10개)
````
kubectl apply -f ./apth-ingress.yaml
````


restart 끝나면 ingress를 통하여 user-app-service로 요청하여 product-app까지 내부 통신 테스트

http://localhost/user?user_nm=tom&product_id=pro-123&product_host=product-app-service
user-app 서비스에서 product-app 으로 내부 통신과 동시에 ClusterIp type 서비스를 외부에서 접속 가능