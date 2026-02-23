package com.urviclean.recordbook.models;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "chemicals")
public class Chemical {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chemical_id")
    private Long chemicalId;

    @Column(name = "chemical_name")
    private String chemicalName;

    @Convert(converter = ChemicalCategoryConverter.class)
    private ChemicalCategory category;

    private String unit;

    @Column(name = "purchase_rate")
    private BigDecimal purchaseRate;

    @Column(name = "transport_cost_per_unit")
    private BigDecimal transportCostPerUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "chemical", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductRecipe> productRecipes;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getChemicalId() {
        return chemicalId;
    }

    public void setChemicalId(Long chemicalId) {
        this.chemicalId = chemicalId;
    }

    public String getChemicalName() {
        return chemicalName;
    }

    public void setChemicalName(String chemicalName) {
        this.chemicalName = chemicalName;
    }

    public ChemicalCategory getCategory() {
        return category;
    }

    public void setCategory(ChemicalCategory category) {
        this.category = category;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getPurchaseRate() {
        return purchaseRate;
    }

    public void setPurchaseRate(BigDecimal purchaseRate) {
        this.purchaseRate = purchaseRate;
    }

    public BigDecimal getTransportCostPerUnit() {
        return transportCostPerUnit;
    }

    public void setTransportCostPerUnit(BigDecimal transportCostPerUnit) {
        this.transportCostPerUnit = transportCostPerUnit;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<ProductRecipe> getProductRecipes() {
        return productRecipes;
    }

    public void setProductRecipes(List<ProductRecipe> productRecipes) {
        this.productRecipes = productRecipes;
    }
}
