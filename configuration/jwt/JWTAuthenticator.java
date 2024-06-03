package com.nike.invoiceshipmentwrkr.configuration.jwt;

import com.nike.cdt.auth.clientsdk.NikeJWTClientAuth;
import com.nike.cdt.auth.clientsdk.NikeJWTClientAuthManager;
import com.nike.cdt.auth.clientsdk.NikeJWTConfigurationRSA;
import com.nike.cdt.auth.clientsdk.constants.NikeAuthErrorCode;
import com.nike.cdt.auth.clientsdk.constants.NikeKeyServerType;
import com.nike.cdt.auth.clientsdk.exceptions.NikeAuthException;
import com.nike.cdt.auth.clientsdk.exceptions.NikeConfigurationException;
import com.nike.cdt.auth.clientsdk.ext.NikeClientS3CfgProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;

/**
 * Tailored JWTAuthenticator for Fulfillment to utilize info.app.group.name
 * property, which wil allow use to use single JWT key for entire fulfillment
 * application..
 */
@Slf4j
@Component(value = "Signer")
public class JWTAuthenticator {

    private String configId = UUID.randomUUID().toString();

    @Value("${jwt.enabled}")
    private boolean jwtEnabled;

    @Value("${jwt.auth.issuer}")
    private String issuer;

    @Value("${nike.cdt.jwt.authScopes}")
    private String[] authScopes;

    @Value("${jwt.auth.audience}")
    private String[] audience;

    @Value("${nike.cdt.jwt.keyProviderType}")
    private String providerType;

    @Value("${nike.cdt.jwt.decryptionKeyLoc}")
    private String decryptionKeyLoc;

    @Value("${nike.cdt.jwt.jwkLoc}")
    private String jwkLoc;

    @Value("${nike.cdt.jwt.s3.keyBucket:unknown}")
    private String s3BucketName;

    /**
     * This method checks whether application is jwt enabled.
     *
     * @return boolean
     */
    public boolean isJwtEnabled() {
        return jwtEnabled;
    }

    /**
     * This method returns the string which is needed to signInternal the http
     * request.
     *
     * @return string
     */
    public String getConfigId() {
        return configId;
    }

    /**
     * This method assumes EC2 instance will be started in an IAM role which
     * will have access policies set up for S3 objects.
     *
     * @throws Exception
     */
    public void configureWithDefaultSettings() throws Exception {
        configureWithDefaultSettings(null);
    }

    /**
     * Sign the http request for given application name.
     *
     * @param target
     *            object to sign
     * @throws NikeAuthException
     */
    public void sign(Object target) throws NikeAuthException {
        signInternal(target, getConfigId());
    }

    /**
     * Sign the http request for given application name.
     *
     * @param target
     *            object to sign
     * @param domain
     *            the domain name claimed.
     * @throws NikeAuthException
     */
    public void sign(Object target, String domain) throws NikeAuthException {
        signInternal(target, getConfigId(), domain);
    }

    /**
     * With a custom credential and config provider for S3 and a custom
     * application name. Applications using this way should send in same
     * application name while signing the http request sign(object,
     * appGroupName) Or they should send in the configName (return string of
     * this method) in signInternal method.
     *
     * @param provider
     *            S3 configuration provider
     * @throws Exception
     */
    private void configureWithDefaultSettings(NikeClientS3CfgProvider provider) throws Exception {
        if ("S3".equals(providerType)) {
            NikeJWTConfigurationRSA builder = NikeJWTConfigurationRSA.builder(authScopes, audience)
                    .setClientApplicationID(issuer).setKeyServerType(NikeKeyServerType.NONE)
                    .setS3KeyBucket(s3BucketName).setClientS3CfgProvider(provider).loadPrivateKeyFromS3().build();

            NikeJWTClientAuthManager.register(getConfigId(), builder);
        } else if ("STATIC".equals(providerType)) {
            NikeJWTConfigurationRSA config = NikeJWTConfigurationRSA.builder(authScopes, audience).setJwtExpireInMin(60)
                    .setClientApplicationID(issuer).setEncryptedJWKLocations(decryptionKeyLoc, jwkLoc).build();
            NikeJWTClientAuthManager.register(getConfigId(), config);

        } else {
            throw new NikeConfigurationException(NikeAuthErrorCode.MISSING_CONFIG,
                    "Invalid nike.cdt.jwt.keyProviderType configuration value :" + providerType);
        }
    }

    /**
     * Sign the http request by given config name (registration id)
     *
     * @param target
     *            object to sign
     * @param configName
     *            the configuration name / id
     * @throws NikeAuthException
     */
    private void signInternal(Object target, String configName) throws NikeAuthException {
        NikeJWTClientAuth.sign(target, configName);
    }

    /**
     * Sign the http request by given config name (registration id) Note: when
     * configuring, you need to enable addJtiClaim and you don't need the
     * server.apps list Example Configuration entry
     * <p>
     * # No longer needed
     * productlaunchadmin.jwt.auth.server.apps=inventoryadmin,allocatedinventory,inventoryservice
     * productlaunchadmin.jwt.addJtiClaim=true
     *
     * @param target
     *            object to sign
     * @param configName
     *            the configuration name / id
     * @param domain
     *            the domain name claimed.
     * @throws NikeAuthException
     */
    private void signInternal(Object target, String configName, String domain) throws NikeAuthException {
        NikeJWTClientAuth.sign(target, configName, domain);
    }

    @PostConstruct
    private void init() {
        try {
            if (isJwtEnabled()) {
                this.configureWithDefaultSettings();
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not initiate jwt signer", e);
        }
    }
}
