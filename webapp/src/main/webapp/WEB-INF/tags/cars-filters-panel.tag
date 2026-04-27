<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="criteria" required="true" type="ar.edu.itba.paw.model.CarSearchCriteria" %>
<%@ attribute name="vehicleCount" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div id="carsFiltersOverlay" class="cars-filters-overlay" data-close-filters-panel></div>

<aside id="carsFiltersPanel"
       class="cars-filters-panel"
       hidden
       role="dialog"
       aria-modal="true"
       aria-labelledby="filtersPanelTitle">

    <div class="cars-filters-panel-inner">

        <div class="cars-filters-header">
            <h2 id="filtersPanelTitle" class="cars-filters-title">Filtros Avanzados</h2>
            <button type="button" class="cars-filters-close" data-close-filters-panel aria-label="Cerrar filtros">
                <svg width="18" height="18" viewBox="0 0 18 18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true" focusable="false">
                    <line x1="4" y1="4" x2="14" y2="14"/><line x1="14" y1="4" x2="4" y2="14"/>
                </svg>
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

        <%-- Motorización — segmented control --%>
        <section class="filters-panel-section">
            <h3 class="filters-panel-section-title">Motorización</h3>
            <div class="segmented-control filter-segmented" data-filter-target="panelFuelType" data-filter-multiple="true">
                <button type="button" class="segmented-control-option filter-segment-option${fn:contains(criteria.fuelType, 'combustion') ? ' is-selected' : ''}" data-value="combustion">Combustión</button>
                <button type="button" class="segmented-control-option filter-segment-option${fn:contains(criteria.fuelType, 'hybrid') ? ' is-selected' : ''}" data-value="hybrid">Híbrido</button>
                <button type="button" class="segmented-control-option filter-segment-option${fn:contains(criteria.fuelType, 'electric') ? ' is-selected' : ''}" data-value="electric">Eléctrico</button>
            </div>
            <input type="hidden" id="panelFuelType" name="fuelType" value="<c:out value='${criteria.fuelType}'/>">
        </section>

        <%-- Precio 0 km (USD) --%>
        <section class="filters-panel-section">
            <h3 class="filters-panel-section-title">Precio 0 km (USD)</h3>
            <div class="dual-range"
                 data-range-min="0"
                 data-range-max="1000"
                 data-real-min="0"
                 data-real-max="5000000"
                 data-scale="log"
                 data-input-low="panelPriceMin"
                 data-input-high="panelPriceMax">
                <div class="dual-range-track">
                    <div class="dual-range-fill" id="panelPriceFill"></div>
                </div>
                <input type="range" class="dual-range-thumb dual-range-low"  min="0" max="1000" step="1"
                       value="0">
                <input type="range" class="dual-range-thumb dual-range-high" min="0" max="1000" step="1"
                       value="1000">
            </div>
            <div class="dual-range-inputs">
                <input type="number" id="panelPriceMin" name="priceMin" class="range-number-input"
                       min="0" max="10000000" placeholder="Mín"
                       aria-describedby="panelPriceError"
                       value="<c:out value='${criteria.priceMin}'/>">
                <span class="range-separator">–</span>
                <input type="number" id="panelPriceMax" name="priceMax" class="range-number-input"
                       min="0" max="10000000" placeholder="Máx"
                       aria-describedby="panelPriceError"
                       value="<c:out value='${criteria.priceMax}'/>">
            </div>
            <p id="panelPriceError" class="filters-field-error" hidden></p>
        </section>

        <%-- Caballos de fuerza --%>
        <section class="filters-panel-section">
            <h3 class="filters-panel-section-title">Caballos de fuerza (HP)</h3>
            <div class="dual-range"
                 data-range-min="0"
                 data-range-max="1500"
                 data-input-low="panelHpMin"
                 data-input-high="panelHpMax">
                <div class="dual-range-track">
                    <div class="dual-range-fill" id="panelHpFill"></div>
                </div>
                <input type="range" class="dual-range-thumb dual-range-low"  min="0" max="1500" step="10"
                       value="<c:out value='${not empty criteria.horsepowerMin ? criteria.horsepowerMin : 0}'/>">
                <input type="range" class="dual-range-thumb dual-range-high" min="0" max="1500" step="10"
                       value="<c:out value='${not empty criteria.horsepowerMax ? criteria.horsepowerMax : 1500}'/>">
            </div>
            <div class="dual-range-inputs">
                <input type="number" id="panelHpMin" name="horsepowerMin" class="range-number-input"
                       min="0" max="1500" placeholder="Mín"
                       aria-describedby="panelHpError"
                       value="<c:out value='${criteria.horsepowerMin}'/>">
                <span class="range-separator">–</span>
                <input type="number" id="panelHpMax" name="horsepowerMax" class="range-number-input"
                       min="0" max="1500" placeholder="Máx"
                       aria-describedby="panelHpError"
                       value="<c:out value='${criteria.horsepowerMax}'/>">
            </div>
            <p id="panelHpError" class="filters-field-error" hidden></p>
        </section>

        <%-- Número de airbags — realistic discrete options --%>
        <section class="filters-panel-section">
            <h3 class="filters-panel-section-title">Número de airbags</h3>
            <div class="filter-toggle-group" data-filter-target="panelAirbagMin">
                <button type="button" class="filter-toggle-option${empty criteria.airbagMin ? ' is-selected' : ''}" data-value="">Todos</button>
                <button type="button" class="filter-toggle-option${criteria.airbagMin eq 2 ? ' is-selected' : ''}" data-value="2">2+</button>
                <button type="button" class="filter-toggle-option${criteria.airbagMin eq 4 ? ' is-selected' : ''}" data-value="4">4+</button>
                <button type="button" class="filter-toggle-option${criteria.airbagMin eq 6 ? ' is-selected' : ''}" data-value="6">6+</button>
                <button type="button" class="filter-toggle-option${criteria.airbagMin eq 8 ? ' is-selected' : ''}" data-value="8">8+</button>
                <button type="button" class="filter-toggle-option${criteria.airbagMin eq 10 ? ' is-selected' : ''}" data-value="10">10+</button>
            </div>
            <input type="hidden" id="panelAirbagMin" name="airbagMin" value="<c:out value='${criteria.airbagMin}'/>">
        </section>

        <%-- Transmisión — segmented control --%>
        <section class="filters-panel-section">
            <h3 class="filters-panel-section-title">Transmisión</h3>
            <div class="segmented-control filter-segmented" data-filter-target="panelTransmission">
                <button type="button" class="segmented-control-option filter-segment-option${empty criteria.transmission ? ' is-selected' : ''}" data-value="">Ambos</button>
                <button type="button" class="segmented-control-option filter-segment-option${'automatic' eq criteria.transmission ? ' is-selected' : ''}" data-value="automatic">Automática</button>
                <button type="button" class="segmented-control-option filter-segment-option${'manual' eq criteria.transmission ? ' is-selected' : ''}" data-value="manual">Manual</button>
            </div>
            <input type="hidden" id="panelTransmission" name="transmission" value="<c:out value='${criteria.transmission}'/>">
        </section>

        <%-- Consumo de nafta (max only) --%>
        <section class="filters-panel-section">
            <div class="filter-range-header">
                <h3 class="filters-panel-section-title">Consumo de nafta</h3>
            </div>
            <p class="filter-range-value-display" id="panelConsumptionDisplay"></p>
            <input type="range" id="panelConsumptionSlider" name="fuelConsumptionMax"
                   class="single-range"
                   min="0" max="30" step="0.5"
                   value="<c:out value='${not empty criteria.fuelConsumptionMax ? criteria.fuelConsumptionMax : 30}'/>">
            <div class="single-range-labels">
                <div class="single-range-label-col">
                    <span>0 L/100km</span>
                    <span class="single-range-sublabel">Menor consumo</span>
                </div>
                <div class="single-range-label-col single-range-label-col--end">
                    <span>30 L/100km</span>
                    <span class="single-range-sublabel">Mayor consumo</span>
                </div>
            </div>
        </section>

        <%-- Velocidad máxima (min only) --%>
        <section class="filters-panel-section">
            <div class="filter-range-header">
                <h3 class="filters-panel-section-title">Velocidad máxima</h3>
            </div>
            <p class="filter-range-value-display" id="panelMaxSpeedDisplay"></p>
            <input type="range" id="panelMaxSpeedSlider" name="maxSpeedMin"
                   class="single-range"
                   min="0" max="500" step="10"
                   value="<c:out value='${not empty criteria.maxSpeedMin ? criteria.maxSpeedMin : 0}'/>">
            <div class="single-range-labels">
                <div class="single-range-label-col">
                    <span>0 km/h</span>
                    <span class="single-range-sublabel">Más lenta</span>
                </div>
                <div class="single-range-label-col single-range-label-col--end">
                    <span>500 km/h</span>
                    <span class="single-range-sublabel">Más rápida</span>
                </div>
            </div>
        </section>

        <%-- Footer --%>
        <div class="cars-filters-footer">
            <button type="button" id="filtersClearBtn" class="filters-clear-btn">Limpiar todo</button>
            <button type="button" id="filtersApplyBtn" class="btn-primary filters-apply-btn">
                Ver <span id="filtersVehicleCount"><c:out value="${vehicleCount}"/></span> vehículos
            </button>
        </div>

    </div>
</aside>
