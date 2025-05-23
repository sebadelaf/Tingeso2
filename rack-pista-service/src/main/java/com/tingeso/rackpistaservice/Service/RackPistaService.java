package com.tingeso.rackpistaservice.Service;

import com.tingeso.rackpistaservice.Entity.ReservaEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate; // Para llamar a M5

import java.util.List;


@Service
public class RackPistaService {


    @Autowired
    private RestTemplate restTemplate; // Para obtener detalles de la reserva de M5
    private final String RESERVAS_SERVICE_URL = "http://localhost:8079/reservas"; // <-- IMPORTANT: Adjust this URL
    public List<ReservaEntity> obtenerTodasLasReservasExternas() {
        String url = RESERVAS_SERVICE_URL + "/todas";

        try {
            // We use ParameterizedTypeReference to correctly deserialize a generic List
            ResponseEntity<List<ReservaEntity>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null, // No request body for GET
                    new ParameterizedTypeReference<List<ReservaEntity>>() {}
            );
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                // Handle non-successful responses (e.g., log error, throw exception)
                System.err.println("Error fetching reservas: " + response.getStatusCode());
                return null; // Or an empty list, or throw a custom exception
            }
        } catch (Exception e) {
            // Handle exceptions during the REST call (e.g., service unavailable)
            System.err.println("Exception while calling /todas endpoint: " + e.getMessage());
            return null; // Or an empty list, or throw a custom exception
        }
    }


}
