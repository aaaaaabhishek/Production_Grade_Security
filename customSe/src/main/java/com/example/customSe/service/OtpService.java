package com.example.customSe.service;

import com.example.customSe.utils.TimedOtpUtil;
import com.sun.istack.FinalArrayList;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {
    private final Map<String, TimedOtpUtil> otpStore = new ConcurrentHashMap<>();
private final MailService mailService;

    public OtpService( MailService mailService) {
        this.mailService = mailService;
    }

    public void generateAndSendOtp(String userIdentifier, String channel) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        TimedOtpUtil timedOtpUtil1=new TimedOtpUtil(otp,getStep());
        otpStore.put(userIdentifier, timedOtpUtil1);
        System.out.println("user"+userIdentifier +"    "+timedOtpUtil1.getStep());

        // Send OTP via email or SMS — call your external provider here
        if ("email".equalsIgnoreCase(channel)) {
            sendEmailOtp(userIdentifier, otp);
        } else if ("sms".equalsIgnoreCase(channel)) {
            sendSmsOtp(userIdentifier, otp);
        }
    }

    public boolean verifyOtp(String userIdentifier, String otp) {
        TimedOtpUtil timedOtpUtil=otpStore.get(userIdentifier);
        System.out.println(timedOtpUtil.getOtp()+"      "+timedOtpUtil.getStep());
        if(timedOtpUtil==null){
            return false;
        }
        long storedStep=timedOtpUtil.getStep();

        if ((storedStep == getStep() || storedStep == getStep() - 1 || storedStep == getStep() + 1)
                && timedOtpUtil.getOtp().equals(otp)) {
            return true;
        }
        return false;
    }

    public void clearOtp(String userIdentifier) {
        otpStore.remove(userIdentifier);
    }

    private void sendEmailOtp(String email, String otp) {
        mailService.sendOtp(email,otp);
    }

    private void sendSmsOtp(String phone, String otp) {
        // TODO: Integrate with SMS gateway like Twilio or Nexmo
        System.out.println("Sending OTP " + otp + " to phone " + phone);
    }
    public static long getStep(){
//        return System.currentTimeMillis()/30*1000;
        return 58586089031000L;
    }
}
