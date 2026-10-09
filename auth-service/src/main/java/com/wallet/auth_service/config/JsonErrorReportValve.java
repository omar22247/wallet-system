package com.wallet.auth_service.config;

import com.wallet.auth_service.dto.response.ApiResponse;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ErrorReportValve;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Writer;
import java.time.Instant;
import java.util.List;

public class JsonErrorReportValve extends ErrorReportValve {

    private final ObjectMapper objectMapper;

    public JsonErrorReportValve(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void report(Request request, Response response, Throwable throwable) {
        int status = response.getStatus();
        if (status < 400 || response.getContentWritten() > 0 || !response.setErrorReported()) {
            return;
        }

        try {
            Writer writer = response.getReporter();
            if (writer == null) {
                return;
            }
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            ApiResponse<Void> body = new ApiResponse<>(
                    false, message(status), null, List.of(), Instant.now(), safePath(request));

            writer.write(objectMapper.writeValueAsString(body));
            response.finishResponse();
        } catch (IOException | RuntimeException ignored) {
        }
    }

    private static String message(int status) {
        HttpStatus resolved = HttpStatus.resolve(status);
        return resolved != null ? resolved.getReasonPhrase() : "Error";
    }

    private static String safePath(Request request) {
        try {
            return request.getRequestURI();
        } catch (RuntimeException e) {
            return null;
        }
    }
}