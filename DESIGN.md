# Design System Document: The Kinetic Gallery

## 1. Overview & Creative North Star: "The Kinetic Gallery"
Automotive design is a study of aerodynamics, tension, and light. This design system moves away from the static, boxy constraints of traditional review sites like IMDb and toward a "Kinetic Gallery" experience. Our North Star is the feeling of a high-end, darkened showroom where the vehicles are the heroes, and the UI serves as the precision-engineered lighting and technical documentation.

We break the "template" look through **Intentional Asymmetry**. Hero images should bleed off-edge or overlap with display typography to create a sense of forward momentum. We favor tonal depth over structural lines, treating the screen as a three-dimensional space where content sits on layered plates of charcoal and silver.

## 2. Colors: Tonal Architecture
The palette is built on a foundation of "Deep Charcoal" and "Metallic Silver," accented by high-performance bursts of "Racing Orange" (`primary`) and "Electric Blue" (`tertiary`).

### The "No-Line" Rule
Standard 1px solid borders are strictly prohibited for sectioning. To define boundaries, designers must use **Background Color Shifts**.
- A review card (`surface_container_highest`) should sit on a section background (`surface_container_low`).
- Navigation bars should be distinguished by a shift from `surface` to `surface_container_lowest`.

### Surface Hierarchy & Nesting
Treat the UI as a physical stack of materials.
- **Base Level:** `surface` (#131313).
- **Secondary Sections:** `surface_container_low`.
- **Primary Content Cards:** `surface_container_high`.
- **Interaction/Floating Elements:** `surface_container_highest`.
By nesting a "Highest" container inside a "Low" section, we create immediate visual hierarchy without clutter.

### The "Glass & Gradient" Rule
To mimic the gloss of a windshield or the chrome of a chassis:
- Use **Glassmorphism** for floating headers and overlay tooltips. Apply `surface_container` at 70% opacity with a `20px` backdrop blur.
- **Signature Textures:** Use subtle linear gradients for primary actions. A transition from `primary` (#ffb59e) to `primary_container` (#ff5719) creates a "glowing filament" effect that feels premium and tactile.

## 3. Typography: The Editorial Engine
We pair the technical precision of **Space Grotesk** with the neutral clarity of **Inter**.

- **Display & Headlines (Space Grotesk):** These are our "Engine" fonts. Use `display-lg` and `headline-lg` for car model names and scores. The wide apertures and geometric shapes of Space Grotesk should feel "machined."
- **Body & Labels (Inter):** These are our "Technical Specs." Use `body-md` for long-form reviews to ensure maximum readability against dark backgrounds.
- **Hierarchy through Scale:** Create drama by pairing a massive `display-lg` headline with a tiny, all-caps `label-md` in `secondary` (Metallic Silver). This high-contrast scaling mimics high-end automotive magazines.

## 4. Elevation & Depth: Atmospheric Layering
Forget drop shadows that look like "fuzz." We use **Tonal Layering** to define space.

### The Layering Principle
Depth is achieved by stacking surface tokens. For example, a car specification list should be `surface_container_low`, while the individual spec "pills" inside it should be `surface_container_high`.

### Ambient Shadows
When an element must float (e.g., a modal or a primary CTA), use an **Ambient Shadow**:
- **Blur:** 32px to 64px.
- **Opacity:** 6% - 10%.
- **Color:** Use a tinted shadow based on `on_surface` (e.g., `rgba(229, 226, 225, 0.08)`) to mimic the way light bounces in a real environment.

### The "Ghost Border" Fallback
If a border is required for accessibility (e.g., inside an input field), use a **Ghost Border**. Apply the `outline_variant` token at 15% opacity. Never use 100% opaque borders; they disrupt the "kinetic" flow of the UI.

## 5. Components: Precision Engineered Parts

### Cards & Feed Items
- **Rule:** Forbid divider lines.
- **Implementation:** Separate review items using the Spacing Scale (specifically `spacing.12` or `3rem`). Use `surface_container_high` for the card body. The image should occupy the top 60% of the card, with a subtle gradient overlay to allow `title-md` text to sit directly on the imagery.

### Buttons: Ignition & Mechanical
- **Primary (Ignition):** Gradient from `primary` to `primary_container`. Bold `label-md` in `on_primary_fixed`. Roundedness: `md` (0.375rem).
- **Secondary (Mechanical):** Ghost border (`outline_variant` @ 20%) with `on_surface` text. These should feel like auxiliary controls on a dashboard.

### Rating Gauges
- Instead of simple stars, use **Radial Gauges**.
- Use `tertiary` (#5ed4ff) for high-performance ratings (8-10/10) to symbolize "Electric/Modern."
- Use `primary_container` (#ff5719) for "Racing/Power" ratings.

### Chips: Filter & Status
- Use `surface_container_highest` with `rounded-full`.
- Active states should use `tertiary_fixed` with `on_tertiary_fixed` text, making them pop like illuminated dash lights.

### Input Fields
- Background: `surface_container_lowest`.
- Active Indicator: A 2px bottom bar using `primary` (Racing Orange) rather than a full box stroke.

## 6. Do’s and Don’ts

### Do:
- **Use "Breathing Room":** Lean into `spacing.16` and `spacing.20` for page margins to give car photography an editorial, expensive feel.
- **Asymmetric Imagery:** Allow car photos to "break" the container grid, overlapping into the margin or heading area.
- **Subtle Motion:** Use `surface_container_highest` on hover states to create a "lifting" effect.

### Don't:
- **Don't use 1px Dividers:** Never use a line to separate content. Use a `0.5rem` background gap or a shift in surface color.
- **Don't use Pure White:** Use `on_surface` (#e5e2e1) or `secondary` (#c4c6cc) for text. Pure #FFFFFF is too harsh against our deep charcoal.
- **Don't Over-Round:** Stick to `md` (0.375rem) or `lg` (0.5rem) for cards. Avoid "bubbly" layouts; automotive design is about sharp, intentional angles and controlled curves.