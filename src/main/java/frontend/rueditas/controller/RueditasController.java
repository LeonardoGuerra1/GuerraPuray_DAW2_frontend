package frontend.rueditas.controller;

import frontend.rueditas.dto.RequestPlacaDTO;
import frontend.rueditas.dto.ResponsePlacaDTO;
import frontend.rueditas.model.Auto;
import frontend.rueditas.model.PlacaModel;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/app")
public class RueditasController {

    @Autowired
    RestTemplate restTemplate;

    @GetMapping("/home")
    public String home(Model model) {
        PlacaModel placaModel = new PlacaModel("00", "", null);
        model.addAttribute("placaModel", placaModel);
        return "home";
    }

    @PostMapping("/buscar")
    public String buscar(@RequestParam String placa, Model model) {
        if (StringUtils.isBlank(placa) || placa.trim().length() != 8) {
            PlacaModel placaModel = new PlacaModel("01", "Debe ingresar una placa correcta.", null);
            model.addAttribute("placaModel", placaModel);
            return "home";
        }

        try {
            String endpoint = "http://localhost:8081/app/buscar";
            RequestPlacaDTO request = new RequestPlacaDTO(placa);
            ResponsePlacaDTO response = restTemplate.postForObject(endpoint, request, ResponsePlacaDTO.class);

            if (response.id() == -1) { //AUTO NO ENCONTRADO
                PlacaModel placaModel = new PlacaModel("02", "No se encontró un vehículo para la placa ingresada.", null);
                model.addAttribute("placaModel", placaModel);
                return "resultado";
            }
            else if (response.id() == -99) { //ERROR INTERNO DESDE BACKEND
                PlacaModel placaModel = new PlacaModel("99", "ERROR INTERNO BACKEND CAUSA.", null);
                model.addAttribute("placaModel", placaModel);
                return "resultado";
            }
            else { //AUTO ENCONTRADO
                Auto auto = new Auto(response.id(), response.placa(), response.marca(), response.modelo(), response.nroAsientos(), response.precio(), response.color());
                PlacaModel placaModel = new PlacaModel("00", "", auto);
                model.addAttribute("placaModel", placaModel);
                return "resultado";
            }
        } catch (Exception e) {
            //throw new RuntimeException(e);
            //CATCH EXCEPTION
            PlacaModel placaModel = new PlacaModel("99", "ERROR INTERNO.", null);
            model.addAttribute("placaModel", placaModel);
            return "home";
        }
    }
}
