package com.meli.inventorymanagement.infrastructure.security;

import com.meli.inventorymanagement.domain.model.User;
import com.meli.inventorymanagement.infrastructure.adapter.output.persistence.UserRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Aspect
@Component
public class StorePermissionAspect {

    private final UserRepository userRepository;

    public StorePermissionAspect(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Around("@annotation(requireStorePermission)")
    public Object checkStorePermission(ProceedingJoinPoint joinPoint, RequireStorePermission requireStorePermission) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("User not found"));

        // Si el endpoint es solo para admin, verificar que sea admin
        if (requireStorePermission.adminOnly()) {
            if (user.getRole() != User.Role.ADMIN) {
                throw new AccessDeniedException("Admin access required");
            }
            return joinPoint.proceed();
        }

        // Si es admin, puede acceder a todo
        if (user.getRole() == User.Role.ADMIN) {
            return joinPoint.proceed();
        }

        // Para usuarios de tienda, verificar permisos específicos
        Long storeId = extractStoreId(joinPoint, requireStorePermission.storeIdParam());
        if (storeId != null && !userRepository.hasStorePermission(username, storeId)) {
            throw new AccessDeniedException("Access denied to store: " + storeId);
        }

        return joinPoint.proceed();
    }

    private Long extractStoreId(ProceedingJoinPoint joinPoint, String paramName) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);

            if (pathVariable != null) {
                String pathVarName = pathVariable.value().isEmpty() ? parameter.getName() : pathVariable.value();
                if (paramName.equals(pathVarName) && args[i] instanceof Long) {
                    return (Long) args[i];
                }
            }
        }
        return null;
    }
}
