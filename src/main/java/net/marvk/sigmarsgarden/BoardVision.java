package net.marvk.sigmarsgarden;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class BoardVision {

    public Board readBoard(final BufferedImage image) {
        final BufferedImage[][] board = ImageUtil.split(image);

        final Tile[][] result = new Tile[board.length][];

        for (int i = 0; i < board.length; i++) {
            final BufferedImage[] tiles = board[i];
            result[i] = new Tile[tiles.length];
            for (int j = 0; j < tiles.length; j++) {
                final BufferedImage tile = tiles[j];
                result[i][j] = bestFitDebug(tile, i, j);
            }
        }

        return new Board(result);
    }

    private static Tile bestFitDebug(
        final BufferedImage bufferedImage,
        int row,
        int col
    ) {
        final Mat base = ImageUtil.bufferedImageToMat(bufferedImage);

        double bestScore = Double.NEGATIVE_INFINITY;
        Tile bestTile = null;
        Map<Tile, Double> scores = new HashMap<>();

        for (final Tile tile : Tile.values()) {
            final Mat[] mats = { tile.getInactiveMat(), tile.getActiveMat() };

            double tileBestScore = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < 2; i++) {
                final double score = compare(base, mats[i]);
                tileBestScore = Math.max(tileBestScore, score);
            }

            scores.put(tile, tileBestScore);

            if (tileBestScore > bestScore) {
                bestScore = tileBestScore;
                bestTile = tile;
            }
        }

        System.out.printf(
            "Tile at [%d,%d] - Best: %s (%.4f)%n",
            row,
            col,
            bestTile,
            bestScore
        );
        scores
            .entrySet()
            .stream()
            .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
            .limit(3)
            .forEach(entry ->
                System.out.printf(
                    "  %s: %.4f%n",
                    entry.getKey(),
                    entry.getValue()
                )
            );

        return bestTile;
    }

    private static double compare(final Mat mat1, final Mat mat2) {
        final Mat score = new Mat();

        Imgproc.matchTemplate(
            adjust(mat1),
            adjust(mat2),
            score,
            Imgproc.TM_CCOEFF_NORMED
        );

        return Core.minMaxLoc(score).maxVal;
    }

    private static Mat adjust(final Mat input) {
        final Mat equalized = new Mat(input.rows(), input.cols(), input.type());

        Imgproc.equalizeHist(input, equalized);

        return adjustMethod(equalized);
    }

    private static Mat adjustMethod(final Mat input) {
        final Mat blurred = new Mat();
        final Mat edges = new Mat();

        Imgproc.GaussianBlur(input, blurred, new org.opencv.core.Size(3, 3), 0);

        Imgproc.Laplacian(blurred, edges, CvType.CV_32F, 3);

        final Mat edges8u = new Mat();
        Core.convertScaleAbs(edges, edges8u);

        final Mat result = new Mat();
        Core.addWeighted(input, 0.7, edges8u, 0.3, 0, result);

        return result;
    }
}
