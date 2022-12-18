FROM maven:latest AS builder
WORKDIR /build/
# IP Auth Plugin
RUN git clone https://github.com/systemofapwne/keycloak-ip-authenticator.git /build/ipauth && cd /build/ipauth && mvn clean install

FROM jboss/keycloak:latest
# IP Auth Plugin
COPY --from=builder /build/ipauth/target/keycloak-ip-authenticator.jar      /opt/jboss/keycloak/standalone/deployments/keycloak-ip-authenticator.jar
