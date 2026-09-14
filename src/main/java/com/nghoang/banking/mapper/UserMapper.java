package com.nghoang.banking.mapper;
import com.nghoang.banking.dto.AccountInfo;
import com.nghoang.banking.dto.request.UserRequest;
import com.nghoang.banking.dto.request.UserUpdateRequest;
import com.nghoang.banking.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    User toUser(UserRequest request);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);

    AccountInfo toAccountInfo(User user);
//    UserResponse toUserResponse(User user);
}
