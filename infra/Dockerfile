FROM amazoncorretto:21

ARG SPRING_ACTIVE_PROFILE
ENV SPRING_ACTIVE_PROFILE ${SPRING_ACTIVE_PROFILE}

COPY build/libs/TnT-0.0.1-SNAPSHOT.jar TnT.jar

ENTRYPOINT ["java", "-jar", "-Duser.timezone=Asia/Seoul", "-Dspring.profiles.active=${SPRING_ACTIVE_PROFILE}", "/TnT.jar"]
