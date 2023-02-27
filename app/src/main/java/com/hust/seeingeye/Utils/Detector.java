package com.hust.seeingeye.Utils;

public class Detector {
        private final String[] names = new String[]{"person", "bicycle", "car", "motorcycle", "airplane", "bus", "train", "truck", "boat", "traffic light",
            "fire hydrant", "stop sign", "parking meter", "bench", "bird", "cat", "dog", "horse", "sheep", "cow",
            "elephant", "bear", "zebra", "giraffe", "backpack", "umbrella", "handbag", "tie", "suitcase", "frisbee",
            "skis", "snowboard", "sports ball", "kite", "baseball bat", "baseball glove", "skateboard", "surfboard",
            "tennis racket", "bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana", "apple",
            "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake", "chair", "couch",
            "potted plant", "bed", "dining table", "toilet", "tv", "laptop", "mouse", "remote", "keyboard", "cell phone",
            "microwave", "oven", "toaster", "sink", "refrigerator", "book", "clock", "vase", "scissors", "teddy bear",
            "hair drier", "toothbrush"};
//    private final String[] names = new String[]{
//            "bottle", "chair", "sofa", "tvmonitor", "light"
//    };
    private int now;

    public void nextObject() {
        now++;
        if (now > names.length - 1) {
            now = 0;
        }
    }

    public void lastObject() {
        now--;
        if (now < 0) {
            now = names.length - 1;
        }
    }

    public String getCurrentObject() {
        return names[now];
    }
}
