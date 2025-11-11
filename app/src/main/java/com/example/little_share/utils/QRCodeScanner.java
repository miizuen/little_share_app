package com.example.little_share.utils;

public class QRCodeScanner {
    public static boolean isValidQRCode(String content){
        if(content == null || content.isEmpty()){
            return false;
        }
        return content.startsWith(Constants.QR_CODE_PREFIX_CAMPAIGN) ||
                content.startsWith(Constants.QR_CODE_PREFIX_GIFT) ||
                content.startsWith(Constants.QR_CODE_PREFIX_VOLUNTEER);
    }

    public static String getQRCodeType(String content){
        if(content == null){
            return null;
        }
        if(content.startsWith(Constants.QR_CODE_PREFIX_CAMPAIGN)){
            return "campaign";
        } else if (content.startsWith(Constants.QR_CODE_PREFIX_GIFT)) {
            return "gift";
        } else if (content.startsWith(Constants.QR_CODE_PREFIX_VOLUNTEER)) {
            return "volunteer";
        }
        return null;
    }
    private QRCodeScanner(){
        throw new AssertionError("Cannot instantiate utility class");
    }
}
