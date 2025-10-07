package com.meli.inventorymanagement.infrastructure.security;

import com.meli.inventorymanagement.common.constant.ErrorCode;
import com.meli.inventorymanagement.domain.exception.BusinessException;
import com.meli.inventorymanagement.domain.port.AuthenticationPort;
import com.meli.inventorymanagement.domain.port.UserPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
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

    private final UserPort userPort;
    private final AuthenticationPort authenticationPort;

    @Around("@annotation(requireStorePermission)")
    public Object checkStorePermission(ProceedingJoinPoint joinPoint, RequireStorePermission requireStorePermission) {

        Long storeId = extractStoreId(joinPoint, requireStorePermission.storeIdParam());

        log.debug("Store permission check - storeId: {}, adminOnly: {}", storeId, requireStorePermission.adminOnly());

        // Verificación de permisos que se ejecutará primero
        Mono<Void> permissionCheck = ReactiveSecurityContextHolder.getContext()
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.USER_NOT_AUTHENTICATED, "User not authenticated")))
                .flatMap(securityContext -> {
                    if (securityContext.getAuthentication() == null) {
                        return Mono.error(new BusinessException(ErrorCode.USER_NOT_AUTHENTICATED, "User not authenticated"));
                    }

                    String username = securityContext.getAuthentication().getName();
                    log.debug("Checking permissions for user: {}", username);

                    return userPort.findByUsername(username)
                            .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.USER_NOT_FOUND, "User not found: " + username)))
                            .flatMap(user -> {
                                log.debug("User found: {} with role: {}", username, user.getRole());

                                // Si el endpoint es solo para admin, verificar que sea admin
                                if (requireStorePermission.adminOnly()) {
                                    if (!"ADMIN".equals(user.getRole())) {
                                        log.warn("User {} attempted admin-only operation without admin role", username);
                                        return Mono.error(new BusinessException(
                                                ErrorCode.ADMIN_ACCESS_REQUIRED,
                                                "User " + username + " does not have admin privileges"));
                                    }
                                    log.debug("Admin access granted for user: {}", username);
                                    return Mono.empty();
                                }

                                // Si es admin, puede acceder a todo
                                if ("ADMIN".equals(user.getRole())) {
                                    log.debug("Admin user {} granted access", username);
                                    return Mono.empty();
                                }

                                // Para usuarios de tienda, verificar permisos específicos
                                if (storeId != null) {
                                    log.debug("Checking store permission for user {} and store {}", username, storeId);
                                    return authenticationPort.hasStorePermission(username, storeId)
                                            .flatMap(hasPermission -> {
                                                log.debug("Store permission result for user {} and store {}: {}",
                                                        username, storeId, hasPermission);
                                                if (!hasPermission) {
                                                    log.warn("User {} denied access to store {}", username, storeId);
                                                    return Mono.error(new BusinessException(
                                                            ErrorCode.ACCESS_DENIED_TO_STORE,
                                                            "User " + username + " does not have permission for store " + storeId));
                                                }
                                                return Mono.empty();
                                            });
                                }

                                log.warn("No storeId found in request for non-admin user: {}", username);
                                return Mono.error(new BusinessException(ErrorCode.INVALID_REQUEST, "Store ID is required"));
                            });
                })
                .then();

        // Determinar el tipo de retorno del método
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> returnType = signature.getReturnType();

        // Defer la ejecución del método hasta después de la verificación de permisos
        if (Mono.class.isAssignableFrom(returnType)) {
            return permissionCheck.then(Mono.defer(() -> {
                try {
                    log.debug("Executing controller method after permission check passed");
                    return (Mono<?>) joinPoint.proceed();
                } catch (Throwable e) {
                    log.error("Error executing controller method: {}", e.getMessage(), e);
                    return Mono.error(e);
                }
            }));
        } else if (Flux.class.isAssignableFrom(returnType)) {
            return permissionCheck.thenMany(Flux.defer(() -> {
                try {
                    log.debug("Executing controller method (Flux) after permission check passed");
                    return (Flux<?>) joinPoint.proceed();
                } catch (Throwable e) {
                    log.error("Error executing controller method: {}", e.getMessage(), e);
                    return Flux.error(e);
                }
            }));
        } else {
            // Para otros tipos de retorno
            return permissionCheck.then(Mono.defer(() -> {
                try {
                    log.debug("Executing controller method (other) after permission check passed");
                    return Mono.just(joinPoint.proceed());
                } catch (Throwable e) {
                    log.error("Error executing controller method: {}", e.getMessage(), e);
                    return Mono.error(e);
                }
            }));
        }
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
                String pathVarName = pathVariable.value().isEmpty() ? pathVariable.name() : pathVariable.value();
                if (pathVarName.isEmpty()) {
                    pathVarName = parameter.getName();
                }

                if (paramName.equals(pathVarName) && args[i] instanceof Long) {
                    return (Long) args[i];
                }
            }
        }

        log.warn("Could not extract storeId from method parameters");
        return null;
    }
}
