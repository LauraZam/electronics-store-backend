package com.example.shop.service;

import com.example.shop.dto.request.AddressRequest;
import com.example.shop.dto.request.UpdateProfileRequest;
import com.example.shop.dto.response.AddressResponse;
import com.example.shop.dto.response.UserResponse;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.model.entity.Address;
import com.example.shop.model.entity.User;
import com.example.shop.repository.AddressRepository;
import com.example.shop.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    public UserService(UserRepository userRepository, AddressRepository addressRepository) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(String email) {
        User user = getUserByEmail(email);
        return new UserResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getPhone(), user.getRole());
    }

    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = getUserByEmail(email);
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(request.phone());
        User saved = userRepository.save(user);
        return new UserResponse(saved.getId(), saved.getEmail(), saved.getFirstName(), saved.getLastName(), saved.getPhone(), saved.getRole());
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> getUserAddresses(String email) {
        User user = getUserByEmail(email);
        return addressRepository.findByUserId(user.getId()).stream().map(this::mapAddress).toList();
    }

    @Transactional
    public AddressResponse addAddress(String email, AddressRequest request) {
        User user = getUserByEmail(email);
        Address address = new Address();
        address.setUser(user);
        copyAddressFields(address, request);
        return mapAddress(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse updateAddress(String email, Long addressId, AddressRequest request) {
        User user = getUserByEmail(email);
        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        copyAddressFields(address, request);
        return mapAddress(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(String email, Long addressId) {
        User user = getUserByEmail(email);
        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        addressRepository.delete(address);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void copyAddressFields(Address address, AddressRequest request) {
        address.setTitle(request.title());
        address.setRecipientName(request.recipientName());
        address.setPhone(request.phone());
        address.setCountry(request.country());
        address.setCity(request.city());
        address.setStreet(request.street());
        address.setZipCode(request.zipCode());
        address.setIsDefault(request.isDefault() != null && request.isDefault());
    }

    private AddressResponse mapAddress(Address address) {
        return new AddressResponse(
                address.getId(), address.getTitle(), address.getRecipientName(),
                address.getPhone(), address.getCountry(), address.getCity(),
                address.getStreet(), address.getZipCode(), address.getIsDefault()
        );
    }
}