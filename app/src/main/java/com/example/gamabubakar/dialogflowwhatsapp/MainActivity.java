package com.example.gamabubakar.dialogflowwhatsapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.devlomi.record_view.OnBasketAnimationEnd;
import com.devlomi.record_view.OnRecordClickListener;
import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.mapzen.speakerbox.Speakerbox;

import java.util.ArrayList;

import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class MainActivity extends AppCompatActivity implements RecognitionListener{
    RecordView recordView;
    RecordButton recordButton;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent=null;
    private String LOG_TAG = "VoiceRecognitionActivity";
    TextView question,answer;
    EditText et;
    Speakerbox speakerbox;  //this library use for text-to speech object ka liya

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        speakerbox = new Speakerbox(getApplication());
        recordView = findViewById(R.id.record_view);
        recordButton = findViewById(R.id.record_button);
        question=findViewById(R.id.question);
        answer=findViewById(R.id.answer);
        et=findViewById(R.id.editText);
        //IMPORTANT
        recordButton.setRecordView(recordView);
        recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                speech = null;
                recognizerIntent=null;
                listen();
                speech.startListening(recognizerIntent);
                Log.d("RecordView", "onStart");
            }

            @Override
            public void onCancel() {
                //On Swipe To Cancel
                if (speech != null) {
                    speech.destroy();
                    Log.i(LOG_TAG, "destroy");
                }
                Log.d("RecordView", "onCancel");

            }

            @Override
            public void onFinish(long recordTime) {
                speech.stopListening();
            }

            @Override
            public void onLessThanSecond() {
                //When the record time is less than One Second
                Log.d("RecordView", "onLessThanSecond");
            }
        });
        recordButton.setListenForRecord(true);

        //ListenForRecord must be false ,otherwise onClick will not be called
        recordButton.setOnRecordClickListener(new OnRecordClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "RECORD BUTTON CLICKED", Toast.LENGTH_SHORT).show();
                Log.d("RecordButton","RECORD BUTTON CLICKED");
            }
        });
        recordView.setOnBasketAnimationEndListener(new OnBasketAnimationEnd() {
            @Override
            public void onAnimationEnd() {
                Log.d("RecordView", "Basket Animation Finished");
            }
        });
        recordView.setCancelBounds(8);
        CheckUserPermsions();
    }



private void listen()
{
    if (speech == null) {
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
    }
    recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
    recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
    recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
    recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
}
    //Recognition Listener Methods
    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int error) {
        String message;
        speech.destroy();
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
                break;
            default:
                message = "Didn't understand, please try again.";
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = matches.get(0);
        question.setText("you ask for: "+text);

       // returnedText.setText(text);
        Toast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG).show();
        matches.clear();
        speech.destroy();
        dialogflow(text);
    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }
//////////////////////////////////////////////////////////////
    void CheckUserPermsions(){
        if ( Build.VERSION.SDK_INT >= 23){
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED  ){
                requestPermissions(new String[]{
                                Manifest.permission.RECORD_AUDIO},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return ;
            }
        }

        //StartIt();// init the contact list

    }
    //get acces to location permsion
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //StartIt();// init the contact list
                } else {
                    // Permission Denied
                    Toast.makeText( this,"your message" , Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //for dialogflow
    public void dialogflow(String text){
        final AIConfiguration config = new AIConfiguration("e5ce550f835f4db6922870f18b3a8ac3",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        final AIDataService aiDataService = new AIDataService(this,config);

        final AIRequest aiRequest = new AIRequest();
        aiRequest.setQuery(text);
        new AsyncTask<AIRequest, Void, AIResponse>() {
            @Override
            protected AIResponse doInBackground(AIRequest... requests) {
                final AIRequest request = requests[0];
                try {
                    final AIResponse response = aiDataService.request(aiRequest);
                    return response;
                } catch (AIServiceException e) {
                }
                return null;
            }
            @Override
            protected void onPostExecute(AIResponse aiResponse) {
                if (aiResponse != null) {
                    Result result=aiResponse.getResult();
                    answer.setText(result.getFulfillment().getSpeech());
                    speakerbox.play(answer.getText().toString());
                }
            }
        }.execute(aiRequest);
    }


//for text
    public void btnClick(View view) {
        closekeyboard();
        question.setText(et.getText().toString());
        dialogflow(et.getText().toString());
        et.setText("");
    }
    public void closekeyboard(){
        View view=this.getCurrentFocus();
        if(view !=null){
            InputMethodManager inputMethodManager= (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);

        }
    }

}

