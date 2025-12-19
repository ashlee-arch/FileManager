package com.example.filemanager;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final DocumentRepository documentRepository;

    public HomeController(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        List<Document> docs = documentRepository.findByOwnerUsername(username);
        model.addAttribute("docs", docs);

        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
