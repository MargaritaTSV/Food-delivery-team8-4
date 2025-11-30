package com.team8.fooddelivery.servlet.shop;

import com.team8.fooddelivery.model.product.Product;
import com.team8.fooddelivery.model.product.ProductCategory;
import com.team8.fooddelivery.service.ShopProductService;
import com.team8.fooddelivery.service.impl.ShopProductServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@WebServlet("/products/*")
public class ProductServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(ProductServlet.class);
    private final ShopProductService productService = new ShopProductServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if ("/list".equals(pathInfo)) {
            handleProductList(request, response);
        } else if ("/add-form".equals(pathInfo)) {
            handleAddForm(request, response);
        } else if ("/edit-form".equals(pathInfo)) {
            handleEditForm(request, response);
        } else {
            response.sendError(404);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        try {
            if ("/add".equals(pathInfo)) {
                handleAddProduct(request, response);
            } else if ("/update".equals(pathInfo)) {
                handleUpdateProduct(request, response);
            } else if ("/delete".equals(pathInfo)) {
                handleDeleteProduct(request, response);
            } else if ("/toggle-availability".equals(pathInfo)) {
                handleToggleAvailability(request, response);
            } else {
                response.sendError(404);
            }
        } catch (Exception e) {
            log.error("Error", e);
            response.sendError(500);
        }
    }

    /**
     * Список товаров магазина
     */
    private void handleProductList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        Long shopId = (Long) request.getSession().getAttribute("shopId");
        if (shopId == null) {
            response.sendRedirect(request.getContextPath() + "/shop/login");
            return;
        }
        
        try {
            List<Product> products = productService.getShopProducts(shopId);
            request.setAttribute("products", products);
            request.setAttribute("shopId", shopId);
            request.getRequestDispatcher("/WEB-INF/jsp/shop/products-list.jsp").forward(request, response);
        } catch (Exception e) {
            log.error("Error loading products", e);
            response.sendError(500);
        }
    }

    /**
     * Форма добавления товара
     */
    private void handleAddForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        Long shopId = (Long) request.getSession().getAttribute("shopId");
        if (shopId == null) {
            response.sendRedirect(request.getContextPath() + "/shop/login");
            return;
        }
        
        request.setAttribute("categories", ProductCategory.values());
        request.getRequestDispatcher("/WEB-INF/jsp/shop/product-form.jsp").forward(request, response);
    }

    /**
     * Форма редактирования товара
     */
    private void handleEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        Long shopId = (Long) request.getSession().getAttribute("shopId");
        if (shopId == null) {
            response.sendRedirect(request.getContextPath() + "/shop/login");
            return;
        }
        
        try {
            Long productId = Long.parseLong(request.getParameter("id"));
            // TODO: Получить товар по ID
            // Product product = productService.getProductById(productId);
            
            request.setAttribute("categories", ProductCategory.values());
            request.setAttribute("product", null); // TODO: product
            request.setAttribute("isEdit", true);
            request.getRequestDispatcher("/WEB-INF/jsp/shop/product-form.jsp").forward(request, response);
        } catch (Exception e) {
            log.error("Error loading product form", e);
            response.sendError(500);
        }
    }

    /**
     * Добавить новый товар
     */
    private void handleAddProduct(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        Long shopId = (Long) request.getSession().getAttribute("shopId");
        if (shopId == null) {
            response.sendRedirect(request.getContextPath() + "/shop/login");
            return;
        }
        
        try {
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            double price = Double.parseDouble(request.getParameter("price"));
            String categoryStr = request.getParameter("category");
            ProductCategory category = ProductCategory.valueOf(categoryStr);
            boolean isAvailable = request.getParameter("isAvailable") != null;
            int cookingTime = Integer.parseInt(request.getParameter("cookingTime") != null ?
                    request.getParameter("cookingTime") : "0");

            Product product = Product.builder()
                    .name(name)
                    .description(description)
                    .price(price)
                    .category(category)
                    .available(isAvailable)
                    .cookingTimeMinutes(Duration.ofMinutes(cookingTime))
                    .build();
            
            Product savedProduct = productService.addProduct(shopId, product);
            log.info("Product added: {} for shop {}", savedProduct.getProductId(), shopId);
            
            response.sendRedirect(request.getContextPath() + "/products/list?added=true");
        } catch (Exception e) {
            log.error("Error adding product", e);
            request.setAttribute("error", "Ошибка при добавлении товара: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/jsp/shop/product-form.jsp").forward(request, response);
        }
    }

    /**
     * Обновить товар
     */
    private void handleUpdateProduct(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        Long shopId = (Long) request.getSession().getAttribute("shopId");
        if (shopId == null) {
            response.sendRedirect(request.getContextPath() + "/shop/login");
            return;
        }
        
        try {
            Long productId = Long.parseLong(request.getParameter("productId"));
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            double price = Double.parseDouble(request.getParameter("price"));
            String categoryStr = request.getParameter("category");
            ProductCategory category = ProductCategory.valueOf(categoryStr);
            boolean isAvailable = request.getParameter("isAvailable") != null;
            int cookingTime = Integer.parseInt(request.getParameter("cookingTime") != null ?
                    request.getParameter("cookingTime") : "0");

            Product product = Product.builder()
                    .productId(productId)
                    .name(name)
                    .description(description)
                    .price(price)
                    .category(category)
                    .available(isAvailable)
                    .cookingTimeMinutes(Duration.ofMinutes(cookingTime))
                    .build();
            
            Product updatedProduct = productService.updateProduct(shopId, productId, product);
            log.info("Product updated: {} for shop {}", updatedProduct.getProductId(), shopId);
            
            response.sendRedirect(request.getContextPath() + "/products/list?updated=true");
        } catch (Exception e) {
            log.error("Error updating product", e);
            request.setAttribute("error", "Ошибка при обновлении товара: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/jsp/shop/product-form.jsp").forward(request, response);
        }
    }

    /**
     * Удалить товар
     */
    private void handleDeleteProduct(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        Long shopId = (Long) request.getSession().getAttribute("shopId");
        if (shopId == null) {
            response.sendRedirect(request.getContextPath() + "/shop/login");
            return;
        }
        
        try {
            Long productId = Long.parseLong(request.getParameter("productId"));
            productService.deleteProduct(shopId, productId);
            log.info("Product deleted: {} from shop {}", productId, shopId);
            
            response.sendRedirect(request.getContextPath() + "/products/list?deleted=true");
        } catch (Exception e) {
            log.error("Error deleting product", e);
            response.sendRedirect(request.getContextPath() + "/products/list?error=delete_failed");
        }
    }

    /**
     * Переключить доступность товара
     */
    private void handleToggleAvailability(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        Long shopId = (Long) request.getSession().getAttribute("shopId");
        if (shopId == null) {
            response.sendRedirect(request.getContextPath() + "/shop/login");
            return;
        }
        
        try {
            Long productId = Long.parseLong(request.getParameter("productId"));
            boolean isAvailable = Boolean.parseBoolean(request.getParameter("available"));
            
            productService.updateProductAvailability(shopId, productId, !isAvailable);
            log.info("Product availability toggled: {} for shop {}", productId, shopId);
            
            response.sendRedirect(request.getContextPath() + "/products/list?availability_changed=true");
        } catch (Exception e) {
            log.error("Error toggling availability", e);
            response.sendRedirect(request.getContextPath() + "/products/list?error=toggle_failed");
        }
    }
}
