package com.example.ForgeX.service;

import com.example.ForgeX.dto.UserDTO;
import com.example.ForgeX.dto.UserResponseDTO;

public interface UserMangementService {
    UserResponseDTO getAllUsers(Integer pageNumber, Integer pageSize,
            String sortBy, String sortOrder);
    
    UserResponseDTO getUserByName(String username,Integer pageNumber, Integer pageSize,
            String sortBy, String sortOrder);

  UserDTO  getUserByUserId(Long userId);
}
