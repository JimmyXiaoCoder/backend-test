package com.codetest.system.controller;

import com.codetest.common.Result;
import com.codetest.common.ResultUtils;
import com.codetest.system.entity.User;
import com.codetest.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return ResultUtils.success(userService.getById(id));
    }

    @GetMapping("/list")
    public Result<java.util.List<User>> listAll() {
        return ResultUtils.success(userService.listAll());
    }

    @PostMapping
    public Result<User> create(@RequestBody User user) {
        return ResultUtils.success(userService.create(user));
    }

    @PutMapping
    public Result<User> update(@RequestBody User user) {
        return ResultUtils.success(userService.update(user));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResultUtils.success();
    }
}
