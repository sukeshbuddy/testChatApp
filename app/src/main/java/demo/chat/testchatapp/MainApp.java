package demo.chat.testchatapp;

import android.app.Application;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import sdk.chat.core.session.ChatSDK;
import sdk.chat.firebase.adapter.module.FirebaseModule;
import sdk.chat.firebase.push.FirebasePushModule;
import sdk.chat.firebase.ui.FirebaseUIModule;
import sdk.chat.firebase.upload.FirebaseUploadModule;
import sdk.chat.ui.module.UIModule;

public class MainApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            ChatSDK.builder()
                    .setGoogleMaps("Your Google Static Maps API key")
                    .setPublicChatRoomLifetimeMinutes(TimeUnit.HOURS.toMinutes(24))
                    .build()

                    // Add the Firebase network adapter module
                    .addModule(
                            FirebaseModule.builder()
                                    .setFirebaseRootPath("pre_1")
                                    .setDevelopmentModeEnabled(true)
                                    .build()
                    )

                    // Add the UI module
                    .addModule(UIModule.builder()
                            .setPublicRoomCreationEnabled(true)
                            .setPublicRoomsEnabled(true)
                            .build()
                    )

                    // Add modules to handle file uploads, push notifications
                    .addModule(FirebaseUploadModule.shared())
                    .addModule(FirebasePushModule.shared())

                    // Enable Firebase UI with phone and email auth
                    .addModule(FirebaseUIModule.builder()
                            .setProviders(EmailAuthProvider.PROVIDER_ID, PhoneAuthProvider.PROVIDER_ID)
                            .build()
                    )

                    // Activate
                    .build()
                    .activate(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ChatSDK.ui().setMainActivity(MainActivity.class);
    }
}
