package com.sghcp.controlador;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de prueba: sirve para verificar que el backend
 * del SGHCP está activo y respondiendo peticiones HTTP.
 */
@RestController            // Indica que esta clase expone endpoints REST (responde JSON)
@RequestMapping("/api")    // Prefijo común de todas las rutas de este controlador
public class PingController {

    @GetMapping("/ping")   // Responde a GET http://localhost:8080/api/ping
    public Map<String, String> ping() {
        return Map.of("estado", "ok", "mensaje", "Backend SGHCP activo");
    }
}