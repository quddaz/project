FROM eclipse-temurin:21-jdk
RUN useradd --create-home --uid 10001 grader
USER grader
WORKDIR /workspace
ENV GRADLE_USER_HOME=/opt/gradle-cache
ENTRYPOINT ["./gradlew"]
