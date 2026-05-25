package com.marketplace.service;

import com.marketplace.entity.Product;
import com.marketplace.entity.ProductFavorite;
import com.marketplace.entity.ProductStatus;
import com.marketplace.entity.User;
import com.marketplace.repository.ProductFavoriteRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FavoriteService {
    private final ProductFavoriteRepository productFavoriteRepository;
    private final ProductService productService;

    public FavoriteService(ProductFavoriteRepository productFavoriteRepository, ProductService productService) {
        this.productFavoriteRepository = productFavoriteRepository;
        this.productService = productService;
    }

    public boolean isFavorite(User user, Product product) {
        return user != null && productFavoriteRepository.existsByUserAndProduct(user, product);
    }

    public List<Product> productsFor(User user) {
        return productFavoriteRepository.findByUserOrderByCreatedAtDesc(user).stream()
            .map(ProductFavorite::getProduct)
            .filter(product -> product.getStatus() != ProductStatus.DELETED)
            .toList();
    }

    @Transactional
    public void add(User user, String slug) {
        Product product = productService.visibleBySlug(slug);
        if (productFavoriteRepository.existsByUserAndProduct(user, product)) {
            return;
        }
        ProductFavorite favorite = new ProductFavorite();
        favorite.setUser(user);
        favorite.setProduct(product);
        productFavoriteRepository.save(favorite);
    }

    @Transactional
    public void remove(User user, String slug) {
        Product product = productService.visibleBySlug(slug);
        productFavoriteRepository.findByUserAndProduct(user, product)
            .ifPresent(productFavoriteRepository::delete);
    }
}
