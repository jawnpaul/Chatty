package ng.org.knowit.chatty;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseSmartReply;
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseTextMessage;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestion;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestionResult;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import ng.org.knowit.chatty.Adapter.SuggestionAdapter;
import ng.org.knowit.chatty.Models.Message;
import ng.org.knowit.chatty.Models.User;

public class ChatActivity extends AppCompatActivity implements SuggestionAdapter.OnListItemClickListener{

    MessagesList mMessagesList;

    private MessageInput mMessageInput;

    private char generatedChar;

    private String clientId, TOPIC;

    private MqttAndroidClient client;

    private MessagesListAdapter<Message> sentMessageAdapter;

    private List<FirebaseTextMessage> mFirebaseTextMessages;

    private ArrayList<String> suggestionList;

    private FirebaseSmartReply mFirebaseSmartReply;

    private RecyclerView mRecyclerView;

    private SuggestionAdapter mSuggestionAdapter;


    private ImageLoader mImageLoader;
    private static final String TAG = ChatActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mFirebaseSmartReply = FirebaseNaturalLanguage.getInstance().getSmartReply();

        mMessagesList = new MessagesList(this);
        mMessagesList = findViewById(R.id.messagesList);
        mMessageInput = findViewById(R.id.message_input);

        mRecyclerView = findViewById(R.id.suggestionRecyclerView);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true));

        mFirebaseTextMessages = new ArrayList<>();

        suggestionList = new ArrayList<>();

        mSuggestionAdapter = new SuggestionAdapter(this, suggestionList, ChatActivity.this);

        mRecyclerView.setAdapter(mSuggestionAdapter);


        mRecyclerView.setVisibility(View.INVISIBLE);

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

                if(!input.toString().trim().isEmpty()){

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

                        //Add the message to conversation history for Firebase smart reply
                        mFirebaseTextMessages.add(FirebaseTextMessage.createForLocalUser(input.toString(), System.currentTimeMillis()));
                    } catch (UnsupportedEncodingException | MqttException e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(ChatActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
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

            mFirebaseTextMessages.add(FirebaseTextMessage.createForRemoteUser(messageToDisplay, System.currentTimeMillis(), "a"));
            suggestReplies();

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

    private void suggestReplies(){
        mFirebaseSmartReply.suggestReplies(mFirebaseTextMessages)
                .addOnSuccessListener(new OnSuccessListener<SmartReplySuggestionResult>() {
            @Override
            public void onSuccess(SmartReplySuggestionResult result) {
                if (result.getStatus() == SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE) {
                    // The conversation's language isn't supported, so the
                    // the result doesn't contain any suggestions.
                } else if (result.getStatus() == SmartReplySuggestionResult.STATUS_SUCCESS) {
                    // Task completed successfully
                    // ...

                    suggestionList.clear();
                    for (SmartReplySuggestion suggestion : result.getSuggestions()) {
                        String replyText = suggestion.getText();
                        Log.d(TAG, replyText);
                        suggestionList.add(replyText);
                    }
                    mSuggestionAdapter.notifyDataSetChanged();
                    mRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        // ...
                    }
                });
    }

    @Override
    public void onListItemClick(int position) {

        String selectedReply = suggestionList.get(position);
        mMessageInput.getInputEditText().setText(" ");
        Log.d(TAG, selectedReply);
        mMessageInput.getInputEditText().setText(selectedReply);
    }
}
