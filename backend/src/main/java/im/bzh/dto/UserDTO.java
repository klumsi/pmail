package im.bzh.dto;

import im.bzh.entity.User;
import lombok.Data;

@Data
public class UserDTO {

    private User user;
    private Boolean stayLoggedIn;
    private String newPassword;
}
