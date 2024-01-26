package com.api.rest.springbootkeycloack.config;

import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;

public class KeycloackProvider {

    private static final String SERVER_URL = "http://54.233.223.93:8080";
    private static final String REALM_NAME = "springboot-realm";
    private static final String REALM_MASTER = "master";
    private static final String USER_NAME = "admin";
    private static final String USER_PASS = "admin";
    private static final String ADMIN_CLI = "admin-cli";
    private static final String CLIENT_SECRET = "glXuh10HzyE8K7POgyAoF6OpnjxlTffJ";


    public static RealmResource getRealmResource() {
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(SERVER_URL)
                .realm(REALM_MASTER)
                .username(USER_NAME)
                .password(USER_PASS)
                .clientId(ADMIN_CLI)
                .clientSecret(CLIENT_SECRET)
                .resteasyClient(new ResteasyClientBuilderImpl()
                        .connectionPoolSize(10)
                        .build())
                .build();

        return keycloak.realm(REALM_NAME);
    }

    public static UsersResource getUserResource() {
        RealmResource realmResource = getRealmResource();
        return realmResource.users();
    }
}
