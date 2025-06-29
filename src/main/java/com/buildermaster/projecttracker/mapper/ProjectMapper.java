package com.buildermaster.projecttracker.mapper;

import com.buildermaster.projecttracker.dto.response.ProjectResponseDTO;
import com.buildermaster.projecttracker.dto.response.ProjectSummaryDTO;
import com.buildermaster.projecttracker.model.EProjectStatus;
import com.buildermaster.projecttracker.model.ETaskStatus;
import com.buildermaster.projecttracker.model.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    @Mapping(target = "taskCount", expression = "java(project.getTasks().size())")
    @Mapping(target = "completionPercentage", expression = "java(calculateCompletionPercentage(project))")
    @Mapping(target = "isOverdue", expression = "java(isProjectOverdue(project))")
    ProjectSummaryDTO toSummaryDTO(Project project);

    @Mapping(target = "taskCount", expression = "java(project.getTasks().size())")
    @Mapping(target = "completedTaskCount", expression = "java(countCompletedTasks(project))")
    @Mapping(target = "completionPercentage", expression = "java(calculateCompletionPercentage(project))")
    @Mapping(target = "isOverdue", expression = "java(isProjectOverdue(project))")
    ProjectResponseDTO toResponseDTO(Project project);

    @Named("calculateCompletionPercentage")
    default double calculateCompletionPercentage(Project project) {
        int taskCount = project.getTasks().size();
        if (taskCount == 0) return 0.0;
        return (double) countCompletedTasks(project) / taskCount * 100.0;
    }

    @Named("countCompletedTasks")
    default int countCompletedTasks(Project project) {
        return (int) project.getTasks().stream()
                .filter(task -> task.getStatus() == ETaskStatus.COMPLETED)
                .count();
    }

    @Named("isProjectOverdue")
    default boolean isProjectOverdue(Project project) {
        return project.getDeadline().isBefore(LocalDate.now()) &&
                project.getStatus() != EProjectStatus.COMPLETED;
    }
}
