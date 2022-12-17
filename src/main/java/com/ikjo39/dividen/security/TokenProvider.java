package com.ikjo39.dividen.security;

import com.ikjo39.dividen.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class TokenProvider {

	private static final long TOKEN_EXPIRED_TIME = 1000 * 60 * 60; // 1hours
	private static final String KEY_ROLES = "roles";
	private final MemberService memberService;
	@Value("{spring.jwt.secret}")
	private String secretKey;

	/**
	 * 토큰 생성 메서드
	 */
	public String generateToken(String username, List<String> roles) {
		Claims claims = Jwts.claims().setSubject(username);
		claims.put(KEY_ROLES, roles);

		var now = new Date();
		var expiredDate = new Date(now.getTime() + TOKEN_EXPIRED_TIME);

		return Jwts.builder()
			.setClaims(claims)
			.setIssuedAt(now)
			.setExpiration(expiredDate)
			.signWith(SignatureAlgorithm.HS512, this.secretKey) // 사용할 암호화 알고리즘, 비밀키
			.compact();
	}

	// jwt토큰으로 부터 인증정보를 가져옴
	public Authentication getAuthentication(String jwt) {
		UserDetails userDetails = this.memberService.loadUserByUsername(this.getUsername(jwt));
		return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
	}


	public String getUsername(String token) {
		return this.parseClaims(token).getSubject();
	}

	public boolean validateToken(String token) {
		if (!StringUtils.hasText(token)) {
			return false;
		}

		var claims = this.parseClaims(token);
		return !claims.getExpiration().before(new Date());
	}

	private Claims parseClaims(String token) {
		// claim 정보 가져오기
		try {
			return Jwts.parser().setSigningKey(this.secretKey).parseClaimsJws(token).getBody();
		} catch (ExpiredJwtException e) {
			// TODO
			return e.getClaims();
		}
	}
}
