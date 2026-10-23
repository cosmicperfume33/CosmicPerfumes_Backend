package com.example.ForgeX.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.ForgeX.dto.UserDTO;
import com.example.ForgeX.dto.UserResponseDTO;
import com.example.ForgeX.exceptions.ResourceNotFoundException;
import com.example.ForgeX.model.User;
import com.example.ForgeX.repository.UserRepository;

@Service
public class UserMangementServiceImpl implements UserMangementService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public UserResponseDTO getAllUsers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Page<User> page = userRepository.findAll(buildPage(pageNumber, pageSize, sortBy, sortOrder));
        List<UserDTO> users = page.getContent().stream().map(user -> modelMapper.map(user, UserDTO.class)).toList();

        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setContent(users);
        userResponseDTO.setPageNumber(page.getNumber());
        userResponseDTO.setPageSize(page.getSize());
        userResponseDTO.setTotalElements(page.getTotalElements());
        userResponseDTO.setLastPage(page.isLast());
        userResponseDTO.setTotalPages(page.getTotalPages());
        return userResponseDTO;
    }

    @Override
    public UserResponseDTO getUserByName(String username, Integer pageNumber, Integer pageSize, String sortBy,
            String sortOrder) {
        PageRequest pageRequest = buildPage(pageNumber, pageSize, sortBy, sortOrder);
        Page<User> page = userRepository.findByUserNameContainingIgnoreCase(username,pageRequest);
        List<UserDTO> users = page.getContent().stream().map(user -> modelMapper.map(user, UserDTO.class)).toList();

        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setContent(users);
        userResponseDTO.setPageNumber(page.getNumber());
        userResponseDTO.setPageSize(page.getSize());
        userResponseDTO.setTotalElements(page.getTotalElements());
        userResponseDTO.setLastPage(page.isLast());
        userResponseDTO.setTotalPages(page.getTotalPages());
        return userResponseDTO;
    }
    
    @Override 
    public UserDTO getUserByUserId(Long userId){
        User user=userRepository.findById(userId)
               .orElseThrow(()->new ResourceNotFoundException("User","userId",userId));
        UserDTO userDTO=modelMapper.map(user, UserDTO.class);
        return  userDTO;
    }

    private PageRequest buildPage(Integer pageNumber, Integer pageSize,
            String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return PageRequest.of(pageNumber, pageSize, sort);
    }

}
