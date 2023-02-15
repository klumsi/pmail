package im.bzh.service;

import im.bzh.dto.UserDTO;
import im.bzh.entity.User;

public interface UserService {

    User getUserByUsername(String username);

    boolean addUser(User user);

    boolean updatePassword(UserDTO userDTO) throws Exception;

    boolean updateNickname(UserDTO userDTO);

    boolean deleteUserByUsername(String username) throws Exception;
}
