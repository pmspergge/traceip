package com.meli.challenge.traceip.service;

import com.meli.challenge.traceip.dto.CountryInfoDTO;
import com.meli.challenge.traceip.dto.CurrencyInfoDTO;
import com.meli.challenge.traceip.dto.IpInfoDTO;
import com.meli.challenge.traceip.dto.StatisticsDTO;
import com.meli.challenge.traceip.model.IpQuery;
import com.meli.challenge.traceip.model.CountryStatistics;
import com.meli.challenge.traceip.repository.CountryStatisticsRepository;
import com.meli.challenge.traceip.repository.IpQueryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TraceIpService {
    private static final Logger logger = LoggerFactory.getLogger(TraceIpService.class);
    private final RestTemplate restTemplate;
    private final IpQueryRepository ipQueryRepository;
    private final CountryStatisticsRepository countryStatisticsRepository;

    @Value("${fixer.api.url}")
    String fixerApiUrl;

    @Value("${fixer.api.key}")
    String fixerApiKey;

    @Value("${restcountries.api.url}")
    String restCountriesApiUrl;

    @Value("${ipapi.api.url}")
    String ipapiApiUrl;

    @Value("${ipapi.api.key}")
    String ipapiApiKey;


    public TraceIpService(RestTemplate restTemplate, IpQueryRepository ipQueryRepository, CountryStatisticsRepository countryStatisticsRepository) {
        this.restTemplate = restTemplate;
        this.ipQueryRepository = ipQueryRepository;
        this.countryStatisticsRepository = countryStatisticsRepository;
    }

    @Cacheable(value = "ipInfo", key = "#ip", unless = "#result == null")
    public IpInfoDTO getIpInfo(String ip) {
        String apiUrl = ipapiApiUrl + ip + "/json/?access_key=" + ipapiApiKey;
        try {
            ResponseEntity<IpInfoDTO> response = restTemplate.getForEntity(apiUrl, IpInfoDTO.class);
            IpInfoDTO ipInfo = response.getBody();
            if (ipInfo != null) {
                double distance = calculateDistance(-34.603722, -58.381592, ipInfo.getLatitude(), ipInfo.getLongitude());
                ipInfo.setDistanceToBA(distance);

                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                String formattedDate = LocalDateTime.now().format(dateFormatter);
                ipInfo.setFormattedCurrentDate(formattedDate);
            }
            return ipInfo;
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener información de la IP", e);
        }
    }

    public IpQuery saveIpQueryAndTrace(String ip) {
        IpInfoDTO ipInfo = getIpInfo(ip);
        if (ipInfo == null) {
            throw new RuntimeException("Error al obtener la información de la IP, no se puede guardar la consulta.");
        }

        double distance = calculateDistance(-34.603722, -58.381592, ipInfo.getLatitude(), ipInfo.getLongitude());
        ipInfo.setDistanceToBA(distance);

        IpQuery ipQuery = IpQuery.builder()
                .ip(ip)
                .countryName(ipInfo.getCountryName())
                .countryCode(ipInfo.getCountryCode())
                .distanceToBA(distance)
                .queryTime(LocalDateTime.now())
                .build();

        logger.info("Guardando IpQuery: {}", ipQuery);
        ipQueryRepository.save(ipQuery);

        updateCountryStatistics(ipInfo.getCountryCode(), distance);

        return ipQuery;
    }

    public void updateCountryStatistics(String countryCode, double distance) {
        CountryStatistics stats = countryStatisticsRepository.findById(countryCode)
                .orElse(CountryStatistics.builder()
                        .countryCode(countryCode)
                        .totalDistance(0)
                        .invocationCount(0)
                        .build());

        logger.info("Actualizando estadísticas de país para {}: {}", countryCode, stats);

        stats.setTotalDistance(stats.getTotalDistance() + distance);
        stats.setInvocationCount(stats.getInvocationCount() + 1);

        logger.info("Guardando CountryStatistics: {}", stats);
        countryStatisticsRepository.save(stats);
    }

    @Cacheable(value = "countryInfo", key = "#countryName", unless = "#result == null")
    public CountryInfoDTO getCountryInfo(String countryName) {
        String apiUrl = restCountriesApiUrl + "/name/" + countryName;
        try {
            ResponseEntity<CountryInfoDTO[]> response = restTemplate.getForEntity(apiUrl, CountryInfoDTO[].class);
            CountryInfoDTO[] countryInfoArray = response.getBody();
            if (countryInfoArray != null && countryInfoArray.length > 0) {
                CountryInfoDTO countryInfo = countryInfoArray[0];

                List<String> CurrentHours = getCurrentTimes(countryInfo.getTimezones());
                countryInfo.setCurrentTimes(CurrentHours);

                return countryInfo;
            } else {
                throw new RuntimeException("No se encontró información para el país: " + countryName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener la información del país", e);
        }
    }

    @Cacheable(value = "currencyInfo", key = "#currencyCode", unless = "#result == null")
    public CurrencyInfoDTO getCurrencyInfo(String currencyCode) {
        String apiUrl = fixerApiUrl + "?access_key=" + fixerApiKey + "&symbols=" + currencyCode + ",USD,ARS";
        try {
            ResponseEntity<CurrencyInfoDTO> response = restTemplate.getForEntity(apiUrl, CurrencyInfoDTO.class);
            if (response.getBody() != null) {
                CurrencyInfoDTO currencyInfo = response.getBody();
                Map<String, Double> rates = currencyInfo.getRates();

                Double rateLocal = rates.get(currencyCode);
                Double rateUSD = rates.get("USD");
                Double rateARS = rates.get("ARS");

                if (!"USD".equals(currencyCode)) {
                    if (rateLocal != null && rateUSD != null) {
                        double localToUsdRate = rateUSD / rateLocal;
                        currencyInfo.setConversionRateToUSD(localToUsdRate);
                    } else {
                        throw new RuntimeException("No se pudo obtener la tasa de cambio para USD y la moneda local");
                    }
                }

                if (rateLocal != null && rateARS != null) {
                    double localToArsRate = rateARS / rateLocal;
                    currencyInfo.setConversionRateToARS(localToArsRate);
                } else {
                    throw new RuntimeException("No se pudo obtener la tasa de cambio para ARS y la moneda local");
                }

                return currencyInfo;
            } else {
                throw new RuntimeException("No se pudo obtener la tasa de cambio para la moneda: " + currencyCode);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener la información de la moneda", e);
        }
    }




    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radio de la Tierra en km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public List<String> getCurrentTimes(List<String> timezones) {
        List<String> CurrentHours = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss (z)");
        for (String timezone : timezones) {
            ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(timezone));
            CurrentHours.add(zonedDateTime.format(formatter));
        }
        return CurrentHours;
    }

    public StatisticsDTO getStatistics() {
        List<CountryStatistics> statsList = countryStatisticsRepository.findAll();
        if (statsList.isEmpty()) {
            throw new RuntimeException("No se han registrado consultas para generar estadísticas.");
        }

        double totalWeightedDistance = statsList.stream()
                .mapToDouble(stats -> stats.getTotalDistance() * stats.getInvocationCount())
                .sum();

        long totalInvocations = statsList.stream()
                .mapToLong(CountryStatistics::getInvocationCount)
                .sum();

        double avgDistance = totalWeightedDistance / totalInvocations;

        double maxDistance = ipQueryRepository.findMaxDistance();
        double minDistance = ipQueryRepository.findMinDistance();

        return StatisticsDTO.builder()
                .maxDistance(maxDistance)
                .minDistance(minDistance)
                .avgDistance(avgDistance)
                .build();
    }

    public boolean isValidIp(String ip) {
        String ipPattern =
                "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        return ip.matches(ipPattern);
    }

}
