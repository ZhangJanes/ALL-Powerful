package com.allpowerful.backend.health;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class HealthServiceTest {
    private final HealthService service = new HealthService(null);

    @Test
    void calculatesAdultFemaleMetricsWithoutOverwritingMeasuredBodyFat() {
        HealthDtos.ProfileResponse profile = new HealthDtos.ProfileResponse(
                1L,
                "female",
                LocalDate.now().minusYears(30),
                new BigDecimal("165"),
                new BigDecimal("60"),
                null,
                new BigDecimal("26.5"),
                null,
                "sedentary",
                null,
                2000,
                null,
                null,
                null
        );

        HealthDtos.Calculations result = service.calculate(profile);

        assertThat(result.age()).isEqualTo(30);
        assertThat(result.bmi()).isEqualByComparingTo("22.04");
        assertThat(result.estimatedBodyFatPercent()).isEqualByComparingTo("27.9");
        assertThat(result.effectiveBodyFatPercent()).isEqualByComparingTo("26.5");
        assertThat(result.bodyFatSource()).isEqualTo("measured");
        assertThat(result.healthyBodyFatMin()).isEqualByComparingTo("21");
        assertThat(result.healthyBodyFatMax()).isEqualByComparingTo("32.9");
        assertThat(result.restingMetabolicRate()).isEqualTo(1320);
        assertThat(result.estimatedTdee()).isEqualTo(1584);
    }

    @Test
    void leavesAgeDependentEstimatesEmptyWhenRequiredFieldsAreMissing() {
        HealthDtos.ProfileResponse profile = new HealthDtos.ProfileResponse(
                1L, null, null, null, null, null, null, null,
                "sedentary", null, 2000, null, null, null
        );

        HealthDtos.Calculations result = service.calculate(profile);

        assertThat(result.bmi()).isNull();
        assertThat(result.estimatedBodyFatPercent()).isNull();
        assertThat(result.restingMetabolicRate()).isNull();
        assertThat(result.estimatedTdee()).isNull();
    }
}
