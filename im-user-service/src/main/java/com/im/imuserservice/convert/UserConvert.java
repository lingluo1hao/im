package com.im.imuserservice.convert;

import com.im.imuserservice.dto.UserDTO;
import com.im.imuserservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserConvert {
    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    UserDTO toDTO(User user);
}