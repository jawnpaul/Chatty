package ng.org.knowit.chatty;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.eclipse.paho.android.service.MqttAndroidClient;

import java.util.Date;

import ng.org.knowit.chatty.Models.Message;

public class ChatActivity extends AppCompatActivity {

    MessagesList mMessagesList;

    private MessageInput mMessageInput;

    private char generatedChar;

    private String clientId;

    private MqttAndroidClient client;

    private MessagesListAdapter<Message> sentMessageAdapter;

    private String TOPIC, name;

    private Date date;

    private ImageLoader mImageLoader;
    private static final String TAG = ChatActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
    }
}
