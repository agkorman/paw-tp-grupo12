package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.CarRequestImage;
import ar.edu.itba.paw.model.CarSearchCriteria;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import ar.edu.itba.paw.services.AdminRequestService;
import ar.edu.itba.paw.services.BodyTypeRequestService;
import ar.edu.itba.paw.services.BodyTypeService;
import ar.edu.itba.paw.services.BrandRequestService;
import ar.edu.itba.paw.services.BrandService;
import ar.edu.itba.paw.services.CarRequestService;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.ReviewTagService;
import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.webapp.auth.AuthenticatedSessionRegistry;
import ar.edu.itba.paw.webapp.controller.support.ControllerTestValidationSupport;
import ar.edu.itba.paw.webapp.util.ImageValidationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminControllerTest {

    /** Minimal JPEG header bytes compatible with catalog image validation flows. */
    private static final byte[] MINIMAL_JPEG_BYTES =
            new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xDB, 0, 1, 2, 3, 4, 5};

    @Mock
    private CarRequestService carRequestService;

    @Mock
    private CarService carService;

    @Mock
    private BrandService brandService;

    @Mock
    private BodyTypeService bodyTypeService;

    @Mock
    private BrandRequestService brandRequestService;

    @Mock
    private BodyTypeRequestService bodyTypeRequestService;

    @Mock
    private AdminRequestService adminRequestService;

    @Mock
    private UserService userService;

    @Mock
    private ReviewTagService reviewTagService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private ImageValidationService imageValidationService;

    private final RecordingAuthenticatedSessionRegistry authenticatedSessionRegistry =
            new RecordingAuthenticatedSessionRegistry();

    private MockMvc adminMvc() throws Exception {
        when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenAnswer(inv -> inv.getArgument(0));
        final AdminController controller = new AdminController(
                carRequestService,
                carService,
                brandService,
                bodyTypeService,
                brandRequestService,
                bodyTypeRequestService,
                adminRequestService,
                userService,
                messageSource,
                imageValidationService,
                authenticatedSessionRegistry);
        return MockMvcBuilders.standaloneSetup(controller)
                .setValidator(
                        ControllerTestValidationSupport.carFormSpringValidator(
                                brandService,
                                bodyTypeService,
                                carService,
                                carRequestService))
                .setControllerAdvice(new SharedModelAttributesAdvice(brandService, bodyTypeService, reviewTagService))
                .build();
    }

    private Brand toyotaBrand() {
        final Brand b = new Brand();
        b.setId(1L);
        b.setName("Toyota");
        return b;
    }

    private BodyType sedanBody() {
        final BodyType bt = new BodyType();
        bt.setId(1L);
        bt.setName("Sedan");
        return bt;
    }

    private void arrangeDashboardDefaults() throws Exception {
        when(brandService.findAll()).thenReturn(Collections.singletonList(toyotaBrand()));
        when(bodyTypeService.findAll()).thenReturn(Collections.singletonList(sedanBody()));
        when(reviewTagService.getAllGroupedBySentiment()).thenReturn(Collections.emptyMap());

        when(carRequestService.countCarRequestsByStatus(eq(CarRequestService.STATUS_PENDING))).thenReturn(0L);
        when(brandRequestService.countBrandRequestsByStatus(eq(BrandRequestService.STATUS_PENDING))).thenReturn(0L);
        when(bodyTypeRequestService.countBodyTypeRequestsByStatus(eq(BodyTypeRequestService.STATUS_PENDING)))
                .thenReturn(0L);
        when(adminRequestService.countAdminRequestsByStatus(eq(AdminRequestService.STATUS_PENDING))).thenReturn(0L);

        when(carRequestService.getCarRequestsByStatus(eq(CarRequestService.STATUS_PENDING), anyInt()))
                .thenAnswer(invocation -> Page.empty(invocation.getArgument(1), Pagination.REQUESTS_PAGE_SIZE));
        when(brandRequestService.getBrandRequestsByStatus(eq(BrandRequestService.STATUS_PENDING), anyInt()))
                .thenAnswer(invocation -> Page.empty(invocation.getArgument(1), Pagination.REQUESTS_PAGE_SIZE));
        when(bodyTypeRequestService.getBodyTypeRequestsByStatus(eq(BodyTypeRequestService.STATUS_PENDING), anyInt()))
                .thenAnswer(invocation -> Page.empty(invocation.getArgument(1), Pagination.REQUESTS_PAGE_SIZE));
        when(adminRequestService.getAdminRequestsByStatus(eq(AdminRequestService.STATUS_PENDING), anyInt()))
                .thenAnswer(invocation -> Page.empty(invocation.getArgument(1), Pagination.REQUESTS_PAGE_SIZE));

        arrangeCarFormBrandBodyLookups();
    }

    private void arrangeDashboardDefaultsSimple() throws Exception {
        arrangeDashboardDefaults();
    }

    /** Wire {@code findByName}/{@code findById} for typical approval/edit forms. */
    private void arrangeCarFormBrandBodyLookups() {
        final Brand tb = toyotaBrand();
        final BodyType sed = sedanBody();
        when(brandService.findByName(eq("Toyota"))).thenReturn(Optional.of(tb));
        when(bodyTypeService.findByName(eq("Sedan"))).thenReturn(Optional.of(sed));
        when(brandService.findById(eq(1L))).thenReturn(Optional.of(tb));
        when(bodyTypeService.findById(eq(1L))).thenReturn(Optional.of(sed));
    }

    private CarRequest pendingRequest(final long id) {
        return TestModels.carRequest(
                id,
                5L,
                "sub@test.com",
                1L,
                1L,
                2020,
                "Corolla",
                "Valid description length for reviewer.",
                null,
                null,
                CarRequestService.STATUS_PENDING,
                LocalDateTime.now(),
                CarSearchCriteria.FUEL_TYPE_COMBUSTION,
                120,
                6,
                CarSearchCriteria.TRANSMISSION_AUTOMATIC,
                BigDecimal.valueOf(8.5),
                200,
                null);
    }

    private MockMultipartFile carImagePart() {
        return new MockMultipartFile("files", "img.jpg", "image/jpeg", MINIMAL_JPEG_BYTES);
    }

    @Test
    void admin_get_defaultTabLoads() throws Exception {
        // Arrange
        arrangeDashboardDefaultsSimple();
        final MockMvc mockMvc = adminMvc();
        // Exercise
        final ResultActions resultActions =
                mockMvc.perform(get("/admin").param("page", "2"));
        // Assertions
        resultActions.andExpect(status().isOk()).andExpect(view().name("admin.jsp"));
    }

    @Test
    void admin_get_brandsTabLoads() throws Exception {
        // Arrange
        arrangeDashboardDefaultsSimple();
        final MockMvc mockMvc = adminMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/admin").param("tab", "brands"));
        // Assertions
        resultActions.andExpect(status().isOk()).andExpect(view().name("admin.jsp"));
    }

    @Test
    void admin_get_bodyTypesTabLoads() throws Exception {
        // Arrange
        arrangeDashboardDefaultsSimple();
        final MockMvc mockMvc = adminMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/admin").param("tab", "body-types"));
        // Assertions
        resultActions.andExpect(status().isOk()).andExpect(view().name("admin.jsp"));
    }

    @Test
    void admin_get_moderatorsTabLoads() throws Exception {
        // Arrange
        arrangeDashboardDefaultsSimple();
        final MockMvc mockMvc = adminMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/admin").param("tab", "moderators"));
        // Assertions
        resultActions.andExpect(status().isOk()).andExpect(view().name("admin.jsp"));
    }

    @Test
    void reviewCarRequest_missing_redirectsDashboard() throws Exception {
        // Arrange
        arrangeDashboardDefaultsSimple();
        when(carRequestService.getCarRequestById(eq(999L))).thenReturn(Optional.empty());
        final MockMvc mockMvc = adminMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/admin/requests/999/review"));
        // Assertions
        resultActions.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin"));
    }

    @Test
    void reviewCarRequest_pending_renderForm() throws Exception {
        // Arrange
        arrangeDashboardDefaultsSimple();
        final CarRequest request = pendingRequest(7L);
        when(carRequestService.getCarRequestById(eq(7L))).thenReturn(Optional.of(request));
        when(carRequestService.getCarRequestImages(eq(7L))).thenReturn(Collections.emptyList());
        when(userService.getUserById(eq(5L))).thenReturn(Optional.empty());
        final MockMvc mockMvc = adminMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/admin/requests/7/review"));
        // Assertions
        resultActions.andExpect(status().isOk()).andExpect(view().name("car-form.jsp"));
    }

    @Test
    void editCar_missing_redirectsCars() throws Exception {
        // Arrange
        arrangeDashboardDefaultsSimple();
        when(carService.getCarById(eq(555L))).thenReturn(Optional.empty());
        final MockMvc mockMvc = adminMvc();
        // Exercise
        final ResultActions resultActions =
                mockMvc.perform(get("/admin/cars/555/edit").header("Referer", "http://localhost/cars?id=55"));
        // Assertions
        resultActions.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/cars?id=55"));
    }

    @Test
    void editCar_found_renderForm() throws Exception {
        // Arrange
        arrangeDashboardDefaultsSimple();
        final Car car = TestModels.car(
                3L,
                1L,
                "Toyota",
                "Corolla",
                1L,
                2024,
                "Sedan",
                "desc",
                LocalDateTime.now(),
                false,
                CarSearchCriteria.FUEL_TYPE_COMBUSTION,
                120,
                6,
                CarSearchCriteria.TRANSMISSION_AUTOMATIC,
                BigDecimal.valueOf(8),
                200,
                BigDecimal.valueOf(20000));

        when(carService.getCarById(eq(3L))).thenReturn(Optional.of(car));
        when(carService.getCarImagesByCarId(eq(3L))).thenReturn(Collections.emptyList());
        final MockMvc mockMvc = adminMvc();
        // Exercise
        final ResultActions resultActions = mockMvc.perform(get("/admin/cars/3/edit"));
        // Assertions
        resultActions.andExpect(status().isOk()).andExpect(view().name("car-form.jsp"));
    }

    @Test
    void rejectCarRequest_redirectsDashboard() throws Exception {
        // Arrange
        arrangeDashboardDefaultsSimple();
        final MockMvc mockMvc = adminMvc();
        // Exercise
        final ResultActions resultActions =
                mockMvc.perform(post("/admin/requests/41/reject"));
        // Assertions
        resultActions.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin?carRejected=1"));
    }

    @Test
    void approveCarRequest_valid_redirectsToAdminViaReferer() throws Exception {
        // Arrange
        arrangeDashboardDefaultsSimple();
        final CarRequest request = pendingRequest(61L);
        when(carRequestService.getCarRequestById(eq(61L))).thenReturn(Optional.of(request));
        when(carRequestService.getCarRequestImages(eq(61L))).thenReturn(Collections.emptyList());
        arrangeCarFormBrandBodyLookups();
        final MockMvc mockMvc = adminMvc();
        // Exercise
        final ResultActions resultActions =
                mockMvc.perform(
                        multipart("/admin/requests/61/accept")
                                .file(carImagePart())
                                .param("brand", "Toyota")
                                .param("bodyType", "Sedan")
                                .param("model", "Corolla")
                                .param("year", "2020")
                                .param(
                                        "description",
                                        "Approved description meets minimum sensible length.")
                                .param("fuelType", CarSearchCriteria.FUEL_TYPE_COMBUSTION)
                                .param("horsepower", "120")
                                .param("airbagCount", "6")
                                .param("transmission", CarSearchCriteria.TRANSMISSION_AUTOMATIC)
                                .param("fuelConsumption", "8.5")
                                .param("maxSpeedKmh", "200")
                                .param("priceUsd", "25000.00")
                                .with(req -> {
                                    req.setMethod("POST");
                                    return req;
                                })
                                .header("Referer", "http://localhost/admin?page=6"));

        // Assertions
        resultActions
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?carAccepted=1"));
    }

    @Test
    void updateCar_missing_redirectsCars() throws Exception {
        // Arrange
        arrangeDashboardDefaultsSimple();
        when(carService.getCarById(eq(99L))).thenReturn(Optional.empty());
        final MockMvc mockMvc = adminMvc();
        // Exercise
        final ResultActions resultActions =
                mockMvc.perform(
                        multipart("/admin/cars/99")
                                .param("brand", "Toyota")
                                .with(req -> {
                                    req.setMethod("POST");
                                    return req;
                                })
                                .header("Referer", "http://localhost/"));
        // Assertions
        resultActions.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"));
    }

    @Test
    void updateCar_valid_redirectsReviews() throws Exception {
        // Arrange
        arrangeDashboardDefaultsSimple();
        final Car car = TestModels.car(
                42L,
                1L,
                "Toyota",
                "Corolla",
                1L,
                2024,
                "Sedan",
                "desc",
                LocalDateTime.now(),
                false,
                CarSearchCriteria.FUEL_TYPE_COMBUSTION,
                120,
                6,
                CarSearchCriteria.TRANSMISSION_AUTOMATIC,
                BigDecimal.valueOf(8),
                200,
                BigDecimal.valueOf(31000));

        when(carService.getCarById(eq(42L))).thenReturn(Optional.of(car));
        when(carService.getCarImagesByCarId(eq(42L))).thenReturn(Collections.emptyList());
        when(carService.updateCar(
                        eq(42L),
                        eq(1L),
                        eq("Corolla"),
                        eq(1L),
                        eq(2020),
                        eq("Fresh description meets length."),
                        any(),
                        any(),
                        eq("combustion"),
                        eq(130),
                        eq(6),
                        eq("automatic"),
                        eq(BigDecimal.valueOf(7.5)),
                        eq(205),
                        eq(BigDecimal.valueOf(31000))))
                .thenReturn(Optional.of(car));

        arrangeCarFormBrandBodyLookups();
        final MockMvc mockMvc = adminMvc();

        // Exercise
        final ResultActions resultActions =
                mockMvc.perform(
                        multipart("/admin/cars/42")
                                .file(carImagePart())
                                .param("brand", "Toyota")
                                .param("bodyType", "Sedan")
                                .param("model", "Corolla")
                                .param("year", "2020")
                                .param("description", "Fresh description meets length.")
                                .param("fuelType", CarSearchCriteria.FUEL_TYPE_COMBUSTION)
                                .param("horsepower", "130")
                                .param("airbagCount", "6")
                                .param("transmission", CarSearchCriteria.TRANSMISSION_AUTOMATIC)
                                .param("fuelConsumption", "7.5")
                                .param("maxSpeedKmh", "205")
                                .param("priceUsd", "31000.00")
                                .with(req -> {
                                    req.setMethod("POST");
                                    return req;
                                }));

        // Assertions
        resultActions.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/reviews/car/42"));
    }

    @Test
    void deleteCar_redirectsAfterDeletion() throws Exception {
        // Arrange
        arrangeDashboardDefaultsSimple();
        final MockMvc mockMvc = adminMvc();
        // Exercise
        final ResultActions resultActions =
                mockMvc.perform(
                        post("/admin/cars/50/delete").header("Referer", "http://localhost/cars?tab=featured"));
        // Assertions
        resultActions.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/cars?tab=featured"));
    }

    @Test
    void brandEndpoints_acceptRejectUpdateDelete_followRedirects() throws Exception {
        // Arrange
        arrangeDashboardDefaultsSimple();
        when(brandRequestService.approvePendingRequest(eq(9L), eq("Override"))).thenReturn(true);
        when(brandRequestService.rejectPendingRequest(eq(9L))).thenReturn(true);
        final MockMvc mockMvc = adminMvc();

        // Exercise / Assertions — accept uses admin referer fallback
        mockMvc.perform(
                        post("/admin/brand-requests/9/accept")
                                .param("name", "Override")
                                .header("Referer", "http://localhost/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?catalogAccepted=1"));

        mockMvc.perform(post("/admin/brand-requests/9/reject"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?catalogRejected=1"));

        mockMvc.perform(post("/admin/brands/4").param("name", "Renamed").header("Referer", "http://localhost/cars"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cars"));

        mockMvc.perform(post("/admin/brands/4/delete").header("Referer", "http://localhost/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void bodyTypeEndpoints_acceptRejectUpdateDelete_followRedirects() throws Exception {
        // Arrange
        arrangeDashboardDefaultsSimple();
        when(bodyTypeRequestService.approvePendingRequest(eq(13L), eq(null))).thenReturn(true);
        when(bodyTypeRequestService.rejectPendingRequest(eq(13L))).thenReturn(true);
        final MockMvc mockMvc = adminMvc();

        mockMvc.perform(
                        post("/admin/body-type-requests/13/accept")
                                .header("Referer", "http://localhost/admin?view=moderator"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?view=moderator&catalogAccepted=1"));

        mockMvc.perform(post("/admin/body-type-requests/13/reject"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?catalogRejected=1"));

        mockMvc.perform(
                        post("/admin/body-types/8")
                                .param("name", "Crossover")
                                .header("Referer", "http://localhost/reviews/car/8"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reviews/car/8"));

        mockMvc.perform(post("/admin/body-types/8/delete")).andExpect(status().is3xxRedirection());
    }

    @Test
    void adminRoleRequests_acceptReject_redirectAdmin() throws Exception {
        // Arrange
        arrangeDashboardDefaultsSimple();
        authenticatedSessionRegistry.clear();
        when(adminRequestService.approvePendingRequestAndReturnUserId(eq(21L))).thenReturn(Optional.of(44L));
        when(adminRequestService.rejectPendingRequest(eq(21L))).thenReturn(true);
        final MockMvc mockMvc = adminMvc();

        mockMvc.perform(
                        post("/admin/admin-requests/21/accept")
                                .header("Referer", "http://localhost/admin?tab=moderators"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?tab=moderators&requestAccepted=1"));

        mockMvc.perform(post("/admin/admin-requests/21/reject"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?requestRejected=1"));

        assertEquals(List.of(44L), authenticatedSessionRegistry.promotedUserIds());
    }

    @Test
    void adminRoleRequests_acceptFailure_doesNotRefreshSessions() throws Exception {
        // Arrange
        arrangeDashboardDefaultsSimple();
        authenticatedSessionRegistry.clear();
        when(adminRequestService.approvePendingRequestAndReturnUserId(eq(21L))).thenReturn(Optional.empty());
        final MockMvc mockMvc = adminMvc();

        // Exercise
        final ResultActions resultActions =
                mockMvc.perform(post("/admin/admin-requests/21/accept"));

        // Assertions
        resultActions
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?requestError=1"));
        assertTrue(authenticatedSessionRegistry.promotedUserIds().isEmpty());
    }

    @Test
    void catalogRequestAcceptFailure_redirectsWithErrorToastFlag() throws Exception {
        // Arrange
        arrangeDashboardDefaultsSimple();
        when(brandRequestService.approvePendingRequest(eq(9L), eq("Toyota"))).thenReturn(false);
        when(bodyTypeRequestService.approvePendingRequest(eq(13L), eq("Sedan"))).thenReturn(false);
        final MockMvc mockMvc = adminMvc();

        mockMvc.perform(
                        post("/admin/brand-requests/9/accept")
                                .param("name", "Toyota")
                                .header("Referer", "http://localhost/admin?tab=brands&catalogAccepted=1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?tab=brands&catalogAcceptError=1"));

        mockMvc.perform(
                        post("/admin/body-type-requests/13/accept")
                                .param("name", "Sedan")
                                .header("Referer", "http://localhost/admin?tab=body-types&catalogRejected=1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?tab=body-types&catalogAcceptError=1"));
    }

    @Test
    void requestCoverImage_returns404_whenRequestMissing() throws Exception {
        // Arrange
        arrangeDashboardDefaultsSimple();
        when(carRequestService.getCarRequestById(eq(800L))).thenReturn(Optional.empty());
        final MockMvc mockMvc = adminMvc();

        mockMvc.perform(get("/admin/requests/800/image")).andExpect(status().isNotFound());
    }

    @Test
    void requestCoverImage_returnsBytes_forLegacyEmbeddedImage() throws Exception {
        // Arrange
        arrangeDashboardDefaultsSimple();
        final byte[] payload = {-1, -37, -1};
        final CarRequest request =
                TestModels.carRequest(
                        700L,
                        null,
                        null,
                        1L,
                        1L,
                        2024,
                        "Legacy",
                        "desc",
                        MediaType.IMAGE_JPEG_VALUE,
                        payload,
                        CarRequestService.STATUS_PENDING,
                        LocalDateTime.now(),
                        CarSearchCriteria.FUEL_TYPE_COMBUSTION,
                        110,
                        6,
                        CarSearchCriteria.TRANSMISSION_AUTOMATIC,
                        BigDecimal.valueOf(8),
                        200,
                        null);
        when(carRequestService.getCarRequestById(eq(700L))).thenReturn(Optional.of(request));
        when(carRequestService.getCarRequestImages(eq(700L))).thenReturn(Collections.emptyList());
        final MockMvc mockMvc = adminMvc();

        mockMvc.perform(get("/admin/requests/700/image"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.IMAGE_JPEG_VALUE))
                .andExpect(header().exists("ETag"));
    }

    @Test
    void requestSpecificImage_returns404_whenMissing() throws Exception {
        arrangeDashboardDefaultsSimple();
        when(carRequestService.getCarRequestImageById(eq(701L), eq(902L))).thenReturn(Optional.empty());
        final MockMvc mockMvc = adminMvc();
        mockMvc.perform(get("/admin/requests/701/images/902")).andExpect(status().isNotFound());
    }

    @Test
    void requestSpecificImage_returnsNotModified_whenEtagMatches() throws Exception {
        arrangeDashboardDefaultsSimple();
        final byte[] payload = {-1, -37};
        final LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 15, 8, 0);
        final CarRequestImage img = TestModels.carRequestImage(903L, 702L, 6, MediaType.IMAGE_JPEG_VALUE, payload, updatedAt);
        when(carRequestService.getCarRequestImageById(eq(702L), eq(903L))).thenReturn(Optional.of(img));
        final String eTag = "\"" + img.getImageId() + "-" + img.getImageData().length + "-" + img.getUpdatedAt() + "\"";
        final MockMvc mockMvc = adminMvc();

        mockMvc.perform(get("/admin/requests/702/images/903").header("If-None-Match", eTag))
                .andExpect(status().isNotModified());
    }

    private static final class RecordingAuthenticatedSessionRegistry extends AuthenticatedSessionRegistry {
        private final List<Long> promotedUserIds = new ArrayList<>();

        @Override
        public int promoteUserToAdmin(final long userId) {
            promotedUserIds.add(userId);
            return 0;
        }

        private List<Long> promotedUserIds() {
            return List.copyOf(promotedUserIds);
        }

        private void clear() {
            promotedUserIds.clear();
        }
    }
}
