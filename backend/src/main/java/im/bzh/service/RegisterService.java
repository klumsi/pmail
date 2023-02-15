package im.bzh.service;

import im.bzh.entity.User;

public interface RegisterService {
    boolean register(User user) throws Exception;
}
