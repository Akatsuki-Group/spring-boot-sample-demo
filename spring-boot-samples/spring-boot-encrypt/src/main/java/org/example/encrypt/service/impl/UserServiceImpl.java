package org.example.encrypt.service.impl;

import com.google.common.collect.Lists;
import org.example.encrypt.dao.UserRepository;
import org.example.encrypt.model.User;
import org.example.encrypt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description: spring-boot-encrypt
 * User: yuanct
 * Date: 2019/7/19 2:06 PM
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Boolean saveUser(User user) {
        userRepository.save(user);
        return true;
    }

    @Override
    public List<User> getAllUser() {
        Iterable<User> userIterable = userRepository.findAll();
        List<User> userList = Lists.newArrayList(userIterable);
        return userList;
    }

    @Override
    public User getUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        return user;
    }

    @Override
    public User getUserByName(String name) {
        List<User> userList = userRepository.findByName(name);
        if (userList.isEmpty()) {
            return null;
        }
        return userList.get(0);
    }

    @Override
    public Boolean deleteUser(Long userId) {
        userRepository.deleteById(userId);
        return true;
    }

}