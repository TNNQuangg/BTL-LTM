package com.nhom8.server;

import com.nhom8.common.dto.AvatarData;
import com.nhom8.server.model.User;
import com.nhom8.server.repository.UserRepository;
import com.nhom8.server.service.AuthService;
import com.nhom8.server.util.AvatarValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class AvatarProfileTest {
    @Autowired UserRepository users;
    @Autowired TestEntityManager entities;

    private String png(int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        var random = new java.util.Random(8);
        for (int y = 0; y < height; y++) for (int x = 0; x < width; x++)
            image.setRGB(x, y, 0xff000000 | random.nextInt(0x1000000));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(image, "png", bytes);
        return AvatarData.PNG_PREFIX + Base64.getEncoder().encodeToString(bytes.toByteArray());
    }

    @Test void savesFullImageAndStillAcceptsLegacyIcons() throws Exception {
        User user = users.saveAndFlush(new User("avatar_test", "password", "Avatar Test"));
        String image = png(256, 256);
        AuthService auth = new AuthService(users);
        auth.updateProfile(user.getUsername(), null, image, null, null);
        entities.flush(); entities.clear();
        assertEquals(image, auth.getProfile(user.getUsername()).getAvatar());
        auth.updateProfile(user.getUsername(), null, "8", null, null);
        entities.flush(); entities.clear();
        assertEquals("8", auth.getProfile(user.getUsername()).getAvatar());
    }

    @Test void rejectsCorruptOversizedAndWrongDimensionsWithoutChangingProfile() throws Exception {
        User user = users.saveAndFlush(new User("avatar_invalid", "password", "Avatar Invalid"));
        AuthService auth = new AuthService(users);
        String[] invalid = {"9", "file:///image.png", AvatarData.PNG_PREFIX + "broken!",
                png(32, 32), png(256, 128), AvatarData.PNG_PREFIX + "A".repeat(AvatarData.MAX_TEXT_LENGTH)};
        for (String value : invalid) {
            assertThrows(IllegalArgumentException.class,
                    () -> auth.updateProfile(user.getUsername(), "Changed Name", value, null, null));
        }
        entities.clear();
        assertEquals("1", auth.getProfile(user.getUsername()).getAvatar());
        assertEquals("Avatar Invalid", auth.getProfile(user.getUsername()).getDisplayName());
        assertThrows(IllegalArgumentException.class, () -> AvatarValidator.validate(png(1024, 1024)));
    }
}
