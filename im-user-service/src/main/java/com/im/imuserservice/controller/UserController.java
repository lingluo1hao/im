package com.im.imuserservice.controller;


import com.im.common.result.R;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.im.imuserservice.dto.LoginDTO;
import com.im.imuserservice.dto.RegisterDTO;
import com.im.imuserservice.dto.UserDTO;
import com.im.imuserservice.entity.User;
import com.im.imuserservice.service.UserService;
import com.im.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 1. 用户注册
     * POST /user/register
     */
    @PostMapping("/register")
    public R<String> register(@Valid @RequestBody RegisterDTO registerDTO) {
        userService.register(registerDTO);
        return R.ok("注册成功");
    }

    /**
     * 2. 用户登录，返回JWT
     * POST /user/login
     */
    @PostMapping("/login")
    public R<String> login(@Valid @RequestBody LoginDTO loginDTO) {
        String token = userService.login(loginDTO);
        return R.ok(token, "登录成功");
    }

    /**
     * 3. 获取当前登录用户信息
     * GET /user/info
     */
    @GetMapping("/info")
    public R<UserDTO> getCurrentUser(HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = jwtUtil.getUserId(token);
        UserDTO userDTO = userService.getUserInfoById(userId);
        return R.ok(userDTO);
    }


    /**
     * 4. 修改个人信息
     * PUT /user/info
     */
    @PutMapping("/info")
    public R<String> updateUserInfo(@RequestBody UserDTO userDTO, HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = jwtUtil.getUserId(token);
        userService.updateUserInfo(userId, userDTO);
        return R.ok("信息修改成功");
    }

    /**
     * 5. 修改密码
     * PUT /user/password
     */
    @PutMapping("/password")
    public R<String> updatePassword(@RequestParam String oldPwd,
                                    @RequestParam String newPwd,
                                    HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = jwtUtil.getUserId(token);
        userService.updatePassword(userId, oldPwd, newPwd);
        return R.ok("密码修改成功，请重新登录");
    }

    /**
     * 6. 用户分页列表（后台管理）
     * GET /user/list
     */
    @GetMapping("/list")
    public R<IPage<UserDTO>> userPage(@RequestParam(defaultValue = "1") Long pageNum,
                                      @RequestParam(defaultValue = "10") Long pageSize,
                                      @RequestParam(required = false) String username,
                                      @RequestParam(required = false) Integer status) {
        Page<User> page = new Page<>(pageNum, pageSize);
        IPage<UserDTO> pageData = userService.getUserPage(page, username, status);
        return R.ok(pageData);
    }

    /**
     * 7. 启用/禁用账号状态管理
     * PUT /user/status/{userId}
     */
    @PutMapping("/status/{userId}")
    public R<String> updateUserStatus(@PathVariable Long userId,
                                      @RequestParam Integer status) {
        // status 0=禁用 1=正常
        userService.updateStatus(userId, status);
        return R.ok(status == 1 ? "账号已启用" : "账号已禁用");
    }

    /**
     * 8. 根据id查询用户
     * GET /user/{id}
     */
    @GetMapping("/{id}")
    public R<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO userDTO = userService.getUserInfoById(id);
        return R.ok(userDTO);
    }

}