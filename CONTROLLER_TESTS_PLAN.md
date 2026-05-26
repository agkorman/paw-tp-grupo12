# Controller Tests Plan — MockMvc + Mockito

## 1. Add dependencies to `webapp/pom.xml`

Add inside `<dependencies>` with `<scope>test</scope>` (no `<version>` — they're already in root `<dependencyManagement>`):

```xml
<dependency>
  <groupId>org.springframework</groupId>
  <artifactId>spring-test</artifactId>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.junit.jupiter</groupId>
  <artifactId>junit-jupiter</artifactId>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.mockito</groupId>
  <artifactId>mockito-core</artifactId>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.mockito</groupId>
  <artifactId>mockito-junit-jupiter</artifactId>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.springframework.security</groupId>
  <artifactId>spring-security-test</artifactId>
  <scope>test</scope>
</dependency>
```

Also add `spring-security-test` to root `pom.xml` `<dependencyManagement>`:

```xml
<dependency>
  <groupId>org.springframework.security</groupId>
  <artifactId>spring-security-test</artifactId>
  <version>${spring-security.version}</version>
</dependency>
```

---

## 2. Test base setup pattern

All three test classes share this skeleton:

```java
@ExtendWith(MockitoExtension.class)
class XxxControllerTest {

    @Mock ServiceA serviceA;
    // ...other mocks...

    @InjectMocks XxxController controller;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setValidator(validator)
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
}
```

`AuthenticationPrincipalArgumentResolver` is needed because `@AuthenticationPrincipal` is a Spring Security annotation
and is not registered automatically in standalone MockMvc setup.

### Authentication helpers (add as private methods in each test class)

```java
private void loginAs(AuthenticatedUser user) {
    UsernamePasswordAuthenticationToken token =
        new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    SecurityContext ctx = SecurityContextHolder.createEmptyContext();
    ctx.setAuthentication(token);
    SecurityContextHolder.setContext(ctx);
}

private AuthenticatedUser testUser() {
    return new AuthenticatedUser(1L, "driver", "driver@test.com", "pass", Collections.emptyList());
}
```

### Domain object factories (add as private static methods in each test class)

```java
private static Car aCar(long id) {
    return new Car(id, 1L, "Toyota", "Corolla", 1L, "Sedan", "desc", LocalDateTime.now());
}

private static Review aReview(long id, long carId, long userId) {
    Review r = new Review();
    r.setId(id);
    r.setCarId(carId);
    r.setUserId(userId);
    r.setRating(new BigDecimal("4.5"));
    r.setTitle("Good car");
    r.setBody("Really enjoyed it.");
    r.setModelYear(2020);
    r.setMileageKm(10000);
    return r;
}

private static CarImage aCarImage(long carId) {
    return new CarImage(1L, carId, 0, "image/jpeg", new byte[]{1, 2, 3}, LocalDateTime.now());
}
```

---

## 3. `AuthControllerTest`

**File:** `webapp/src/test/java/ar/edu/itba/paw/webapp/controller/AuthControllerTest.java`

**Mocks:** `UserService`, `AuthenticationManager`

| Test method | Request | Setup | Expected |
|---|---|---|---|
| `loginPage_anonymous_showsLoginView` | `GET /login` | no auth | status 200, view `login.jsp` |
| `loginPage_alreadyAuthenticated_redirectsHome` | `GET /login` | `loginAs(testUser())` | redirect `/` |
| `loginPage_withErrorParam_addsErrorCode` | `GET /login?error` | no auth | model attribute `loginErrorCode` is present |
| `loginPage_withLogoutParam_addsMessageCode` | `GET /login?logout` | no auth | model attribute `loginMessageCode` is present |
| `loginPage_withRegisteredParam_addsMessageCode` | `GET /login?registered` | no auth | model attribute `loginMessageCode` is present |
| `registerPage_anonymous_showsRegisterView` | `GET /register` | no auth | status 200, view `register.jsp` |
| `registerPage_alreadyAuthenticated_redirectsHome` | `GET /register` | `loginAs(testUser())` | redirect `/` |
| `createAccount_validForm_autoLoginSucceeds_redirectsHome` | `POST /register` | valid params; `authManager.authenticate()` returns a token | redirect `/` |
| `createAccount_validForm_autoLoginFails_redirectsToRegistered` | `POST /register` | valid params; `authManager.authenticate()` throws `BadCredentialsException` | redirect `/login?registered` |
| `createAccount_emailAlreadyExists_showsFormWithError` | `POST /register` | valid params; `userService.createUser()` throws `EmailAlreadyExistsException` | view `register.jsp`, model `registrationErrorCode = "auth.register.error.email.exists"` |
| `createAccount_usernameAlreadyExists_showsFormWithError` | `POST /register` | valid params; `userService.createUser()` throws `UsernameAlreadyExistsException` | view `register.jsp`, model `registrationErrorCode = "auth.register.error.username.exists"` |
| `createAccount_validationErrors_missingUsername_showsForm` | `POST /register` | missing `username` param (triggers `@NotBlank`) | view `register.jsp`, model `registrationErrorCode` is present |
| `createAccount_alreadyAuthenticated_redirectsHome` | `POST /register` | `loginAs(testUser())` | redirect `/` |

**POST params for the happy path:**
```
username=validuser
email=valid@test.com
password=securepass
confirmPassword=securepass
```

---

## 4. `CarControllerTest`

**File:** `webapp/src/test/java/ar/edu/itba/paw/webapp/controller/CarControllerTest.java`

**Mocks:** `CarService`, `CarFavoriteService`, `BrandService`, `BodyTypeService`, `ReviewService`, `EmailService`

**Standard stubs** (set up in `@BeforeEach` after MockMvc build):
```java
when(carService.searchCars(any())).thenReturn(Page.empty(1, 10));
when(reviewService.getReviewStatsByCarIds(any())).thenReturn(Collections.emptyList());
when(carService.getFeaturedCars(anyInt())).thenReturn(Collections.emptyList());
```

| Test method | Request | Setup | Expected |
|---|---|---|---|
| `home_anonymous_showsLandingPage` | `GET /` | standard stubs | view `landing.jsp` |
| `home_withFeaturedCars_addsHeroCar` | `GET /` | `getFeaturedCars()` returns `[aCar(1)]`; `reviewService.getTopRatedLatestReviewByCar()` returns `Optional.empty()` | model `heroCar` is not null |
| `listCars_showsCarsPage` | `GET /cars` | standard stubs | view `cars.jsp` |
| `listCars_withCreateCarParam_redirectsToNew` | `GET /cars?createCar=true` | — | redirect `/cars/new` |
| `newCarRequest_showsCarForm` | `GET /cars/new` | — | view `car-form.jsp`, model `carForm` present |
| `createCar_anonymous_redirectsToNew` | `POST /cars` multipart | no auth | redirect `/cars/new` |
| `createCar_validForm_redirectsToCatalog` | `POST /cars` multipart | `loginAs(testUser())`; stubs below | redirect `/cars?submitted=true` |
| `createCar_validationErrors_showsForm` | `POST /cars` multipart | `loginAs(testUser())`; missing `model` param | view `car-form.jsp` |
| `updateFavorite_anonymous_ajaxRequest_returns401` | `POST /cars/1/favorite?favorite=true` + `X-Requested-With: XMLHttpRequest` | no auth | status 401 |
| `updateFavorite_anonymous_browserRequest_redirectsToLogin` | `POST /cars/1/favorite?favorite=true` | no auth | redirect `/login` |
| `updateFavorite_carNotFound_ajaxRequest_returns404` | `POST /cars/1/favorite?favorite=true` + AJAX header | `loginAs(testUser())`; `carService.getCarById(1)` returns `Optional.empty()` | status 404 |
| `updateFavorite_valid_ajaxRequest_returnsTrue` | `POST /cars/1/favorite?favorite=true` + AJAX header | `loginAs(testUser())`; car exists; `isFavorited()` returns `true` | status 200, body `"true"` |
| `getCarImage_notFound_returns404` | `GET /cars/1/image` | `carService.getCarImageByCarId(1)` returns `Optional.empty()` | status 404 |
| `getCarImage_found_returnsImageBytes` | `GET /cars/1/image` | `getCarImageByCarId(1)` returns `Optional.of(aCarImage(1))` | status 200, content-type `image/jpeg` |
| `getCarImage_etagMatch_returns304` | `GET /cars/1/image` with computed `If-None-Match` | same image stub | status 304 |

**Extra stubs for `createCar_validForm`:**
```java
when(brandService.findByName("Toyota")).thenReturn(Optional.of(new Brand(1L, "Toyota")));
when(bodyTypeService.findByName("Sedan")).thenReturn(Optional.of(new BodyType(1L, "Sedan")));
when(carService.requestCarCreation(...)).thenReturn(new CarRequest(...));
```

**Multipart request example:**
```java
mockMvc.perform(multipart("/cars")
    .file(new MockMultipartFile("files", "img.jpg", "image/jpeg", new byte[0]))
    .param("brand", "Toyota")
    .param("bodyType", "Sedan")
    .param("model", "Corolla")
    .param("year", "2020"))
```

**ETag test:** compute the expected etag with the same SHA-256 logic as `CarController.buildImageEtag()` and pass it
in the `If-None-Match` header.

---

## 5. `CarReviewControllerTest`

**File:** `webapp/src/test/java/ar/edu/itba/paw/webapp/controller/CarReviewControllerTest.java`

**Mocks:** `CarService`, `CarFavoriteService`, `ReviewService`, `ReviewReplyService`, `ReviewLikeService`,
`EmailService`, `UserService`, `MessageSource`

**Standard stubs** (set up in `@BeforeEach`):
```java
when(reviewService.getReviewsByCar(anyLong(), anyInt())).thenReturn(Page.empty(1, 10));
when(reviewService.getReviewStatsByCar(anyLong())).thenReturn(Optional.empty());
when(reviewService.getLatestReviewByCar(anyLong())).thenReturn(Optional.empty());
when(carService.getCarImagesByCarId(anyLong())).thenReturn(Collections.emptyList());
when(reviewReplyService.getRepliesByReviewIds(any())).thenReturn(Collections.emptyMap());
when(reviewLikeService.countReviewLikesByReviewIds(any())).thenReturn(Collections.emptyMap());
when(reviewLikeService.getLikedReviewIds(any(), anyLong())).thenReturn(Collections.emptySet());
when(reviewLikeService.countReplyLikesByReplyIds(any())).thenReturn(Collections.emptyMap());
when(reviewLikeService.getLikedReplyIds(any(), anyLong())).thenReturn(Collections.emptySet());
when(carService.getCarsByBrandAndBodyType(any(), any())).thenReturn(Collections.emptyList());
when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("");
```

| Test method | Request | Setup | Expected |
|---|---|---|---|
| `reviewForm_noCarId_redirectsToCars` | `GET /reviews` | — | redirect `/cars` |
| `reviewForm_carNotFound_throwsResourceNotFound` | `GET /reviews?carId=99` | `carService.getCarById(99)` returns empty | `NestedServletException` wrapping `ResourceNotFoundException` |
| `reviewForm_validCar_showsReviewPage` | `GET /reviews?carId=1` | car exists + standard stubs | view `car-review.jsp`, model `selectedCar` present |
| `newReview_carFound_showsForm` | `GET /reviews/new?carId=1` | `carService.getCarById(1)` returns `aCar(1)` | view `review-form.jsp`, model `reviewForm.carId == 1` |
| `newReview_carNotFound_redirectsToCars` | `GET /reviews/new?carId=99` | car not found | redirect `/cars` |
| `createReview_anonymous_redirectsToNewWithCarId` | `POST /reviews` with `carId=1` | no auth | redirect `/reviews/new?carId=1` |
| `createReview_carNotFound_throwsResourceNotFound` | `POST /reviews` with `carId=99` | `loginAs(testUser())`; car not found | `ResourceNotFoundException` |
| `createReview_validationErrors_showsForm` | `POST /reviews` | `loginAs(testUser())`; missing `rating` param | view `review-form.jsp` |
| `createReview_valid_redirectsToReviews` | `POST /reviews` | `loginAs(testUser())`; car exists; valid form | redirect `/reviews?carId=1` |
| `toggleReviewLike_anonymous_ajaxReturns401` | `POST /reviews/1/like` + AJAX header | no auth | status 401 |
| `toggleReviewLike_anonymous_browserRedirectsToLogin` | `POST /reviews/1/like` | no auth | redirect `/login` |
| `toggleReviewLike_reviewNotFound_throwsResourceNotFound` | `POST /reviews/1/like` | `loginAs(testUser())`; `getReviewById(1)` returns empty | `ResourceNotFoundException` |
| `toggleReviewLike_valid_ajaxReturnsLikedAndCount` | `POST /reviews/1/like` + AJAX header | `loginAs(testUser())`; review exists; `toggleReviewLike()` returns `true`; `countReviewLikes()` returns `3` | status 200, body `"true\|3"` |
| `deleteReview_owner_redirectsToProfile` | `POST /reviews/1/delete` | `loginAs(testUser())`; review owned by user id 1 | redirect `/profile` |
| `deleteReview_notOwner_throwsForbidden` | `POST /reviews/1/delete` | logged in as user id 2; review owned by user id 1 | `ForbiddenException` |
| `createReply_anonymous_redirectsToLogin` | `POST /reviews/1/replies` | no auth | redirect `/login` |
| `createReply_reviewNotFound_throwsResourceNotFound` | `POST /reviews/1/replies` | `loginAs(testUser())`; review not found | `ResourceNotFoundException` |
| `createReply_valid_redirectsToReviews` | `POST /reviews/1/replies` | `loginAs(testUser())`; review exists; valid body param | redirect contains `/reviews?carId=` |

**POST params for `createReview_valid`:**
```
carId=1
rating=4.5
title=Great car
body=Really enjoyed driving it every day.
modelYear=2020
mileageKm=15000
```

**Asserting exceptions from controllers:**
```java
mockMvc.perform(get("/reviews?carId=99"))
    .andExpect(result -> assertInstanceOf(
        ResourceNotFoundException.class,
        result.getResolvedException()
    ));
```

---

## 6. File locations summary

```
webapp/
  pom.xml                                        ← add 5 test dependencies
src/
  test/
    java/ar/edu/itba/paw/webapp/controller/
      AuthControllerTest.java                    ← 12 tests
      CarControllerTest.java                     ← 15 tests
      CarReviewControllerTest.java               ← 17 tests

pom.xml (root)                                   ← add spring-security-test to dependencyManagement
```

---

## 7. Key implementation notes

- **`@ValidReviewForm`** is a custom class-level constraint defined in the webapp module.
  `LocalValidatorFactoryBean` discovers it automatically from the classpath — no extra registration needed.

- **`Authentication` vs `@AuthenticationPrincipal`:** `AuthController` injects `Authentication` directly
  (resolved by Spring MVC's built-in `AuthenticationMethodArgumentResolver` which reads `SecurityContextHolder`).
  Other controllers use `@AuthenticationPrincipal AuthenticatedUser` which requires the manually registered
  `AuthenticationPrincipalArgumentResolver`. Both work off `SecurityContextHolder.setContext(...)`.

- **Clearing security context:** always call `SecurityContextHolder.clearContext()` in `@AfterEach` to avoid
  leaking auth state across tests (tests run in the same thread with Mockito JUnit 5).

- **Multipart POST:** use `MockMvcRequestBuilders.multipart("/cars")` instead of `post()`. Add form fields with
  `.param()` and files with `.file(new MockMultipartFile(...))`.

- **AJAX detection:** the controllers check `"XMLHttpRequest".equals(request.getHeader("X-Requested-With"))`.
  Set it with `.header("X-Requested-With", "XMLHttpRequest")` on the request builder.

- **`MessageSource` in `CarReviewController`:** stub it globally with
  `when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("")` to prevent NPEs
  wherever the controller resolves i18n messages.
