package com.api.rest.springbootkeycloack.service;


import com.api.rest.springbootkeycloack.entity.dto.UserDTO;

public interface IKeycloackService {


    String login(String username, String password, String clientId, String clientSecret);

    void logout(String refreshToken);

    void checkValidity(String token);

    Object refresh(String refreshToken);

    Object create(UserDTO userDTO);

    Object delete(String id);

    Object update(String id, UserDTO userDTO);

    Object search(String username);

    Object searchAll();
}
