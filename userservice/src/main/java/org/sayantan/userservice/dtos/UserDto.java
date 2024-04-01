package org.sayantan.userservice.dtos;

import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;
import org.sayantan.userservice.models.Role;
import org.sayantan.userservice.models.User;

import java.util.List;

@Getter
@Setter
public class UserDto {
    private Long id;
    private String email;
    private String name;

    @ManyToMany
    private List<Role> roles;

    private boolean isEmailVerified;

    public static UserDto from (User user) {
        if(user == null) {
            return null;
        }
        UserDto userDto = new UserDto();
        userDto.id = user.getId();
        userDto.name = user.getName();
        userDto.email = user.getEmail();
        userDto.roles = user.getRoles();
        userDto.isEmailVerified = user.isEmailVerified();

        return userDto;
    }
}
