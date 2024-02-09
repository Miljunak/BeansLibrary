package pl.edu.agh.to.kolektyw_glazurniczy.gui;

import javafx.concurrent.Task;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ImageCacheManager {
    private final Map<String, Image> imageCache = new ConcurrentHashMap<>();

    Logger logger = LoggerFactory.getLogger(ImageCacheManager.class);

    public Image getImage(String imageUrl) {
        if (imageCache.containsKey(imageUrl)) {
            return imageCache.get(imageUrl);
        } else {
            Image image;
            String defaultImage = "imgs/no_image.png";
            if (imageUrl == null || imageUrl.isEmpty()) {
                image = new Image(defaultImage, true);
            } else {
                try {
                    image = new Image(imageUrl, true);
                } catch (IllegalArgumentException exception) {
                    logger.error("Failed to get image {}", exception.getMessage());
                    image = new Image(defaultImage);
                }
            }
            imageCache.put(imageUrl, image);
            return image;
        }
    }
}
