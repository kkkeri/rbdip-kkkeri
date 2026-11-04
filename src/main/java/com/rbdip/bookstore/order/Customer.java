package com.rbdip.bookstore.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customers")
public class Customer {

    private static final int FULL_NAME_MAX_LENGTH = 255;
    private static final int ADDRESS_MAX_LENGTH = 500;
    private static final int PHONE_MAX_LENGTH = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", length = FULL_NAME_MAX_LENGTH)
    private String firstName;

    @Column(name = "last_name", length = FULL_NAME_MAX_LENGTH)
    private String lastName;

    @Column(length = ADDRESS_MAX_LENGTH)
    private String address;

    @Column(length = PHONE_MAX_LENGTH)
    private String phone;

    protected Customer() {
        // Конструктор для JPA.
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return lastName.isEmpty() ? firstName : firstName + " " + lastName;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }
}