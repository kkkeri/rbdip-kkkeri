package com.rbdip.bookstore.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query(value = """
            WITH saved_customer AS (
                INSERT INTO customers (full_name, address, phone)
                VALUES (:fullName, :address, :phone)
                ON CONFLICT (full_name, address, phone)
                DO UPDATE SET full_name = EXCLUDED.full_name
                RETURNING *
            )
            SELECT * FROM saved_customer
            """, nativeQuery = true)
    Customer findOrCreate(
            @Param("fullName") String fullName,
            @Param("address") String address,
            @Param("phone") String phone);
}