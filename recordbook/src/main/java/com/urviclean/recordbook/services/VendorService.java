package com.urviclean.recordbook.services;

import com.urviclean.recordbook.exception.ResourceNotFoundException;
import com.urviclean.recordbook.models.Vendor;
import com.urviclean.recordbook.repositories.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing vendor (raw-material supplier) data.
 */
@Service
@Transactional
public class VendorService {

    @Autowired
    private VendorRepository vendorRepository;

    public List<Vendor> getAll() {
        return vendorRepository.findAll();
    }

    public Vendor getById(Long id) {
        return vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", id));
    }

    public Vendor save(Vendor vendor) {
        return vendorRepository.save(vendor);
    }

    public void delete(Long id) {
        if (!vendorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vendor", "id", id);
        }
        vendorRepository.deleteById(id);
    }
}
