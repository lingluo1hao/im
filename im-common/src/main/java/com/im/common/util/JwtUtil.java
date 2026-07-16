package com.im.common.util;

import com.im.common.config.JwtProperties;
import com.im.common.dto.UserInfoDTO;
import com.im.common.exception.BusinessException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.jsonwebtoken.security.SignatureException;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    private SecretKey getSignKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成token：仅封装核心字段 (已适配 0.12.5 标准)
     */
    public String generateToken(UserInfoDTO userInfo) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + jwtProperties.getExpire());

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userInfo.getId());
        claims.put("username", userInfo.getUsername());
        claims.put("phone", userInfo.getPhone());

        return Jwts.builder()
                .subject(userInfo.getId().toString())
                .claims(claims)            // ✅ 0.12.x 规范：使用 claims() 代替 addClaims()
                .issuedAt(now)
                .expiration(expireDate)
                .signWith(getSignKey())    // ✅ 0.12.x 规范：直接传入 SecretKey
                .compact();
    }

    /**
     * 解析token获取用户ID (已适配 0.12.5 标准)
     */
    public Long getUserId(String token) {
        // ✅ 0.12.x 规范：全面改用 Jwts.parser().verifyWith().build().parseSignedClaims()
        Claims claims = Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("userId", Long.class);
    }

    /**
     * 解析token获取基础用户信息 (已适配 0.12.5 标准)
     */
    public UserInfoDTO getUserInfo(String token) {
        try {
            // ✅ 0.12.x 规范：全面改用 Jwts.parser().verifyWith().build().parseSignedClaims()
            Claims claims = Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            UserInfoDTO userInfo = new UserInfoDTO();
            userInfo.setId(claims.get("userId", Long.class));
            userInfo.setUsername(claims.get("username", String.class));
            userInfo.setPhone(claims.get("phone", String.class));
            return userInfo;
        } catch (ExpiredJwtException e) {
            throw new BusinessException(401, "登录凭证已过期，请重新登录");
        } catch (MalformedJwtException | SignatureException e) {
            throw new BusinessException(401, "非法登录凭证，校验失败");
        } catch (Exception e) {
            throw new BusinessException(401, "登录凭证解析失败，请重新登录");
        }
    }
}
