package com.example.ForgeX.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BannerReorderDTO {

    private String placement;
    private List<Long> ids;
}
