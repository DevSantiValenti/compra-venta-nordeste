package com.marketplace.service;

import com.marketplace.dto.ProductForm;
import com.marketplace.dto.ProductSearch;
import com.marketplace.entity.Category;
import com.marketplace.entity.Product;
import com.marketplace.entity.ProductStatus;
import com.marketplace.entity.User;
import com.marketplace.exception.NotFoundException;
import com.marketplace.repository.ProductRepository;
import com.marketplace.util.SlugUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final ProductImageStorage productImageStorage;

    public ProductService(ProductRepository productRepository, CategoryService categoryService, ProductImageStorage productImageStorage) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.productImageStorage = productImageStorage;
    }

    public Page<Product> search(ProductSearch search, Pageable pageable) {
        return productRepository.searchActive(
            blankToNull(search.getQ()),
            search.getCategoryId(),
            search.getMinPrice(),
            search.getMaxPrice(),
            blankToNull(search.getCity()),
            blankToNull(search.getProvince()),
            search.getCondition(),
            blankToNull(search.getBrand()),
            blankToNull(search.getSize()),
            pageable
        );
    }

    public Product activeBySlug(String slug) {
        return productRepository.findBySlugAndStatus(slug, ProductStatus.ACTIVE)
            .orElseThrow(() -> new NotFoundException("Publicación no encontrada"));
    }

    public Product visibleBySlug(String slug) {
        return productRepository.findBySlugAndStatusNot(slug, ProductStatus.DELETED)
            .orElseThrow(() -> new NotFoundException("Publicación no encontrada"));
    }

    public Page<Product> byCategory(String slug, Pageable pageable) {
        return productRepository.findByCategorySlugAndStatusOrderByFeaturedDescCreatedAtDesc(slug, ProductStatus.ACTIVE, pageable);
    }

    public Page<Product> mine(User seller, Pageable pageable) {
        return productRepository.findBySellerAndStatusNotOrderByCreatedAtDesc(seller, ProductStatus.DELETED, pageable);
    }

    public Product getOwned(Long id, User user) {
        Product product = productRepository.findById(id).orElseThrow(() -> new NotFoundException("Publicación no encontrada"));
        if (!product.getSeller().getId().equals(user.getId())) {
            throw new SecurityException("No podés editar esta publicación");
        }
        return product;
    }

    @Transactional
    public Product create(ProductForm form, MultipartFile[] images, User seller) {
        ensureCanPublish(seller);
        Product product = new Product();
        applyForm(product, form);
        product.setSeller(seller);
        product.setSlug(uniqueSlug(form.getTitle()));
        Product saved = productRepository.save(product);
        productImageStorage.store(saved, images);
        return saved;
    }

    @Transactional
    public Product update(Long id, ProductForm form, MultipartFile[] images, User seller) {
        ensureCanPublish(seller);
        Product product = getOwned(id, seller);
        applyForm(product, form);
        productImageStorage.store(product, images);
        return product;
    }

    @Transactional
    public void markSold(Long id, User seller) {
        getOwned(id, seller).setStatus(ProductStatus.SOLD);
    }

    @Transactional
    public void toggleActive(Long id, User seller) {
        Product product = getOwned(id, seller);
        if (product.getStatus() == ProductStatus.ACTIVE) {
            product.setStatus(ProductStatus.HIDDEN);
            return;
        }
        if (product.getStatus() == ProductStatus.HIDDEN) {
            product.setStatus(ProductStatus.ACTIVE);
        }
    }

    @Transactional
    public void deactivate(Long id, User seller) {
        getOwned(id, seller).setStatus(ProductStatus.DELETED);
    }

    @Transactional
    public void adminSetStatus(Long id, ProductStatus status) {
        productRepository.findById(id).orElseThrow(() -> new NotFoundException("Publicación no encontrada")).setStatus(status);
    }

    private void applyForm(Product product, ProductForm form) {
        Category category = categoryService.requireById(form.getCategoryId());
        product.setTitle(form.getTitle());
        product.setDescription(form.getDescription());
        product.setPrice(form.getPrice());
        product.setCondition(form.getCondition());
        product.setBrand(form.getBrand());
        product.setSize(form.getSize());
        product.setCity(form.getCity());
        product.setProvince(form.getProvince());
        product.setCategory(category);
    }

    private String uniqueSlug(String title) {
        String base = SlugUtil.slugify(title);
        String slug = base;
        int index = 2;
        while (productRepository.existsBySlug(slug)) {
            slug = base + "-" + index++;
        }
        return slug;
    }

    private void ensureCanPublish(User seller) {
        if (seller.isBlocked() || !seller.isEnabled()) {
            throw new SecurityException("Tu cuenta no está habilitada para publicar");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
