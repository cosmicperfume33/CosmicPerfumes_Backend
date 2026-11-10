package com.example.ForgeX.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BannerImageUploadResponse {

    private BannerDTO banner;
    private List<BannerUploadResultDTO> results = new ArrayList<>();
}
