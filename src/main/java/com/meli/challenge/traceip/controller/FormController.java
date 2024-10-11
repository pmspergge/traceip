package com.meli.challenge.traceip.controller;

import com.meli.challenge.traceip.dto.StatisticsDTO;
import com.meli.challenge.traceip.service.TraceIpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.meli.challenge.traceip.dto.CountryInfoDTO;
import com.meli.challenge.traceip.dto.CurrencyInfoDTO;
import com.meli.challenge.traceip.dto.IpInfoDTO;

@Controller
@RequestMapping("/api")
public class FormController {

    private final TraceIpService traceIpService;

    @Autowired
    public FormController(TraceIpService traceIpService) {
        this.traceIpService = traceIpService;
    }

    @GetMapping("/")
    public String showMainPage() {
        return "index";
    }


    @GetMapping("/form")
    public String showForm() {
        return "ipForm";
    }

    @PostMapping("/trace")
    public String traceIp(@RequestParam String ip, Model model) {
        // Validar la IP
        if (!traceIpService.isValidIp(ip)) {
            model.addAttribute("error", "Dirección IP inválida.");
            return "ipForm";
        }

        // Realiza la búsqueda de información y guarda la consulta
        traceIpService.saveIpQueryAndTrace(ip);

        // Obtener la información para mostrar en la vista
        IpInfoDTO ipInfo = traceIpService.getIpInfo(ip);
        CountryInfoDTO countryInfo = traceIpService.getCountryInfo(ipInfo.getCountryName());
        CurrencyInfoDTO currencyInfo = traceIpService.getCurrencyInfo(ipInfo.getCurrencyCode());

        // Agregar la información al modelo
        model.addAttribute("ip", ip);
        model.addAttribute("formattedCurrentDate", ipInfo.getFormattedCurrentDate());
        model.addAttribute("countryName", ipInfo.getCountryName());
        model.addAttribute("isoCode", ipInfo.getCountryCode());
        model.addAttribute("languages", countryInfo.getLanguages().values());
        model.addAttribute("currentTimes", countryInfo.getCurrentTimes());
        model.addAttribute("currency", countryInfo.getCurrencies().values().stream().findFirst().orElse(null));
        model.addAttribute("currencyCode", ipInfo.getCurrencyCode());

        if (currencyInfo.getConversionRateToUSD() != null) {
            model.addAttribute("localToUsdRate", String.format("%.2f", currencyInfo.getConversionRateToUSD()));
        } else {
            model.addAttribute("localToUsdRate", "No disponible");
        }

        if (currencyInfo.getConversionRateToARS() != null) {
            model.addAttribute("localToArsRate", String.format("%.2f", currencyInfo.getConversionRateToARS()));
        } else {
            model.addAttribute("localToArsRate", "No disponible");
        }

        model.addAttribute("distanceToBA", ipInfo.getDistanceToBA());
        model.addAttribute("latitude", ipInfo.getLatitude());
        model.addAttribute("longitude", ipInfo.getLongitude());

        return "result";
    }

    @GetMapping("/statistics")
    public String showStatistics(Model model) {
        StatisticsDTO statistics = traceIpService.getStatistics();
        model.addAttribute("maxDistance", statistics.getMaxDistance());
        model.addAttribute("minDistance", statistics.getMinDistance());
        model.addAttribute("avgDistance", statistics.getAvgDistance());
        return "statistics";
    }
}

