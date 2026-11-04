package com.rbdip.bookstore.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    default Customer findOrCreate(String fullName, String address, String phone) {
        int separator = fullName.indexOf(' ');

        if (separator < 0 || separator == fullName.length() - 1) {
            return saveByNameParts(fullName, "", address, phone);
        }

        String firstName = fullName.substring(0, separator);
        String lastName = fullName.substring(separator + 1);
        return saveByNameParts(firstName, lastName, address, phone);
    }

    @Query(value = """
            WITH saved_customer AS (
                INSERT INTO customers (first_name, last_name, address, phone)
                VALUES (:firstName, :lastName, :address, :phone)
                ON CONFLICT (first_name, last_name, address, phone)
                DO UPDATE SET first_name = EXCLUDED.first_name
                RETURNING id, first_name, last_name, address, phone
            )
            SELECT * FROM saved_customer
            """, nativeQuery = true)
    Customer saveByNameParts(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("address") String address,
            @Param("phone") String phone);
}