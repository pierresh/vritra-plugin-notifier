package com.vritra.notifier;

import com.vritra.notifier.Notifier;
import java.util.ArrayList;
import java.util.Random;
import com.vritra.notifier.ActionListener;
import org.json.JSONObject;
import org.json.JSONArray;
import androidx.core.graphics.drawable.IconCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Action.Builder;
import android.graphics.Bitmap;
import androidx.core.text.HtmlCompat;
import android.text.Spanned;
import android.content.Intent;
import android.os.Build;
import android.app.PendingIntent;
import androidx.core.app.RemoteInput;


public class Action {

    private JSONObject props=null;
    protected String ref=null;
    private String type=null;
    private String label=null;
    private Spanned span=null;
    private IconCompat icon=null;

    public Action(JSONObject props){
        this.props=props;
        ref=props.optString("ref","ref");
        type=props.optString("type","button");
        this.setLabel();
        this.setIcon();
    }

    private void setLabel(){
        label=props.optString("label");
        final String color=props.optString("color");
        span=HtmlCompat.fromHtml("<font color='"+color+"'>"+label+"</font>",HtmlCompat.FROM_HTML_MODE_LEGACY);
    }

    private void setIcon(){
        String iconPath=props.optString("icon");
        final Bitmap bitmap=Notifier.getBitmapIcon(iconPath);
        if(bitmap!=null){
            icon=IconCompat.createWithBitmap(bitmap);
        }
    }

    public void addTo(NotificationCompat.Builder notibuilder){
        final Intent intent=new Intent(Notifier.context,ActionListener.class);
        /* intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setAction(Intent.ACTION_MAIN); */
        intent.putExtra("ref",ref);
        intent.putExtra("type",type);
        final int notificationId=props.optInt("notificationId");
        intent.putExtra("notificationId",notificationId);
        intent.putExtra("once",props.optBoolean("once"));

        final PendingIntent pendingIntent = PendingIntent.getBroadcast(
            Notifier.context,
            new Random().nextInt(1000),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT |
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? PendingIntent.FLAG_IMMUTABLE : 0)
        );
        
        final Builder builder=new Builder(icon,span!=null?span:label,pendingIntent);
        if(!type.equals("button")){
            final RemoteInput.Builder inputbuilder=new RemoteInput.Builder(ref);
            if(type.equals("input")){
                String placeholder=props.optString("placeholder","");
                inputbuilder.setLabel(placeholder);
            }
            else if(type.equals("select")){
                final JSONArray options=props.optJSONArray("options");
                if(options!=null){
                    final int length=options.length();
                    ArrayList<String> choices=new ArrayList<String>(length);
                    for(int i=0;i<length;i++){
                        final String option=options.optString(i);
                        if(option!=null){
                            choices.add(option);
                        }
                    }
                    if(choices.size()>0){
                        inputbuilder.setChoices(choices.toArray(new CharSequence[choices.size()]));
                        inputbuilder.setLabel("Choose from the options above");
                        builder.setAllowGeneratedReplies(true);
                    }
                }
            }
            builder.addRemoteInput(inputbuilder.build());
        }
        
        notibuilder.addAction(builder.build());
    }
}
