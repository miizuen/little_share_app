package com.example.little_share.utils;

public class Constants {
    // API Configuration
    public static final String BASE_URL = "https://api.charityapp.com/";
    public static final String API_VERSION = "v1";
    public static final int CONNECT_TIMEOUT = 30;
    public static final int READ_TIMEOUT = 30;
    public static final int WRITE_TIMEOUT = 30;

    // SharedPreferences Keys
    public static final String PREFS_NAME = "CharityAppPrefs";
    public static final String KEY_USER_TOKEN = "user_token";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_TYPE = "user_type";
    public static final String KEY_USER_NAME = "user_name";
    public static final String KEY_USER_EMAIL = "user_email";
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";
    public static final String KEY_FIRST_TIME = "first_time";

    // User Types
    public static final String USER_TYPE_VOLUNTEER = "volunteer";
    public static final String USER_TYPE_SPONSOR = "sponsor";
    public static final String USER_TYPE_ORGANIZATION = "organization";

    // Campaign Categories
    public static final String CATEGORY_ALL = "all";
    public static final String CATEGORY_EDUCATION = "education";
    public static final String CATEGORY_FOOD = "food";
    public static final String CATEGORY_ENVIRONMENT = "environment";
    public static final String CATEGORY_HEALTH = "health";
    public static final String CATEGORY_URGENT = "urgent";

    // Campaign Status
    public static final String STATUS_UPCOMING = "upcoming";
    public static final String STATUS_ONGOING = "ongoing";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_CANCELLED = "cancelled";

    // Registration Status
    public static final String REGISTRATION_PENDING = "pending";
    public static final String REGISTRATION_APPROVED = "approved";
    public static final String REGISTRATION_REJECTED = "rejected";

    // Donation Categories
    public static final String DONATION_BOOKS = "books";
    public static final String DONATION_CLOTHES = "clothes";
    public static final String DONATION_TOYS = "toys";
    public static final String DONATION_MONEY = "money";

    // Item Conditions
    public static final String CONDITION_NEW = "new";
    public static final String CONDITION_GOOD = "good";
    public static final String CONDITION_FAIR = "fair";
    public static final String CONDITION_ACCEPTABLE = "acceptable";

    // Donation Status
    public static final String DONATION_PENDING = "pending";
    public static final String DONATION_CONFIRMED = "confirmed";
    public static final String DONATION_RECEIVED = "received";

    // Gift Redemption Status
    public static final String REDEMPTION_PENDING = "pending";
    public static final String REDEMPTION_COMPLETED = "completed";

    // Attendance Status
    public static final String ATTENDANCE_PRESENT = "present";
    public static final String ATTENDANCE_LATE = "late";
    public static final String ATTENDANCE_ABSENT = "absent";

    // Points Calculation
    public static final int BASE_POINTS_PER_CAMPAIGN = 50;
    public static final int POINTS_PER_HOUR = 10;
    public static final int BONUS_POINTS_URGENT = 20;

    // Donation Points Calculation
    public static final int POINTS_PER_ITEM_NEW = 15;
    public static final int POINTS_PER_ITEM_GOOD = 12;
    public static final int POINTS_PER_ITEM_FAIR = 8;
    public static final int POINTS_PER_ITEM_ACCEPTABLE = 5;

    // Money Donation Points (per 10,000 VND)
    public static final int POINTS_PER_10K_VND = 1;

    // Request Codes
    public static final int REQUEST_CAMERA_PERMISSION = 100;
    public static final int REQUEST_STORAGE_PERMISSION = 101;
    public static final int REQUEST_PICK_IMAGE = 102;
    public static final int REQUEST_SCAN_QR = 103;
    public static final int REQUEST_NOTIFICATION_PERMISSION = 104;

    // Intent Extra Keys
    public static final String EXTRA_CAMPAIGN_ID = "campaign_id";
    public static final String EXTRA_CAMPAIGN = "campaign";
    public static final String EXTRA_ROLE_ID = "role_id";
    public static final String EXTRA_GIFT_ID = "gift_id";
    public static final String EXTRA_REPORT_ID = "report_id";
    public static final String EXTRA_QR_CODE = "qr_code";
    public static final String EXTRA_USER_TYPE = "user_type";
    public static final String EXTRA_REDEMPTION_CODE = "redemption_code";

    // Date Formats
    public static final String DATE_FORMAT_DISPLAY = "dd/MM/yyyy";
    public static final String DATE_FORMAT_API = "yyyy-MM-dd";
    public static final String TIME_FORMAT_DISPLAY = "HH:mm";
    public static final String DATETIME_FORMAT_DISPLAY = "dd/MM/yyyy HH:mm";
    public static final String DATETIME_FORMAT_API = "yyyy-MM-dd'T'HH:mm:ss";

    // Pagination
    public static final int PAGE_SIZE = 20;
    public static final int INITIAL_LOAD_SIZE = 20;

    // Image Upload
    public static final int MAX_IMAGE_SIZE_MB = 5;
    public static final int IMAGE_COMPRESSION_QUALITY = 80;

    // QR Code
    public static final int QR_CODE_SIZE = 512;
    public static final String QR_CODE_PREFIX_CAMPAIGN = "CAMP_";
    public static final String QR_CODE_PREFIX_GIFT = "GIFT_";
    public static final String QR_CODE_PREFIX_VOLUNTEER = "VOL_";

    // ZaloPay
    public static final String ZALOPAY_APP_ID = "YOUR_ZALOPAY_APP_ID";
    public static final String ZALOPAY_KEY1 = "YOUR_ZALOPAY_KEY1";
    public static final String ZALOPAY_KEY2 = "YOUR_ZALOPAY_KEY2";
    public static final String ZALOPAY_CALLBACK_URL = "charityapp://zalopay";

    // Error Messages
    public static final String ERROR_NETWORK = "Không có kết nối mạng";
    public static final String ERROR_SERVER = "Lỗi máy chủ";
    public static final String ERROR_UNKNOWN = "Đã xảy ra lỗi";
    public static final String ERROR_TIMEOUT = "Kết nối quá thời gian";
    public static final String ERROR_INVALID_CREDENTIALS = "Email hoặc mật khẩu không đúng";

    // Success Messages
    public static final String SUCCESS_REGISTRATION = "Đăng ký thành công";
    public static final String SUCCESS_LOGIN = "Đăng nhập thành công";
    public static final String SUCCESS_DONATION = "Quyên góp thành công";
    public static final String SUCCESS_REDEMPTION = "Đổi quà thành công";
    public static final String SUCCESS_ATTENDANCE = "Điểm danh thành công";

    // Notification Channels
    public static final String CHANNEL_ID_GENERAL = "general";
    public static final String CHANNEL_ID_CAMPAIGN = "campaign";
    public static final String CHANNEL_ID_DONATION = "donation";
    public static final String CHANNEL_ID_REMINDER = "reminder";

    // Database
    public static final String DATABASE_NAME = "charity_app_db";
    public static final int DATABASE_VERSION = 1;

    // Cache
    public static final long CACHE_EXPIRY_TIME = 5 * 60 * 1000; // 5 minutes

    // Validation
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 32;
    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 50;
    public static final int MAX_DESCRIPTION_LENGTH = 500;
    public static final int MAX_NOTE_LENGTH = 200;

    // Private constructor to prevent instantiation
    private Constants() {
        throw new AssertionError("Cannot instantiate constants class");
    }
}
