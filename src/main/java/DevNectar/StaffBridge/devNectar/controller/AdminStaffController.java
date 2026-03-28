package DevNectar.StaffBridge.devNectar.controller;

import DevNectar.StaffBridge.devNectar.entity.Role;
import DevNectar.StaffBridge.devNectar.entity.User;
import DevNectar.StaffBridge.devNectar.repository.RoleRepository;
import DevNectar.StaffBridge.devNectar.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin/staff")
public class AdminStaffController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public AdminStaffController(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public String listStaff(@RequestParam(name = "search", required = false) String search, Model model) {
        List<User> staff;
        if (search != null && !search.isEmpty()) {
            staff = userRepository.findByFullNameContainingIgnoreCaseAndIsDeletedFalse(search);
        } else {
            staff = userRepository.findByIsDeletedFalse();
        }
        model.addAttribute("staff", staff);
        model.addAttribute("activePage", "staff_management");
        return "admin/staff-list";
    }

    @GetMapping("/details/{id}")
    public String staffDetails(@PathVariable(name = "id") Long id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        model.addAttribute("user", user);
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("activePage", "staff_management");
        return "admin/staff-details";
    }

    @PostMapping("/update/{id}")
    @Transactional
    public String updateStaff(@PathVariable(name = "id") Long id, 
                             @RequestParam(name = "fullName") String fullName, 
                             @RequestParam(name = "employeeExternalId") String employeeExternalId,
                             @RequestParam(name = "roleName") String roleName) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        
        user.setFullName(fullName);
        user.setEmployeeExternalId(employeeExternalId);
        
        // Update Roles
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Invalid role name:" + roleName));
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        
        userRepository.save(user);
        return "redirect:/admin/staff/details/" + id + "?updated";
    }

    @PostMapping("/toggle-status/{id}")
    @Transactional
    public String toggleStatus(@PathVariable(name = "id") Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        
        boolean newStatus = !user.isEnabled();
        user.setEnabled(newStatus);
        
        if (!newStatus) {
            user.setDeactivatedAt(LocalDateTime.now());
        } else {
            user.setDeactivatedAt(null);
        }
        
        userRepository.save(user);
        return "redirect:/admin/staff/details/" + id + "?statusChanged";
    }

    @PostMapping("/delete/{id}")
    @Transactional
    public String deleteStaff(@PathVariable(name = "id") Long id) {
        userRepository.softDeleteUserById(id);
        return "redirect:/admin/staff?deleted";
    }
}
