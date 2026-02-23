package com.urviclean.recordbook.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "salesmen")
public class Salesman {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "salesman_id")
    private Long salesmanId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "alias", nullable = false, unique = true)
    private String alias;

    @Column(name = "address")
    private String address;

    @Column(name = "contact_number")
    private String contactNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "salesman", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SalesmanExpense> expenses;

    @OneToMany(mappedBy = "salesman", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SalesRecord> salesRecords;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getSalesmanId() {
        return salesmanId;
    }

    public void setSalesmanId(Long salesmanId) {
        this.salesmanId = salesmanId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<SalesmanExpense> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<SalesmanExpense> expenses) {
        this.expenses = expenses;
    }

    public List<SalesRecord> getSalesRecords() {
        return salesRecords;
    }

    public void setSalesRecords(List<SalesRecord> salesRecords) {
        this.salesRecords = salesRecords;
    }
}
