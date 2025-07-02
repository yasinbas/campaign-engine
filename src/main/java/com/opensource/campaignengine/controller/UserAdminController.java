package com.opensource.campaignengine.controller;

import com.opensource.campaignengine.domain.User;
import com.opensource.campaignengine.domain.exception.ResourceNotFoundException;
import com.opensource.campaignengine.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.opensource.campaignengine.repository.RoleRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;


@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER')")
public class UserAdminController {

    private final UserService userService;
    private final RoleRepository roleRepository;


    public UserAdminController(UserService userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "users"; // users.html dosyasını gösterecek
    }

    @GetMapping("/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
                // Artık kendi özel hatamızı fırlatıyoruz
                .orElseThrow(() -> new ResourceNotFoundException("Bu ID ile kullanıcı bulunamadı: " + id));

        model.addAttribute("user", user);
        model.addAttribute("allRoles", roleRepository.findAll());
        return "user-from";
    }

    // YENİ METOT: Formdan gelen güncellemeyi kaydeder
    @PostMapping("/update-roles") // Adresi daha anlaşılır hale getirdik
    public String updateUserRoles(@RequestParam("userId") Long userId,
                                  @RequestParam(name = "roles", required = false) List<Long> roleIds) {
        userService.updateUserRoles(userId, roleIds);
        return "redirect:/admin/users";
    }


}