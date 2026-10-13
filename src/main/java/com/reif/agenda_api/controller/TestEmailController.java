package com.reif.agenda_api.controller;

import com.reif.agenda_api.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller temporário só pra validar a configuração do JavaMailSender.
 * REMOVER depois de confirmar que o envio está funcionando.
 */
@RestController
@RequestMapping("/test")
public class TestEmailController {

    private final EmailService emailService;

    public TestEmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/email")
    public ResponseEntity<String> sendTestEmail(@RequestParam String to) {
        emailService.send(to, "Teste - Reif Beauty Studio",
                "Se você recebeu isso, o JavaMailSender está funcionando!");
        return ResponseEntity.ok("E-mail de teste enviado para " + to);
    }
}