package com.rentify.core.bootstrap;

import com.rentify.core.entity.Amenity;
import com.rentify.core.enums.AmenityCategory;
import com.rentify.core.repository.AmenityRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@ConditionalOnProperty(name = "app.amenities.seed.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class AmenityDataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AmenityDataInitializer.class);

    private static final List<SeedAmenity> DEFAULT_AMENITIES = List.of(
            new SeedAmenity("Ліфт", AmenityCategory.BASIC, "elevator", "elevator"),
            new SeedAmenity("Паркінг", AmenityCategory.BASIC, "parking", "parking"),
            new SeedAmenity("З меблями", AmenityCategory.BASIC, "furnished", "chair"),
            new SeedAmenity("Кондиціонер", AmenityCategory.BASIC, "air_conditioning", "ac_unit"),
            new SeedAmenity("Балкон", AmenityCategory.BASIC, "balcony", "balcony"),
            new SeedAmenity("Тераса", AmenityCategory.BASIC, "terrace", "deck"),
            new SeedAmenity("Інтернет", AmenityCategory.BASIC, "internet", "wifi"),
            new SeedAmenity("Пральна машина", AmenityCategory.BASIC, "washing_machine", "local_laundry_service"),
            new SeedAmenity("Посудомийна машина", AmenityCategory.BASIC, "dishwasher", "dishwasher"),
            new SeedAmenity("Телевізор", AmenityCategory.BASIC, "tv", "tv"),

            new SeedAmenity("Перевірені квартири", AmenityCategory.VERIFICATION, "verified_property", "verified"),
            new SeedAmenity("Перевірені рієлтори", AmenityCategory.VERIFICATION, "verified_realtor", "real_estate_agent"),
            new SeedAmenity("Пошук без дублів", AmenityCategory.VERIFICATION, "hide_duplicates", "content_copy"),

            new SeedAmenity("Без ремонту", AmenityCategory.RENOVATION, "no_renovation", "construction"),
            new SeedAmenity("З ремонтом", AmenityCategory.RENOVATION, "renovated", "home_repair_service"),
            new SeedAmenity("Дизайнерський ремонт", AmenityCategory.RENOVATION, "designer_renovation", "palette"),

            new SeedAmenity("Душ без сходинок", AmenityCategory.ACCESSIBILITY, "step_free_shower", "shower"),
            new SeedAmenity("Поручні у ванній кімнаті", AmenityCategory.ACCESSIBILITY, "bathroom_handrails", "accessible"),
            new SeedAmenity("Двері шириною понад 90 см", AmenityCategory.ACCESSIBILITY, "wide_doorways_90_plus", "door_front"),

            new SeedAmenity("Світло 24/7", AmenityCategory.BLACKOUT_SUPPORT, "power_24_7", "bolt"),
            new SeedAmenity("Є газ", AmenityCategory.BLACKOUT_SUPPORT, "gas", "local_fire_department"),
            new SeedAmenity("Є резервне живлення", AmenityCategory.BLACKOUT_SUPPORT, "backup_generator", "generator"),
            new SeedAmenity("Працює опалення", AmenityCategory.BLACKOUT_SUPPORT, "heating_works", "mode_heat"),
            new SeedAmenity("Є водопостачання", AmenityCategory.BLACKOUT_SUPPORT, "water_supply", "water_drop"),
            new SeedAmenity("Працює ліфт", AmenityCategory.BLACKOUT_SUPPORT, "elevator_works", "elevator"),
            new SeedAmenity("Є мобільний зв'язок", AmenityCategory.BLACKOUT_SUPPORT, "mobile_signal", "signal_cellular_alt"),
            new SeedAmenity("Є інтернет", AmenityCategory.BLACKOUT_SUPPORT, "backup_internet", "router"),

            new SeedAmenity("Можна з тваринами", AmenityCategory.LIVING_CONDITIONS, "pets_allowed", "pets"),
            new SeedAmenity("Спільна оренда", AmenityCategory.LIVING_CONDITIONS, "co_living", "groups"),
            new SeedAmenity("Можливе підселення", AmenityCategory.LIVING_CONDITIONS, "family_friendly", "family_restroom"),

            new SeedAmenity("Кухня-студія", AmenityCategory.LAYOUT, "studio", "single_bed"),
            new SeedAmenity("Без меблів", AmenityCategory.LAYOUT, "open_space_kitchen", "kitchen"),
            new SeedAmenity("Багаторівнева", AmenityCategory.LAYOUT, "multi_level", "stairs"),
            new SeedAmenity("Окремі кімнати", AmenityCategory.LAYOUT, "separate_rooms", "meeting_room"),
            new SeedAmenity("Пентхаус", AmenityCategory.LAYOUT, "penthouse", "roofing"),
            new SeedAmenity("Джакузі", AmenityCategory.LAYOUT, "jacuzzi", "hot_tub"),

            new SeedAmenity("Цегляні", AmenityCategory.WALL_TYPE, "brick_walls", "view_module"),
            new SeedAmenity("Каркасні", AmenityCategory.WALL_TYPE, "frame_walls", "grid_3x3"),
            new SeedAmenity("Панельні", AmenityCategory.WALL_TYPE, "panel_walls", "grid_view"),
            new SeedAmenity("Блочні", AmenityCategory.WALL_TYPE, "block_walls", "category"),
            new SeedAmenity("Монолітні", AmenityCategory.WALL_TYPE, "monolith_walls", "crop_square"),
            new SeedAmenity("Кам'яні", AmenityCategory.WALL_TYPE, "stone_walls", "foundation"),
            new SeedAmenity("Залізобетонні", AmenityCategory.WALL_TYPE, "reinforced_concrete_walls", "view_in_ar"),
            new SeedAmenity("Дерев'яні", AmenityCategory.WALL_TYPE, "wooden_walls", "forest"),

            new SeedAmenity("Централізоване", AmenityCategory.HEATING, "central_heating", "device_thermostat"),
            new SeedAmenity("Індивідуальне", AmenityCategory.HEATING, "individual_heating", "thermostat_auto"),
            new SeedAmenity("Комбіноване", AmenityCategory.HEATING, "combined_heating", "settings_input_component"),
            new SeedAmenity("Без опалення", AmenityCategory.HEATING, "no_heating", "mode_off_on"),

            new SeedAmenity("Від власника", AmenityCategory.OFFER_TYPE, "from_owner", "person"),
            new SeedAmenity("Від забудовника", AmenityCategory.OFFER_TYPE, "from_developer", "apartment"),
            new SeedAmenity("Від представника забудовника", AmenityCategory.OFFER_TYPE, "from_agency", "business"),
            new SeedAmenity("Від посередника", AmenityCategory.OFFER_TYPE, "from_broker", "support_agent"),

            new SeedAmenity("Можливий торг", AmenityCategory.RENTAL_TERMS, "bargaining_possible", "gavel"),
            new SeedAmenity("Без комісії", AmenityCategory.RENTAL_TERMS, "no_commission", "money_off"),

            new SeedAmenity("Зарядка для електромобіля", AmenityCategory.OTHER, "ev_charging", "ev_station")
    );

    private final AmenityRepository amenityRepository;

    @Override
    @Transactional
    public void run(String... args) {
        int created = 0;
        int updated = 0;

        for (SeedAmenity defaultAmenity : DEFAULT_AMENITIES) {
            Amenity amenity = amenityRepository.findBySlugIgnoreCase(defaultAmenity.slug())
                    .or(() -> amenityRepository.findByNameIgnoreCase(defaultAmenity.name()))
                    .orElse(null);

            if (amenity == null) {
                amenityRepository.save(Amenity.builder()
                        .name(defaultAmenity.name())
                        .category(defaultAmenity.category())
                        .slug(defaultAmenity.slug())
                        .icon(defaultAmenity.icon())
                        .build());
                created++;
                continue;
            }

            boolean changed = false;

            if (!defaultAmenity.name().equals(amenity.getName())) {
                amenity.setName(defaultAmenity.name());
                changed = true;
            }
            if (amenity.getCategory() != defaultAmenity.category()) {
                amenity.setCategory(defaultAmenity.category());
                changed = true;
            }
            if (!defaultAmenity.slug().equals(amenity.getSlug())) {
                amenity.setSlug(defaultAmenity.slug());
                changed = true;
            }
            if (!defaultAmenity.icon().equals(amenity.getIcon())) {
                amenity.setIcon(defaultAmenity.icon());
                changed = true;
            }

            if (changed) {
                amenityRepository.save(amenity);
                updated++;
            }
        }

        if (created > 0 || updated > 0) {
            logger.info("Amenity seed applied: created={}, updated={}, total={}", created, updated, DEFAULT_AMENITIES.size());
        }
    }

    private record SeedAmenity(
            String name,
            AmenityCategory category,
            String slug,
            String icon
    ) {
    }
}
