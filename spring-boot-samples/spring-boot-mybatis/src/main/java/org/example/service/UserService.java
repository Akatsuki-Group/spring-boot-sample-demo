package org.example.service;


import org.example.entity.User;

import java.util.List;

public interface UserService {
    User getUser(Long id);

    List<User> getAll();

    User getUser(Long id, String name);
}
