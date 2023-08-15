package com.example.authserver.util;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

public class MessageUtil {
    private static MessageSource messageSource;

    private MessageUtil() {
        throw new UnsupportedOperationException(MessageUtil.getMessage("exception.utils.instantiation"));
    }

    public static void setMessageSource(MessageSource messageSource) {
        MessageUtil.messageSource = messageSource;
    }

    public static String getMessage(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
}
