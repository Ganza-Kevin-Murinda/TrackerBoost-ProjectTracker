package com.buildermaster.projecttracker.mapper;

import com.buildermaster.projecttracker.dto.response.UserResponseDTO;
import com.buildermaster.projecttracker.dto.response.UserSummaryDTO;
import com.buildermaster.projecttracker.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDTO toResponseDTO(User user);
    UserSummaryDTO toSummaryDTO(User user);
}
