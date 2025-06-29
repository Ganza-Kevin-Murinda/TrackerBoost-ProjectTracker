package com.buildermaster.projecttracker.mapper;

import com.buildermaster.projecttracker.dto.response.DeveloperResponseDTO;
import com.buildermaster.projecttracker.dto.response.DeveloperSummaryDTO;
import com.buildermaster.projecttracker.model.Developer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface DeveloperMapper {

    @Mapping(target = "totalTaskCount", expression = "java(developer.getTasks() != null ? developer.getTasks().size() : 0)")
    DeveloperResponseDTO toResponseDTO(Developer developer);

    @Mapping(target = "primarySkills", expression = "java(truncateSkills(developer.getSkills()))")
    @Mapping(target = "totalTaskCount", expression = "java(developer.getTasks() != null ? developer.getTasks().size() : 0)")
    DeveloperSummaryDTO toSummaryDTO(Developer developer);

    @Named("truncateSkills")
    default String truncateSkills(String skills) {
        if (skills != null && skills.length() > 100) {
            return skills.substring(0, 100) + "...";
        }
        return skills;
    }
}
