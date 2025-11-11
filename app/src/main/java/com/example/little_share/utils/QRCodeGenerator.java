package com.example.little_share.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public class QRCodeGenerator {
    public static Bitmap generateQRCode(String content, int size) {
        if (content == null || content.isEmpty()) return null;

        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    content,
                    BarcodeFormat.QR_CODE,
                    size,
                    size
            );

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            int[] pixels = new int[width * height];

            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;

        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Tạo qr code cho chiến dịch đăng kí
    public static String generateCampaignRegistrationCode(String userId, String campaignId, String registrationId){
        return Constants.QR_CODE_PREFIX_CAMPAIGN + registrationId + ":" + userId + ":" + campaignId;
    }

    //Tạo qr code cho quà tặng
    public static String generateGiftRedemptionCode(String userId, String giftId, String redemptionId){
        return Constants.QR_CODE_PREFIX_GIFT + redemptionId + ":" + userId + ":" + giftId;
    }

    public static QRCodeData parseQRCode(String qrContent){
        if(qrContent == null || qrContent.isEmpty())
            return null;

        try{
            String type;
            String content;

            if(qrContent.startsWith(Constants.QR_CODE_PREFIX_CAMPAIGN)){
                type = "campaign";
                content = qrContent.substring(Constants.QR_CODE_PREFIX_CAMPAIGN.length());
            }else if(qrContent.startsWith(Constants.QR_CODE_PREFIX_GIFT)){
                type = "gift";
                content = qrContent.substring(Constants.QR_CODE_PREFIX_GIFT.length());
            } else if (qrContent.startsWith(Constants.QR_CODE_PREFIX_VOLUNTEER)) {
                type = "volunteer";
                content = qrContent.substring(Constants.QR_CODE_PREFIX_VOLUNTEER.length());
            }else {
                return null;
            }

            String[] parts = content.split(":");
            return new QRCodeData(type, parts);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static class QRCodeData{
        public String type;
        public String[] data;

        public QRCodeData(String type, String[] data) {
            this.type = type;
            this.data = data;
        }

        public String getRegistrationId(){
            return data.length > 0 ? data[0] : null;
        }

        public String getUserId(){
            return data.length > 1 ? data[1] : null;
        }

        public String getReferenceId(){
            return data.length > 2 ? data[2] : null;
        }
    }

    private QRCodeGenerator(){
        throw new AssertionError("Cannot instantiate utility class");
    }
}
