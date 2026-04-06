package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;

public interface EmailService {
    void sendCarCreatedNotification(Car car);
}
