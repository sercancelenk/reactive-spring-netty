FROM eclipse-temurin:21-jdk-alpine as builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app


COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENV JAVA_OPTS="\
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=100 \
    -XX:+UseStringDeduplication \
    -Dio.netty.allocator.type=pooled \
    -Dio.netty.leakDetection.level=disabled \
    -Dio.netty.net.somaxconn.trySysctl=true \
    -Dkern.ipc.somaxconn=4096"
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"] 