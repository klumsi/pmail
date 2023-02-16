package im.bzh.service;

import im.bzh.entity.User;

public interface RegisterService {
    Integer register(User user) throws Exception;
}
