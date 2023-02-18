package im.bzh.controller;

import im.bzh.dto.UserDTO;
import im.bzh.entity.User;
import im.bzh.service.UserService;
import im.bzh.utils.R;
import im.bzh.vo.UserVO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Key;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private Key JWTKey;

    @GetMapping("/{username}")
    public R getUserByUsername(@PathVariable String username, @RequestHeader String Authorization) {
        Jws<Claims> claimsJws = null;
        try {
            claimsJws = Jwts.parserBuilder().setSigningKey(JWTKey).build().parseClaimsJws(Authorization);
            if (!claimsJws.getBody().getSubject().equals(username)) {
                return new R(false, "authentication failed", null);
            }
        } catch (JwtException e) {
            return new R(false, "authentication failed", null);
        }

        User user = userService.getUserByUsername(username);
        UserVO userVO = null;
        boolean success = user != null;
        if (success) {
            userVO = new UserVO(user.getUsername(), user.getNickname());
        }
        return new R(success, success ? null : "failed to get user data", userVO);
    }

    @PutMapping("/{username}")
    public R update(@PathVariable String username, @RequestBody UserDTO userDTO, @RequestHeader String Authorization) throws Exception {
        Jws<Claims> claimsJws = null;
        try {
            claimsJws = Jwts.parserBuilder().setSigningKey(JWTKey).build().parseClaimsJws(Authorization);
            if (!claimsJws.getBody().getSubject().equals(username)) {
                return new R(false, "authentication failed", null);
            }
        } catch (JwtException e) {
            return new R(false, "authentication failed", null);
        }

        if (userDTO.getNewPassword() != null) {
            boolean success = userService.updatePassword(userDTO);
            return new R(success, success ? null : "failed to change password", null);
        }
        if (userDTO.getUser().getNickname() != null) {
            boolean success = userService.updateNickname(userDTO);
            return new R(success, success ? null : "failed to change nickname", null);
        }
        return new R(false, "parameter missing", null);

    }

    @DeleteMapping("/{username}")
    public R deleteUserByUsername(@PathVariable String username, @RequestHeader String Authorization) throws Exception {
        Jws<Claims> claimsJws = null;
        try {
            claimsJws = Jwts.parserBuilder().setSigningKey(JWTKey).build().parseClaimsJws(Authorization);
            if (!claimsJws.getBody().getSubject().equals(username)) {
                return new R(false, "authentication failed", null);
            }
        } catch (JwtException e) {
            return new R(false, "authentication failed", null);
        }

        boolean success = userService.deleteUserByUsername(username);
        return new R(success, success ? null : "failed to delete user", null);
    }

}
