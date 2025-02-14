FROM eclipse-temurin:21-jre-alpine

ARG SPRING_ACTIVE_PROFILE
ENV SPRING_ACTIVE_PROFILE=${SPRING_ACTIVE_PROFILE}

COPY build/libs/TnT-0.0.1-SNAPSHOT.jar TnT.jar
COPY src/main/resources/config/agent.java/ agent.java

ENTRYPOINT ["java", "-jar", "-javaagent:agent.java/scouter.agent.jar", "-Dscouter.config=agent.java/conf/scouter.conf", "-Duser.timezone=Asia/Seoul", "-Dspring.profiles.active=${SPRING_ACTIVE_PROFILE}", "/TnT.jar"]
