package tuval.api.web.controller.pruebas;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProfileCheckController {

    @Value("${entorno}")
    private String entorno;

    @CrossOrigin
    @GetMapping("/entorno")
    public String entorno() {
        return "Entorno: " + entorno;
    }

    
}
