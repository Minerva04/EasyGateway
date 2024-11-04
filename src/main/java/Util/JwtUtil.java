package Util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

public class JwtUtil {
    private static final String SECRET_KEY = "secret-key";
    private static final String ALGORITHM = "HmacSHA256";

    // 验证 JWT 令牌
    public static boolean validateToken(String token) {

            // 解析 JWT
            Claims claims = Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .parseClaimsJws(token)
                    .getBody();

            // 检查过期时间
            return !claims.getExpiration().before(new Date());
    }

    // 获取签名密钥
    private static SecretKeySpec getSigningKey() {
        return new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
    }

}
