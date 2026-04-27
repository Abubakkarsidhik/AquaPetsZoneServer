FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN ./gradlew build

EXPOSE 8080

CMD ["sh", "-c", "java -jar build/libs/server.jar"]