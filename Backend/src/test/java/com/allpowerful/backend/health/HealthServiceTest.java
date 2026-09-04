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
        assertThat(result.bodyFatLevel()).isEqualTo("HEALTHY");
        assertThat(result.bodyFatLevelLabel()).isEqualTo("健康范围");
        assertThat(result.restingMetabolicRate()).isEqualTo(1320);
        assertThat(result.estimatedTdee()).isEqualTo(1584);
    }

    @Test
    void classifiesBodyFatIntoAgeAndGenderSpecificStages() {
        assertThat(service.calculate(profileWithBodyFat("male", 30, "7")).bodyFatLevel()).isEqualTo("LOW");
        assertThat(service.calculate(profileWithBodyFat("male", 30, "18")).bodyFatLevel()).isEqualTo("HEALTHY");
        assertThat(service.calculate(profileWithBodyFat("male", 30, "24")).bodyFatLevel()).isEqualTo("HIGH");
        assertThat(service.calculate(profileWithBodyFat("male", 30, "28")).bodyFatLevel()).isEqualTo("OBESE");
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
        assertThat(result.bodyFatLevel()).isNull();
    }

    private HealthDtos.ProfileResponse profileWithBodyFat(String gender, int age, String bodyFat) {
        return new HealthDtos.ProfileResponse(
                1L, gender, LocalDate.now().minusYears(age), new BigDecimal("175"),
                new BigDecimal("70"), null, new BigDecimal(bodyFat), null,
                "sedentary", null, 2000, null, null, null
        );
    }
}
