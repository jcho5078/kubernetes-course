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

````
helm-upgrade.sh
````

## 확인
````
helm list --all-namespaces
````