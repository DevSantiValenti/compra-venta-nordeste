package com.marketplace.repository;

import com.marketplace.entity.Product;
import com.marketplace.entity.ProductCondition;
import com.marketplace.entity.ProductStatus;
import com.marketplace.entity.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @EntityGraph(attributePaths = {"category", "seller", "images"})
    Optional<Product> findBySlugAndStatus(String slug, ProductStatus status);

    @EntityGraph(attributePaths = {"category", "seller", "images"})
    Optional<Product> findBySlugAndStatusNot(String slug, ProductStatus status);

    boolean existsBySlug(String slug);

    Page<Product> findBySellerAndStatusNotOrderByCreatedAtDesc(User seller, ProductStatus status, Pageable pageable);

    long countBySellerAndStatus(User seller, ProductStatus status);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByStatus(ProductStatus status);

    long countByFeaturedTrue();

    @EntityGraph(attributePaths = {"category", "seller", "images"})
    @Query("""
        select p from Product p
        where p.status = com.marketplace.entity.ProductStatus.ACTIVE
          and (:categoryId is null or p.category.id = :categoryId)
          and (:q is null or lower(p.title) like lower(concat('%', :q, '%')) or lower(p.description) like lower(concat('%', :q, '%')))
          and (:minPrice is null or p.price >= :minPrice)
          and (:maxPrice is null or p.price <= :maxPrice)
          and (:city is null or lower(p.city) like lower(concat('%', :city, '%')))
          and (:province is null or lower(p.province) like lower(concat('%', :province, '%')))
          and (:condition is null or p.condition = :condition)
          and (:brand is null or lower(p.brand) like lower(concat('%', :brand, '%')))
          and (:size is null or lower(p.size) like lower(concat('%', :size, '%')))
        order by p.featured desc, p.createdAt desc
        """)
    Page<Product> searchActive(
        @Param("q") String q,
        @Param("categoryId") Long categoryId,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("city") String city,
        @Param("province") String province,
        @Param("condition") ProductCondition condition,
        @Param("brand") String brand,
        @Param("size") String size,
        Pageable pageable
    );

    @EntityGraph(attributePaths = {"category", "seller", "images"})
    Page<Product> findByCategorySlugAndStatusOrderByFeaturedDescCreatedAtDesc(String slug, ProductStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "seller", "images"})
    List<Product> findTop10ByStatusOrderByViewsCountDesc(ProductStatus status);

    @EntityGraph(attributePaths = {"category", "seller", "images"})
    List<Product> findTop10ByStatusOrderByWhatsappClicksCountDesc(ProductStatus status);

    @Query("select p.category.name, count(p) from Product p group by p.category.name order by count(p) desc")
    List<Object[]> countByCategory();

    @Query("select coalesce(sum(p.viewsCount),0) from Product p")
    long sumViewsCount();

    @Query("select coalesce(sum(p.whatsappClicksCount),0) from Product p")
    long sumWhatsappClicksCount();

    @Query("select coalesce(sum(p.viewsCount),0) from Product p where p.seller = :seller and p.status <> com.marketplace.entity.ProductStatus.DELETED")
    long sumViewsBySeller(@Param("seller") User seller);

    @Query("select coalesce(sum(p.whatsappClicksCount),0) from Product p where p.seller = :seller and p.status <> com.marketplace.entity.ProductStatus.DELETED")
    long sumWhatsappClicksBySeller(@Param("seller") User seller);
}
