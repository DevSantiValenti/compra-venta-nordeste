package com.marketplace.service;

import com.marketplace.config.AppProperties;
import jakarta.annotation.PostConstruct;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserAvatarStorage {
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png");
    private static final int AVATAR_SIZE = 360;
    private static final String PUBLIC_PREFIX = "/uploads/avatars/";

    private final AppProperties appProperties;
    private Path root;

    public UserAvatarStorage(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @PostConstruct
    void init() throws IOException {
        root = Path.of(appProperties.upload().avatarsDir()).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    public String replace(MultipartFile file, String previousAvatarUrl) {
        if (file == null || file.isEmpty()) {
            return previousAvatarUrl;
        }
        validate(file);
        String avatarUrl = save(file);
        deletePrevious(previousAvatarUrl);
        return avatarUrl;
    }

    private String save(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage source = ImageIO.read(inputStream);
            if (source == null) {
                throw new IllegalArgumentException("No se pudo leer la imagen. Subi un archivo JPG o PNG valido.");
            }
            String fileName = UUID.randomUUID() + ".jpg";
            writeJpg(cropSquare(source), root.resolve(fileName));
            return PUBLIC_PREFIX + fileName;
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo guardar la foto de perfil", ex);
        }
    }

    private void validate(MultipartFile file) {
        if (file.getSize() > appProperties.upload().maxFileSize().toBytes()) {
            throw new IllegalArgumentException("La foto supera el peso maximo permitido");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Formato no permitido. Subi una foto JPG o PNG.");
        }
    }

    private BufferedImage cropSquare(BufferedImage source) {
        int side = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - side) / 2;
        int y = (source.getHeight() - side) / 2;
        BufferedImage target = new BufferedImage(AVATAR_SIZE, AVATAR_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = target.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, AVATAR_SIZE, AVATAR_SIZE);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(source, 0, 0, AVATAR_SIZE, AVATAR_SIZE, x, y, x + side, y + side, null);
        graphics.dispose();
        return target;
    }

    private void writeJpg(BufferedImage image, Path path) throws IOException {
        ImageIO.write(image, "jpg", path.toFile());
    }

    private void deletePrevious(String previousAvatarUrl) {
        if (previousAvatarUrl == null || !previousAvatarUrl.startsWith(PUBLIC_PREFIX)) {
            return;
        }
        Path candidate = root.resolve(previousAvatarUrl.substring(PUBLIC_PREFIX.length())).normalize();
        if (!candidate.startsWith(root)) {
            return;
        }
        try {
            Files.deleteIfExists(candidate);
        } catch (IOException ignored) {
            // Si no se puede borrar, no bloqueamos la actualización del perfil.
        }
    }
}
