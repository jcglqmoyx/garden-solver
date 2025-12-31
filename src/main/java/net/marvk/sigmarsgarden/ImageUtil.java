package net.marvk.sigmarsgarden;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

public class ImageUtil {

    public static final int SIZE = 52;
    public static final int X_OFFSET = 33;
    public static final int X_DIST = 14;
    public static final int X_START = 1026;
    public static final int Y_DIST = 5;
    public static final int Y_START = 195;
    private static final int PADDING = 10;
    private static final int ROWS = 11;

    private ImageUtil() {
        throw new AssertionError(
            "No instances of utility class " + ImageUtil.class
        );
    }

    public static Mat bufferedImageToMat(final BufferedImage image) {
        final ByteArrayOutputStream byteArrayOutputStream =
            new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", byteArrayOutputStream);
            byteArrayOutputStream.flush();
            return Imgcodecs.imdecode(
                new MatOfByte(byteArrayOutputStream.toByteArray()),
                Imgcodecs.IMREAD_UNCHANGED
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static BufferedImage loadImageMonochrome(final Path path)
        throws IOException {
        return monochrome(loadImage(path));
    }

    public static BufferedImage loadImage(final Path path) throws IOException {
        return ImageIO.read(path.toFile());
    }

    public static BufferedImage shrink(final BufferedImage bufferedImage) {
        return bufferedImage.getSubimage(
            PADDING,
            PADDING,
            bufferedImage.getWidth() - PADDING * 2,
            bufferedImage.getHeight() - PADDING * 2
        );
    }

    public static BufferedImage monochrome(final BufferedImage read) {
        final BufferedImage result = new BufferedImage(
            read.getWidth(),
            read.getHeight(),
            BufferedImage.TYPE_BYTE_GRAY
        );

        final Graphics2D graphics = result.createGraphics();
        graphics.drawImage(read, 0, 0, null);

        return result;
    }

    public static BufferedImage[][] split(final BufferedImage image) {
        final BufferedImage[][] result = new BufferedImage[ROWS][];

        for (int y = 0; y < ROWS; y++) {
            final int rowOffset = rowOffset(y);

            final int xMax = xMax(rowOffset);
            final int yPixel = yPixel(y);

            result[y] = new BufferedImage[xMax];

            for (int x = 0; x < xMax; x++) {
                final int xPixel = xPixel(rowOffset, x);

                result[y][x] = image.getSubimage(xPixel, yPixel, SIZE, SIZE);
            }
        }

        return result;
    }

    private static int rowOffset(final int y) {
        return ROWS / 2 - Math.abs(y - ROWS / 2);
    }

    private static int xMax(final int rowOffset) {
        return ROWS / 2 + 1 + rowOffset;
    }

    private static int xPixel(final int rowOffset, final int x) {
        return X_START + (SIZE + X_DIST) * x - rowOffset * X_OFFSET;
    }

    private static int yPixel(final int y) {
        return Y_START + (SIZE + Y_DIST) * y;
    }
}
