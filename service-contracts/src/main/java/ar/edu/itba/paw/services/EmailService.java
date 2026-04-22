package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.CarRequest;

public interface EmailService {
    void sendNewCarRequestNotification(CarRequest request, String brandName, String bodyTypeName);
    void sendCarApprovedNotification(String recipientEmail, String brandName, String model);
}
