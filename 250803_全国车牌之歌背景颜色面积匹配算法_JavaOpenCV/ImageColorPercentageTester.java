import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class ImageColorPercentageTester {
    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    public static void main(String[] args) {
        String imagePath = "C:\\Users\\astri\\Desktop\\Program\\Mianji_OpenCV\\src\\image_96.png";
        int targetGreen = 175;
        int targetRed = 0;
        int tolerance = 26;

        double percentage = matchDualChannels(imagePath, targetGreen, targetRed, tolerance);
        System.out.printf("匹配占比: %.2f%% (G=%d±%d, R=%d±%d)\n",
                percentage, targetGreen, tolerance, targetRed, tolerance);
    }

    public static double matchDualChannels(String imagePath, int targetGreen,
                                           int targetRed, int tolerance) {
        // 读图片
        Mat image = Imgcodecs.imread(imagePath);
        if (image.empty()) {
            System.err.println("无法加载图片: " + imagePath);
            return 0;
        }
        // 分离通道
        List<Mat> channels = new ArrayList<>();
        Core.split(image, channels);
        Mat greenChannel = channels.get(1);
        Mat redChannel = channels.get(2);


        Mat greenMask = new Mat(), redMask = new Mat(), finalMask = new Mat();
        Core.inRange(greenChannel, new Scalar(targetGreen - tolerance),
                new Scalar(targetGreen + tolerance), greenMask);
        Core.inRange(redChannel, new Scalar(targetRed - tolerance),
                new Scalar(targetRed + tolerance/5.0), redMask);
        Core.bitwise_and(greenMask, redMask, finalMask);

        // 显示结果
        Mat resizedMask = new Mat();
        Imgproc.resize(finalMask, resizedMask,
                new Size(finalMask.cols()*0.5, finalMask.rows()*0.5));
        HighGui.imshow("匹配结果(白色=目标区域)", resizedMask);
        HighGui.waitKey();
        HighGui.destroyAllWindows();

        // return百分比
        return Core.countNonZero(finalMask) * 100.0 / (image.rows() * image.cols());
    }
}