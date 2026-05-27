package com.marketplace.service;

import com.marketplace.config.AppProperties;
import com.marketplace.entity.Product;
import com.marketplace.entity.ProductImage;
import com.marketplace.repository.ProductImageRepository;
import jakarta.annotation.PostConstruct;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductImageStorage {
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png");

    private final AppProperties appProperties;
    private final ProductImageRepository productImageRepository;
    private Path root;

    public ProductImageStorage(AppProperties appProperties, ProductImageRepository productImageRepository) {
        this.appProperties = appProperties;
        this.productImageRepository = productImageRepository;
    }

    @PostConstruct
    void init() throws IOException {
        root = Path.of(appProperties.upload().productsDir()).toAbsolutePath();
        Files.createDirectories(root);
    }

    public List<ProductImage> store(Product product, MultipartFile[] files) {
        List<ProductImage> saved = new ArrayList<>();
        if (files == null) {
            return saved;
        }
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            if (file == null || file.isEmpty()) {
                continue;
            }
            validate(file);
            saved.add(saveImage(product, file, i, saved.isEmpty()));
        }
        return saved;
    }

    public void deleteForProduct(Product product) {
        List<ProductImage> images = productImageRepository.findByProductOrderByOrderIndexAsc(product);
        images.forEach(this::deleteFiles);
        productImageRepository.deleteAll(images);
        product.getImages().clear();
    }

    private ProductImage saveImage(Product product, MultipartFile file, int order, boolean main) {
        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage source = ImageIO.read(inputStream);
            if (source == null) {
                throw new IllegalArgumentException("No se pudo leer la imagen. Subi un archivo JPG o PNG valido.");
            }
            String base = UUID.randomUUID().toString();
            String fileName = base + ".jpg";
            String thumbName = base + "-thumb.jpg";
            writeJpg(resize(source, appProperties.upload().imageMaxWidth()), root.resolve(fileName));
            writeJpg(resize(source, appProperties.upload().thumbnailWidth()), root.resolve(thumbName));

            ProductImage image = new ProductImage();
            image.setProduct(product);
            image.setFileName(fileName);
            image.setFilePath("/uploads/products/" + fileName);
            image.setOrderIndex(order);
            image.setMainImage(main);
            return productImageRepository.save(image);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo guardar la imagen", ex);
        }
    }

    private void validate(MultipartFile file) {
        if (file.getSize() > appProperties.upload().maxFileSize().toBytes()) {
            throw new IllegalArgumentException("La imagen supera el peso máximo permitido");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Formato no permitido. Subi imagenes JPG o PNG.");
        }
    }

    private BufferedImage resize(BufferedImage source, int maxWidth) {
        int targetWidth = Math.min(source.getWidth(), maxWidth);
        int targetHeight = (int) ((double) source.getHeight() * targetWidth / source.getWidth());
        BufferedImage target = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = target.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, targetWidth, targetHeight);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        graphics.dispose();
        return target;
    }

    private void writeJpg(BufferedImage image, Path path) throws IOException {
        ImageIO.write(image, "jpg", path.toFile());
    }

    private void deleteFiles(ProductImage image) {
        deleteIfInsideRoot(image.getFileName());
        String thumbName = image.getFileName().replaceFirst("\\.jpg$", "-thumb.jpg");
        deleteIfInsideRoot(thumbName);
    }

    private void deleteIfInsideRoot(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return;
        }
        Path candidate = root.resolve(fileName).normalize();
        if (!candidate.startsWith(root)) {
            return;
        }
        try {
            Files.deleteIfExists(candidate);
        } catch (IOException ignored) {
            // No bloqueamos la eliminación lógica si el archivo físico ya no existe o no se puede borrar.
        }
    }
}
