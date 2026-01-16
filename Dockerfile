ARG KEYCLOAK_VERSION=latest

### Build: IP Auth Plugin
FROM maven:latest AS builder
WORKDIR /build/
RUN git clone https://github.com/systemofapwne/keycloak-ip-authenticator.git /build/ipauth && cd /build/ipauth && mvn clean install

### Build: Keycloak (https://www.keycloak.org/server/containers)
FROM quay.io/keycloak/keycloak:${KEYCLOAK_VERSION} AS kcbuilder

# Configure your database backend etc below...
# ENV KC_DB=mariadb

WORKDIR /opt/keycloak
COPY --from=plugin /build/ipauth/target/keycloak-ip-authenticator.jar providers/keycloak-ip-authenticator.jar

RUN /opt/keycloak/bin/kc.sh build

### Deploy: Keycloak
FROM quay.io/keycloak/keycloak:latest
WORKDIR /opt/keycloak
COPY --from=kcbuilder /opt/keycloak .

ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start", "--optimized" ]
