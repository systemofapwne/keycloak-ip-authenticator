package com.github.lukaszbudnik.keycloak.ipauthenticator;

import java.util.Collections;
import java.util.Map;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.*;
import org.keycloak.models.credential.OTPCredentialModel;

public class IPAuthenticator implements Authenticator {

    private static final Logger logger = Logger.getLogger(IPAuthenticator.class);
    private static final String IP_BASED_OTP_CONDITIONAL_USER_ATTRIBUTE = "ip_based_otp_conditional";

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        RealmModel realm = context.getRealm();
        UserModel user = context.getUser();

        String remoteIPAddress = context.getConnection().getRemoteAddr();
        String allowedIPAddresses = getAllowedIPAddresses(context); // Comma separated list of allowed IP-Addresses in CIDR notation

        //Check against all valid CIDR addresses
        boolean validIP = false;
        for(String CIDR : allowedIPAddresses.split("[\\s,;]+")){
            if(new IpAddressMatcher(CIDR).matches(remoteIPAddress)){
                validIP = true;
                break;
            }
        }

        if(user == null){   //No user context?: This plugin is used as a conditional before user validation. Fail, when IP is out of range. Succeed, when in range.
            if(!validIP){
                logger.infof("IPs do not match. Realm %s expected %s but IP was %s", realm.getName(), allowedIPAddresses, remoteIPAddress);
                context.failure(AuthenticationFlowError.INVALID_CLIENT_CREDENTIALS);
                return;
            }
        } else {            //User context available?: This plugin is now meant to work together with conditional OTP. It will always succeed but also set the field stored in IP_BASED_OTP_CONDITIONAL_USER_ATTRIBUTE to "skip" or "force" like the original addon
            if(validIP){
                user.setAttribute(IP_BASED_OTP_CONDITIONAL_USER_ATTRIBUTE, Collections.singletonList("skip"));
            } else {
                logger.infof("IPs do not match. Realm %s expected %s but user %s logged in from %s", realm.getName(), allowedIPAddresses, user.getUsername(), remoteIPAddress);

                UserCredentialManager credentialManager = (UserCredentialManager) user.credentialManager();
                if (!credentialManager.isConfiguredFor(OTPCredentialModel.TYPE)) {
                    user.addRequiredAction(UserModel.RequiredAction.CONFIGURE_TOTP);
                }

                user.setAttribute(IP_BASED_OTP_CONDITIONAL_USER_ATTRIBUTE, Collections.singletonList("force"));
            }
        }
        context.success();
    }

    private String getAllowedIPAddresses(AuthenticationFlowContext context) {
        AuthenticatorConfigModel configModel = context.getAuthenticatorConfig();
        Map<String, String> config = configModel.getConfig();
        return config.get(IPAuthenticatorFactory.ALLOWED_IP_ADDRESS_CONFIG);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

    @Override
    public void close() {
    }

}
