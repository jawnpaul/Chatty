package ng.org.knowit.chatty;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    private TextInputLayout chatIdInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatIdInputLayout = findViewById(R.id.chatIdTextInputLayout);

        Button chatButton = findViewById(R.id.chatButton);

        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean noError = true;
                String editTextString = chatIdInputLayout.getEditText().getText().toString();
                if (editTextString.isEmpty()) {
                    chatIdInputLayout.setError(getResources().getString(R.string.error_string));
                    noError = false;
                } else {
                    chatIdInputLayout.setError(null);
                }

                if (noError) {
                    // All fields are valid!
                    Toast.makeText(MainActivity.this, "Not well", Toast.LENGTH_SHORT).show();
                }
            }

        });

    }
}
