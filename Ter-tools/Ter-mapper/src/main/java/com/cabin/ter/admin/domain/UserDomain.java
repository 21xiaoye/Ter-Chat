package com.cabin.ter.admin.domain;



import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;


/**
 * <p>
 *      用户
 * </p>
 *
 * @author xiaoye
 * @date Created in 2024-04-27 15:44
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserDomain implements Serializable {
    public static final Integer SEX_MALE = 1;
    public static final Integer SEX_FEMALE = 0;
    @Serial
    private static final long serialVersionUID = 1L;

    public static Long UID_SYSTEM = 1L;
    /**
     * 主键
     */
    private Long userId;
    /**
     * 用户性别
     */
    private Integer sex;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 头像
     */
    private String userAvatar;
    /**
     * 密码
     */
    @JsonIgnore
    private String userPasswd;
    /**
     * 邮箱
     */
    private String userEmail;
    /**
     * 微信标识
     */
    private String openId;
    /**
     * 角色
     */
    private Integer roleId;
    /**
     * 用户最后上下线时间
     */
    private Long lastOptTime;
    /**
     * 盐值
     */
    @JsonIgnore
    private String salt;
    /**
     * 状态，启用-1，禁用-0
     */
    private Integer userStatus;
    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;
}

