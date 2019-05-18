package ng.org.knowit.chatty;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import ng.org.knowit.chatty.Models.Message;
import ng.org.knowit.chatty.Models.User;

public class ChatActivity extends AppCompatActivity {

    MessagesList mMessagesList;

    private MessageInput mMessageInput;

    private char generatedChar;

    private String clientId;

    private MqttAndroidClient client;

    private MessagesListAdapter<Message> sentMessageAdapter;

    private String TOPIC;


    private ImageLoader mImageLoader;
    private static final String TAG = ChatActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mMessagesList = new MessagesList(this);
        mMessagesList = findViewById(R.id.messagesList);
        mMessageInput = findViewById(R.id.message_input);


        generatedChar = generateChar();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("subscribeTo")){
            TOPIC = intent.getStringExtra("subscribeTo");
        }

        clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), "tcp://broker.hivemq.com:1883",
                clientId);

        if (isOnline()){
            connect();
        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }


        mImageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, @Nullable String url,
                    @Nullable Object payload) {
                Glide.with(ChatActivity.this).load(url).into(imageView);
            }
        };

        sentMessageAdapter = new MessagesListAdapter<>("John", mImageLoader);
        mMessagesList.setAdapter(sentMessageAdapter);

        mMessageInput.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {

                //Publisher will publish to his own topic
                String topic = TOPIC;

                //Add the generated character to the end of the message input
                String payload = input.toString() + generatedChar;
                byte[] encodedPayload = new byte[0];
                try {
                    encodedPayload = payload.getBytes("UTF-8");
                    MqttMessage mqttmessage = new MqttMessage(encodedPayload);

                    //Publish to a specific topic; in this case Unique chat ID
                    client.publish(topic, mqttmessage);

                    Date date = Calendar.getInstance().getTime();

                    User user = new User("John", "df", null);
                    Message message1 = new Message("Will", input.toString(), date, user);

                    sentMessageAdapter.addToStart(message1, true);

                } catch (UnsupportedEncodingException | MqttException e) {
                    e.printStackTrace();
                }

                return true;
            }
        });


    }

    private void subscribe( String topic){
        topic = TOPIC;
        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published


                    Log.d(TAG, "Subscription successful");

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                        Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                //Receiver will receive from publisher topic so it has to be subscribed to publisher topic
                //For receiver to receive it has to be subscribed to the sender/publisher topic

                processMessage(message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });


    }

    private void processMessage(MqttMessage message){
        String messageContent = message.toString();
        char a = messageContent.charAt(messageContent.length() - 1);

        if (a == generatedChar){
            //do not show message

        } else {
            //show message

            User user = new User("Paul", "df", null);

            Date date = Calendar.getInstance().getTime();

            String messageToDisplay = messageContent.substring(0, messageContent.length()-1);

            Message message1 = new Message("Doe", messageToDisplay, date, user);

            sentMessageAdapter.addToStart(message1, true);
        }


    }

    private void connect(){

        clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), "tcp://broker.hivemq.com:1883",
                clientId);

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "onSuccess");

                    subscribe(TOPIC);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private char generateChar(){
        Random r = new Random();
        return (char)(r.nextInt(26) + 'a');
    }
}
