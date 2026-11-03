package com.example.shop.controller.customer;

import com.example.shop.dto.request.ValidatePromoRequest;
import com.example.shop.dto.response.PromoCodeResponse;
import com.example.shop.service.PromoCodeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/promo")
public class PromoCodeController {

    private final PromoCodeService promoCodeService;

    public PromoCodeController(PromoCodeService promoCodeService) {
        this.promoCodeService = promoCodeService;
    }

    @PostMapping("/validate")
    public ResponseEntity<PromoCodeResponse> validate(@Valid @RequestBody ValidatePromoRequest request) {
        return ResponseEntity.ok(promoCodeService.validateCode(request));
    }
}