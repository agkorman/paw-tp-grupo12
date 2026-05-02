package ar.edu.itba.paw.model;

import java.math.BigDecimal;

public enum HighlightTier {

    FUROR("🔥", "Furor", "review-tag-chip--furor", true),
    DESTACADO("👍", "Destacado", "review-tag-chip--destacado", true),
    MENCIONADO("🙂", "Mencionado", "review-tag-chip--mencionado", true),
    ALERTA_COMUN("⚠️", "Alerta común", "review-tag-chip--alerta", false),
    CUESTIONADO("👎", "Cuestionado", "review-tag-chip--cuestionado", false),
    ALGUNAS_QUEJAS("💬", "Algunas quejas", "review-tag-chip--quejas", false),
    MINOR("", "", "", true);

    private static final BigDecimal HIGH_THRESHOLD = new BigDecimal("0.70");
    private static final BigDecimal MID_THRESHOLD = new BigDecimal("0.45");
    private static final BigDecimal LOW_THRESHOLD = new BigDecimal("0.20");

    private final String emoji;
    private final String labelEs;
    private final String cssClass;
    private final boolean positiveImpact;

    HighlightTier(final String emoji, final String labelEs, final String cssClass, final boolean positiveImpact) {
        this.emoji = emoji;
        this.labelEs = labelEs;
        this.cssClass = cssClass;
        this.positiveImpact = positiveImpact;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getLabelEs() {
        return labelEs;
    }

    public String getCssClass() {
        return cssClass;
    }

    public boolean isPositiveImpact() {
        return positiveImpact;
    }

    public boolean isVisible() {
        return this != MINOR;
    }

    public static HighlightTier from(final BigDecimal frequency, final boolean positiveImpact) {
        if (frequency == null || frequency.compareTo(LOW_THRESHOLD) < 0) {
            return MINOR;
        }
        if (positiveImpact) {
            if (frequency.compareTo(HIGH_THRESHOLD) >= 0) return FUROR;
            if (frequency.compareTo(MID_THRESHOLD) >= 0) return DESTACADO;
            return MENCIONADO;
        }
        if (frequency.compareTo(HIGH_THRESHOLD) >= 0) return ALERTA_COMUN;
        if (frequency.compareTo(MID_THRESHOLD) >= 0) return CUESTIONADO;
        return ALGUNAS_QUEJAS;
    }
}
