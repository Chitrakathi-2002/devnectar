package DevNectar.StaffBridge.CloudNex.repository;

import DevNectar.StaffBridge.CloudNex.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    java.util.Optional<User> findByUsername(String username);
    java.util.List<User> findByRoles_Name(String roleName);
    java.util.List<User> findByRegistrationStatus(String status);
    java.util.List<User> findByIsDeletedFalse();
    java.util.List<User> findByFullNameContainingIgnoreCaseAndIsDeletedFalse(String fullName);
    java.util.List<User> findByRoles_NameAndIsDeletedFalse(String roleName);
    long countByRoles_NameAndIsEnabledTrueAndIsDeletedFalse(String roleName);
    
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = 'ROLE_EMPLOYEE' AND u.isEnabled = true AND u.isDeleted = false")
    long countAllActiveEmployees();

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = 'ROLE_INTERN' AND u.isEnabled = true AND u.isDeleted = false")
    long countAllActiveInterns();
    
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("UPDATE User u SET u.isDeleted = true WHERE u.id = :id")
    void softDeleteUserById(Long id);
}
