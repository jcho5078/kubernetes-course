FROM openjdk:17-jdk-slim
ARG JAR_FILE=../build/product-app.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]