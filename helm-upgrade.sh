ENV=${1:-dev}

echo "Deploying all apps to namespace [$ENV]..."

helm upgrade --install user-app-$ENV ./user-app \
  -n $ENV --create-namespace \
  -f ./user-app/values.yaml -f ./user-app/values-$ENV.yaml \
  --atomic --wait

helm upgrade --install product-app-$ENV ./product-app \
  -n $ENV --create-namespace \
  -f ./product-app/values.yaml -f ./product-app/values-$ENV.yaml \
  --atomic --wait