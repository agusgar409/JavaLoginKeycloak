package com.api.rest.springbootkeycloack.entity.dto;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Set;

@Value
@RequiredArgsConstructor
@Builder
public class UserDTO {

    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private Set<String> roles;
}
