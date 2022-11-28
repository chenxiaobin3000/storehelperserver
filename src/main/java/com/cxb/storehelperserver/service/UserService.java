package com.cxb.storehelperserver.service;

import com.cxb.storehelperserver.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 *
 */
@Service
public class UserService {
    //logger

    @Resource
    private UserRepository userRepository;

    /**
     *
     * @return
     */
    public String getUserName() {
        return userRepository.getUserName();
    }
}
