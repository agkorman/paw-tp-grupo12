package ar.edu.itba.paw.webapp.form;

import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class CarForm {

    @NotBlank(message = "La marca es obligatoria.")
    private String brand;

    @NotBlank(message = "El tipo de carrocería es obligatorio.")
    private String bodyType;

    @NotBlank(message = "El modelo es obligatorio.")
    @Size(max = 120, message = "El modelo debe tener como máximo 120 caracteres.")
    private String model;

    @Email(message = "Ingresá un email válido.")
    @Size(max = 100, message = "El email debe tener como máximo 100 caracteres.")
    private String submitterEmail;

    @NotBlank(message = "La descripción es obligatoria.")
    @Size(max = 1500, message = "La descripción debe tener como máximo 1500 caracteres.")
    private String description;

    private MultipartFile file;

    public String getBrand() {
        return brand;
    }

    public void setBrand(final String brand) {
        this.brand = brand;
    }

    public String getBodyType() {
        return bodyType;
    }

    public void setBodyType(final String bodyType) {
        this.bodyType = bodyType;
    }

    public String getModel() {
        return model;
    }

    public void setModel(final String model) {
        this.model = model;
    }

    public String getSubmitterEmail() {
        return submitterEmail;
    }

    public void setSubmitterEmail(final String submitterEmail) {
        this.submitterEmail = submitterEmail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(final MultipartFile file) {
        this.file = file;
    }
}
