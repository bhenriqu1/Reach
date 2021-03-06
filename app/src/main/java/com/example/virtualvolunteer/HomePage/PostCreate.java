package com.example.virtualvolunteer.HomePage;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.virtualvolunteer.Navigation;
import com.example.virtualvolunteer.R;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class PostCreate extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference postRef = database.getReference("Posts");

    private EditText description;
    private ImageView postImage;
    private Uri imageUri;
    private CheckBox opportunityTag;
    private CheckBox experienceTag;
    private Button virtualTag;
    private boolean virClicked = false;
    private Button teachingTag;
    private boolean teachClicked = false;
    private Button environmentalTag;
    private boolean envClicked = false;
    private Button recreationalTag;
    private boolean recClicked = false;
    private Button distributionTag;
    private boolean disClicked = false;
    private final int PICK_IMAGE_REQUEST = 123;
    private ArrayList<String> tags = new ArrayList<String>();
    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference("uploads");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_create);

        description = findViewById(R.id.new_post_description);
        Button postBtn = findViewById(R.id.new_post_button);
        Button postImagePicker = findViewById(R.id.new_post_image_picker);
        postImage = findViewById(R.id.new_post_image);
        opportunityTag = findViewById(R.id.new_post_opportunity);
        virtualTag = findViewById(R.id.new_post_virtual);
        teachingTag = findViewById(R.id.new_post_teaching);
        environmentalTag = findViewById(R.id.new_post_environmental);
        recreationalTag = findViewById(R.id.new_post_recreational);
        distributionTag = findViewById(R.id.new_post_distribution);
        experienceTag = findViewById(R.id.new_post_experience);
        BottomNavigationView navView = findViewById(R.id.Bottom_navigation_icon);
        Navigation.enableNavigationClick(this, navView);

        postBtn.setOnClickListener(v -> {
            if (description.getText().toString().isEmpty()) {
                Toast.makeText(PostCreate.this, "Description cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (imageUri == null) {
                Toast.makeText(PostCreate.this, "No photo selected", Toast.LENGTH_SHORT).show();
                return;
            }
            storePost(description.getText().toString());
            Toast.makeText(PostCreate.this, "Successfully made new post", Toast.LENGTH_SHORT).show();
            openHome();
        });

        postImagePicker.setOnClickListener(v -> {
            Intent imagePickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
            imagePickerIntent.setType("image/*");
            if (imagePickerIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(imagePickerIntent, PICK_IMAGE_REQUEST);
            }
        });


        virtualTag.setOnClickListener(v -> {
            virClicked = !virClicked;
            int resId = virClicked ? R.color.purple : R.color.gray;
            virtualTag.setBackgroundColor(getResources().getColor(resId));
            if (virClicked)
                tags.add("Virtual");
            else
                tags.remove("Virtual");
        });
        teachingTag.setOnClickListener(v -> {
            teachClicked = !teachClicked;
            int resId = teachClicked ? R.color.purple : R.color.gray;
            teachingTag.setBackgroundColor(getResources().getColor(resId));
            if (teachClicked)
                tags.add("Teaching");
            else
                tags.remove("Teaching");
        });
        environmentalTag.setOnClickListener(v -> {
            envClicked = !envClicked;
            int resId = envClicked ? R.color.purple : R.color.gray;
            environmentalTag.setBackgroundColor(getResources().getColor(resId));
            if (envClicked)
                tags.add("Environment");
            else
                tags.remove("Environment");
        });
        recreationalTag.setOnClickListener(v -> {
            recClicked = !recClicked;
            int resId = recClicked ? R.color.purple : R.color.gray;
            recreationalTag.setBackgroundColor(getResources().getColor(resId));
            if (recClicked) {
                tags.add("Recreational");
            } else
                tags.remove("Recreational");
        });
        distributionTag.setOnClickListener(v -> {
            disClicked = !disClicked;
            int resId = disClicked ? R.color.purple : R.color.gray;
            distributionTag.setBackgroundColor(getResources().getColor(resId));
            if (disClicked)
                tags.add("Distribution");
            else
                tags.remove("Distribution");
        });
    }

    public void storePost(String description) {
        if (opportunityTag.isChecked()) tags.add("Opportunity");
        if (experienceTag.isChecked()) tags.add("Experience");


        StorageReference photoRef = storageRef.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
        photoRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            if (taskSnapshot.getMetadata() != null) {
                Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                result.addOnSuccessListener(uri -> {
                    Post post = new Post(user.getEmail(), description, uri.toString(), System.currentTimeMillis(), tags);
                    String key = postRef.push().getKey();
                    postRef.child(key).setValue(post);
                });
            }
        }).addOnFailureListener(e -> Toast.makeText(PostCreate.this, e.getMessage(), Toast.LENGTH_SHORT).show());

    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    public void openHome() {
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            postImage.setImageURI(imageUri);
        }
    }
}
