package com.buildermaster.projecttracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Project entity representing a project in the system
 */
@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "tasks") // Avoid circular reference in toString
public class Project {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = "Project name is required")
    @Size(min = 3, max = 100, message = "Project name must be between 3 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Project description is required")
    @Size(max = 500, message = "Project description cannot exceed 500 characters")
    @Column(nullable = false, length = 500)
    private String description;

    @NotNull(message = "Project deadline is required")
    @Future(message = "Project deadline must be in the future")
    @Column(nullable = false)
    private LocalDate deadline;

    @NotNull(message = "Project status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EProjectStatus status;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    // Custom constructor without ID and timestamps (for creation)
    public Project(String name, String description, LocalDate deadline, EProjectStatus status) {
        this.name = name;
        this.description = description;
        this.deadline = deadline;
        this.status = status;
        this.tasks = new ArrayList<>();
    }

    // Helper methods for bidirectional relationship
    public void addTask(Task task) {
        tasks.add(task);
        task.setProject(this);
    }

    public void removeTask(Task task) {
        tasks.remove(task);
        task.setProject(null);
    }
}
