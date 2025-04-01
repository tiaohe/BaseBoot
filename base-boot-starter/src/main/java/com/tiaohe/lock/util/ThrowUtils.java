package com.tiaohe.lock.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

@Slf4j
public class ThrowUtils {
    public static void logInfo(String message, Throwable throwable) {
        log.info("{}: {}", message, getStackTraceAsString(throwable));
    }

    public static void logError(String message, Throwable throwable) {
        log.error("{}: {}", message, getStackTraceAsString(throwable));
    }

    private static String getStackTraceAsString(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
            sb.append(sw);
        } catch (IOException e) {
            log.error("Failed to convert stack trace to string", e);
        }
        return sb.toString();
    }
}
