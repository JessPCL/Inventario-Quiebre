package com.deber.inventario.auditoria;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final AuditoriaService auditoria;

    public AuditoriaController(AuditoriaService auditoria) {
        this.auditoria = auditoria;
    }

    @GetMapping
    public List<AuditoriaService.Registro> ultimos(
            @RequestParam(name = "limite", defaultValue = "50") int limite) {
        return auditoria.ultimos(Math.min(Math.max(limite, 1), 200));
    }
}
