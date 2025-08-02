import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.text.DecimalFormat;

public class Main {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
//        String baseName = "anhui"; // 基础名称参数
//        int G = 170;     // 目标绿色通道值
//        int R = 10;        // 目标红色通道值
//        int tolerance = 24;        // 容差范围
//        int fps = 30;              // 视频帧率

//        analyzeVideoToSRT("anhui", 175, 5, 26,30);
//        analyzeVideoToSRT("fujian");
//        analyzeVideoToSRT("gansu");
        analyzeVideoToSRT("guangdong",165,5,26);
//        analyzeVideoToSRT("guangxi",180,5,22);
//        analyzeVideoToSRT("guizhou");
//        analyzeVideoToSRT("hainan");
//        analyzeVideoToSRT("hebei");
//        analyzeVideoToSRT("heilongjiang");
//        analyzeVideoToSRT("henan");
//        analyzeVideoToSRT("hubei");
//        analyzeVideoToSRT("hunan");
//        analyzeVideoToSRT("jiangsu", 185, 5, 28);
//        analyzeVideoToSRT("jiangxi");
//        analyzeVideoToSRT("jilin");
//        analyzeVideoToSRT("liaoning");
//        analyzeVideoToSRT("neimenggu");
//        analyzeVideoToSRT("ningxia");
//        analyzeVideoToSRT("qingzang",185,5,24);
//        analyzeVideoToSRT("shandong");
//        analyzeVideoToSRT("shannxi");
//        analyzeVideoToSRT("shanxi");
//        analyzeVideoToSRT("sichuan");
//        analyzeVideoToSRT("xining");
//        analyzeVideoToSRT("xinjiang");
//        analyzeVideoToSRT("yunnan");
//        analyzeVideoToSRT("zhejiang");
    }
    public static void analyzeVideoToSRT(String baseName){
        analyzeVideoToSRT(baseName,175,5,26,30);
    }
    public static void analyzeVideoToSRT(String baseName, int G, int R, int tolerance){
        analyzeVideoToSRT(baseName,G,R,tolerance,30);
    }
    public static void analyzeVideoToSRT(String baseName, int G, int R, int tolerance, int fps) {
        String videoPath = "C:\\Users\\astri\\Desktop\\Program\\Mianji_OpenCV\\src\\input\\" + baseName + ".mp4";
        String srtPath = "C:\\Users\\astri\\Desktop\\Program\\Mianji_OpenCV\\src\\output\\" + baseName + "_output.srt";

        VideoCapture cap = new VideoCapture(videoPath);
        if (!cap.isOpened()) {
            System.err.println("无法打开视频文件: " + videoPath);
            return;
        }

        try (FileWriter writer = new FileWriter(srtPath)) {
            DecimalFormat df = new DecimalFormat("0.00");
            Mat frame = new Mat();
            int frameCount = 0;
            double frameDuration = 1000.0 / fps;
            while (cap.read(frame)) {
                frameCount++;
                double percentage = analyzeFrame(frame, G, R, tolerance);
                String formattedPercentage = df.format(percentage);

                String startTime = formatTime((frameCount-1) * frameDuration);
                String endTime = formatTime(frameCount * frameDuration);

                writer.write(frameCount + "\n");
                writer.write(startTime + " --> " + endTime + "\n");
                writer.write(formattedPercentage + "\n\n");

                if (frameCount % 30 == 0) {
                    System.out.printf("已处理 %d 帧... 当前占比: %s%%\n",
                            frameCount, formattedPercentage);
                }
            }

            System.out.println("分析完成！共处理 " + frameCount + " 帧");

        } catch (Exception e) {
            System.err.println("处理出错: " + e.getMessage());
        } finally {
            cap.release();
        }
    }

    private static String formatTime(double milliseconds) {
        int hours = (int) (milliseconds / 3600000);
        milliseconds %= 3600000;
        int minutes = (int) (milliseconds / 60000);
        milliseconds %= 60000;
        int seconds = (int) (milliseconds / 1000);
        milliseconds %= 1000;
        return String.format("%02d:%02d:%02d,%03d", hours, minutes, seconds, (int)milliseconds);
    }

    public static double analyzeFrame(Mat frame, int G,
                                      int R, int tolerance) {
        List<Mat> channels = new ArrayList<>();
        Core.split(frame, channels);
        Mat greenChannel = channels.get(1);
        Mat redChannel = channels.get(2);

        Mat greenMask = new Mat(), redMask = new Mat(), finalMask = new Mat();
        Core.inRange(greenChannel, new Scalar(G - tolerance),
                new Scalar(G + tolerance), greenMask);
        Core.inRange(redChannel, new Scalar(R - tolerance),
                new Scalar(R + tolerance/5.0 ), redMask);
        Core.bitwise_and(greenMask, redMask, finalMask);

        if (System.currentTimeMillis() % 50 == 0) {
            Mat display = new Mat();
            Imgproc.resize(finalMask, display, new Size(finalMask.cols()/2, finalMask.rows()/2));
            HighGui.imshow("Processing Preview", display);
            HighGui.waitKey(1);
        }

        return Core.countNonZero(finalMask) * 100.0 / (frame.rows() * frame.cols());
    }
}