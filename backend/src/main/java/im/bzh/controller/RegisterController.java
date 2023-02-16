package im.bzh.controller;

import im.bzh.entity.User;
import im.bzh.service.RegisterService;
import im.bzh.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/register")
public class RegisterController {

    @Autowired
    private RegisterService registerService;

    @PostMapping
    public R register(@RequestBody User user) throws Exception {
        Integer success = registerService.register(user);
        String res = null;
        if (success == -1) {
            res = "account name already taken";
        } else if (success == 0){
            res = "failed to register";
        }
        return new R(success == 1, res, null);
    }

}
