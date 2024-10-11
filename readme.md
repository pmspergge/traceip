# Trace IP Application

## Descripción

Aplicación que, dado una dirección IP, proporciona información sobre:

- País de origen
- Idiomas oficiales
- Moneda y cotizaciones
- Distancia estimada a Buenos Aires
- Hora(s) actual(es) en el país
- Estadísticas de consultas anteriores

## Requisitos Previos

- Java 17
- Maven 3.8+
- Docker y Docker Compose

## Instrucciones de Ejecución

### Clonar el Repositorio

```bash
git clone https://github.com/pmspergge/traceip.git
cd traceip
```

### Construir y Ejecutar con Docker Compose

```bash
docker-compose up --build
```

### Acceder a la Aplicación:
- Página principal: http://localhost:8080/
- Formulario de búsqueda: http://localhost:8080/form
- Resultado de búsqueda de dirección IP: http://localhost:8080/form/trace
- Estadísticas: http://localhost:8080/api/statistics

