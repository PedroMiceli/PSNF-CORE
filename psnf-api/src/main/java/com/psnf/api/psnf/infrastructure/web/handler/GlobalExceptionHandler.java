package com.psnf.api.psnf.infrastructure.web.handler;

import com.psnf.api.psnf.domain.shared.exception.AcessoNegadoException;
import com.psnf.api.psnf.domain.shared.exception.NaoAutenticadoException;
import com.psnf.api.psnf.domain.shared.exception.RecursoNaoEncontradoException;
import com.psnf.api.psnf.domain.shared.exception.RegraDeNegocioException;
import com.psnf.api.psnf.domain.shared.exception.ServicoExternoIndisponivelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import software.amazon.awssdk.core.exception.SdkException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> erros = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        erro -> erro.getDefaultMessage() == null ? "inválido" : erro.getDefaultMessage(),
                        (msg1, msg2) -> msg1
                ));

        return ResponseEntity.badRequest().body(erros);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleNaoEncontrado(RecursoNaoEncontradoException ex) {
        return erro(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<Map<String, String>> handleRegraDeNegocio(RegraDeNegocioException ex) {
        return erro(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
    }

    @ExceptionHandler(NaoAutenticadoException.class)
    public ResponseEntity<Map<String, String>> handleNaoAutenticado(NaoAutenticadoException ex) {
        return erro(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<Map<String, String>> handleAcessoNegado(AcessoNegadoException ex) {
        return erro(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleArgumentoInvalido(IllegalArgumentException ex) {
        return erro(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, String>> handleRequisicaoMalFormada(Exception ex) {
        logger.warn("Requisição mal formada: {}", ex.getMessage());
        return erro(HttpStatus.BAD_REQUEST, "Requisição inválida: verifique o formato dos campos enviados");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> handleParametroAusente(MissingServletRequestParameterException ex) {
        return erro(HttpStatus.BAD_REQUEST, "Informe o parâmetro '" + ex.getParameterName() + "'");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleIntegridade(DataIntegrityViolationException ex) {
        logger.warn("Violação de integridade", ex);
        return erro(HttpStatus.CONFLICT, "Operação viola uma restrição de integridade dos dados");
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<Map<String, String>> handleServicoIndisponivel(DataAccessResourceFailureException ex) {
        logger.error("Serviço externo indisponível", ex);
        return erro(HttpStatus.SERVICE_UNAVAILABLE, "Serviço temporariamente indisponível, tente novamente mais tarde");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleUploadGrande(MaxUploadSizeExceededException ex) {
        return erro(HttpStatus.CONTENT_TOO_LARGE, "Arquivo muito grande: o limite é 10 MB");
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Map<String, String>> handleSemArquivo(MissingServletRequestPartException ex) {
        return erro(HttpStatus.BAD_REQUEST, "Envie o arquivo no campo '" + ex.getRequestPartName() + "' (multipart/form-data)");
    }

    @ExceptionHandler(SdkException.class)
    public ResponseEntity<Map<String, String>> handleStorage(SdkException ex) {
        logger.error("Falha no storage de arquivos (MinIO/S3)", ex);
        return erro(HttpStatus.SERVICE_UNAVAILABLE, "Armazenamento de arquivos indisponível. O MinIO está rodando?");
    }

    @ExceptionHandler(ServicoExternoIndisponivelException.class)
    public ResponseEntity<Map<String, String>> handleServicoExterno(ServicoExternoIndisponivelException ex) {
        return erro(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    private ResponseEntity<Map<String, String>> erro(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(Map.of("erro", mensagem == null ? status.getReasonPhrase() : mensagem));
    }
}
