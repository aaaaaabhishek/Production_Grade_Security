package com.example.customSe.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
public class GoogleAuthenticatorService {
    private final GoogleAuthenticator gAuth=new GoogleAuthenticator() ;
//    public TwoFactorAuthService(GoogleAuthenticator gAuth) {
////        GoogleAuthenticatorConfig config = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
////                .setTimeStepSizeInMillis(5 * 60 * 1000) // 5 minutes
////                .setWindowSize(1) // allows OTP for ±1 time step = ±5 min
////                .build();
//
    ////        this.gAuth = new GoogleAuthenticator(config);
//        this.gAuth=new GoogleAuthenticator();
//    }
    public GoogleAuthenticatorKey generateSecret() {
        return gAuth.createCredentials();
    }

    public String getOtpAuthUrl(String username, String issuer, String secret) {
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s&period=300",
                issuer, username, secret, issuer
        );
    }
    public String generateQRCodeBase64(String otpAuthUrl) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(otpAuthUrl, BarcodeFormat.QR_CODE, 250, 250);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return Base64.getEncoder().encodeToString(pngOutputStream.toByteArray());
    }

    public boolean verifyCode(String secret, int code) {
//        GoogleAuthenticator gAuth = new GoogleAuthenticator();
//        System.out.println( gAuth.getTotpPassword(secret)+"   "+code);

        return gAuth.authorize(secret, code);
    }
}
