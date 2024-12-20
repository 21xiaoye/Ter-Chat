package com.cabin.ter.vo;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.cabin.ter.admin.domain.PermissionDomain;
import com.cabin.ter.admin.domain.UserDomain;
import com.cabin.ter.config.ConstantPool;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 *     自定义用户主体
 * </p>
 * @author xiaoye
 * @date Created in 2024-04-27 15:42
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements UserDetails, Serializable {
    /**
     * 用户Id
     */
    private Long userId;
    /**
     * 用户名称
     */
    private String userName;
    /**
     * 用户头像
     */
    private String userAvatar;
    /**
     * 最后上下线时间
     */
    private Long lastOptTime;
    /**
     * 邮箱
     */
    @Email
    private String userEmail;
    /**
     * 密码
     */
    @JsonIgnore
    private String userPasswd;
    /**
     * 状态： 启用1 禁止0
     */
    private Integer userStatus;
    /**
     * 盐值
     */
    @JsonIgnore
    private String salt;
    /**
     * 用户角色
     */
    private Integer roleId;
    /**
     * 用户权限列表
     */
    private Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(UserDomain userDomain,  List<PermissionDomain> permissions) {
        List<GrantedAuthority> authorities = null;
        if(Objects.nonNull(permissions)){
            authorities = permissions.stream().filter(permission -> StrUtil.isNotBlank(permission.getPermission()))
                    .map(permission -> new SimpleGrantedAuthority(permission.getPermission())).collect(Collectors.toList());
        }
        UserPrincipal userPrincipal = new UserPrincipal();
        BeanUtil.copyProperties(userDomain, userPrincipal);
        userPrincipal.setAuthorities(authorities);
        return userPrincipal;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return userPasswd;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Objects.equals(this.userStatus, ConstantPool.ENABLE);
    }
}
