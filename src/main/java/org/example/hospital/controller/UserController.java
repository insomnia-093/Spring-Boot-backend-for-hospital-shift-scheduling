package  org.example.hospital.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.example.hospital.dto.ChangePasswordRequest;
import org.example.hospital.service.UserService;
import org.example.hospital.service.UserService.UserSummary;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    // 管理后台读取所有用户概览数据。
    public ResponseEntity<List<UserSummary>> listUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{userId}")
    // 查看具体用户的角色与部门信息。
    public ResponseEntity<UserSummary> findById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.findById(userId));
    }

    @PutMapping("/{userId}/password")
    // 管理员重置指定用户密码。
    public ResponseEntity<Void> changePassword(@PathVariable Long userId,
                                               @RequestBody @Valid ChangePasswordRequest request) {
        userService.changePassword(userId, request.getNewPassword());
        return ResponseEntity.noContent().build();
    }
}
