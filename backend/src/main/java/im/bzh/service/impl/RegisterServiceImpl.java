package im.bzh.service.impl;

import im.bzh.entity.User;
import im.bzh.service.RegisterService;
import im.bzh.service.UserService;
import im.bzh.utils.Shell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RegisterServiceImpl implements RegisterService {

    @Value("${mail.domain}")
    private String domain;

    @Autowired
    private UserService userService;

    @Override
    public boolean register(User user) throws Exception {
        if (userService.getUserByUsername(user.getUsername()) == null) {
            userService.addUser(user);
            String cmd_1 = "maddy creds create " + user.getUsername() + "@" + domain;
            String cmd_2 = "maddy imap-acct create " + user.getUsername() + "@" + domain;
            Shell.exec(cmd_1, user.getPassword());
            Shell.exec(cmd_2, null);
            return true;
        }
        return false;
    }
}
