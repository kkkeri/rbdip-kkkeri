package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PricingCalculatorTest {

    private final PricingCalculator calculator = new PricingCalculator();

    @ParameterizedTest
    @CsvSource({"10, 100.00", "11, 104.50"})
    void appliesBulkDiscountOnlyAboveTenCopies(int quantity, String expectedTotal) {
        var item = new PricingCalculator.LineItem(new BigDecimal("10.00"), quantity);

        BigDecimal total = calculator.calculateOrderTotal(List.of(item), "regular", null);

        assertThat(total).isEqualByComparingTo(expectedTotal);
    }

    @ParameterizedTest
    @CsvSource({
            "100.00, vip,       SAVE10,        80.00",
            "100.00, wholesale, SAVE20PERCENT, 68.00",
            "  7.00, regular,   SAVE10,         0.00"
    })
    void appliesCustomerDiscountThenCouponAndKeepsTotalNonNegative(
            String price, String customerType, String couponCode, String expectedTotal) {
        var item = new PricingCalculator.LineItem(new BigDecimal(price), 1);

        BigDecimal total = calculator.calculateOrderTotal(List.of(item), customerType, couponCode);

        assertThat(total).isEqualByComparingTo(expectedTotal);
    }

    @ParameterizedTest
    @CsvSource({
            "1000.00,       , 1000.00",
            "1000.01,       ,  980.01",
            "1010.00, SAVE10, 1000.00"
    })
    void appliesLargeOrderDiscountOnlyWhenTotalAfterCouponExceedsThreshold(
            String price, String couponCode, String expectedTotal) {
        var item = new PricingCalculator.LineItem(new BigDecimal(price), 1);

        BigDecimal total = calculator.calculateOrderTotal(List.of(item), "regular", couponCode);

        assertThat(total).isEqualByComparingTo(expectedTotal);
    }

}