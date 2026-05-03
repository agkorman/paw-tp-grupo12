package ar.edu.itba.paw.webapp.util;

/**
 * Hardens user-controlled strings before they are written to logs to reduce log forging and broken
 * line-oriented ingestion (embedded newlines, control characters).
 */
public final class LogSanitizer {

    /** Practical upper bound for email length used only for log-field truncation. */
    public static final int MAX_LOG_EMAIL_CODE_POINTS = 254;
    public static final int MAX_LOG_CONTENT_TYPE_CODE_POINTS = 128;
    public static final int MAX_LOG_FILENAME_CODE_POINTS = 255;

    private LogSanitizer() {
        // Utility class.
    }

    /**
     * Replaces ISO control characters and Unicode line separators, then truncates to {@code maxCodePoints}.
     *
     * @return empty string when {@code raw} is null
     */
    public static String forLog(final String raw, final int maxCodePoints) {
        if (raw == null) {
            return "";
        }
        final StringBuilder sb = new StringBuilder(Math.min(raw.length(), maxCodePoints + 8));
        int i = 0;
        int count = 0;
        while (i < raw.length() && count < maxCodePoints) {
            final int cp = raw.codePointAt(i);
            i += Character.charCount(cp);
            count++;
            if (cp == '\n' || cp == '\r' || cp == '\u2028' || cp == '\u2029' || Character.isISOControl(cp)) {
                sb.append('_');
            } else {
                sb.appendCodePoint(cp);
            }
        }
        if (i < raw.length()) {
            sb.append('…');
        }
        return sb.toString();
    }
}
