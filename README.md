# keycloak-ip-authenticator

This is a simple Keycloak Java Authenticator that checks if the user is coming from a trusted network or not. The network is checked against CIDR IP notation. Multiple IP ranges can be added (space, comma, semicolon separated).
This Authenticator intially has been written with `Conditional OTP Form` in mind, that only happens after a user entered his credentials. In fact, it still supports it as show in the YouTube video [1] below.

However, this fork can also be used before a user entered his credentials. The Authenticator then simply will continue or block the execution flow in Keycloak (when set to Required) if the IPs do match or not.


[1] Youtube video which explains how to deploy and configure it in Keycloak: https://youtu.be/u36QK9oyrtM.

## build

Make sure that Keycloak SPI dependencies and your Keycloak server versions match. Keycloak SPI dependencies version is configured in `pom.xml` in the `keycloak.version` property.  

To build the project execute the following command:

```bash
mvn clean package
```

## deploy

And then, assuming `$KEYCLOAK_HOME` is pointing to you Keycloak installation, just copy it into deployments directory:
 
```bash
cp target/keycloak-ip-authenticator.jar $KEYCLOAK_HOME/standalone/deployments/
```

## build & deploy with docker

You can use and modify the `Dockerfile` in this repo to deploy your own Keycloak instance with this addon already added.
