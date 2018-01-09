import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;



public class Main {

    public static void main(String[] args) throws IOException {

//        javax.swing.filechooser.FileSystemView fsv = javax.swing.filechooser.FileSystemView
//                .getFileSystemView();
        BufferedImage bi = ImageIO.read(new File(".\\out\\production\\HelloAdb\\screenshot.png"));//通过imageio将图像载入
        int h = bi.getHeight();//获取图像的高
        int w = bi.getWidth();//获取图像的宽
        int[][] gray = new int[w][w];
        int temp = (h - w) / 2;
        for (int x = 0; x < w; x++) {
            for (int y = temp; y < h - temp; y++) {
                gray[x][y - temp] = getGray(bi.getRGB(x, y));
            }
        }

        ArrayList<Point> prepareArrayList = new ArrayList<>();

        int binary[][] = new int[w][w];
        //fast特征点 start
        int gate = 4;
        for (int x = 3; x < w - 3; x++) {
            for (int y = 3; y < w - 3; y++) {
                if (Math.abs(gray[x][y] - gray[x][y + 2]) > gate || Math.abs(gray[x][y] - gray[x][y - 2]) > gate) {
                    if (Math.abs(gray[x + 2][y] - gray[x][y]) > gate || Math.abs(gray[x - 2][y] - gray[x][y]) > gate) {
                        prepareArrayList.add(new Point(x, y));
                        binary[x][y] = 1;
                    }
                }
                if (Math.abs(gray[x][y] - gray[x + 1][y + 1]) > gate || Math.abs(gray[x][y] - gray[x - 1][y - 1]) > gate) {
                    if (Math.abs(gray[x + 1][y - 1] - gray[x][y]) > gate || Math.abs(gray[x - 1][y + 1] - gray[x][y]) > gate) {
                        prepareArrayList.add(new Point(x, y));
                        binary[x][y] = 1;
                    }
                }
            }
        }

        System.out.println("一次预备点:" + prepareArrayList.size());
//        fast特征点 end

        BufferedImage nbi = new BufferedImage(w, w, BufferedImage.TYPE_BYTE_BINARY);

        for (int i = 0; i < prepareArrayList.size(); i++) {
            int x = prepareArrayList.get(i).getX();
            int y = prepareArrayList.get(i).getY();
            int max = new Color(255, 255, 255).getRGB();
            nbi.setRGB(x, y, max);
        }
        ImageIO.write(nbi, "png", new File(".\\out\\production\\HelloAdb\\new.png"));

        Point head = findHead(binary, w);

        int centerX = head.getX();
        int centerY = head.getY() + 160;


        int topX_l = centerX;
        int topY_l = centerY;
        int topX_r = centerX;
        int topY_r = centerY;
        int topX = 0;
        int topY = 0;


        int minY = centerY;
        int numTopYL = 0;
        int numTopYR = 0;
        for (int i = w / 2; i < w; i++) {
            for (int j = centerY - 40; j > 0; j--) {
                if (binary[i][j] == 1 && j <= minY) {
                    if (j < minY) {
                        numTopYR = 1;
                    } else {
                        numTopYR++;
                    }
                    topX_r = i;
                    topY_r = j;
                    minY = j;
                }
            }
        }
        minY = centerY;
        for (int i = w / 2; i > 0; i--) {
            for (int j = 0; j < centerY - 40; j++) {
                if (binary[i][j] == 1 && j <= minY) {
                    if (j < minY) {
                        numTopYL = 1;
                    } else {
                        numTopYL++;
                    }
                    minY = j;
                    topX_l = i;
                    topY_l = j;
                }
            }
        }

        int objX;
        int objY;
        int direct = 0;
        if (Math.abs(topX_l - centerX) > Math.abs(topX_r - centerX)) {
            direct = 0; // 0 是左
            topX_l = topX_l + numTopYL / 2;
            objX = topX_l;
            topY = topY_l;
        } else {
            direct = 1;// 1是右
            topX_r = topX_r - numTopYR / 2;
            objX = topX_r;
            topY = topY_r;
        }

        int targetY = 0;
        int numtargetY = 0;

        for (int j = temp + 5; j < temp + 180; j++) {
            int targetRGB = bi.getRGB(objX, topY + j);
            int r = (targetRGB >> 16) & 0xff;
            int g = (targetRGB >> 8) & 0xff;
            int b = (targetRGB) & 0xff;
            if (r == 245 && g == 245 && b == 245) {
                targetY += j + topY - temp;
                numtargetY++;
            }
        }
        if (targetY > 0) {
            objY = targetY / numtargetY;
        } else {

            /*
            * 灰度中心start
            * */

            int sumX = 0;
            int numPoint = 0;
            int sumY = 0;
            int numTopY = 0;


            if (direct == 0) {
                numTopY = numTopYL;
                topX = topX_l + numTopYL / 2;
                topY = topY_l;
                int objGray = GetMainGray(gray, topX, topY);
                for (int i = -200; i < 200; i++) {
                    for (int j = -10; j < 200; j++) {
                        if (topX_l + i > 0 && topX_l + i < w && topY_l + j > 0 && topY_l + j < w) {
                            if (Math.abs(gray[topX_l + i][topY_l + j] - objGray) < 1) {
                                sumX += topX_l + i;
                                sumY += topY_l + j;
                                numPoint++;
                            }
                        }
                    }

                }
            } else {
                numTopY = numTopYR;
                topX = topX_r - numTopYR / 2;
                topY = topY_r;
                int objGray = GetMainGray(gray, topX, topY);
                for (int i = -200; i < 200; i++) {
                    for (int j = -10; j < 200; j++) {
                        if (topY_r + j > 0 && topY_r + j < w && topX_r + i > 0 && topX_r + i < w) {
                            int target = gray[topX_r + i][topY_r + j];
                            if (Math.abs(target - objGray) < 1) {
                                sumX += topX_r + i;
                                sumY += topY_r + j;
                                numPoint++;
                            }
                        }
                    }
                }
            }
            objX = sumX / (numPoint + 1);
            objY = sumY / (numPoint + 1);

            /*
            * 灰度中心 end
            * */



            /*
            * 图形中心 start
            * */


            int justObjY = topY;
            int justObjX = topX;

            double minJustK = 1;
            for (int i = Math.min(Math.abs(centerX - objX) - 50, 220); i > 0; i--) {
                for (int di = -Math.min(3, 1 + numTopY / 2 + 1); di < Math.min(3, 1 + numTopY + 1) / 2; di++) {
                    int topXTemp = topX;
                    topXTemp += di;
                    if (topXTemp + i < w && topXTemp - i > 0) {
                        for (int j = 5; j < i; j++) {
                            if (binary[topXTemp + i][topY + j] == 1 && binary[topXTemp - i][topY + j] == 1) {
//                            System.out.println(Math.abs(0.5771 - Math.abs(1.0*j/i)));
                                double justObjK = Math.abs(0.5771 - Math.abs(1.0 * j / i));
                                if (justObjK < minJustK) {
                                    minJustK = justObjK;
                                    justObjY = topY + j;
                                }
                            }
                        }
                    }
                }
            }

            objX = (objX + justObjX) / 2;
//        objY = (2*justObjY + objY) / 3;

            if (justObjY < objY) {
                objY = (2 * justObjY + objY) / 3;
            } else {
                objY = (justObjY + 2 * objY) / 3;
            }

            /*
            * 图形中心 end
            * */

        }


        int dy = centerY - objY;
        int dx = objX - centerX;


        System.out.println((direct == 0 ? "left" : "right") + ":centerX:" + centerX + "--centerY:" + centerY + "--objX:" + objX + "--objY:" + objY + "--------------w:" + w);


        double dist = (Math.sqrt((double) (dx * dx + dy * dy)));
//        dist=dist*(1-Math.abs((ds-0.577)*(1+ds*0.577)));
//        if(dist>50) dist=dist * 1.3 ;
//        else dist=1;
//        dist *= 1.5;

        if(dist<400) dist = dist * (-0.0006 * dist + 1.8);
        else dist = dist * (-0.00055 * dist + 1.8);
        dist = Math.max(200, dist);

//        double scale = 0.945 * 2 / 60;
//        double actual_distance = dist * scale * (Math.sqrt(6) / 2);
//        dist = (-945 + Math.sqrt(945 * 945 + 4 * 105 * 36 * actual_distance))/(2 * 105) * 1000;

        System.out.print("X:" + head.getX() + "--Y:" + head.getY());

        writerBat("adb shell input swipe 557 1000 550 1000 " + (int) dist, ".\\out\\production\\HelloAdb\\2.bat", false);
    }

