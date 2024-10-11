package com.meli.challenge.traceip.service;

import com.meli.challenge.traceip.dto.CountryInfoDTO;
import com.meli.challenge.traceip.dto.CurrencyInfoDTO;
import com.meli.challenge.traceip.dto.IpInfoDTO;
import com.meli.challenge.traceip.dto.StatisticsDTO;
import com.meli.challenge.traceip.model.CountryStatistics;
import com.meli.challenge.traceip.model.IpQuery;
import com.meli.challenge.traceip.repository.CountryStatisticsRepository;
import com.meli.challenge.traceip.repository.IpQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
public class TraceIpServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private IpQueryRepository ipQueryRepository;

    @Mock
    private CountryStatisticsRepository countryStatisticsRepository;

    @InjectMocks
    private TraceIpService traceIpService;

    // Valores por defecto para las URLs y claves API
    private String ipapiApiUrl = "https://ipapi.co/";
    private String ipapiApiKey = "your_ipapi_api_key";
    private String restCountriesApiUrl = "https://restcountries.com/v3.1";
    private String fixerApiUrl = "https://data.fixer.io/api/latest";
    private String fixerApiKey = "your_fixer_api_key";

    @BeforeEach
    public void setUp() {
        // Configurar los valores de las variables de entorno
        traceIpService = new TraceIpService(restTemplate, ipQueryRepository, countryStatisticsRepository);
        traceIpService.fixerApiUrl = fixerApiUrl;
        traceIpService.fixerApiKey = fixerApiKey;
        traceIpService.restCountriesApiUrl = restCountriesApiUrl;
        traceIpService.ipapiApiUrl = ipapiApiUrl;
        traceIpService.ipapiApiKey = ipapiApiKey;
    }

    @Test
    public void testGetIpInfo() {
        // Configurar un objeto IpInfoDTO mock
        IpInfoDTO ipInfoMock = new IpInfoDTO();
        ipInfoMock.setIp("161.185.160.93");
        ipInfoMock.setCountryName("United States");
        ipInfoMock.setLatitude(40.7128);
        ipInfoMock.setLongitude(-74.0060);

        // Simular la respuesta de RestTemplate
        String apiUrl = ipapiApiUrl + "161.185.160.93/json/?access_key=" + ipapiApiKey;
        ResponseEntity<IpInfoDTO> response = new ResponseEntity<>(ipInfoMock, HttpStatus.OK);
        Mockito.when(restTemplate.getForEntity(anyString(), eq(IpInfoDTO.class))).thenReturn(response);

        // Ejecutar el método y validar el resultado
        IpInfoDTO result = traceIpService.getIpInfo("161.185.160.93");

        assertNotNull(result);
        assertEquals("United States", result.getCountryName());
        assertEquals(40.7128, result.getLatitude());
        assertEquals(-74.0060, result.getLongitude());
        assertNotNull(result.getFormattedCurrentDate());
    }

    @Test
    public void testTraceIpAndSave() {
        // Configurar un objeto IpInfoDTO mock
        IpInfoDTO ipInfoMock = new IpInfoDTO();
        ipInfoMock.setIp("161.185.160.93");
        ipInfoMock.setCountryName("United States");
        ipInfoMock.setCountryCode("US");
        ipInfoMock.setLatitude(40.7128);
        ipInfoMock.setLongitude(-74.0060);

        // Simular la respuesta de getIpInfo
        Mockito.doReturn(ipInfoMock).when(traceIpService).getIpInfo("161.185.160.93");

        // Simular el guardado en el repositorio
        Mockito.when(ipQueryRepository.save(any(IpQuery.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Simular el método updateCountryStatistics
        Mockito.doNothing().when(countryStatisticsRepository).save(any());

        // Ejecutar el método traceIpAndSave
        IpQuery result = traceIpService.saveIpQueryAndTrace("161.185.160.93");

        // Verificar que el resultado sea correcto
        assertNotNull(result);
        assertEquals("161.185.160.93", result.getIp());
        assertEquals("United States", result.getCountryName());
    }

    @Test
    public void testGetCountryInfo() {
        // Configurar un objeto CountryInfoDTO mock
        CountryInfoDTO countryInfoMock = CountryInfoDTO.builder()
                .name(new CountryInfoDTO.Name())
                .timezones(List.of("America/New_York"))
                .languages(Map.of("en", "English"))
                .currencies(Map.of("USD", new CountryInfoDTO.Currency()))
                .build();
        countryInfoMock.getName().setCommon("United States");

        // Simular la respuesta de RestTemplate
        String apiUrl = restCountriesApiUrl + "/name/" + "United States";
        ResponseEntity<CountryInfoDTO[]> response = new ResponseEntity<>(new CountryInfoDTO[]{countryInfoMock}, HttpStatus.OK);
        Mockito.when(restTemplate.getForEntity(anyString(), eq(CountryInfoDTO[].class))).thenReturn(response);

        // Ejecutar el método y validar el resultado
        CountryInfoDTO result = traceIpService.getCountryInfo("United States");

        assertNotNull(result);
        assertEquals("United States", result.getName().getCommon());
        assertNotNull(result.getCurrentTimes());
    }

    @Test
    public void testGetCurrencyInfo() {
        // Configurar un objeto CurrencyInfoDTO mock
        CurrencyInfoDTO currencyInfoMock = CurrencyInfoDTO.builder()
                .base("EUR")
                .rates(Map.of(
                        "EUR", 1.0,
                        "USD", 1.2,
                        "ARS", 200.0
                ))
                .build();

        // Simular la respuesta de RestTemplate
        String apiUrl = fixerApiUrl + "?access_key=" + fixerApiKey + "&symbols=EUR,USD,ARS";
        ResponseEntity<CurrencyInfoDTO> response = new ResponseEntity<>(currencyInfoMock, HttpStatus.OK);
        Mockito.when(restTemplate.getForEntity(anyString(), eq(CurrencyInfoDTO.class))).thenReturn(response);

        // Ejecutar el método y validar el resultado
        CurrencyInfoDTO result = traceIpService.getCurrencyInfo("EUR");

        assertNotNull(result);
        assertEquals(1.2 / 1.0, result.getConversionRateToUSD());
        assertEquals(200.0 / 1.0, result.getConversionRateToARS());
    }

    @Test
    public void testGetStatistics() {
        // Configurar datos mock de CountryStatistics
        CountryStatistics stats1 = CountryStatistics.builder()
                .countryCode("US")
                .totalDistance(1000)
                .invocationCount(10)
                .build();

        CountryStatistics stats2 = CountryStatistics.builder()
                .countryCode("AR")
                .totalDistance(500)
                .invocationCount(5)
                .build();

        Mockito.when(countryStatisticsRepository.findAll()).thenReturn(List.of(stats1, stats2));
        Mockito.when(ipQueryRepository.findMaxDistance()).thenReturn(1000.0);
        Mockito.when(ipQueryRepository.findMinDistance()).thenReturn(100.0);

        // Ejecutar el método y validar el resultado
        StatisticsDTO result = traceIpService.getStatistics();

        assertNotNull(result);
        assertEquals(1000.0, result.getMaxDistance());
        assertEquals(100.0, result.getMinDistance());
        double expectedAvgDistance = (stats1.getTotalDistance() + stats2.getTotalDistance()) / (stats1.getInvocationCount() + stats2.getInvocationCount());
        assertEquals(expectedAvgDistance, result.getAvgDistance());
    }

    @Test
    public void testIsValidIp() {
        assertTrue(traceIpService.isValidIp("192.168.1.1"));
        assertTrue(traceIpService.isValidIp("255.255.255.255"));
        assertFalse(traceIpService.isValidIp("256.256.256.256"));
        assertFalse(traceIpService.isValidIp("abc.def.ghi.jkl"));
    }
}
