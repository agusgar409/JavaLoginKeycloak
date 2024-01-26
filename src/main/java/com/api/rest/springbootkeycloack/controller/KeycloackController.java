package com.api.rest.springbootkeycloack.controller;

import com.api.rest.springbootkeycloack.entity.dto.UserDTO;
import com.api.rest.springbootkeycloack.service.IKeycloackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/keycloack/${api.version}")
public class KeycloackController {

    @Autowired
    private IKeycloackService keycloackService;


    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> login(String username, String password, String clientId, String clientSecret) {
        String login = keycloackService.login(username, password, clientId, clientSecret);
        return ResponseEntity.ok(login);
    }

    @PostMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> refresh(@RequestParam(value = "refresh_token", name = "refresh_token") String refreshToken) {
        return ResponseEntity.ok(keycloackService.refresh(refreshToken));
    }

    @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> logout(@RequestParam(value = "refresh_token", name = "refresh_token") String refreshToken) throws Exception {
        try {
            keycloackService.logout(refreshToken);
            return ResponseEntity.ok("logout ok");
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    @GetMapping("/valid")
    public ResponseEntity<Object> valid(@RequestHeader("Authorization") String authHeader) {
        keycloackService.checkValidity(authHeader);
        return ResponseEntity.ok("Token válido");
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> create(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(keycloackService.create(userDTO));
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> delete(@PathVariable("id") String id) {
        return ResponseEntity.ok(keycloackService.delete(id));
    }

    @PutMapping(value = "/update/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> update(@PathVariable("id") String id, @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(keycloackService.update(id, userDTO));
    }

    @GetMapping(value = "/search/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> search(@PathVariable("username") String username) {
        return ResponseEntity.ok(keycloackService.search(username));
    }

    @GetMapping(value = "/searchAll", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> searchAll() {
        return ResponseEntity.ok(keycloackService.searchAll());
    }


}
