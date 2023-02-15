package im.bzh;

import im.bzh.dao.UserDAO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BackendApplicationTests {
    @Autowired
    UserDAO userDAO;

    @Test
    void contextLoads() {
        System.out.println(userDAO.selectByUsername("hello"));
    }

}
