package com.meli.inventorymanagement.infrastructure.security;

import com.meli.inventorymanagement.infrastructure.adapter.output.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class StorePermissionAspect {

    private final UserRepository userRepository;

    @Around("@annotation(requireStorePermission)")
    public Object checkStorePermission(ProceedingJoinPoint joinPoint, RequireStorePermission requireStorePermission) throws Throwable {

        Long storeId = extractStoreId(joinPoint, requireStorePermission.storeIdParam());

        Mono<Void> permissionCheck = ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> {
                    String username = securityContext.getAuthentication().getName();

                    return userRepository.findByUsername(username)
                            .switchIfEmpty(Mono.error(new AccessDeniedException("User not found")))
                            .flatMap(user -> {
                                // Si el endpoint es solo para admin, verificar que sea admin
                                if (requireStorePermission.adminOnly()) {
                                    if (!"ADMIN".equals(user.getRole())) {
                                        return Mono.error(new AccessDeniedException("Admin access required"));
                                    }
                                    return Mono.empty();
                                }

                                // Si es admin, puede acceder a todo
                                if ("ADMIN".equals(user.getRole())) {
                                    return Mono.empty();
                                }

                                // Para usuarios de tienda, verificar permisos específicos
                                if (storeId != null) {
                                    return userRepository.hasStorePermission(username, storeId)
                                            .flatMap(hasPermission -> {
                                                if (!hasPermission) {
                                                    return Mono.error(new AccessDeniedException("Access denied to store: " + storeId));
                                                }
                                                return Mono.empty();
                                            });
                                }

                                return Mono.empty();
                            });
                })
                .then()
                .onErrorResume(ex -> {
                    if (ex instanceof AccessDeniedException) {
                        return Mono.error(ex);
                    }
                    return Mono.error(new AccessDeniedException("Permission check failed: " + ex.getMessage()));
                });

        Object result = joinPoint.proceed();

        if (result instanceof Mono) {
            return permissionCheck.then((Mono<?>) result);
        } else if (result instanceof Flux) {
            return permissionCheck.thenMany((Flux<?>) result);
        } else {
            return permissionCheck.then(Mono.fromCallable(() -> result));
        }
    }

    private Long extractStoreId(ProceedingJoinPoint joinPoint, String paramName) {
        if (paramName == null || paramName.isEmpty()) {
            return null;
        }

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
