FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew
RUN ./gradlew build -x test
RUN ls -la build/libs

EXPOSE 8080

CMD ["sh", "-c", "java -jar build/libs/server.jar"]