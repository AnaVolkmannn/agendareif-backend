package com.reif.agenda_api.config;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Traduz as exceções de regra de negócio em respostas com mensagem.
 *
 * Sem isto, elas subiam até o Spring e eram reencaminhadas para /error, que
 * exige autenticação. Como a sessão é STATELESS, esse redirecionamento
 * chegava sem usuário e o Spring Security devolvia 403 de corpo vazio: o
 * front recebia "proibido", sem qualquer pista do que havia acontecido.
 *
 * O corpo usa a chave "message" porque é o que o client.ts do front lê.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /** Dados inválidos ou que violam uma regra (ex.: "Senha atual incorreta."). */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> argumentoInvalido(IllegalArgumentException erro) {
        return resposta(HttpStatus.BAD_REQUEST, erro.getMessage());
    }

    /** Conflito com o estado atual (ex.: horário já ocupado). */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> conflito(IllegalStateException erro) {
        return resposta(HttpStatus.CONFLICT, erro.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> naoEncontrado(EntityNotFoundException erro) {
        return resposta(HttpStatus.NOT_FOUND, erro.getMessage());
    }

    /** Falha de @Valid: junta as mensagens dos campos recusados. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validacao(MethodArgumentNotValidException erro) {
        String mensagem = erro.getBindingResult().getFieldErrors().stream()
                .map(campo -> campo.getDefaultMessage() == null ? campo.getField() : campo.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining(" "));

        return resposta(HttpStatus.BAD_REQUEST, mensagem.isBlank() ? "Dados inválidos." : mensagem);
    }

    private ResponseEntity<Map<String, String>> resposta(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status)
                .body(Map.of("message", mensagem == null ? status.getReasonPhrase() : mensagem));
    }
}
