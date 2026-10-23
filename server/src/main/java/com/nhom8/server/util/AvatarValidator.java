package com.nhom8.server.util;

import com.nhom8.common.dto.AvatarData;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.util.Base64;

/** Kiểm tra kích thước và nội dung ảnh trước khi lưu vào hồ sơ. */
public final class AvatarValidator {
    private AvatarValidator() {}

    public static String validate(String value) {
        String avatar = value.trim();
        if (avatar.matches("[1-8]")) return avatar;
        if (avatar.length() > AvatarData.MAX_TEXT_LENGTH || !avatar.startsWith(AvatarData.PNG_PREFIX))
            throw new IllegalArgumentException("Ảnh đại diện phải là ảnh PNG đã cắt, tối đa 512 KB.");
        try {
            byte[] bytes = Base64.getDecoder().decode(avatar.substring(AvatarData.PNG_PREFIX.length()));
            if (bytes.length > AvatarData.MAX_BYTES) throw new IllegalArgumentException();
            try (var input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
                var readers = ImageIO.getImageReaders(input);
                if (!readers.hasNext()) throw new IllegalArgumentException();
                var reader = readers.next();
                try {
                    reader.setInput(input);
                    if (!reader.getFormatName().equalsIgnoreCase("png")
                            || reader.getWidth(0) != AvatarData.OUTPUT_SIZE
                            || reader.getHeight(0) != AvatarData.OUTPUT_SIZE
                            || reader.read(0) == null) throw new IllegalArgumentException();
                } finally { reader.dispose(); }
            }
            return avatar;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Ảnh đại diện không hợp lệ. Vui lòng chọn lại ảnh và cắt thành hình vuông 256 × 256.");
        }
    }
}
