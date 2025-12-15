#
# Dependency build stage
#
FROM maven:3.9.9-eclipse-temurin-21 AS common_builder
WORKDIR /app/common-library
COPY library/pom.xml .
COPY library/src ./src
RUN mvn clean install -DskipTests

#
# Build stage
#
FROM maven:3.9.9-eclipse-temurin-21 AS payment_vnpay_builder
WORKDIR /app/payment-vnpay
COPY pom.xml .
COPY src ./src
COPY --from=common_builder /root/.m2 /root/.m2
RUN mvn clean package -DskipTests

#
# Package stage
#
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=payment_vnpay_builder /app/payment-vnpay/target/*.jar app.jar
EXPOSE 9006
ENTRYPOINT ["java", "-jar", "app.jar"]
