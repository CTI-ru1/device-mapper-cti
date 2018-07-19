FROM openjdk:8u131-jre-alpine
ARG JARFILE
LABEL maintainer="amaxilat@cti.gr"

COPY $JARFILE /server.jar

COPY docker-entrypoint.sh /usr/local/bin/
RUN ln -s usr/local/bin/docker-entrypoint.sh / # backwards compat
RUN chmod +x /usr/local/bin/docker-entrypoint.sh
ENTRYPOINT ["docker-entrypoint.sh"]
CMD ["java", "-Dspring.profiles.active=docr", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-jar", "/server.jar"]

