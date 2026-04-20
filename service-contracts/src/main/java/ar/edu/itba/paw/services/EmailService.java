package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarRequest;

public interface EmailService {
    void sendCarCreatedNotification(Car car);

    void sendCarRequestCreatedNotification(CarRequest carRequest);
}
