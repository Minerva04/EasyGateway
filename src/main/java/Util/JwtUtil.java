package Util;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import server.ConfigReader;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {
    private static final long EXPIRATION_TIME = 86400000; // 24小时
    private static final String ALGORITHM = "HmacSHA512"; // 使用的算法

    public JwtUtil() {
    }

    // 生成JWT令牌的方法
    public static String generateToken(String secretKet,Map<String, Object> claims) {
        Algorithm algorithm = Algorithm.HMAC512(secretKet);
        long currentTimeMillis = System.currentTimeMillis();
        Date expirationDate = new Date(currentTimeMillis + EXPIRATION_TIME);

        // 使用Claims生成JWT
        JWTCreator.Builder jwtBuilder = JWT.create()
                .withIssuedAt(new Date(currentTimeMillis))
                .withExpiresAt(expirationDate);

        for (Map.Entry<String, Object> entry : claims.entrySet()) {
            if (entry.getValue() instanceof String) {
                jwtBuilder.withClaim(entry.getKey(), (String) entry.getValue());
            } else if (entry.getValue() instanceof Integer) {
                jwtBuilder.withClaim(entry.getKey(), (Integer) entry.getValue());
            } else if (entry.getValue() instanceof Boolean) {
                jwtBuilder.withClaim(entry.getKey(), (Boolean) entry.getValue());
            } else if (entry.getValue() instanceof Long) {
                jwtBuilder.withClaim(entry.getKey(), (Long) entry.getValue());
            } else if (entry.getValue() instanceof Double) {
                jwtBuilder.withClaim(entry.getKey(), (Double) entry.getValue());
            } else if (entry.getValue() instanceof Date) {
                jwtBuilder.withClaim(entry.getKey(), (Date) entry.getValue());
            }
        }
        return jwtBuilder.sign(algorithm);
    }

    // 生成简单的JWT令牌示例
    public static String createToken(String secretKet,String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username); // 添加自定义信息
        return generateToken(secretKet,claims);
    }

    // 验证JWT令牌的方法
    public static boolean validateToken(String secreteKey, String token) {
        Algorithm algorithm = Algorithm.HMAC512(secreteKey);
        DecodedJWT jwt = JWT.require(algorithm)
                .build()
                .verify(token);
        // 检查过期时间
        return !jwt.getExpiresAt().before(new Date());
    }
}


