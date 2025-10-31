# helm 사용하여 환경 분리 및 배포


## helm yaml 환경파일 생성
````
helm create user-app

helm create product-app
````

## helm 각 app 배포명령(환경별)

## - dev
````
helm upgrade --install user-app-dev ./user-app \
-n dev --create-namespace \
-f ./user-app/values.yaml -f ./user-app/values-dev.yaml

helm upgrade --install product-app-dev ./product-app \
-n dev --create-namespace \
-f ./product-app/values.yaml -f ./product-app/values-dev.yaml
````

## - prod
````
helm upgrade --install user-app-prod ./user-app \
-n prod --create-namespace \
-f ./user-app/values.yaml -f ./user-app/values-prod.yaml

helm upgrade --install product-app-prod ./product-app \
-n prod --create-namespace \
-f ./product-app/values.yaml -f ./product-app/values-prod.yaml
````

## 쉘스크립트

````[helmfile.yaml.gotmpl](helmfile.yaml.gotmpl)
helm-upgrade.sh
````

## 확인
````
helm list --all-namespaces
````


# 프로젝트 jar파일 삭제 후 helmfile을 이용한 배포

````
helmfile -e dev sync

helmfile -e prod sync
````

# 적용 helmfile 내용 확인

````
helmfile -e dev template > render.yaml

# 생성된 render.yaml 확인 
````

# 윈도우환경 minikube 각 서비스 접속(name space 'dev' 기준)
````
minikube service user-app-dev -n dev
minikube service product-app-dev -n dev
````

# user-app 에서 브라우저 접근

product-app 사용 service : product-app-dev

namespace : dev

service host : localhost

접근 port : 8080

최종 포드 접근 URL : product-app-dev.dev.svc.cluster.local:8080

- user-app 접근 pod 통신 url
````
http://127.0.0.1:57747/?user_nm=tom&product_id=pro-123&product_host=product-app-dev.dev.svc.cluster.local:8080
````