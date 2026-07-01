package kz.ask.shared.infrastructure.uuid;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

public final class UuidV7 {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int VERSION = 7;
    private static final int VARIANT = 2;

    private UuidV7() {
    }

    public static UUID create() {
        byte[] value = new byte[16];
        byte[] random = new byte[16];
        RANDOM.nextBytes(random);

        long timestamp = Instant.now().toEpochMilli();
        value[0] = (byte) (timestamp >>> 40);
        value[1] = (byte) (timestamp >>> 32);
        value[2] = (byte) (timestamp >>> 24);
        value[3] = (byte) (timestamp >>> 16);
        value[4] = (byte) (timestamp >>> 8);
        value[5] = (byte) timestamp;
        value[6] = (byte) ((VERSION << 4) | (random[6] & 0x0F));
        value[7] = random[7];
        value[8] = (byte) ((VARIANT << 6) | (random[8] & 0x3F));
        for (int i = 9; i < value.length; i++) {
            value[i] = random[i];
        }

        return UUID.fromString(HexFormat.of().formatHex(value, 0, 4) + "-"
                + HexFormat.of().formatHex(value, 4, 6) + "-"
                + HexFormat.of().formatHex(value, 6, 8) + "-"
                + HexFormat.of().formatHex(value, 8, 10) + "-"
                + HexFormat.of().formatHex(value, 10, 16));
    }
}
