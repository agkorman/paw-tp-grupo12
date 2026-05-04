<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="criteria" required="true" type="ar.edu.itba.paw.model.CarSearchCriteria" %>
<%@ attribute name="vehicleCount" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<spring:message var="filterMinPlaceholder" code="cars.filter.placeholder.min"/>
<spring:message var="filterMaxPlaceholder" code="cars.filter.placeholder.max"/>
<spring:message var="closeFiltersLabel" code="cars.filter.close"/>
<c:set var="electricOnlyFilter" value="${criteria.electricOnly}"/>
<div id="carsFiltersOverlay" class="cars-filters-overlay" data-close-filters-panel></div>

<aside id="carsFiltersPanel"
       class="cars-filters-panel"
       hidden
       role="dialog"
       aria-modal="true"
       aria-labelledby="filtersPanelTitle">

    <div class="cars-filters-panel-inner">

        <div class="cars-filters-header">
            <h2 id="filtersPanelTitle" class="cars-filters-title"><spring:message code="cars.filter.title"/></h2>
            <button type="button" class="cars-filters-close" data-close-filters-panel aria-label="${closeFiltersLabel}">
                <pa:icon name="close" size="18"/>
            </button>
        </div>

        <%-- Hidden inputs to carry through existing basic filters --%>
        <input type="hidden" id="panelHiddenQ"        name="q"        value="">
        <input type="hidden" id="panelHiddenBrand"    name="brand"    value="">
        <input type="hidden" id="panelHiddenBodyType" name="bodyType" value="">
        <p id="filtersPanelValidationMessage"
           class="filters-panel-validation-message"
           role="alert"
           hidden></p>

        <section class="filters-panel-section">
            <h3 class="filters-panel-section-title"><spring:message code="cars.form.fuelType"/></h3>
            <div class="fuel-type-picker" data-filter-target="panelFuelType" data-filter-multiple="true">
                <button type="button" class="fuel-type-option filter-segment-option${fn:contains(criteria.fuelType, 'combustion') ? ' is-selected' : ''}" data-value="combustion">
                    <pa:icon name="gas-pump" size="28"/>
                    <span><spring:message code="domain.fuel.combustion"/></span>
                </button>
                <button type="button" class="fuel-type-option filter-segment-option${fn:contains(criteria.fuelType, 'hybrid') ? ' is-selected' : ''}" data-value="hybrid">
                    <pa:icon name="eco" size="28"/>
                    <span><spring:message code="domain.fuel.hybrid"/></span>
                </button>
                <button type="button" class="fuel-type-option filter-segment-option${fn:contains(criteria.fuelType, 'electric') ? ' is-selected' : ''}" data-value="electric">
                    <pa:icon name="bolt" size="28"/>
                    <span><spring:message code="domain.fuel.electric"/></span>
                </button>
            </div>
            <input type="hidden" id="panelFuelType" name="fuelType" value="<c:out value='${criteria.fuelType}'/>">
        </section>

        <section class="filters-panel-section">
            <h3 class="filters-panel-section-title"><spring:message code="cars.form.transmission"/></h3>
            <div class="segmented-control filter-segmented" data-filter-target="panelTransmission">
                <button type="button" class="segmented-control-option filter-segment-option${empty criteria.transmission ? ' is-selected' : ''}" data-value=""><spring:message code="cars.filter.both"/></button>
                <button type="button" class="segmented-control-option filter-segment-option${'automatic' eq criteria.transmission ? ' is-selected' : ''}" data-value="automatic"><spring:message code="domain.transmission.automatic"/></button>
                <button type="button" class="segmented-control-option filter-segment-option${'manual' eq criteria.transmission ? ' is-selected' : ''}" data-value="manual"><spring:message code="domain.transmission.manual"/></button>
            </div>
            <input type="hidden" id="panelTransmission" name="transmission" value="<c:out value='${criteria.transmission}'/>">
        </section>

        <section class="filters-panel-section">
            <h3 class="filters-panel-section-title"><spring:message code="cars.form.priceZeroKm"/></h3>
            <div class="dual-range"
                 data-range-min="0"
                 data-range-max="1000"
                 data-real-min="10000"
                 data-real-max="5000000"
                 data-scale="price"
                 data-input-low="panelPriceMin"
                 data-input-high="panelPriceMax">
                <div class="dual-range-track">
                    <div class="dual-range-fill" id="panelPriceFill"></div>
                </div>
                <input type="range" class="dual-range-thumb dual-range-low" min="0" max="1000" step="10"
                       value="0">
                <input type="range" class="dual-range-thumb dual-range-high" min="0" max="1000" step="10"
                       value="1000">
                <div class="dual-range-labels">
                    <span>$10k</span>
                    <span>$250k+</span>
                </div>
            </div>
            <div class="dual-range-inputs">
                <input type="text" id="panelPriceMin" name="priceMin" class="range-number-input range-number-input--currency"
                       inputmode="numeric" autocomplete="off" data-currency-input="true"
                       data-min="10000" data-max="5000000" data-display-max="250000" data-step="1000"
                       data-boundary="min" placeholder="${filterMinPlaceholder}"
                       aria-describedby="panelPriceError"
                       value="<c:out value='${criteria.priceMin}'/>">
                <span class="range-separator">–</span>
                <input type="text" id="panelPriceMax" name="priceMax" class="range-number-input range-number-input--currency"
                       inputmode="numeric" autocomplete="off" data-currency-input="true"
                       data-min="10000" data-max="5000000" data-display-max="250000"
                       data-step="1000" data-boundary="max" placeholder="${filterMaxPlaceholder}"
                       aria-describedby="panelPriceError"
                       value="<c:out value='${criteria.priceMax}'/>">
            </div>
            <p id="panelPriceError" class="filters-field-error" hidden></p>
        </section>

        <section class="filters-panel-section">
            <h3 class="filters-panel-section-title"><spring:message code="cars.form.modelYear"/></h3>
            <div class="dual-range"
                 data-range-min="1950"
                 data-range-max="2100"
                 data-input-low="panelYearMin"
                 data-input-high="panelYearMax">
                <div class="dual-range-track">
                    <div class="dual-range-fill" id="panelYearFill"></div>
                </div>
                <input type="range" class="dual-range-thumb dual-range-low" min="1950" max="2100" step="1"
                       value="<c:out value='${not empty criteria.yearMin ? criteria.yearMin : 1950}'/>">
                <input type="range" class="dual-range-thumb dual-range-high" min="1950" max="2100" step="1"
                       value="<c:out value='${not empty criteria.yearMax ? criteria.yearMax : 2100}'/>">
                <div class="dual-range-labels">
                    <span>1950</span>
                    <span>2100</span>
                </div>
            </div>
            <div class="dual-range-inputs">
                <input type="text" id="panelYearMin" name="yearMin" class="range-number-input"
                       inputmode="numeric" autocomplete="off" data-number-input="true"
                       data-min="1950" data-max="2100" data-step="1" data-boundary="min" placeholder="${filterMinPlaceholder}"
                       aria-describedby="panelYearError"
                       value="<c:out value='${criteria.yearMin}'/>">
                <span class="range-separator">–</span>
                <input type="text" id="panelYearMax" name="yearMax" class="range-number-input"
                       inputmode="numeric" autocomplete="off" data-number-input="true"
                       data-min="1950" data-max="2100" data-step="1" data-boundary="max" placeholder="${filterMaxPlaceholder}"
                       aria-describedby="panelYearError"
                       value="<c:out value='${criteria.yearMax}'/>">
            </div>
            <p id="panelYearError" class="filters-field-error" hidden></p>
        </section>

        <section class="filters-panel-section">
            <div class="filters-panel-subsection">
                <h4 class="filters-panel-subsection-title"><spring:message code="cars.filter.horsepower"/></h4>
                <div class="dual-range"
                     data-range-min="50"
                     data-range-max="800"
                     data-input-low="panelHpMin"
                     data-input-high="panelHpMax">
                    <div class="dual-range-track">
                        <div class="dual-range-fill" id="panelHpFill"></div>
                    </div>
                    <input type="range" class="dual-range-thumb dual-range-low" min="50" max="800" step="25"
                           value="<c:out value='${not empty criteria.horsepowerMin ? criteria.horsepowerMin : 50}'/>">
                    <input type="range" class="dual-range-thumb dual-range-high" min="50" max="800" step="25"
                           value="<c:out value='${not empty criteria.horsepowerMax ? criteria.horsepowerMax : 800}'/>">
                    <div class="dual-range-labels">
                        <span>50 HP</span>
                        <span>800+ HP</span>
                    </div>
                </div>
                <div class="dual-range-inputs">
                    <input type="text" id="panelHpMin" name="horsepowerMin" class="range-number-input"
                           inputmode="numeric" autocomplete="off" data-number-input="true"
                           data-min="50" data-max="800" data-step="25" data-boundary="min" placeholder="${filterMinPlaceholder}"
                           aria-describedby="panelHpError"
                           value="<c:out value='${criteria.horsepowerMin}'/>">
                    <span class="range-separator">–</span>
                    <input type="text" id="panelHpMax" name="horsepowerMax" class="range-number-input"
                           inputmode="numeric" autocomplete="off" data-number-input="true"
                           data-min="50" data-max="800" data-step="25" data-boundary="max" placeholder="${filterMaxPlaceholder}"
                           aria-describedby="panelHpError"
                           value="<c:out value='${criteria.horsepowerMax}'/>">
                </div>
                <p id="panelHpError" class="filters-field-error" hidden></p>
            </div>

            <div class="filters-panel-subsection">
                <div class="filter-range-header">
                    <h4 class="filters-panel-subsection-title"><spring:message code="cars.filter.speed"/></h4>
                </div>
                <p class="filter-range-value-display" id="panelMaxSpeedDisplay"></p>
                <input type="range" id="panelMaxSpeedSlider" name="maxSpeedMin"
                       class="single-range"
                       min="0" max="500" step="25"
                       value="<c:out value='${not empty criteria.maxSpeedMin ? criteria.maxSpeedMin : 0}'/>">
                <div class="single-range-labels">
                    <div class="single-range-label-col">
                        <span>0 km/h</span>
                        <span class="single-range-sublabel"><spring:message code="cars.filter.speed.slow"/></span>
                    </div>
                    <div class="single-range-label-col single-range-label-col--end">
                        <span>500 km/h</span>
                        <span class="single-range-sublabel"><spring:message code="cars.filter.speed.fast"/></span>
                    </div>
                </div>
            </div>

            <c:if test="${not electricOnlyFilter}">
                <div class="filters-panel-subsection" id="panelConsumptionSection" data-hide-when-electric-only="true">
                    <div class="filter-range-header">
                        <h4 class="filters-panel-subsection-title"><spring:message code="cars.filter.consumption"/></h4>
                    </div>
                    <p class="filter-range-value-display" id="panelConsumptionDisplay"></p>
                    <input type="range" id="panelConsumptionSlider" name="fuelConsumptionMax"
                           class="single-range"
                           min="0" max="30" step="1"
                           value="<c:out value='${not empty criteria.fuelConsumptionMax ? criteria.fuelConsumptionMax : 30}'/>">
                    <div class="single-range-labels">
                        <div class="single-range-label-col">
                            <span>0 L/100km</span>
                            <span class="single-range-sublabel"><spring:message code="cars.filter.consumption.low"/></span>
                        </div>
                        <div class="single-range-label-col single-range-label-col--end">
                            <span>30 L/100km</span>
                            <span class="single-range-sublabel"><spring:message code="cars.filter.consumption.high"/></span>
                        </div>
                    </div>
                </div>
            </c:if>
        </section>

        <section class="filters-panel-section">
            <h3 class="filters-panel-section-title"><spring:message code="cars.filter.airbags"/></h3>
            <div class="filter-toggle-group" data-filter-target="panelAirbagMin">
                <button type="button" class="filter-toggle-option${empty criteria.airbagMin ? ' is-selected' : ''}" data-value=""><spring:message code="cars.filter.all"/></button>
                <button type="button" class="filter-toggle-option${criteria.airbagMin eq 2 ? ' is-selected' : ''}" data-value="2">2+</button>
                <button type="button" class="filter-toggle-option${criteria.airbagMin eq 4 ? ' is-selected' : ''}" data-value="4">4+</button>
                <button type="button" class="filter-toggle-option${criteria.airbagMin eq 6 ? ' is-selected' : ''}" data-value="6">6+</button>
                <button type="button" class="filter-toggle-option${criteria.airbagMin eq 8 ? ' is-selected' : ''}" data-value="8">8+</button>
                <button type="button" class="filter-toggle-option${criteria.airbagMin eq 10 ? ' is-selected' : ''}" data-value="10">10+</button>
            </div>
            <input type="hidden" id="panelAirbagMin" name="airbagMin" value="<c:out value='${criteria.airbagMin}'/>">
        </section>

        <%-- Footer --%>
        <div class="cars-filters-footer">
            <button type="button" id="filtersClearBtn" class="filters-clear-btn"><spring:message code="cars.filter.clear"/></button>
            <button type="button" id="filtersApplyBtn" class="btn-primary filters-apply-btn">
                <spring:message code="cars.filter.view"/> <span id="filtersVehicleCount"><c:out value="${vehicleCount}"/></span> <spring:message code="cars.filter.vehicles"/>
            </button>
        </div>

    </div>
</aside>
