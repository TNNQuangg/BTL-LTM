package com.nhom8.common.dto;

/** Quy ước lưu ảnh đại diện dùng chung giữa máy chủ và máy khách. */
public final class AvatarData {
    public static final String PNG_PREFIX = "data:image/png;base64,";
    public static final int OUTPUT_SIZE = 256;
    public static final int MAX_BYTES = 512 * 1024;
    public static final int MAX_TEXT_LENGTH = PNG_PREFIX.length() + ((MAX_BYTES + 2) / 3) * 4;

    private AvatarData() {}
}
