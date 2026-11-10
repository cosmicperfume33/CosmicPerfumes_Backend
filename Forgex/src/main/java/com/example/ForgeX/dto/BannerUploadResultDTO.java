package com.example.ForgeX.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BannerUploadResultDTO {

    private String fileName;
    private boolean uploaded;
    private String reason;
    private Long imageId;
}
