package org.mitre.openid.connect.client;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jwt.*;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.mitre.jwt.signer.service.JwtSigningAndValidationService;
import org.mitre.jwt.signer.service.impl.JWKSetCacheService;
import org.mitre.jwt.signer.service.impl.SymmetricCacheService;
import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.client.service.ClientConfigurationService;
import org.mitre.openid.connect.client.service.ServerConfigurationService;
import org.mitre.openid.connect.config.ServerConfiguration;
import org.mitre.openid.connect.model.OIDCAuthenticationToken;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.text.ParseException;
import java.util.Date;

import static org.mitre.oauth2.model.ClientDetailsEntity.AuthMethod.*;

public class LDOIDCAuthenticationFilter extends OIDCAuthenticationFilter {
    /**
     * @param request The request from which to extract parameters and perform the
     *                authentication
     * @return The authenticated user token, or null if authentication is
     * incomplete.
     */
    @Override
    protected Authentication handleAuthorizationCodeResponse(HttpServletRequest request, HttpServletResponse response) {

        String authorizationCode = request.getParameter("code");

        HttpSession session = request.getSession();

        // check for state, if it doesn't match we bail early
        String storedState = getStoredState(session);
        if (!Strings.isNullOrEmpty(storedState)) {
            String state = request.getParameter("state");
            if (!storedState.equals(state)) {
                throw new AuthenticationServiceException("State parameter mismatch on return. Expected " + storedState + " got " + state);
            }
        }

        // look up the issuer that we set out to talk to
        String issuer = null;
        Method getStoredSessionString = null;
        try {
            getStoredSessionString = OIDCAuthenticationFilter.class.getDeclaredMethod("getStoredSessionString", HttpSession.class, String.class);
            getStoredSessionString.setAccessible(true);
            issuer = (String) getStoredSessionString.invoke(this, session, ISSUER_SESSION_VARIABLE);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

        // pull the configurations based on that issuer
        ServerConfigurationService servers = null;
        ClientConfigurationService clients = null;
        try {
            Field serversField = OIDCAuthenticationFilter.class.getDeclaredField("servers");
            serversField.setAccessible(true);
            servers = (ServerConfigurationService) serversField.get(this);
            Field clientsField = OIDCAuthenticationFilter.class.getDeclaredField("clients");
            clientsField.setAccessible(true);
            clients = (ClientConfigurationService) clientsField.get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        ServerConfiguration serverConfig = servers.getServerConfiguration(issuer);
        final RegisteredClient clientConfig = clients.getClientConfiguration(serverConfig);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "authorization_code");
        form.add("code", authorizationCode);

        String redirectUri = null;
        try {
            redirectUri = (String) getStoredSessionString.invoke(this, session, REDIRECT_URI_SESION_VARIABLE);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        if (redirectUri != null) {
            form.add("redirect_uri", redirectUri);
        }

        // Handle Token Endpoint interaction
        HttpClient httpClient = new SystemDefaultHttpClient();

        httpClient.getParams().setParameter("http.socket.timeout", new Integer(httpSocketTimeout));

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        RestTemplate restTemplate;

        if (SECRET_BASIC.equals(clientConfig.getTokenEndpointAuthMethod())) {
            // use BASIC auth if configured to do so
            restTemplate = new RestTemplate(factory) {

                @Override
                protected ClientHttpRequest createRequest(URI url, HttpMethod method) throws IOException {
                    ClientHttpRequest httpRequest = super.createRequest(url, method);
                    httpRequest.getHeaders().add("Authorization",
                            String.format("Basic %s", Base64.encode(String.format("%s:%s", clientConfig.getClientId(), clientConfig.getClientSecret()))));


                    return httpRequest;
                }
            };
        } else {
            // we're not doing basic auth, figure out what other flavor we have
            restTemplate = new RestTemplate(factory);

            if (SECRET_JWT.equals(clientConfig.getTokenEndpointAuthMethod()) || PRIVATE_KEY.equals(clientConfig.getTokenEndpointAuthMethod())) {
                // do a symmetric secret signed JWT for auth


                JwtSigningAndValidationService signer = null;
                JWSAlgorithm alg = clientConfig.getTokenEndpointAuthSigningAlg();

                if (SECRET_JWT.equals(clientConfig.getTokenEndpointAuthMethod()) &&
                        (alg.equals(JWSAlgorithm.HS256)
                                || alg.equals(JWSAlgorithm.HS384)
                                || alg.equals(JWSAlgorithm.HS512))) {

                    try {
                        Field symmetricCacheServiceField = OIDCAuthenticationFilter.class.getDeclaredField("symmetricCacheService");
                        symmetricCacheServiceField.setAccessible(true);
                        SymmetricCacheService symmetricCacheService = (SymmetricCacheService) symmetricCacheServiceField.get(this);
                        // generate one based on client secret
                        signer = symmetricCacheService.getSymmetricValidtor(clientConfig.getClient());
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }

                } else if (PRIVATE_KEY.equals(clientConfig.getTokenEndpointAuthMethod())) {
                    try {
                        Field authenticationSignerServiceField = OIDCAuthenticationFilter.class.getDeclaredField("authenticationSignerService");
                        authenticationSignerServiceField.setAccessible(true);
                        // needs to be wired in to the bean
                        signer = (JwtSigningAndValidationService) authenticationSignerServiceField.get(this);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

                if (signer == null) {
                    throw new AuthenticationServiceException("Couldn't find required signer service for use with private key auth.");
                }

                JWTClaimsSet claimsSet = new JWTClaimsSet();

                claimsSet.setIssuer(clientConfig.getClientId());
                claimsSet.setSubject(clientConfig.getClientId());
                claimsSet.setAudience(Lists.newArrayList(serverConfig.getTokenEndpointUri()));

                // TODO: make this configurable
                Date exp = new Date(System.currentTimeMillis() + (60 * 1000)); // auth good for 60 seconds
                claimsSet.setExpirationTime(exp);

                Date now = new Date(System.currentTimeMillis());
                claimsSet.setIssueTime(now);
                claimsSet.setNotBeforeTime(now);

                SignedJWT jwt = new SignedJWT(new JWSHeader(alg), claimsSet);

                signer.signJwt(jwt, alg);

                form.add("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer");
                form.add("client_assertion", jwt.serialize());
            } else {
                //Alternatively use form based auth
                form.add("client_id", clientConfig.getClientId());
                form.add("client_secret", clientConfig.getClientSecret());
            }

        }

        logger.debug("tokenEndpointURI = " + serverConfig.getTokenEndpointUri());
        logger.debug("form = " + form);

        String jsonString = null;

        try {
            jsonString = restTemplate.postForObject(serverConfig.getTokenEndpointUri(), form, String.class);
        } catch (HttpClientErrorException httpClientErrorException) {

            // Handle error

            logger.error("Token Endpoint error response:  "
                    + httpClientErrorException.getStatusText() + " : "
                    + httpClientErrorException.getMessage());

            throw new AuthenticationServiceException("Unable to obtain Access Token: " + httpClientErrorException.getMessage());
        }

        logger.debug("from TokenEndpoint jsonString = " + jsonString);

        JsonElement jsonRoot = new JsonParser().parse(jsonString);
        if (!jsonRoot.isJsonObject()) {
            throw new AuthenticationServiceException("Token Endpoint did not return a JSON object: " + jsonRoot);
        }

        JsonObject tokenResponse = jsonRoot.getAsJsonObject();

        if (tokenResponse.get("error") != null) {

            // Handle error

            String error = tokenResponse.get("error").getAsString();

            logger.error("Token Endpoint returned: " + error);

            throw new AuthenticationServiceException("Unable to obtain Access Token.  Token Endpoint returned: " + error);

        } else {

            // Extract the id_token to insert into the
            // OIDCAuthenticationToken

            // get out all the token strings
            String accessTokenValue = null;
            String idTokenValue = null;
            String refreshTokenValue = null;

            if (tokenResponse.has("access_token")) {
                accessTokenValue = tokenResponse.get("access_token").getAsString();
            } else {
                throw new AuthenticationServiceException("Token Endpoint did not return an access_token: " + jsonString);
            }

            if (tokenResponse.has("id_token")) {
                idTokenValue = tokenResponse.get("id_token").getAsString();
            } else {
                logger.error("Token Endpoint did not return an id_token");
                throw new AuthenticationServiceException("Token Endpoint did not return an id_token");
            }

            if (tokenResponse.has("refresh_token")) {
                refreshTokenValue = tokenResponse.get("refresh_token").getAsString();
            }

            try {
                JWT idToken = JWTParser.parse(idTokenValue);

                // validate our ID Token over a number of tests
                ReadOnlyJWTClaimsSet idClaims = idToken.getJWTClaimsSet();

                // check the signature
                JwtSigningAndValidationService jwtValidator = null;

                Algorithm tokenAlg = idToken.getHeader().getAlgorithm();

                Algorithm clientAlg = clientConfig.getIdTokenSignedResponseAlg();

                if (clientAlg != null) {
                    if (!clientAlg.equals(tokenAlg)) {
                        throw new AuthenticationServiceException("Token algorithm " + tokenAlg + " does not match expected algorithm " + clientAlg);
                    }
                }

                if (idToken instanceof PlainJWT) {

                    if (clientAlg == null) {
                        throw new AuthenticationServiceException("Unsigned ID tokens can only be used if explicitly configured in client.");
                    }

                    if (tokenAlg != null && !tokenAlg.equals(JWSAlgorithm.NONE)) {
                        throw new AuthenticationServiceException("Unsigned token received, expected signature with " + tokenAlg);
                    }
                } else if (idToken instanceof SignedJWT) {

                    SignedJWT signedIdToken = (SignedJWT) idToken;

                    if (tokenAlg.equals(JWSAlgorithm.HS256)
                            || tokenAlg.equals(JWSAlgorithm.HS384)
                            || tokenAlg.equals(JWSAlgorithm.HS512)) {

                        // generate one based on client secret
                        try {
                            Field symmetricCacheServiceField = OIDCAuthenticationFilter.class.getDeclaredField("symmetricCacheService");
                            symmetricCacheServiceField.setAccessible(true);
                            SymmetricCacheService symmetricCacheService = (SymmetricCacheService) symmetricCacheServiceField.get(this);
                            jwtValidator = symmetricCacheService.getSymmetricValidtor(clientConfig.getClient());
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // otherwise load from the server's public key
                        try {
                            Field validationServicesField = OIDCAuthenticationFilter.class.getDeclaredField("validationServices");
                            validationServicesField.setAccessible(true);
                            JWKSetCacheService validationServices = (JWKSetCacheService) validationServicesField.get(this);
                            jwtValidator = validationServices.getValidator(serverConfig.getJwksUri());
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    if (jwtValidator != null) {
                        if (!jwtValidator.validateSignature(signedIdToken)) {
                            throw new AuthenticationServiceException("Signature validation failed");
                        }
                    } else {
                        logger.error("No validation service found. Skipping signature validation");
                        throw new AuthenticationServiceException("Unable to find an appropriate signature validator for ID Token.");
                    }
                } // TODO: encrypted id tokens

                // check the issuer
                if (idClaims.getIssuer() == null) {
                    throw new AuthenticationServiceException("Id Token Issuer is null");
                } else if (!idClaims.getIssuer().equals(serverConfig.getIssuer())) {
                    throw new AuthenticationServiceException("Issuers do not match, expected " + serverConfig.getIssuer() + " got " + idClaims.getIssuer());
                }

                // check expiration
                if (idClaims.getExpirationTime() == null) {
                    throw new AuthenticationServiceException("Id Token does not have required expiration claim");
                } else {
                    int timeSkewAllowance = -1;
                    try {
                        Field timeSkewAllowanceField = OIDCAuthenticationFilter.class.getDeclaredField("timeSkewAllowance");
                        timeSkewAllowanceField.setAccessible(true);
                        timeSkewAllowance = (int) timeSkewAllowanceField.get(this);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    // it's not null, see if it's expired
                    Date now = new Date(System.currentTimeMillis() - (timeSkewAllowance * 1000));
                    if (now.after(idClaims.getExpirationTime())) {
                        throw new AuthenticationServiceException("Id Token is expired: " + idClaims.getExpirationTime());
                    }
                }

                // check not before
                if (idClaims.getNotBeforeTime() != null) {
                    int timeSkewAllowance = -1;
                    try {
                        Field timeSkewAllowanceField = OIDCAuthenticationFilter.class.getDeclaredField("timeSkewAllowance");
                        timeSkewAllowanceField.setAccessible(true);
                        timeSkewAllowance = (int) timeSkewAllowanceField.get(this);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    Date now = new Date(System.currentTimeMillis() + (timeSkewAllowance * 1000));
                    if (now.before(idClaims.getNotBeforeTime())) {
                        throw new AuthenticationServiceException("Id Token not valid untill: " + idClaims.getNotBeforeTime());
                    }
                }

                // check issued at
                if (idClaims.getIssueTime() == null) {
                    throw new AuthenticationServiceException("Id Token does not have required issued-at claim");
                } else {
                    int timeSkewAllowance = -1;
                    try {
                        Field timeSkewAllowanceField = OIDCAuthenticationFilter.class.getDeclaredField("timeSkewAllowance");
                        timeSkewAllowanceField.setAccessible(true);
                        timeSkewAllowance = (int) timeSkewAllowanceField.get(this);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    // since it's not null, see if it was issued in the future
                    Date now = new Date(System.currentTimeMillis() + (timeSkewAllowance * 1000));
                    if (now.before(idClaims.getIssueTime())) {
                        throw new AuthenticationServiceException("Id Token was issued in the future: " + idClaims.getIssueTime());
                    }
                }

                // check audience
                if (idClaims.getAudience() == null) {
                    throw new AuthenticationServiceException("Id token audience is null");
                } else if (!idClaims.getAudience().contains(clientConfig.getClientId())) {
                    throw new AuthenticationServiceException("Audience does not match, expected " + clientConfig.getClientId() + " got " + idClaims.getAudience());
                }

                // compare the nonce to our stored claim
                String nonce = idClaims.getStringClaim("nonce");
                if (Strings.isNullOrEmpty(nonce)) {

                    logger.error("ID token did not contain a nonce claim.");

                    throw new AuthenticationServiceException("ID token did not contain a nonce claim.");
                }

                String storedNonce = getStoredNonce(session);
                if (!nonce.equals(storedNonce)) {
                    logger.error("Possible replay attack detected! The comparison of the nonce in the returned "
                            + "ID Token to the session " + NONCE_SESSION_VARIABLE + " failed. Expected " + storedNonce + " got " + nonce + ".");

                    throw new AuthenticationServiceException(
                            "Possible replay attack detected! The comparison of the nonce in the returned "
                                    + "ID Token to the session " + NONCE_SESSION_VARIABLE + " failed. Expected " + storedNonce + " got " + nonce + ".");
                }

                // pull the subject (user id) out as a claim on the id_token

                String userId = idClaims.getSubject();

                // construct an OIDCAuthenticationToken and return a Authentication object w/the userId and the idToken

                OIDCAuthenticationToken token = new OIDCAuthenticationToken(userId, idClaims.getIssuer(), serverConfig, idTokenValue, accessTokenValue, refreshTokenValue);

                Authentication authentication = this.getAuthenticationManager().authenticate(token);

                return authentication;
            } catch (ParseException e) {
                throw new AuthenticationServiceException("Couldn't parse idToken: ", e);
            }
        }
    }
}
