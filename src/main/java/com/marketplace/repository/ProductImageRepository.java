package com.marketplace.repository;

import com.marketplace.entity.Product;
import com.marketplace.entity.ProductImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductOrderByOrderIndexAsc(Product product);
}
