package it.project.mail.common;

import java.util.regex.Pattern;

public final class MailAddress {
    private static final Pattern PATTERN = Pattern.compile("^[\\w.-]+@[\\w-]+\\.(com|it)$", Pattern.CASE_INSENSITIVE);

    private MailAddress() {
    }

    public static boolean isValid(String address) {
        return address != null && address.length() <= 25 && PATTERN.matcher(address).matches();
    }
}
