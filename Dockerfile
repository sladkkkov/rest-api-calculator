FROM adoptopenjdk/openjdk11:jdk-11.0.5_10-alpine as builder

ADD . /src
WORKDIR /src
RUN  ./mvnw package -DskipTests

ARG UID=1001
ARG GID=1001

RUN addgroup --system --gid ${GID}  user && adduser --system --uid ${UID}  user --ingroup  user
RUN chown -R user:user /opt
RUN chown -R user:user /src

USER user


FROM alpine:3.10.3 as packager
RUN apk --no-cache add openjdk11-jdk openjdk11-jmods
ENV JAVA_MINIMAL="/opt/java-minimal"
RUN /usr/lib/jvm/java-11-openjdk/bin/jlink \
    --verbose \
    --add-modules \
        java.base,java.naming,java.desktop,java.management,java.security.jgss,java.instrument \
    --compress 2 --strip-debug --no-header-files --no-man-pages \
      --output "$JAVA_MINIMAL"

FROM alpine:3.10.3
ENV JAVA_HOME=/opt/java-minimal
ENV PATH="$PATH:$JAVA_HOME/bin"

COPY  --from=packager "$JAVA_HOME" "$JAVA_HOME"
COPY  --from=builder /src/target/Calculator-0.0.1-SNAPSHOT.jar Calculator.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/Calculator.jar"]


