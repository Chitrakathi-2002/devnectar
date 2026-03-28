package DevNectar.StaffBridge.devNectar.repository;

import DevNectar.StaffBridge.devNectar.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    java.util.Optional<Role> findByName(String name);
}
