# AGENTS.md

## Proyecto: manejo de imagenes y recursos estaticos

Estas reglas sintetizan el material de referencia compartido por el equipo:

- Baeldung: `https://www.baeldung.com/spring-file-upload`
- Apuntes internos sobre imagenes
- Apuntes internos sobre recursos estaticos

El objetivo es evitar implementaciones inconsistentes en uploads, almacenamiento y renderizado de imagenes.

## Reglas de trabajo

### 1. Uploads de imagenes

- Los archivos se suben con requests `multipart/form-data`.
- El mismo request puede incluir campos normales del formulario y el archivo.
- En controllers Spring MVC, los archivos se reciben con `MultipartFile`.
- Si hay varios campos ademas del archivo, se puede usar `@RequestParam` para cada campo o un `@ModelAttribute` que contenga el `MultipartFile`.
- No usar Base64 para transportar o persistir imagenes del proyecto salvo que exista una necesidad excepcional y explicitamente aprobada.

### 2. Configuracion esperada en Spring MVC

- El proyecto debe tener un `multipartResolver` configurado. Hoy existe en [webapp/src/main/java/ar/edu/itba/paw/webapp/config/WebConfig.java](/Users/agkorman/itba/paw/paw-webapp2026a/webapp/src/main/java/ar/edu/itba/paw/webapp/config/WebConfig.java#L45).
- Si se toca la inicializacion del servlet o la configuracion de uploads, mantener compatibilidad con parsing multipart de Servlet 3.
- Los limites de upload deben validarse del lado servidor aunque exista validacion del lado cliente.

### 3. Como guardar imagenes en este proyecto

- Las imagenes subidas por usuarios se deben guardar en la base de datos, no como Base64 embebido en HTML ni como string enorme en tablas de negocio.
- La estructura actual separa el binario en `car_images` y deja metadatos en columnas especificas.
- Mantener el patron actual:
  - entidad de negocio principal en `cars`
  - binario y `content_type` en `car_images`
  - acceso via DAO/servicio/controlador
- Para nuevas features similares, seguir el mismo enfoque: tabla dedicada para binario + endpoint HTTP que entregue los bytes.

### 4. Como exponer imagenes

- Las imagenes guardadas en BD se deben servir con endpoints propios del backend.
- Los `<img>` deben apuntar a URLs del proyecto, no a blobs Base64 incrustados.
- Mantener respuesta con `contentType`, `contentLength` y, cuando aplique, headers de cache.
- En este repo ya existe el patron correcto para imagenes de autos en [webapp/src/main/java/ar/edu/itba/paw/webapp/controller/CarController.java](/Users/agkorman/itba/paw/paw-webapp2026a/webapp/src/main/java/ar/edu/itba/paw/webapp/controller/CarController.java#L166) y [webapp/src/main/java/ar/edu/itba/paw/webapp/controller/CarController.java](/Users/agkorman/itba/paw/paw-webapp2026a/webapp/src/main/java/ar/edu/itba/paw/webapp/controller/CarController.java#L208).
- Cuando una imagen viene de BD, preferir este patron antes que guardar una URL externa en la tabla principal.

### 5. Validaciones obligatorias para imagenes subidas

- Validar que el archivo exista y no este vacio.
- Validar tamano maximo.
- Validar `content-type`.
- Validar que el recurso pertenezca a una entidad existente antes de guardarlo.
- Mantener mensajes de error claros y HTTP status correctos.
- Si se cambia la politica de formatos o tamano, actualizar controller, servicio y documentacion.

### 6. Recursos estaticos

- CSS y JS deben ir en carpetas estaticas del webapp.
- Imagenes estaticas del frontend, como logos, iconos e ilustraciones decorativas, tambien deben vivir en carpetas estaticas del proyecto junto al webapp, no en la base.
- En JSP/tag files se deben referenciar con `c:url`.
- Si se agrega una nueva carpeta estatica, debe exponerse en `WebConfig.addResourceHandlers(...)`.
- El proyecto ya expone `/favicon.ico`, `/css/**` y `/js/**` en [webapp/src/main/java/ar/edu/itba/paw/webapp/config/WebConfig.java](/Users/agkorman/itba/paw/paw-webapp2026a/webapp/src/main/java/ar/edu/itba/paw/webapp/config/WebConfig.java#L31).
- Si agregamos una carpeta como `images/` para assets estaticos, tambien hay que agregar su `ResourceHandler`.

### 7. Distincion importante: imagen estatica vs imagen subida por usuario

- Imagen estatica:
  - vive en el repo
  - se sirve como recurso estatico
  - se referencia con `c:url`
- Imagen subida por usuario:
  - llega via `multipart/form-data`
  - se valida en backend
  - se persiste en BD
  - se sirve por endpoint del controller

No mezclar ambos mecanismos.

### 8. Patron ya presente en el repo

- Upload multipart con `MultipartFile`: [webapp/src/main/java/ar/edu/itba/paw/webapp/controller/CarController.java](/Users/agkorman/itba/paw/paw-webapp2026a/webapp/src/main/java/ar/edu/itba/paw/webapp/controller/CarController.java#L208)
- Persistencia del binario: [services/src/main/java/ar/edu/itba/paw/services/CarServiceImpl.java](/Users/agkorman/itba/paw/paw-webapp2026a/services/src/main/java/ar/edu/itba/paw/services/CarServiceImpl.java#L78)
- Renderizado por URL interna del proyecto: [webapp/src/main/webapp/WEB-INF/tags/car-card.tag](/Users/agkorman/itba/paw/paw-webapp2026a/webapp/src/main/webapp/WEB-INF/tags/car-card.tag#L17)
- Recursos estaticos con `c:url`: [webapp/src/main/webapp/WEB-INF/jsp/cars.jsp](/Users/agkorman/itba/paw/paw-webapp2026a/webapp/src/main/webapp/WEB-INF/jsp/cars.jsp#L14)

### 9. Decision de arquitectura para futuras tareas

- Para nuevas imagenes subidas por usuarios, seguir el esquema actual de BD + endpoint.
- Para assets visuales del sitio, usar recursos estaticos del webapp.
- Evitar introducir nuevas soluciones paralelas para imagenes si no hay una justificacion fuerte.
- No introducir Base64 en JSP, DTOs o tablas como camino rapido.

### 10. Nota sobre codigo existente

- Hay vistas legacy o seed data que todavia usan `imageUrl` externo en algunos lugares, por ejemplo [webapp/src/main/webapp/WEB-INF/jsp/landing.jsp](/Users/agkorman/itba/paw/paw-webapp2026a/webapp/src/main/webapp/WEB-INF/jsp/landing.jsp#L82).
- Eso no define la direccion futura del proyecto.
- Para codigo nuevo, priorizar el patron interno de upload + almacenamiento binario + endpoint propio, o recursos estaticos del repo si la imagen es parte fija de la UI.

### 11. Uso de librerias y utilidades de Spring

- Antes de implementar una funcionalidad nueva de forma manual, verificar si Spring o las librerias ya presentes en el proyecto ofrecen una implementacion adecuada.
- Preferir APIs, helpers y patrones provistos por Spring cuando cubran correctamente el caso de uso, en lugar de duplicar comportamiento a mano.
- Si no existe una implementacion disponible o no encaja con los requisitos del proyecto, documentar brevemente la razon antes de agregar una solucion propia.

### 12. Modularizacion de vistas y componentes

- Siempre que una funcionalidad pueda separarse de forma clara, preferir modularizarla en archivos especificos antes que concentrar demasiado codigo en una sola vista o clase.
- En frontend JSP, priorizar tag files reutilizables para componentes o bloques con comportamiento propio, por ejemplo cards, botones, headers, secciones de perfil, formularios o items de listas.
- Mantener las JSP como composicion de componentes y flujo de pagina; evitar que acumulen markup repetido o logica de presentacion extensa.
- Para CSS y JS, separar estilos y scripts por responsabilidad cuando la funcionalidad lo justifique, reutilizando archivos existentes si el comportamiento es compartido.
- Modularizar sin sobredisenar: crear un nuevo archivo cuando mejore legibilidad, reutilizacion, testeo o mantenimiento, y evitar abstracciones innecesarias para codigo trivial de un solo uso.
