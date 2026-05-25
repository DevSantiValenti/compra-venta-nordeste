package com.marketplace.service;

import com.marketplace.entity.Product;
import java.util.List;

public record AdminStats(
    long totalUsers,
    long usersToday,
    long usersWeek,
    long usersMonth,
    long totalProducts,
    long productsToday,
    long productsWeek,
    long productsMonth,
    long activeProducts,
    long hiddenProducts,
    long soldProducts,
    long featuredProducts,
    long totalSiteViews,
    long siteViewsToday,
    long siteViewsWeek,
    long siteViewsMonth,
    long totalProductViews,
    long totalWhatsappClicks,
    long pendingReports,
    long blockedUsers,
    List<Product> topViewed,
    List<Product> topWhatsapp,
    List<Object[]> categories
) {
}
