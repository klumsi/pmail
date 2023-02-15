package im.bzh.service.impl;

import im.bzh.dao.UserDAO;
import im.bzh.dto.UserDTO;
import im.bzh.entity.User;
import im.bzh.service.UserService;
import im.bzh.utils.Shell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Value("${mail.domain}")
    private String domain;

    @Autowired
    private UserDAO userDAO;

    @Override
    public User getUserByUsername(String username) {
        return userDAO.selectByUsername(username);
    }

    @Override
    public boolean addUser(User user) {
        user.setNickname(user.getUsername());
        return userDAO.insert(user) > 0;
    }

    @Override
    public boolean updatePassword(UserDTO userDTO) throws Exception {
        User user = getUserByUsername(userDTO.getUser().getUsername());
        if (!user.getPassword().equals(userDTO.getUser().getPassword())) {
            return false;
        }

        String cmd = "maddy creds password " + userDTO.getUser().getUsername() + "@" + domain;
        Shell.exec(cmd, userDTO.getNewPassword());
        userDTO.getUser().setPassword(userDTO.getNewPassword());
        return userDAO.updatePassword(userDTO.getUser()) > 0;
    }

    @Override
    public boolean updateNickname(UserDTO userDTO) {
        return userDAO.updateNickname(userDTO.getUser()) > 0;
    }

    @Override
    public boolean deleteUserByUsername(String username) throws Exception {
        String cmd_1 = "maddy creds remove " + username + "@" + domain;
        String cmd_2 = "maddy imap-acct remove " + username + "@" + domain;
        Shell.exec(cmd_1, "y");
        Shell.exec(cmd_2, "y");
        return userDAO.deleteUserByUsername(username) > 0;
    }
}
