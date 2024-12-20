package com.cabin.ter.security;

import cn.hutool.core.util.StrUtil;
import com.cabin.ter.admin.domain.PermissionDomain;
import com.cabin.ter.admin.domain.UserDomain;
import com.cabin.ter.admin.mapper.PermissionDomainMapper;
import com.cabin.ter.cache.UserInfoCache;
import com.cabin.ter.constants.dto.RequestInfoDTO;
import com.cabin.ter.exception.SecurityException;
import com.cabin.ter.util.JwtUtil;
import com.cabin.ter.constants.enums.Status;
import com.cabin.ter.util.RequestHolderUtil;
import com.cabin.ter.util.ResponseUtil;
import com.cabin.ter.vo.JwtPrincipal;
import com.cabin.ter.vo.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 *     JWT 认证过滤器
 * </p>
 * @author xiaoye
 * @date Created in 2024-04-27 13:54
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final static String AUTH_HEADER = "Authorization";
    private final static String AUTH_HEADER_TYPE = "Bearer";

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserInfoCache userInfoCache;
    @Autowired
    private PermissionDomainMapper permissionDomainMapper;
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(AUTH_HEADER);
        if (Objects.isNull(authHeader) || !authHeader.startsWith(AUTH_HEADER_TYPE)){
            filterChain.doFilter(request,response);
            return;
        }
        String jwt = jwtUtil.getJwtFromRequest(request);

        if (StrUtil.isNotBlank(jwt)) {
            try {
                JwtPrincipal jwtInfo = jwtUtil.getJwtInfo(jwt);

                RequestInfoDTO requestInfoDTO = RequestInfoDTO.builder()
                        .ip(request.getRemoteAddr())
                        .userId(jwtInfo.getId()).build();

                RequestHolderUtil.set(requestInfoDTO);
                UserDomain userDomain = userInfoCache.getUserInfoBatch(jwtInfo.getSubject());

                Integer roleId = userDomain.getRoleId();

                // 根据角色Id查询用户权限
                List<Long> permissionIds = permissionDomainMapper.findPermissionIdsByRoleId(roleId);
                List<PermissionDomain> permissions = permissionDomainMapper.selectPermissionsByPermissionIds(permissionIds);
                UserPrincipal userPrincipal = UserPrincipal.create(userDomain, permissions);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, null);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                filterChain.doFilter(request, response);
            } catch (SecurityException e) {
                ResponseUtil.renderJson(response, e);
            }
        } else {
            ResponseUtil.renderJson(response, Status.UNAUTHORIZED, null);
        }
    }
}
