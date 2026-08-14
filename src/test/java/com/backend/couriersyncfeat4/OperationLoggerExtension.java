package com.backend.couriersyncfeat4;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Decorador que loguea el endpoint (@Operation) antes de cada test.
 */
public class OperationLoggerExtension implements BeforeEachCallback {

    private static final Logger log = LoggerFactory.getLogger(OperationLoggerExtension.class);

    @Override
    public void beforeEach(ExtensionContext context) {
        context.getTestMethod()
                .flatMap(method -> Optional.ofNullable(method.getAnnotation(Operation.class)))
                .ifPresent(operation -> log.info(">>> Probando: {}", operation.value()));
    }
}
