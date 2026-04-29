package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.services.RecommendationCriteria;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.LinkedHashMap;
import java.util.Map;

public class RecommendationForm {

    @NotBlank(message = "Elegí una opción.")
    @Pattern(regexp = "city|highway|mixed|any", message = "Opción inválida.")
    private String driving = "any";

    @NotBlank(message = "Elegí una opción.")
    @Pattern(regexp = "yes|no|any", message = "Opción inválida.")
    private String firstCar = "any";

    @NotBlank(message = "Elegí una opción.")
    @Pattern(regexp = "very|somewhat|not", message = "Opción inválida.")
    private String fuelEconomy = "not";

    @NotBlank(message = "Elegí una opción.")
    @Pattern(regexp = "very|somewhat|not", message = "Opción inválida.")
    private String comfort = "not";

    @NotBlank(message = "Elegí una opción.")
    @Pattern(regexp = "lot|sometimes|rarely|any", message = "Opción inválida.")
    private String cargo = "any";

    @NotBlank(message = "Elegí una opción.")
    @Pattern(regexp = "very|somewhat|not", message = "Opción inválida.")
    private String performance = "not";

    private String bodyType;

    @Pattern(regexp = "|combustion|hybrid|electric", message = "Tipo de motorización inválido.")
    private String fuelType;

    public RecommendationCriteria toCriteria() {
        final Map<String, String> answers = new LinkedHashMap<>();
        answers.put("driving", driving);
        answers.put("firstCar", firstCar);
        answers.put("fuelEconomy", fuelEconomy);
        answers.put("comfort", comfort);
        answers.put("cargo", cargo);
        answers.put("performance", performance);
        return new RecommendationCriteria(answers, normalizeOptional(bodyType), normalizeOptional(fuelType));
    }

    private String normalizeOptional(final String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    public String getDriving() {
        return driving;
    }

    public void setDriving(final String driving) {
        this.driving = driving;
    }

    public String getFirstCar() {
        return firstCar;
    }

    public void setFirstCar(final String firstCar) {
        this.firstCar = firstCar;
    }

    public String getFuelEconomy() {
        return fuelEconomy;
    }

    public void setFuelEconomy(final String fuelEconomy) {
        this.fuelEconomy = fuelEconomy;
    }

    public String getComfort() {
        return comfort;
    }

    public void setComfort(final String comfort) {
        this.comfort = comfort;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(final String cargo) {
        this.cargo = cargo;
    }

    public String getPerformance() {
        return performance;
    }

    public void setPerformance(final String performance) {
        this.performance = performance;
    }

    public String getBodyType() {
        return bodyType;
    }

    public void setBodyType(final String bodyType) {
        this.bodyType = bodyType;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(final String fuelType) {
        this.fuelType = fuelType;
    }
}
