package watcharaphans.bitcombine.co.th.bitcamera.model;

import android.util.Log;

public class QrCodeData {

    private static final String TAG = QrCodeData.class.getName();

    public String id;
    public String licenseCar;
    public String remark;
    public String machineNumber;
    public String dirName;
    public String fileNameC;
    public String fileNameD;

    public String dateString;
    public String timeString;

    public QrCodeData(String qrCode) {
        //        รับค่า String ที่ Decode แล้ว
        String QRcode_Convert = "";
        String Tag = "29AugV1", DateTimeIn = "เข้า : ";
        String DateIn = "", TimeIn = "";

//        เช็คข้อมูลตัวแรกเท่ากับเครื่องหมาย | หรือไม่
        if (qrCode.charAt(0) == '|') {
            int check_FontThai = 0;
//            วน loop เพื่อทำการ Decode Qrcode
            for (int i = 0; i < qrCode.length(); ++i) {
                if (qrCode.charAt(i) == '!') {
                    i++;
                    char CharDecimal = qrCode.charAt(i);
                    int ValueASCII = (int) CharDecimal;

                    char char_decode = (char) (ValueASCII + 3536);
                    if (ValueASCII == '}') {
                        QRcode_Convert += "ะ";
                    } else {
                        QRcode_Convert += char_decode;
                    }
                } else {
                    //กรณี QRcode ที่เข้ามาเป็นเครื่องหมาย | ให้คืนค่าว่างกลับไป
                    if (qrCode.charAt(i) == '|') {
                        QRcode_Convert += "";
                    } else {
                        char CharDecimal = qrCode.charAt(i);
                        int ValueASCII = (int) CharDecimal;
                        //เช็คว่า เป็นตัวอักษรไทยหรือไม่
                        if (ValueASCII <= 0)
                            check_FontThai = 1;

                        if (check_FontThai == 1) {
                            QRcode_Convert += qrCode.charAt(i);
                        } else {
                            char char_decode = (char) (158 - ValueASCII);
                            if (ValueASCII == '>') {
                                //char_decode
                                QRcode_Convert += ">";
                            } else if (ValueASCII == ' ') {
                                QRcode_Convert += " ";
                            } else {
                                QRcode_Convert += char_decode;
                            }
                        }
                    }
                }
            }
        }

        if (QRcode_Convert.split("\\$", -1).length - 1 == 10) {
            String[] data = QRcode_Convert.split("\\$");

            this.id = data[0];
            this.licenseCar = data[2];
            this.machineNumber = data[10];
            this.remark = data[7];
            this.dirName = data[8] + data[10];
            this.fileNameC = data[9] + data[10] + "C.jpg";
            this.fileNameD = data[9] + data[10] + "D.jpg";
            this.dateString = data[8];
            this.timeString = data[9];

            Log.d(TAG, "id ===> " + this.id);
            Log.d(TAG, "license car ===> " + this.licenseCar);
            Log.d(TAG, "machine number ===> " + this.machineNumber);
            Log.d(TAG, "remark ===> " + this.remark);
            Log.d(TAG, "directory name ===> " + this.dirName);
            Log.d(TAG, "file name [C] ===> " + this.fileNameC);
            Log.d(TAG, "file name [D] ===> " + this.fileNameD);
        }
    }
}
