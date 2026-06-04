package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.CarSearchCriteria;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import ar.edu.itba.paw.services.BodyTypeService;
import ar.edu.itba.paw.services.BrandService;
import ar.edu.itba.paw.services.CarFavoriteService;
import ar.edu.itba.paw.services.CarRequestService;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.EmailService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.services.ReviewTagService;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import ar.edu.itba.paw.webapp.controller.support.ControllerTestValidationSupport;
import ar.edu.itba.paw.webapp.util.ImageValidationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CarControllerTest {

    /** Minimal JPEG header bytes that pass {@link ar.edu.itba.paw.webapp.validation.ImageSignatureValidator}. */
    private static final byte[] MINIMAL_JPEG_BYTES =
            new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xDB, 0, 1, 2, 3, 4, 5};

    @Mock
    private CarService carService;

    @Mock
    private CarFavoriteService carFavoriteService;

    @Mock
    private BrandService brandService;

    @Mock
    private BodyTypeService bodyTypeService;

    @Mock
    private ReviewService reviewService;

    @Mock
    private ReviewTagService reviewTagService;

    @Mock
    private EmailService emailService;

    @Mock
    private CarRequestService carRequestService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private ImageValidationService imageValidationService;

    @InjectMocks
    private CarController controller;

    private MockMvc carMockMvc() throws Exception {
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenAnswer(inv -> inv.getArgument(0));
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(
                        new SharedModelAttributesAdvice(brandService, bodyTypeService, reviewTagService))
                .setValidator(
                        ControllerTestValidationSupport.carFormSpringValidator(
                                brandService,
                                bodyTypeService,
                                carService,
                                carRequestService))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    private static void bindPrincipal(final AuthenticatedUser user) {
        final UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    private static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void arrangeFeaturedEmptyCatalogEmpty() {
        when(carService.getFeaturedCars(anyInt())).thenReturn(Collections.emptyList());
        when(carService.searchCars(any(CarSearchCriteria.class)))
                .thenReturn(Page.empty(1, Pagination.CARS_PAGE_SIZE));
        when(reviewService.getReviewStatsByCarIds(any())).thenReturn(Collections.emptyList());
    }

    private void arrangeNewCarFormMocks() {
        when(brandService.findAll()).thenReturn(Collections.emptyList());
        when(bodyTypeService.findAll()).thenReturn(Collections.emptyList());
    }

    @Test
    void home_anonymous_showsLandingPage() throws Exception {
        // Arrange
        arrangeFeaturedEmptyCatalogEmpty();
        final MockMvc mockMvc = carMockMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/"));
        // Assertions
        resultActions.andExpect(status().isOk()).andExpect(view().name("landing.jsp"));
    }

    @Test
    void home_withFeaturedCars_addsHeroCar() throws Exception {
        // Arrange
        final Car hero = aCar(1L);
        when(carService.getFeaturedCars(anyInt())).thenReturn(List.of(hero));
        when(carService.searchCars(any(CarSearchCriteria.class)))
                .thenReturn(Page.empty(1, Pagination.CARS_PAGE_SIZE));
        when(reviewService.getReviewStatsByCarIds(any())).thenReturn(Collections.emptyList());
        when(reviewService.getTopRatedLatestReviewByCar(eq(1L))).thenReturn(Optional.empty());

        final MockMvc mockMvc = carMockMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/"));
        // Assertions
        resultActions.andExpect(status().isOk()).andExpect(model().attributeExists("heroCar"));
    }

    @Test
    void listCars_showsCarsPage() throws Exception {
        // Arrange
        arrangeFeaturedEmptyCatalogEmpty();

        final MockMvc mockMvc = carMockMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/cars"));
        // Assertions
        resultActions.andExpect(status().isOk()).andExpect(view().name("cars.jsp"));
    }

    @Test
    void listCars_withCreateCarParam_redirectsToNew() throws Exception {
        // Arrange
        arrangeFeaturedEmptyCatalogEmpty();

        final MockMvc mockMvc = carMockMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/cars").param("createCar", "true"));
        // Assertions
        resultActions.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/cars/new"));
    }

    @Test
    void newCarRequest_showsCarForm() throws Exception {
        // Arrange
        arrangeNewCarFormMocks();

        final MockMvc mockMvc = carMockMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/cars/new"));
        // Assertions
        resultActions
                .andExpect(status().isOk())
                .andExpect(view().name("car-form.jsp"))
                .andExpect(model().attributeExists("carForm"));
    }

    @Test
    void createCar_anonymous_redirectsToNew() throws Exception {
        // Arrange
        final MockMvc mockMvc = carMockMvc();
        // Exercise
        final ResultActions resultActions =
                mockMvc.perform(
                        multipart("/cars")
                                .file(carImageMultipart())
                                .param("brand", "Toyota")
                                .param("bodyType", "Sedan")
                                .param("model", "Corolla")
                                .param("year", "2020")
                                .param("description", "A sedan description that is valid.")
                                .param("fuelType", CarSearchCriteria.FUEL_TYPE_COMBUSTION)
                                .param("horsepower", "120")
                                .param("airbagCount", "6")
                                .param("transmission", CarSearchCriteria.TRANSMISSION_AUTOMATIC)
                                .param("fuelConsumption", "8.5")
                                .param("maxSpeedKmh", "200"));
        // Assertions
        resultActions.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/cars/new"));
    }

    @Test
    void createCar_validForm_redirectsToCatalog() throws Exception {
        // Arrange
        stubValidCarSubmission();
        bindPrincipal(testUser());
        try {
            final MockMvc mockMvc = carMockMvc();
            // Exercise
            final ResultActions resultActions =
                    mockMvc.perform(
                            multipart("/cars")
                                    .file(carImageMultipart())
                                    .param("brand", "Toyota")
                                    .param("bodyType", "Sedan")
                                    .param("model", "Corolla")
                                    .param("year", "2020")
                                    .param("description", "A sedan description that is valid.")
                                    .param("fuelType", CarSearchCriteria.FUEL_TYPE_COMBUSTION)
                                    .param("horsepower", "120")
                                    .param("airbagCount", "6")
                                    .param("transmission", CarSearchCriteria.TRANSMISSION_AUTOMATIC)
                                    .param("fuelConsumption", "8.5")
                                    .param("maxSpeedKmh", "200"));
            // Assertions
            resultActions
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/cars?submitted=true"));
        } finally {
            clearSecurityContext();
        }
    }

    @Test
    void createCar_validationErrors_showsForm() throws Exception {
        // Arrange
        stubCarFormValidationPartners();
        bindPrincipal(testUser());
        try {
            final MockMvc mockMvc = carMockMvc();
            // Exercise
            final ResultActions resultActions =
                    mockMvc.perform(
                            multipart("/cars")
                                    .file(carImageMultipart())
                                    .param("brand", "Toyota")
                                    .param("bodyType", "Sedan")
                                    .param("year", "2020")
                                    .param("description", "A sedan description that is valid.")
                                    .param("fuelType", CarSearchCriteria.FUEL_TYPE_COMBUSTION)
                                    .param("horsepower", "120")
                                    .param("airbagCount", "6")
                                    .param("transmission", CarSearchCriteria.TRANSMISSION_AUTOMATIC)
                                    .param("fuelConsumption", "8.5")
                                    .param("maxSpeedKmh", "200"));
            // Assertions
            resultActions.andExpect(status().isOk()).andExpect(view().name("car-form.jsp"));
        } finally {
            clearSecurityContext();
        }
    }

    @Test
    void createCar_ignoresSubmittedEditContextForImageValidation() throws Exception {
        // Arrange
        stubCarFormValidationPartners();
        when(carService.getCarById(eq(1L))).thenReturn(Optional.of(aCar(1L)));
        when(carService.getCarImagesByCarId(eq(1L))).thenReturn(List.of(aCarImage(1L)));
        bindPrincipal(testUser());
        try {
            final MockMvc mockMvc = carMockMvc();
            // Exercise
            final ResultActions resultActions =
                    mockMvc.perform(
                            multipart("/cars")
                                    .param("brand", "Toyota")
                                    .param("bodyType", "Sedan")
                                    .param("model", "Corolla")
                                    .param("year", "2020")
                                    .param("description", "A sedan description that is valid.")
                                    .param("fuelType", CarSearchCriteria.FUEL_TYPE_COMBUSTION)
                                    .param("horsepower", "120")
                                    .param("airbagCount", "6")
                                    .param("transmission", CarSearchCriteria.TRANSMISSION_AUTOMATIC)
                                    .param("fuelConsumption", "8.5")
                                    .param("maxSpeedKmh", "200")
                                    .param("formMode", "edit-car")
                                    .param("carId", "1")
                                    .param("retainedImageIds", "1"));
            // Assertions
            resultActions.andExpect(status().isOk()).andExpect(view().name("car-form.jsp"));
        } finally {
            clearSecurityContext();
        }
    }

    @Test
    void updateFavorite_anonymous_browserRequest_redirectsToLogin() throws Exception {
        // Arrange
        final MockMvc mockMvc = carMockMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(post("/cars/1/favorite").param("favorite", "true"));
        // Assertions
        resultActions.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
    }

    @Test
    void updateFavorite_carNotFound_redirectsToCars() throws Exception {
        // Arrange
        when(carService.getCarById(eq(1L))).thenReturn(Optional.empty());
        bindPrincipal(testUser());

        try {
            final MockMvc mockMvc = carMockMvc();
            // Exercise
            final ResultActions resultActions =
                    mockMvc.perform(post("/cars/1/favorite").param("favorite", "true"));
            // Assertions
            resultActions.andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/cars"));
        } finally {
            clearSecurityContext();
        }
    }

    @Test
    void updateFavorite_valid_redirectsToReferer() throws Exception {
        // Arrange
        when(carService.getCarById(eq(1L))).thenReturn(Optional.of(aCar(1L)));
        bindPrincipal(testUser());

        try {
            final MockMvc mockMvc = carMockMvc();
            // Exercise
            final ResultActions resultActions =
                    mockMvc.perform(
                            post("/cars/1/favorite")
                                    .param("favorite", "true")
                                    .header("Referer", "http://localhost/cars"));
            // Assertions
            resultActions.andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/cars"));
        } finally {
            clearSecurityContext();
        }
    }

    @Test
    void getCarImage_notFound_returns404() throws Exception {
        // Arrange
        when(carService.getCarImageByCarId(eq(1L))).thenReturn(Optional.empty());

        final MockMvc mockMvc = carMockMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/cars/1/image"));
        // Assertions
        resultActions.andExpect(status().isNotFound());
    }

    @Test
    void getCarImage_found_returnsImageBytes() throws Exception {
        // Arrange
        final CarImage image = aCarImage(1L);
        when(carService.getCarImageByCarId(eq(1L))).thenReturn(Optional.of(image));

        final MockMvc mockMvc = carMockMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/cars/1/image"));
        // Assertions
        resultActions
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.IMAGE_JPEG_VALUE))
                .andExpect(content().bytes(image.getImageData()));
    }

    @Test
    void getCarImage_etagMatch_returns304() throws Exception {
        // Arrange
        final CarImage image = aCarImage(1L);
        when(carService.getCarImageByCarId(eq(1L))).thenReturn(Optional.of(image));
        final String etag = buildImageEtag(image);

        final MockMvc mockMvc = carMockMvc();
        // Exercise
        final ResultActions resultActions =
                mockMvc.perform(get("/cars/1/image").header("If-None-Match", etag));
        // Assertions
        resultActions.andExpect(status().isNotModified()).andExpect(header().string("ETag", etag));
    }

    private void stubCarFormValidationPartners() {
        final Brand toyota = brand("Toyota", 1L);
        final BodyType sedan = bodyType("Sedan", 1L);
        when(brandService.findByName(eq("Toyota"))).thenReturn(Optional.of(toyota));
        when(bodyTypeService.findByName(eq("Sedan"))).thenReturn(Optional.of(sedan));
        when(carService.existsDuplicateCar(anyString(), anyString(), anyString(), any(), anyLong()))
                .thenReturn(false);
    }

    private void stubValidCarSubmission() {
        stubCarFormValidationPartners();

        final CarRequest created =
                TestModels.carRequest(42L, 1L, "driver@test.com", 1L, 1L, 2020, "Corolla", "desc", null,
                        null, "pending", LocalDateTime.now(), CarSearchCriteria.FUEL_TYPE_COMBUSTION, 120,
                        6, CarSearchCriteria.TRANSMISSION_AUTOMATIC, BigDecimal.valueOf(8.5), 200, null);

        when(carService.requestCarCreation(
                        eq(1L), eq("Corolla"), eq(1L), eq(2020),
                        eq(1L), eq("driver@test.com"),
                        eq(Optional.of("A sedan description that is valid.")),
                        any(),
                        eq("combustion"), eq(120), eq(6),
                        eq("automatic"),
                        eq(BigDecimal.valueOf(8.5)), eq(200), any()))
                .thenReturn(created);
    }

    private static MockMultipartFile carImageMultipart() {
        return new MockMultipartFile("files", "img.jpg", "image/jpeg", MINIMAL_JPEG_BYTES);
    }

    private static Car aCar(final long id) {
        final LocalDateTime now = LocalDateTime.now();
        return TestModels.car(id, 1L, "Toyota", "Corolla", 1L, 2024, "Sedan",
                "desc", now, false, CarSearchCriteria.FUEL_TYPE_COMBUSTION, 120,
                6, CarSearchCriteria.TRANSMISSION_AUTOMATIC, BigDecimal.valueOf(8),
                200, BigDecimal.valueOf(25000));
    }

    private static CarImage aCarImage(final long carId) {
        final LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 2, 12, 0);
        final byte[] data =
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xDA, 0x01};
        return TestModels.carImage(1L, carId, 0, MediaType.IMAGE_JPEG_VALUE, data, updatedAt);
    }

    private static Brand brand(final String name, final long id) {
        final Brand b = new Brand();
        b.setId(id);
        b.setName(name);
        return b;
    }

    private static BodyType bodyType(final String name, final long id) {
        final BodyType bt = new BodyType();
        bt.setId(id);
        bt.setName(name);
        return bt;
    }

    private static String buildImageEtag(final CarImage carImage) throws Exception {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(carImage.getContentType().getBytes(StandardCharsets.UTF_8));
        digest.update(carImage.getImageData());
        digest.update(carImage.getUpdatedAt().toString().getBytes(StandardCharsets.UTF_8));
        return "\"" + HexFormat.of().formatHex(digest.digest()) + "\"";
    }

    private static AuthenticatedUser testUser() {
        return new AuthenticatedUser(
                1L, "driver", "driver@test.com", "pass", Collections.emptyList());
    }
}
