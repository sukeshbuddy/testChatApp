package demo.chat.testchatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import sdk.chat.app.firebase.ChatSDKFirebase;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.AccountDetails;
import sdk.chat.core.types.MessageType;
import sdk.chat.firebase.adapter.FirebasePaths;
import sdk.chat.firebase.adapter.wrappers.UserWrapper;
import sdk.guru.common.RX;

import static sdk.chat.ui.update.ThreadUpdateAction.reload;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText inputMessage;
//    private final String emailID = "abc@gmail.com";
    private final String emailID = "sukeshbuddy@gmail.com";
    private TextView chatView;
    private Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        inputMessage = findViewById(R.id.textInput);
        chatView = findViewById(R.id.textOutput);
        btnSend = findViewById(R.id.btnSend);

        ChatSDK.auth().authenticate(AccountDetails.username(emailID, "password")).subscribe(() -> {

        }, throwable -> {

        });

        // Check if user is logged in
        boolean isLoggedIn = ChatSDK.auth().isAuthenticated();
        Log.e("isLoggedIn : ",isLoggedIn+"");
        if (isLoggedIn){
            FirebaseUser user = mAuth.getCurrentUser();
            Log.d("user",user.getDisplayName());
            postLoginActivity();

//            getAllRegisteredUsers().subscribe((users, throwable) -> {
//                Log.d("All users :: ",users.toString());
//            });

        }else {
            mAuth.signInWithEmailAndPassword(emailID, "password")
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("TAG", "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                postLoginActivity();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("TAG", "signInWithEmail:failure", task.getException());
                            }
                        }
                    });
        }


    }

    private void postLoginActivity(){
//        Log.d("size:",ChatSDK.+"");
        Log.d("chat", ChatSDK.ui().getChatOptions().get(0).getTitle()+"");
        System.out.println(Arrays.toString(ChatSDK.ui().getChatOptions().toArray()));
        User user = ChatSDK.core().currentUser();
        Log.d("user :: ",user.getName()+" : entityId : "+user.getEntityID());

//        User otherUser = ChatSDK.core().getUserNowForEntityID(user.getEntityID());
//        ChatSDK.thread().createThread("h1", Collections.singletonList(otherUser)).subscribe(thread1 -> {
//
//            // Send a message
//            ChatSDK.thread().sendMessageWithText("Hi ll", thread1).subscribe();
//
//        });
//
//        // Get a list of public threads
        List<Thread> threads = ChatSDK.thread().getThreads(ThreadType.Private1to1);
        printLog("threads 1:: "+threads.size());
        printLog("threads 1:: "+threads.get(0).getMembers());
        printLog("threads 2:: "+threads.get(0).getMetaValues().get(0).getId());
        printLog("threads 3:: "+threads.get(0).getDisplayName());
        printLog("threads 3:: "+threads.get(0).getEntityID());

        try {
//            User secondUser = ChatSDK.core().getUserNowForEntityID("wsuHRezVZMRVHVXICHsX20tchVq1");
            User secondUser = ChatSDK.core().getUserNowForEntityID("fOjsEb17bzXZdzgH0S1v84TFI9v1");
            ChatSDK.core().userOn(secondUser);

            btnSend.setOnClickListener(v -> {
                printLog("input message :: "+inputMessage.getText().toString());
//                Disposable disposableThread = ChatSDK.thread().createThread("chatwithabcAndSukesh", Collections.singletonList(secondUser)).subscribe(thread1 -> {
//
//                            // Send a message
//                            ChatSDK.thread().sendMessageWithText(inputMessage.getText().toString(), thread1).subscribe();
//                            addChat();
//                            displayChat();
//
//                        });

                ChatSDK.thread().sendMessageWithText(inputMessage.getText().toString(), threads.get(0)).subscribe();
                addChat();
                displayChat();

//                Disposable disposableThread = ChatSDK.thread().createThread("chatuser2", secondUser, ChatSDK.currentUser())
//                        .observeOn(RX.main())
//                        .doFinally(() -> {
//                            // Runs when process completed from error or success
//                            Log.d("do finally block","");
//                        })
//                        .subscribe(thread -> {
//                            // When the thread type created
//                            Log.d("subscribe block","");
//                            ChatSDK.thread().sendMessageWithText(inputMessage.getText().toString(), thread).subscribe();
//                            addChat();
//                            displayChat();
//                        }, throwable -> {
//                            // If there type an error
//                            Log.d("error block","");
//                        });
//                disposableThread.dispose();
                });


        }catch (Exception e){
            e.printStackTrace();
        }

        printLog("Messages :: "+threads.get(0).getMessages().toString());

        Disposable disposableMessageListener = ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.MessageAdded))
                .subscribe(new Consumer<NetworkEvent>() {
                    @Override
                    public void accept(NetworkEvent networkEvent) throws Exception {
//                        Message message = networkEvent.getMessage();
                       printLog("messageReceived");
//                        addChat();
//                        displayChat();
                    }
                });


    }


    public static Single<List<User>> getAllRegisteredUsers() {
        return Single.create(emitter -> {
            DatabaseReference ref = FirebasePaths.usersRef();
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<User> users = new ArrayList<>();
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            UserWrapper uw = new UserWrapper(child);
                            users.add(uw.getModel());
                        }
                    }
                    emitter.onSuccess(users);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    emitter.onError(databaseError.toException());
                }
            });
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            reload();
        }else {
            createUser();
        }
    }

    private void createUser() {
        mAuth.createUserWithEmailAndPassword(emailID, "password")
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.e("user ", "created");
                            postLoginActivity();
                        }
                    }
                });
    }

    private void addChat(){
        SharedPreferences prefs = getSharedPreferences("MY_PREFS_CHATApp", MODE_PRIVATE);
        String chats = prefs.getString("chat", "");
        SharedPreferences.Editor editor = getSharedPreferences("MY_PREFS_CHATApp", MODE_PRIVATE).edit();

        editor.putString("chat", chats+" \n"+ inputMessage.getText().toString());
        editor.apply();
    }

    private String getChatsFromPreference(){
        SharedPreferences prefs = getSharedPreferences("MY_PREFS_CHATApp", MODE_PRIVATE);
        return prefs.getString("chat", "No chat found");
    }

    private void displayChat(){
        chatView.setText( getChatsFromPreference());
    }

    private void printLog(String message){
        System.out.println(message);
    }
}