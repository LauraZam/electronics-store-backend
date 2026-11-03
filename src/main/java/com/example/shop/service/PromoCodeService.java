package com.example.shop.service;

import com.example.shop.dto.request.ValidatePromoRequest;
import com.example.shop.dto.response.PromoCodeResponse;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.model.entity.PromoCode;
import com.example.shop.repository.PromoCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;

    public PromoCodeService(PromoCodeRepository promoCodeRepository) {
        this.promoCodeRepository = promoCodeRepository;
    }

    @Transactional(readOnly = true)
    public PromoCodeResponse validateCode(ValidatePromoRequest request) {
        PromoCode promo = promoCodeRepository.findByCode(request.code())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired promo code"));

        if (!promo.getIsActive()) {
            throw new IllegalArgumentException("Promo code is inactive");
        }

        if (promo.getMinOrderAmount() != null && request.subtotal().compareTo(promo.getMinOrderAmount()) < 0) {
            throw new IllegalArgumentException("Minimum order amount not met: $" + promo.getMinOrderAmount());
        }

        BigDecimal discount = switch (promo.getDiscountType()) {
            case PERCENTAGE -> request.subtotal().multiply(promo.getDiscountValue().divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
            case FIXED_AMOUNT -> promo.getDiscountValue();
            case FREE_SHIPPING -> BigDecimal.ZERO;
        };

        if (promo.getMaxDiscountAmount() != null && discount.compareTo(promo.getMaxDiscountAmount()) > 0) {
            discount = promo.getMaxDiscountAmount();
        }

        return new PromoCodeResponse(
                promo.getId(), promo.getCode(), promo.getDescription(),
                promo.getDiscountType(), promo.getDiscountValue(), discount, true
        );
    }
}