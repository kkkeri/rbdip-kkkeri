package com.rbdip.bookstore.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Модуль расчёта цены заказа. Намеренно почти не покрыт тестами и
 * содержит magic numbers / нечитаемые ветвления скидок - цель для
 * характеризационных тестов (ЛР2) и mutation-testing гейта PIT (ЛР5).
 */
public class PricingCalculator {

    private static final int BULK_QUANTITY_THRESHOLD = 10;
    private static final BigDecimal BULK_PRICE_MULTIPLIER = new BigDecimal("0.95");
    private static final BigDecimal VIP_PRICE_MULTIPLIER = new BigDecimal("0.9");
    private static final BigDecimal WHOLESALE_PRICE_MULTIPLIER = new BigDecimal("0.85");
    private static final BigDecimal SAVE10_DISCOUNT_AMOUNT = BigDecimal.TEN;
    private static final BigDecimal SAVE20_PRICE_MULTIPLIER = new BigDecimal("0.8");
    private static final BigDecimal LARGE_ORDER_THRESHOLD = new BigDecimal("1000");
    private static final BigDecimal LARGE_ORDER_PRICE_MULTIPLIER = new BigDecimal("0.98");

    public record LineItem(BigDecimal price, int quantity) {
    }

    public BigDecimal calculateOrderTotal(List<LineItem> items, String customerType, String couponCode) {
        BigDecimal total = BigDecimal.ZERO;

        for (LineItem item : items) {
            BigDecimal linePrice = item.price().multiply(BigDecimal.valueOf(item.quantity()));
            if (item.quantity() > BULK_QUANTITY_THRESHOLD) {
                linePrice = linePrice.multiply(BULK_PRICE_MULTIPLIER);
            }
            total = total.add(linePrice);
        }

        if ("vip".equals(customerType)) {
            total = total.multiply(VIP_PRICE_MULTIPLIER);
        } else if ("wholesale".equals(customerType)) {
            total = total.multiply(WHOLESALE_PRICE_MULTIPLIER);
        }

        if ("SAVE10".equals(couponCode)) {
            total = total.subtract(SAVE10_DISCOUNT_AMOUNT);
        } else if ("SAVE20PERCENT".equals(couponCode)) {
            total = total.multiply(SAVE20_PRICE_MULTIPLIER);
        }

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        if (total.compareTo(LARGE_ORDER_THRESHOLD) > 0) {
            total = total.multiply(LARGE_ORDER_PRICE_MULTIPLIER);
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }
}