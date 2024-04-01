package com.sayantan.productservices.dtos;


import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserDto {
    private Long id;
    private String email;
    private String name;

    private List<Role> roles;

    private boolean isEmailVerified;

}
