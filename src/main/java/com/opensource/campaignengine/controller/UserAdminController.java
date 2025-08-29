package com.opensource.campaignengine.controller;

import com.opensource.campaignengine.domain.User;
import com.opensource.campaignengine.domain.exception.ResourceNotFoundException;
import com.opensource.campaignengine.service.RoleService;
import com.opensource.campaignengine.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;


@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER')")
@Slf4j
public class UserAdminController {

    private final UserService userService;
    private final RoleService roleService;


    public UserAdminController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping
    public String listUsers(Model model) {
        log.info("Listing all users");
        model.addAttribute("users", userService.findAllUsers());
        return "users"; // users.html dosyasını gösterecek
    }

    @GetMapping("/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        log.debug("Editing user with id {}", id);
        User user = userService.findById(id)
                // Artık kendi özel hatamızı fırlatıyoruz
                .orElseThrow(() -> new ResourceNotFoundException("Bu ID ile kullanıcı bulunamadı: " + id));

        model.addAttribute("user", user);
        model.addAttribute("allRoles", roleService.findAll());
        return "user-from";
    }

    // YENİ METOT: Formdan gelen güncellemeyi kaydeder
    @PostMapping("/update-roles") // Adresi daha anlaşılır hale getirdik
    public String updateUserRoles(@RequestParam("userId") Long userId,
                                  @RequestParam(name = "roles", required = false) List<Long> roleIds) {
        userService.updateUserRoles(userId, roleIds);
        log.info("Updated roles for user {}", userId);
        return "redirect:/admin/users";
    }


}