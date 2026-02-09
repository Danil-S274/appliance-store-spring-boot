package com.danil.appliances.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public String handleNotFound(NotFoundException ex, HttpServletRequest req, Model model) {
        log.warn("NotFound: {} [{} {}]", ex.getMessage(), req.getMethod(), req.getRequestURI());
        model.addAttribute("status", 404);
        model.addAttribute("error", "Not Found");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("path", req.getRequestURI());
        return "errors/error";
    }

    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(BusinessException ex, HttpServletRequest req, Model model) {
        log.info("Business error: {} [{} {}]", ex.getMessage(), req.getMethod(), req.getRequestURI());
        model.addAttribute("status", 400);
        model.addAttribute("error", "Business Error");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("path", req.getRequestURI());
        return "errors/error";
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public String handleValidation(Exception ex, HttpServletRequest req, Model model) {
        log.info("Validation error: {} [{} {}]", ex.getMessage(), req.getMethod(), req.getRequestURI());
        model.addAttribute("status", 400);
        model.addAttribute("error", "Validation Error");
        model.addAttribute("message", "Validation failed");
        model.addAttribute("path", req.getRequestURI());
        return "errors/error";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException ex, HttpServletRequest req, Model model) {
        log.warn("AccessDenied: {} [{} {}]", ex.getMessage(), req.getMethod(), req.getRequestURI());
        model.addAttribute("status", 403);
        model.addAttribute("error", "Forbidden");
        model.addAttribute("message", "Access denied");
        model.addAttribute("path", req.getRequestURI());
        return "errors/error";
    }

    @ExceptionHandler(Exception.class)
    public String handleAny(Exception ex, HttpServletRequest req, Model model) {
        log.error("Unhandled error [{} {}]", req.getMethod(), req.getRequestURI(), ex);
        model.addAttribute("status", 500);
        model.addAttribute("error", "Internal Server Error");
        model.addAttribute("message", "Something went wrong");
        model.addAttribute("path", req.getRequestURI());
        return "errors/error";
    }
}
