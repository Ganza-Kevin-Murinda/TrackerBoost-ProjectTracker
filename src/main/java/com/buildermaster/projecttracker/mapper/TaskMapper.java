package com.buildermaster.projecttracker.mapper;

import com.buildermaster.projecttracker.dto.response.TaskResponseDTO;
import com.buildermaster.projecttracker.dto.response.TaskStatsDTO;
import com.buildermaster.projecttracker.dto.response.TaskSummaryDTO;
import com.buildermaster.projecttracker.model.ETaskStatus;
import com.buildermaster.projecttracker.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(target = "projectName", expression = "java(task.getProject() != null ? task.getProject().getName() : null)")
    @Mapping(target = "developerName", expression = "java(task.getDeveloper() != null ? task.getDeveloper().getName() : null)")
    @Mapping(target = "isOverdue", expression = "java(isTaskOverdue(task))")
    @Mapping(target = "daysRemaining", expression = "java(calculateDaysRemaining(task))")
    @Mapping(target = "daysOverdue", expression = "java(calculateDaysOverdue(task))")
    TaskResponseDTO toResponseDTO(Task task);

    @Mapping(target = "projectName", expression = "java(task.getProject() != null ? task.getProject().getName() : null)")
    @Mapping(target = "developerName", expression = "java(task.getDeveloper() != null ? task.getDeveloper().getName() : null)")
    @Mapping(target = "isOverdue", expression = "java(isTaskOverdue(task))")
    TaskSummaryDTO toSummaryDTO(Task task);

    default TaskStatsDTO toStatsDTO(List<Object[]> resultList) {
        if (resultList == null || resultList.isEmpty()) {
            return TaskStatsDTO.builder()
                    .totalTasks(0L)
                    .tasksByStatus(new HashMap<>())
                    .overdueTasks(0L)
                    .unassignedTasks(0L)
                    .build();
        }

        Object[] result = resultList.get(0);
        Map<String, Long> tasksByStatus = new HashMap<>();
        tasksByStatus.put("COMPLETED", ((Number) result[1]).longValue());
        tasksByStatus.put("TODO", ((Number) result[2]).longValue());
        tasksByStatus.put("IN_PROGRESS", ((Number) result[3]).longValue());
        tasksByStatus.put("BLOCKED", ((Number) result[4]).longValue());

        return TaskStatsDTO.builder()
                .totalTasks(((Number) result[0]).longValue())
                .tasksByStatus(tasksByStatus)
                .build();
    }

    @Named("isTaskOverdue")
    default boolean isTaskOverdue(Task task) {
        return task.getDueDate().isBefore(LocalDate.now()) &&
                task.getStatus() != ETaskStatus.COMPLETED;
    }

    @Named("calculateDaysRemaining")
    default long calculateDaysRemaining(Task task) {
        if (isTaskOverdue(task)) return 0L;
        return ChronoUnit.DAYS.between(LocalDate.now(), task.getDueDate());
    }

    @Named("calculateDaysOverdue")
    default long calculateDaysOverdue(Task task) {
        if (!isTaskOverdue(task)) return 0L;
        return ChronoUnit.DAYS.between(task.getDueDate(), LocalDate.now());
    }
}
