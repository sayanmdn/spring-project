package com.sayantan.productservices.commons;

import com.sayantan.productservices.dtos.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthenticationCommons {
    private RestTemplate restTemplate;

    public AuthenticationCommons() {
        this.restTemplate = new RestTemplate();
    }
    public UserDto validateToken(String token) {
        ResponseEntity<UserDto> userDtoResponse = restTemplate.postForEntity("http://localhost:8081/users/validate/" + token, null, UserDto.class);

        if(userDtoResponse.getBody() == null){
            return null;
        }

        return userDtoResponse.getBody();
    }
}
