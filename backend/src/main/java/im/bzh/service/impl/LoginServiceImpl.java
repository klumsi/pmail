package im.bzh.service.impl;

import im.bzh.dto.UserDTO;
import im.bzh.entity.User;
import im.bzh.service.LoginService;
import im.bzh.service.UserService;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private UserService userService;

    @Autowired
    private Key JWTKey;

    @Override
    public String login(UserDTO userDTO) {
        try {
            User resultUser = userService.getUserByUsername(userDTO.getUser().getUsername());
            if (resultUser == null) {
                return "";
            }

            boolean checkPassword =  resultUser.getPassword().equals(userDTO.getUser().getPassword());
            if (!checkPassword) {
                return "";
            }

            final long EXPIRED = 1000 * 60 * 60 * 24;
            final long EXPIRED_STAY_LOGGED_IN = 1000L * 60 * 60 * 24 * 365 * 10;
            return Jwts.builder()
                    .setHeaderParam("alg", "HS256")
                    .setSubject(userDTO.getUser().getUsername())
                    .setExpiration(new Date(System.currentTimeMillis() + (userDTO.getStayLoggedIn() ? EXPIRED_STAY_LOGGED_IN : EXPIRED)))
                    .signWith(JWTKey)
                    .compact();

        } catch (Exception e) {
            return "-1";
        }
    }
}
