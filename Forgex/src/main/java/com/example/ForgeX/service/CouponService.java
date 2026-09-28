package com.example.ForgeX.service;

import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ForgeX.dto.CouponDTO;
import com.example.ForgeX.exceptions.APIException;
import com.example.ForgeX.exceptions.ResourceNotFoundException;
import com.example.ForgeX.model.Coupon;
import com.example.ForgeX.model.DiscountType;
import com.example.ForgeX.repository.CouponRepository;

@Service
public class CouponService {

    @Autowired private CouponRepository couponRepository;
    @Autowired private ModelMapper modelMapper;

   
    public record CouponResult(long discount, String code, String message) {
        static CouponResult none(String message) { return new CouponResult(0, null, message); }
    }

    public CouponResult evaluate(String rawCode, long subtotal) {
        if (rawCode == null || rawCode.isBlank()) {
            return CouponResult.none(null);
        }
        String code = rawCode.trim().toUpperCase();

        Coupon coupon = couponRepository.findByCodeIgnoreCase(code).orElse(null);
        LocalDateTime now = LocalDateTime.now();

        if (coupon == null || !Boolean.TRUE.equals(coupon.getActive()))
            return CouponResult.none("Invalid coupon code");
        if (coupon.getValidFrom() != null && now.isBefore(coupon.getValidFrom()))
            return CouponResult.none("This coupon is not active yet");
        if (coupon.getValidTo() != null && now.isAfter(coupon.getValidTo()))
            return CouponResult.none("This coupon has expired");
        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit())
            return CouponResult.none("This coupon has reached its usage limit");
        if (coupon.getMinOrderPaise() != null && subtotal < coupon.getMinOrderPaise())
            return CouponResult.none("Add ₹" + (coupon.getMinOrderPaise() - subtotal) / 100 + " more to use this coupon");

        long discount;
        if (coupon.getType() == DiscountType.PERCENT) {
            discount = subtotal * coupon.getValue() / 100;
            if (coupon.getMaxDiscountPaise() != null) {
                discount = Math.min(discount, coupon.getMaxDiscountPaise());
            }
        } else {
            discount = coupon.getValue();
        }
        discount = Math.min(discount, subtotal);    // never below zero

        return new CouponResult(discount, coupon.getCode(), "Applied");
    }

    

    @Transactional
    public CouponDTO create(CouponDTO dto) {
        String code = dto.getCode().trim().toUpperCase();
        if (couponRepository.existsByCodeIgnoreCase(code)) {
            throw new APIException("Coupon code already exists: " + code);
        }
        validateRules(dto);
        Coupon coupon = modelMapper.map(dto, Coupon.class);
        coupon.setCouponId(null);
        coupon.setCode(code);
        coupon.setUsedCount(0);
        return modelMapper.map(couponRepository.save(coupon), CouponDTO.class);
    }

    @Transactional
    public CouponDTO update(Long id, CouponDTO dto) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "couponId", id));
        validateRules(dto);
        coupon.setType(dto.getType());
        coupon.setValue(dto.getValue());
        coupon.setMinOrderPaise(dto.getMinOrderPaise());
        coupon.setMaxDiscountPaise(dto.getMaxDiscountPaise());
        coupon.setUsageLimit(dto.getUsageLimit());
        coupon.setValidFrom(dto.getValidFrom());
        coupon.setValidTo(dto.getValidTo());
        if (dto.getActive() != null) coupon.setActive(dto.getActive());
        return modelMapper.map(coupon, CouponDTO.class);
    }

    public List<CouponDTO> findAll() {
        return couponRepository.findAll().stream()
                .map(c -> modelMapper.map(c, CouponDTO.class))
                .toList();
    }

    @Transactional
    public void deactivate(Long id) {
        Coupon c = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "couponId", id));
        c.setActive(false);
    }

    private void validateRules(CouponDTO dto) {
        if (dto.getType() == DiscountType.PERCENT && (dto.getValue() < 1 || dto.getValue() > 90)) {
            throw new APIException("Percent coupons must be between 1 and 90");
        }
        if (dto.getValidFrom() != null && dto.getValidTo() != null
                && dto.getValidTo().isBefore(dto.getValidFrom())) {
            throw new APIException("validTo must be after validFrom");
        }
    }
}