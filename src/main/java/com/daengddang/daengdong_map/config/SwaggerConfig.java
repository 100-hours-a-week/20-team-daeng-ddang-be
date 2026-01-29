package com.daengddang.daengdong_map.config;

import com.daengddang.daengdong_map.common.ErrorCode;
import com.daengddang.daengdong_map.common.api.ErrorCodes;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springdoc.core.customizers.OperationCustomizer;

@Configuration
public class SwaggerConfig {

    private static final ErrorCode[] COMMON_ERROR_CODES = {
            ErrorCode.UNAUTHORIZED,
            ErrorCode.FORBIDDEN,
            ErrorCode.INTERNAL_SERVER_ERROR
    };

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Daeng Dong Map")
                        .description("Daeng Dong Map - Project")
                        .version("v1.0.0")
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT"))
                );
    }

    @Bean
    public OperationCustomizer errorResponseCustomizer() {
        return (operation, handlerMethod) -> {
            Set<ErrorCode> errorCodes = resolveErrorCodes(handlerMethod);
            if (errorCodes.isEmpty()) {
                return operation;
            }

            ApiResponses responses = operation.getResponses();
            if (responses == null) {
                responses = new ApiResponses();
                operation.setResponses(responses);
            }

            for (ErrorCode errorCode : errorCodes) {
                String statusCode = String.valueOf(errorCode.getHttpStatus().value());
                io.swagger.v3.oas.models.responses.ApiResponse response =
                        responses.get(statusCode);
                if (response == null) {
                    response = new io.swagger.v3.oas.models.responses.ApiResponse();
                    responses.addApiResponse(statusCode, response);
                }
                if (response.getDescription() == null) {
                    response.setDescription(errorCode.name());
                }

                Content content = response.getContent();
                if (content == null) {
                    content = new Content();
                    response.setContent(content);
                }

                MediaType mediaType = content.get("application/json");
                if (mediaType == null) {
                    mediaType = new MediaType();
                    content.addMediaType("application/json", mediaType);
                }

                if (mediaType.getExamples() == null
                        || !mediaType.getExamples().containsKey(errorCode.name())) {
                    Example example = new Example();
                    example.setValue(buildErrorExample(errorCode));
                    mediaType.addExamples(errorCode.name(), example);
                }
            }

            return operation;
        };
    }

    private Set<ErrorCode> resolveErrorCodes(HandlerMethod handlerMethod) {
        Set<ErrorCode> codes = new LinkedHashSet<>();
        ErrorCodes methodAnnotation = handlerMethod.getMethodAnnotation(ErrorCodes.class);
        if (methodAnnotation != null) {
            addCodesWithDefaults(codes, methodAnnotation);
            return codes;
        }

        ErrorCodes interfaceAnnotation = findInterfaceMethodAnnotation(handlerMethod);
        if (interfaceAnnotation != null) {
            addCodesWithDefaults(codes, interfaceAnnotation);
            return codes;
        }

        ErrorCodes classAnnotation = handlerMethod.getBeanType().getAnnotation(ErrorCodes.class);
        if (classAnnotation != null) {
            addCodesWithDefaults(codes, classAnnotation);
        }

        return codes;
    }

    private void addCodesWithDefaults(Set<ErrorCode> codes, ErrorCodes annotation) {
        if (annotation.includeCommon()) {
            addCodes(codes, COMMON_ERROR_CODES);
        }
        addCodes(codes, annotation.value());
    }

    private void addCodes(Set<ErrorCode> codes, ErrorCode[] values) {
        if (values == null) {
            return;
        }
        for (ErrorCode value : values) {
            if (value != null) {
                codes.add(value);
            }
        }
    }

    private Map<String, Object> buildErrorExample(ErrorCode errorCode) {
        Map<String, Object> example = new LinkedHashMap<>();
        example.put("message", errorCode.getMessage());
        example.put("data", null);
        example.put("errorCode", errorCode.name());
        return example;
    }

    private ErrorCodes findInterfaceMethodAnnotation(HandlerMethod handlerMethod) {
        Class<?> beanType = handlerMethod.getBeanType();
        String methodName = handlerMethod.getMethod().getName();
        Class<?>[] paramTypes = handlerMethod.getMethod().getParameterTypes();

        for (Class<?> iface : beanType.getInterfaces()) {
            try {
                ErrorCodes annotation = iface.getMethod(methodName, paramTypes)
                        .getAnnotation(ErrorCodes.class);
                if (annotation != null) {
                    return annotation;
                }
            } catch (NoSuchMethodException ignored) {
            }
        }

        return null;
    }
}
