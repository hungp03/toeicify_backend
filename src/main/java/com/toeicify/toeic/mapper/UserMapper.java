package com.toeicify.toeic.mapper;
import com.toeicify.toeic.dto.response.user.AdminUserResponse;
import com.toeicify.toeic.dto.response.user.UserInfoResponse;
import com.toeicify.toeic.dto.response.user.UserUpdateResponse;
import com.toeicify.toeic.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserUpdateResponse toUserUpdateResponse(User user);
    @Mapping(target = "roleId", source = "role.roleId")
    @Mapping(target = "roleName", source = "role.roleName")
    AdminUserResponse toAdminUserResponse(User user);
    @Mapping(target = "roleId", source = "role.roleId")
    @Mapping(target = "roleName", source = "role.roleName")
    UserInfoResponse toUserInfoResponse(User user);
}
