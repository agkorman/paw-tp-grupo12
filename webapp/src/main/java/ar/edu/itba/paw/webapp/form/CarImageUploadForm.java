package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.webapp.validation.ValidCarImageUploadForm;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@ValidCarImageUploadForm
public class CarImageUploadForm {

    private Long carId;

    private List<MultipartFile> files = new ArrayList<>();

    public Long getCarId() {
        return carId;
    }

    public void setCarId(final Long carId) {
        this.carId = carId;
    }

    public List<MultipartFile> getFiles() {
        return files;
    }

    public void setFiles(final List<MultipartFile> files) {
        this.files = files == null ? new ArrayList<>() : files;
    }
}
