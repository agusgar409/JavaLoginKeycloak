package com.api.rest.springbootkeycloack.service.impl;

import com.api.rest.springbootkeycloack.config.KeycloackProvider;
import com.api.rest.springbootkeycloack.entity.dto.UserDTO;
import com.api.rest.springbootkeycloack.service.IKeycloackService;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class KeycloackServiceImpl implements IKeycloackService {

    private static final String CLIENT_ID = "client_id";
    private static final String CLIENT_SECRET = "client_secret";

    @Autowired
    private RestTemplate restTemplate;

    @Value("${keycloak.authorization-grant-type}")
    private String grantType;

    @Value("${keycloak.scope}")
    private String scope;

    @Value("${keycloak.token-uri}")
    private String keycloakTokenUri;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.logout}")
    private String keycloakLogout;

    @Value("${keycloak.user-info-uri}")
    private String keycloakUserInfo;

    @Value("${keycloak.authorization-grant-type-refresh}")
    private String grantTypeRefresh;


    @Override
    public String login(String username, String password, String clientId, String clientSecret) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username",username);
        map.add("password",password);
        map.add(CLIENT_ID,clientId);
        map.add("grant_type",this.grantType);
        map.add(CLIENT_SECRET,clientSecret);
        map.add("scope",this.scope);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, new HttpHeaders());
        return restTemplate.postForObject(keycloakTokenUri, request, String.class);
    }

    public void logout(String refreshToken) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(CLIENT_ID,clientId);
        map.add(CLIENT_SECRET,clientSecret);
        map.add("refresh_token",refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, null);
        restTemplate.postForObject(keycloakLogout, request, String.class);
    }

    public void checkValidity(String token) {
        getUserInfo(token);
    }

    @Override
    public Object refresh(String refreshToken) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(CLIENT_ID,clientId);
        map.add("grant_type",grantTypeRefresh);
        map.add("refresh_token",refreshToken);
        map.add(CLIENT_SECRET,clientSecret);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, null);
        return restTemplate.postForObject(keycloakTokenUri, request, String.class);
    }

    @Override
    public Object create(UserDTO userDTO) {
        int status;

        UsersResource usersResource = KeycloackProvider.getUserResource();

        UserRepresentation user = new UserRepresentation();
        user.setUsername(userDTO.getUsername());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setEmailVerified(true);
        user.setEnabled(true);

        Response response = usersResource.create(user);
        status = response.getStatus();


        if(status == 201) {
            String path = response.getLocation().getPath();
            String userId = path.substring(path.lastIndexOf('/') + 1);

            CredentialRepresentation passwordCred = new CredentialRepresentation();
            passwordCred.setTemporary(false);
            passwordCred.setType(CredentialRepresentation.PASSWORD);
            passwordCred.setValue(userDTO.getPassword());

            usersResource.get(userId).resetPassword(passwordCred);

            RealmResource realmResource = KeycloackProvider.getRealmResource();

            List<RoleRepresentation> roles = null;

            if(userDTO.getRoles() == null || userDTO.getRoles().isEmpty()) {
                roles = List.of(realmResource.roles().get("Administrator").toRepresentation());
            } else {
                roles = realmResource.roles().list()
                        .stream()
                        .filter(role -> userDTO.getRoles()
                                .stream()
                                .anyMatch(roleName -> roleName.equalsIgnoreCase(role.getName())))
                        .toList();
            }

            realmResource.users().get(userId).roles().realmLevel().add(roles);

            return "User created successfully";
        } else if(status == 409) {
            return "User already exists";
        } else {
            return "Something went wrong";
        }
    }

    @Override
    public Object delete(String id) {
        UsersResource usersResource = KeycloackProvider.getUserResource();
        Response response = usersResource.delete(id);
        int status = response.getStatus();
        if(status == 204) {
            return "User deleted successfully";
        } else if(status == 404) {
            return "User not found";
        } else {
            return "Something went wrong";
        }
    }

    @Override
    public Object update(String id, UserDTO userDTO) {


        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(userDTO.getPassword());

        UsersResource usersResource = KeycloackProvider.getUserResource();

        UserRepresentation user = usersResource.get(id).toRepresentation();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setCredentials(Collections.singletonList(passwordCred));


        usersResource.get(id).update(user);
        return "User updated successfully";
    }

    @Override
    public Object search(String username) {
        UsersResource usersResource = KeycloackProvider.getUserResource();
        return usersResource.search(username);
    }

    @Override
    public Object searchAll() {
        UsersResource usersResource = KeycloackProvider.getUserResource();
        return usersResource.list();
    }

    private void getUserInfo(String token) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", token);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null, headers);
        restTemplate.postForObject(keycloakUserInfo, request, String.class);
    }
}
