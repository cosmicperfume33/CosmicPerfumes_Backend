package com.example.ForgeX.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ForgeX.config.AppConstants;
import com.example.ForgeX.dto.UserDTO;
import com.example.ForgeX.dto.UserResponseDTO;
import com.example.ForgeX.service.UserMangementService;

@RestController
@RequestMapping("/api")
public class UserManagement {
    
    @Autowired 
    private UserMangementService userMangementService;
   
    @GetMapping ("/admin/users")
    public ResponseEntity<UserResponseDTO>getAllUsers(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_USERS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {
                UserResponseDTO response = userMangementService.getAllUsers(pageNumber, pageSize, sortBy, sortOrder);

        return new ResponseEntity<>(response,HttpStatus.OK);
    }
       @GetMapping ("/admin/users/{username}")
    public ResponseEntity<UserResponseDTO>getAllUsers(@PathVariable String username,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_USERS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {
                UserResponseDTO response = userMangementService.getUserByName(username,pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }     
    
     @GetMapping ("/admin/users/userId/{userId}")
     public ResponseEntity<UserDTO> getUserByuserId(@PathVariable  Long userId){
        UserDTO userDTO=userMangementService.getUserByUserId(userId);
        return  new ResponseEntity<>(userDTO,HttpStatus.OK);
     }
    //  @DeleteMapping("/admin/users/user/{userId}")
    //  public ResponseEntity<String> deleteUserByuserId(@PathVariable("userId")Long userId){
    //      String deletedUser=userMangementService.deleteUserbyId(userId);
    //     return new ResponseEntity<>(deletedUser,HttpStatus.OK);
    //  }



}
