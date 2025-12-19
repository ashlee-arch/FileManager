package com.example.filemanager;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class SignupController {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public SignupController(MemberRepository memberRepository,
                            PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/signup")
    public String signupForm() {
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@RequestParam String username,
                         @RequestParam String password) {

        // ✅ 중복 아이디 차단
        if (memberRepository.existsByUsername(username)) {
            return "redirect:/signup?error=duplicate";
        }

        Member m = new Member();
        m.setUsername(username);
        m.setPassword(passwordEncoder.encode(password));
        memberRepository.save(m);

        return "redirect:/login?signup=success";
    }
}
