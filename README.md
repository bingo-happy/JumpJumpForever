######老人不知道发在哪里呢，就发在这里吧~

![](http://file.cc98.org/v2-upload/mc20qz32.jpg)

历史最高其实到过5000多，不过不知道为什么分数记录不上去了，再打开又变成了400多，emmmmmm有没有大神知道原因的分享一下

###进入正题

首先脚本基于adb，所以只适用于*android*，ios的同学请参考*抓取数据包*的方法。
#### abd脚本

     adb shell /system/bin/screencap -p /sdcard/screenshot.png//截屏
	 adb pull /sdcard/screenshot.png screenshot.png//存到pc脚本路径
 !

    adb shell input swipe x1 y1 x2 y2 time //以time时间模拟滑动屏幕，当x1=x2,y1=y1时为长按
!

    choice /t 2 /d y /n  //这里我用cmd延时两秒用来跳和等待加分

#### 算法
* 灰度化，存为p1
* 基于fast的思想检测边界，存为p2
* 求的棋子起跳位置，思想是检测棋子头部的严格圆形，网上有别的方案是检测棋子颜色，emmmm，各有所长吧，颜色会有可能撞色，而圆形要排除成片白点
* 以求的目标物体中心，思想是`灰度中心`+`图形中心`双约束（仅两个误差能在10个像素距离以内，跳到1000+随便，后来加上了鄙人很不喜欢的中心连中白点检测，就直飙5000了。。。白点检测误差只有3像素以内吧）
* 计算距离，公式需要自己拟合一下，越准分数越高，加上学习的话万分不是梦

`楼主用的是小米4C，宽1080分辨率，以下代码有绝对值编程，不一一更换了`

##### 灰度化
    int h = bi.getHeight();//获取图像的高
	int w = bi.getWidth();//获取图像的宽
	int[][] gray = new int[w][w];
	int temp = (h - w) / 2;
	for (int x = 0; x < w; x++) {
		for (int y = temp; y < h - temp; y++) {
			gray[x][y - temp] = getGray(bi.getRGB(x, y));
		}
	}//只取中间关键信息的正方形区域

##### 边界检测
     int Binary[][] = new int[w][w];
        //fast特征点 start
        int gate = 4;
        for (int x = 3; x < w - 3; x++) {
            for (int y = 3; y < w - 3; y++) {
                if (Math.abs(gray[x][y] - gray[x][y + 2]) > gate || Math.abs(gray[x][y] - gray[x][y - 2]) > gate) {
                    if (Math.abs(gray[x + 2][y] - gray[x][y]) > gate || Math.abs(gray[x - 2][y] - gray[x][y]) > gate) {
                        //prepareArrayList.add(new Point(x, y));这里是用来输出二值化的边界图形的，和算法无关
                        Binary[x][y] = 1;
                    }
                }
                if (Math.abs(gray[x][y] - gray[x + 1][y + 1]) > gate || Math.abs(gray[x][y] - gray[x - 1][y - 1]) > gate) {
                    if (Math.abs(gray[x + 1][y - 1] - gray[x][y]) > gate || Math.abs(gray[x - 1][y + 1] - gray[x][y]) > gate) {
                        //prepareArrayList.add(new Point(x, y));
                        Binary[x][y] = 1;
                    }
                }
            }
        }
![](http://file.cc98.org/v2-upload/42frlicq.jpg)

##### 找棋子
    private static Point findHead(int[][] a, int w) {
        for (int i = 30; i < w - 30; i++)
            for (int j = 30; j < w - 30; j++) {
                if (a[i][j + 30] == 1 && a[i][j - 30] == 1) {
                    if (a[i + 30][j] == 1 && a[i + 30][j] == 1) {
                        if (a[i + 21][j + 21] == 1 && a[i - 21][j + 21] == 1 && a[i + 21][j - 21] == 1 && a[i - 21][j - 21] == 1) {
                            boolean flag = true;
                            for (int ki = 0; ki < 30&&flag; ki++)
                                for (int kj = 0; kj < 30&&flag; kj++) {
                                    if (Math.abs(ki * ki + kj * kj - 30 * 30) < 5) {
                                        if (a[i + ki][j + kj] == 0) {flag = false;break;}
                                        if (a[i - ki][j + kj] == 0) {flag = false;break;}
                                        if (a[i + ki][j - kj] == 0) {flag = false;break;}
                                        if (a[i - ki][j - kj] == 0) {flag = false;break;}
                                    }
                                }//缩进感人，但多重排除可以提高效率
                            if (flag) {
                                int kk = 0;
                                for (int jj = 0; jj < 30; jj++) {
                                    if (a[i - jj][j] == 1) kk++;
                                    if (a[i][j] == 1 - jj) kk++;
                                }
                                if (kk < 10)//这里是为了排除成片白点的情况，当然边缘检测足够细的话就不需要了
                                    return new Point(i, j);
                            }
                        }
                    }
                }
            }
        return new Point(0, 0);
    }

##### 白点检测
     for (int j = temp + 5; j < temp + 150; j++) {
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
	//白点没有命中，进行灰度和图形中心检测
	}

##### 灰度中心
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
	//灰度中心start,topXY是目标点最高点
	 for (int i = -200; i < 200; i++) {
                    for (int j = -10; j < 200; j++) {
                        if (topY + j > 0 && topY + j < w && topX + i > 0 && topX + i < w) {
                            int target = gray[topX + i][topY + j];
                            if (Math.abs(target - objGray) < 1) {
                                sumX += topX + i;
                                sumY += topY + j;
                                numPoint++;
                            }
                        }
                    }
                }
            }
            objX = sumX / (numPoint);//这里可以考虑排除一下除数0
            objY = sumY / (numPoint);
			

##### 图形中心（关键要知道，目标椭圆其实也是内接两边30°的菱形）

    int justObjY = topY;
    int justObjX = topX;
	double minJustK = 1;
    for (int i = Math.min(Math.abs(centerX - objX) - 50, 220); i > 0; i--) {
    	for (int di = -Math.min(3, 1 + numTopY / 2); di < Math.min(3, 1 + numTopY + 1); di++) {//numTopY是考虑圆形上顶点形成一条线的情况
                    int lastXTemp = topX;
                    lastXTemp += di;
                    if (lastXTemp + i < w && lastXTemp - i > 0) {
                        for (int j = 5; j < i; j++) {
                            if (binary[lastXTemp + i][topY + j] == 1 && binary[lastXTemp - i][topY + j] == 1) {
                                double justObjK = Math.abs(0.58 - Math.abs(1.0 * j / i));
                                if (justObjK < minJustK) {
                                    minJustK = justObjK;//调整最相似K值
                                    justObjY = lastY + j;
                                }
                            }
                        }
                    }
                }
            }

##### 得到最后目标点(非白点)
                objX = (objX + justObjX) / 2;
			if (justObjY < objY) {//取最近top点为最相似点，加权
                objY = (2 * justObjY + objY) / 3;
            } else {
                objY = (justObjY + 2 * objY) / 3;
            }

##### 拟合公式
        if(dist<400)dist = dist * (-0.0006 * dist + 1.8);
        else dist = dist * (-0.00055 * dist + 1.8);
        dist = Math.max(200, dist);//不要问为什么是200...
		//dist 本来是算的距离，这三行转化成了swipe里的time


##### 总结：还是白点检测的方式比较准确，虽然觉得这个检测感觉好蠢233333，楼主开始不知道白点规律做的，在10的误差范围内也能到1000分啦

##欢迎一起讨论

