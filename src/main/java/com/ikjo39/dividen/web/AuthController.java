package com.ikjo39.dividen.web;

import com.ikjo39.dividen.model.Auth;
import com.ikjo39.dividen.security.TokenProvider;
import com.ikjo39.dividen.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final MemberService memberService;
	private final TokenProvider tokenProvider;

	@PostMapping("/signup")
	public ResponseEntity<?> signup(@RequestBody Auth.SignUp request) {
		// 회원가입용 API
		var result = this.memberService.register(request);
		return ResponseEntity.ok(result);
	}

	@PostMapping("/signin")
	public ResponseEntity<?> signin(@RequestBody Auth.SignIn request) {
		// 1. 패스워드 검증
		var member = this.memberService.authenticate(request);
		// 2. generate Token으로 생성함
		var token = this.tokenProvider.generateToken(member.getUsername(), member.getRoles());
		log.info("user login -> " + request.getUsername());
		return ResponseEntity.ok(token);
	}

}
