package DevNectar.StaffBridge.CloudNex.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {
    public Role() {}

    public Role(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