    private static void writerBat(String conent, String txtPath, boolean isNextWriter) {
        try {

            File file = new File(txtPath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();//创建txt文件 如：testData.txt文件
            }
            //写入txt文件
            FileWriter fileWriter = new FileWriter(txtPath, isNextWriter);
            BufferedWriter bw = new BufferedWriter(fileWriter);
            bw.newLine();
            bw.write(conent);
            fileWriter.flush();
            bw.close();
            fileWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int getGray(int rgb) {
        String str = Integer.toHexString(rgb);
        int r = Integer.parseInt(str.substring(2, 4), 16);
        int g = Integer.parseInt(str.substring(4, 6), 16);
        int b = Integer.parseInt(str.substring(6, 8), 16);
        //or 直接new个color对象
        Color c = new Color(rgb);
        r = c.getRed();
        g = c.getGreen();
        b = c.getBlue();
        int top = (r + g + b) / 3;
        return (int) (top);
    }

    private static Point findHead(int[][] a, int w) {
        for (int i = 30; i < w - 30; i++)
            for (int j = 30; j < w - 30; j++) {
                //检测
                if (a[i][j + 30] == 1 && a[i][j - 30] == 1) {
                    if (a[i + 30][j] == 1 && a[i + 30][j] == 1) {
                        if (a[i + 21][j + 21] == 1 && a[i - 21][j + 21] == 1 && a[i + 21][j - 21] == 1 && a[i - 21][j - 21] == 1) {
                            boolean flag = true;
                            for (int ki = 0; ki < 30; ki++)
                                for (int kj = 0; kj < 30; kj++) {
                                    if (Math.abs(ki * ki + kj * kj - 30 * 30) < 5) {
                                        if (a[i + ki][j + kj] == 0) flag = false;
                                        if (a[i - ki][j + kj] == 0) flag = false;
                                        if (a[i + ki][j - kj] == 0) flag = false;
                                        if (a[i - ki][j - kj] == 0) flag = false;
                                    }
                                }
                            if (flag) {
                                int kk = 0;
                                for (int jj = 0; jj < 30; jj++) {
                                    if (a[i - jj][j] == 1) kk++;
                                    if (a[i][j] == 1 - jj) kk++;
                                }
                                if (kk < 10)
                                    return new Point(i, j);
                            }
                        }
                    }
                }
            }
        return new Point(0, 0);
    }

    private static int GetMainGray(int[][] gray, int topX, int topY) {
        int mainGray = 0;
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
        for (int j = 5; j < 30; j++) {
            for (int i = -6; i < 6; i++) {
                int grayTemp = gray[topX + i][topY + j];
                if (map.containsKey(grayTemp)) {
                    map.put(grayTemp, map.get(grayTemp) + 1);
                } else {
                    map.put(grayTemp, 1);    // 该数字第一次出现
                }
            }
        }
        Collection<Integer> count = map.values();// 找出map的value中最大值，也就是数组中出现最多的数字所出现的次数
        int maxCount = Collections.max(count);
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if (entry.getValue() == maxCount) {
                mainGray = entry.getKey();
            }
        }
        return mainGray;
    }
}

