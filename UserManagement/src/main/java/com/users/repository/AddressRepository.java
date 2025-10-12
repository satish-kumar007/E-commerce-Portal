package com.users.repository;

import com.users.entity.Address;
import com.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUser(User user);

    List<Address> findByUserId(Long userId);

    List<Address> findByUserAndAddressType(User user, Address.AddressType addressType);

    List<Address> findByUserIdAndAddressType(Long userId, Address.AddressType addressType);

    List<Address> findByUserAndIsDefault(User user, boolean isDefault);

    List<Address> findByUserIdAndIsDefault(Long userId, boolean isDefault);

    List<Address> findByUserAndIsActive(User user, boolean isActive);

    List<Address> findByUserIdAndIsActive(Long userId, boolean isActive);

    @Query("SELECT a FROM Address a WHERE a.user = :user AND a.addressType = :addressType AND a.isActive = true")
    Optional<Address> findActiveAddressByType(@Param("user") User user, @Param("addressType") Address.AddressType addressType);

    @Query("SELECT a FROM Address a WHERE a.user = :user AND a.isDefault = true AND a.isActive = true")
    Optional<Address> findDefaultAddress(@Param("user") User user);

    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.isDefault = true AND a.isActive = true")
    Optional<Address> findDefaultAddressByUserId(@Param("userId") Long userId);

    void deleteByUser(User user);

    void deleteByUserId(Long userId);

    @Query("SELECT a FROM Address a WHERE a.user = :user AND a.isActive = true ORDER BY a.isDefault DESC")
    List<Address> findActiveAddressesOrderedByDefault(@Param("user") User user);

    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.isActive = true ORDER BY a.isDefault DESC")
    List<Address> findActiveAddressesByUserIdOrderedByDefault(@Param("userId") Long userId);
}
