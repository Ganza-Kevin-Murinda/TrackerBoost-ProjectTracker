package com.buildermaster.projecttracker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Developer entity representing a developer in the system
 */
@Entity
@Table(
        name = "developers",
        uniqueConstraints = {
        @UniqueConstraint(columnNames = "email", name = "uk_developer_email")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "tasks") // Avoid circular reference in toString
public class Developer {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = "Developer name is required")
    @Size(min = 2, max = 100, message = "Developer name must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "Skills are required")
    @Size(max = 500, message = "Skills cannot exceed 500 characters")
    @Column(nullable = false, length = 500)
    private String skills;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;

    @OneToOne(optional = false, fetch = FetchType.LAZY) // Every developer must be a user
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true)
    private User user;

    @OneToMany(mappedBy = "developer", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<Task> tasks = new ArrayList<>();

    // Custom constructor without ID and timestamps (for creation)
    public Developer(String name, String email, String skills, User user) {
        this.name = name;
        this.email = email;
        this.skills = skills;
        this.user = user;
        this.tasks = new ArrayList<>();
    }

    // Helper methods for bidirectional relationship
    public void addTask(Task task) {
        tasks.add(task);
        task.setDeveloper(this);
    }

    public void removeTask(Task task) {
        tasks.remove(task);
        task.setDeveloper(null);
    }
}
