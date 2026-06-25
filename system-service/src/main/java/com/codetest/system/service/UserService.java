package com.codetest.system.service;

import com.codetest.common.BusinessException;
import com.codetest.common.ResultCode;
import com.codetest.system.entity.User;
import com.codetest.system.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    public User getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }

    public User getByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    public List<User> listAll() {
        return userMapper.selectAll();
    }

    public User create(User user) {
        User exist = userMapper.selectByUsername(user.getUsername());
        if (exist != null) {
            throw new BusinessException(ResultCode.USER_EXISTED);
        }
        userMapper.insert(user);
        return user;
    }

    public User update(User user) {
        User exist = userMapper.selectById(user.getId());
        if (exist == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        userMapper.update(user);
        return userMapper.selectById(user.getId());
    }

    public void delete(Long id) {
        User exist = userMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        userMapper.deleteById(id);
    }
}
