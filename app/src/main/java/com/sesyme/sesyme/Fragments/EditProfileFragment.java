package com.sesyme.sesyme.Fragments;


import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sesyme.sesyme.ForgotPasswordActivity;
import com.sesyme.sesyme.R;
import com.sesyme.sesyme.SignUpProcess;
import com.sesyme.sesyme.data.Methods;
import com.sesyme.sesyme.data.SefnetContract;
import com.sesyme.sesyme.data.UserDetails;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressWarnings("ConstantConditions")
public class EditProfileFragment extends Fragment implements AdapterView.
        OnItemSelectedListener {

    private static final String TAG = "EditProfileFragment";
    private static final int REQUEST_CODE_PROFILE = 100;
    private static final int REQUEST_CODE_COVER = 101;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference userInfoRef;
    private EditText etDate, etFName, etCourse, etStaff, etPhone;
    private ImageView profileImage, coverImage;
    private Uri profileImageUri, coverImageUri;
    private ProgressBar progressBar;
    private String profileImageUrl, profile, coverImageUrl, gender, email, course,
            dobString, title, affiliation, university, fName, phone;
    private DocumentReference userProfile;
    private Spinner genderSpinner, affiliationSpinner, universitySpinner;
    private TextView changePassword, progressView;
    private StorageReference profileImageRef, coverImageRef;
    private FirebaseUser user;
    private ViewPager pager;
    private FirebaseAuth auth;
    private String token;
    private Methods methods;
    private Date limit, current;
    private Toast toast;
    private String staff = null;
    private LinearLayout staffLayout;
    private int cDay, cMonth, cYear;
    private Boolean complete;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View editProfileView = inflater.inflate(R.layout.activity_edit_profile, container, false);

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        userInfoRef = db.collection(SefnetContract.USER_DETAILS);
        methods = new Methods();

        etFName = editProfileView.findViewById(R.id.et_first_name_edit_profile);
        etCourse = editProfileView.findViewById(R.id.et_course_edit_profile);
        etPhone = editProfileView.findViewById(R.id.phone_edit_profile);
        etDate = editProfileView.findViewById(R.id.et_birth_date_edit_profile);
        etStaff = editProfileView.findViewById(R.id.et_staff_edit_profile);
        changePassword = editProfileView.findViewById(R.id.edit_profile_change_password);
        profileImage = editProfileView.findViewById(R.id.profile_image_edit_profile);
        coverImage = editProfileView.findViewById(R.id.cover_image_edit_profile);
        progressBar = editProfileView.findViewById(R.id.progress_bar_edit_profile);
        progressView = editProfileView.findViewById(R.id.progress_text_edit_profile);
        ImageView chooseProfilePic = editProfileView.findViewById(R.id.btnProfileEditProfile);
        staffLayout = editProfileView.findViewById(R.id.staff_number_layout);
        Button chooseCoverPic = editProfileView.findViewById(R.id.btnCoverEditProfile);
        genderSpinner = editProfileView.findViewById(R.id.spinner_gender_edit_profile);
        affiliationSpinner = editProfileView.findViewById(R.id.affiliation_spinner);
        universitySpinner = editProfileView.findViewById(R.id.spinner_university_edit_profile);
        gender = "Unspecified";
        affiliation = "Student";

        etDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    selectDate();
                }
            }
        });

        Calendar c = Calendar.getInstance();
        cYear = c.get(Calendar.YEAR) - 13;
        cMonth = c.get(Calendar.MONTH);
        cDay = c.get(Calendar.DAY_OF_MONTH);

        c.set(Calendar.YEAR, cYear);
        c.set(Calendar.MONTH, cMonth);
        c.set(Calendar.DAY_OF_MONTH, cDay);
        limit = c.getTime();

        pager = ((SignUpProcess) getActivity()).findViewById(R.id.pager);

        Glide.with(getActivity().getApplicationContext())
                .load(R.drawable.img).into(profileImage);
        Glide.with(getActivity().getApplicationContext()).load(R.drawable.leee)
                .centerCrop().into(coverImage);

        ArrayAdapter<CharSequence> universityAdapter = ArrayAdapter.
                createFromResource(getActivity(), R.array.universities, android.R.layout.simple_spinner_item);
        universityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        universitySpinner.setAdapter(universityAdapter);
        universitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                university = adapterView.getItemAtPosition(i).toString().trim();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.
                createFromResource(getActivity(), R.array.gender, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);
        genderSpinner.setOnItemSelectedListener(this);

        ArrayAdapter<CharSequence> affiliationAdapter = ArrayAdapter.
                createFromResource(getActivity(), R.array.affiliation, android.R.layout.simple_spinner_item);
        affiliationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        affiliationSpinner.setAdapter(affiliationAdapter);
        affiliationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                affiliation = adapterView.getItemAtPosition(i).toString().trim();
                if (affiliation.equals("Lecturer")) {
                    staffLayout.setVisibility(View.VISIBLE);
                } else {
                    staffLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (affiliation.equals("Lecturer")) {
            staffLayout.setVisibility(View.VISIBLE);
        } else {
            staffLayout.setVisibility(View.GONE);
        }

        title = "Create Profile";
        changePassword.setVisibility(View.GONE);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user != null) {
            email = user.getEmail();
            if (email != null) {
                userProfile = userInfoRef.document(email);
                progressBar.setVisibility(View.VISIBLE);
                userProfile.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            title = "Edit Profile";
                            fName = documentSnapshot.getString(SefnetContract.FULL_NAME);
                            dobString = documentSnapshot.getString(SefnetContract.DOB);
                            etFName.setText(fName);
                            etDate.setText(dobString);
                            profile = documentSnapshot.getString(SefnetContract.PROFILE_URL);
                            coverImageUrl = documentSnapshot.getString(SefnetContract.COVER_URL);
                            course = documentSnapshot.getString("course");
                            etCourse.setText(course);

                            if (documentSnapshot.getString("staffNumber") != null) {
                                String staff = documentSnapshot.getString("staffNumber");
                                etStaff.setText(staff);
                            }
                            Glide.with(getActivity().getApplicationContext()).load(profile)
                                    .error(R.drawable.img).into(profileImage);
                            Glide.with(getActivity().getApplicationContext()).load(coverImageUrl)
                                    .centerCrop().error(R.drawable.leee).into(coverImage);
                            String genderSelected = documentSnapshot.getString(SefnetContract.GENDER);
                            if (genderSelected != null) {
                                switch (genderSelected) {
                                    case "Male":
                                        genderSpinner.setSelection(1);
                                        break;
                                    case "Female":
                                        genderSpinner.setSelection(2);
                                        break;
                                    default:
                                        genderSpinner.setSelection(0);
                                }
                            }
                            String aff = documentSnapshot.getString("affiliation");
                            if (aff != null) {
                                switch (aff) {
                                    case "Student":
                                        affiliationSpinner.setSelection(0);
                                        break;
                                    case "Lecturer":
                                        affiliationSpinner.setSelection(1);
                                        break;
                                    case "Alumnus":
                                        affiliationSpinner.setSelection(2);
                                        break;
                                }
                            }
                            university = documentSnapshot.getString("university");
                            if (university != null) {
                                setUniversity(university);
                            } else {
                                Toast.makeText(getActivity(), "university null", Toast.LENGTH_SHORT).show();
                            }
                            changePassword.setVisibility(View.VISIBLE);
                            changePassword.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startActivity(new Intent(getActivity(), ForgotPasswordActivity.class));
                                }
                            });
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        methods.showToast(e.getMessage());
                    }
                });

                if (dobString != null) {
                    try {
                        current = new SimpleDateFormat("dd LLL yyyy", Locale.getDefault()).parse(dobString);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        Button save = editProfileView.findViewById(R.id.btn_save_edit_profile);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = auth.getCurrentUser();
                email = user.getEmail();
                if (email != null) {
                    progressBar.setVisibility(View.VISIBLE);
                    Toast.makeText(getActivity(), "Saving Profile", Toast.LENGTH_SHORT).show();
                    if (affiliation.equals("Lecturer")) {
                        staff = etStaff.getText().toString().trim();
                        if (staff != null && !staff.isEmpty()) {
                            saveUserInfo();
                        } else {
                            showToast("Please provide staff number for verification");
                        }
                    } else {
                        saveUserInfo();
                    }
                } else {
                    methods.showToast("Please Login or SignUp First");
                }
            }
        });
        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDate();
            }
        });

        chooseProfilePic.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (methods.checkIfAlreadyHavePermission()) {
                    requestPermissions(new String[]{Manifest.permission
                            .WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_PROFILE);
                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, REQUEST_CODE_PROFILE);
                }
            }
        });

        chooseCoverPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (methods.checkIfAlreadyHavePermission()) {
                    requestPermissions(new String[]{Manifest.permission
                            .WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_COVER);
                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, REQUEST_CODE_COVER);
                }
            }
        });

        return editProfileView;
    }

    private void uploadImageToStorage() {
        fName = methods.sentenceCaseForText(etFName.getText().toString().trim());
        if (TextUtils.isEmpty(fName)) {
            showToast("Please Enter your Name and Surname to Continue");
        } else {
            profileImageRef = FirebaseStorage.getInstance()
                    .getReference(SefnetContract.PROFILE_PICS_REF + email + " Profile pic" + ".jpg");
            // Upload profile picture
            if (profileImageUri != null) {
                profileTask();
            } else {
                uploadCover();
            }
        }
    }

    private void profileTask() {
        progressBar.setVisibility(View.VISIBLE);
        profileImageRef.putFile(profileImageUri).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double percent = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                percent = methods.RoundOff(percent, 1);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progressBar.setProgress((int) percent, true);
                } else {
                    progressBar.setProgress((int) percent);
                }
                progressView.setVisibility(View.VISIBLE);
                String progressText = percent + "%";
                progressView.setText(progressText);

            }
        }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return profileImageRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    profileImageUrl = String.valueOf(downloadUri);
                    userProfile = userInfoRef.document(email);
                    userProfile.update(SefnetContract.PROFILE_URL, profileImageUrl);
                    progressView.setVisibility(View.GONE);
                    uploadCover();
                } else {
                    showToast("upload failed: "
                            + task.getException().getMessage());
                }
            }
        });
    }

    private void uploadCover() {
        coverImageRef = FirebaseStorage.getInstance()
                .getReference("Cover Pics/" + email + " Cover" + ".jpg");
        coverTask();
    }

    private void coverTask() {
        if (coverImageUri != null) {
            // Upload cover picture
            progressBar.setVisibility(View.VISIBLE);
            coverImageRef.putFile(coverImageUri).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double percent = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    percent = methods.RoundOff(percent, 1);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        progressBar.setProgress((int) percent, true);
                    } else {
                        progressBar.setProgress((int) percent);
                    }
                    progressView.setVisibility(View.VISIBLE);
                    String progressText = percent + "%";
                    progressView.setText(progressText);
                }
            }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return coverImageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadCUri = task.getResult();
                        coverImageUrl = String.valueOf(downloadCUri);
                        userProfile = userInfoRef.document(email);
                        userProfile.update(SefnetContract.COVER_URL, coverImageUrl);
                        showToast("Cover Picture Uploaded");
                    } else {
                        showToast("Cover picture upload failed: "
                                + task.getException().getMessage());
                        Log.e(TAG, "onComplete: ", task.getException());
                    }
                    progressBar.setVisibility(View.GONE);
                    progressView.setVisibility(View.GONE);
                    redirect();
                }
            });
        } else {
            redirect();
        }
    }

    private void redirect() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
                if (title.equals("Create Profile")) {
                    pager.setCurrentItem(2);
                    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                    getActivity().setTitle("Choose Interests");
                } else {
                    getActivity().finish();
                }
            }
        }, 500);
    }

    private void saveUserInfo() {
        fName = etFName.getText().toString().trim();
        course = etCourse.getText().toString().trim();
        staff = etStaff.getText().toString().trim();
        phone = etPhone.getText().toString().trim();

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    token = task.getResult().getToken();
                }
            }
        });

        if (TextUtils.isEmpty(fName) || TextUtils.isEmpty(course)
                || TextUtils.isEmpty(dobString) ||
                dobString == null || course == null) {
            showToast("Please fill in all the information.");
            progressBar.setVisibility(View.GONE);
        } else if (fName.length() < 6 || course.length() < 3) {
            showToast("Please enter correct names and course");
            progressBar.setVisibility(View.GONE);
        } else if (current != null && limit != null && current.after(limit)) {
            showToast("Check your Date of Birth");
            progressBar.setVisibility(View.GONE);
        } else if (university.equals("Please Select your Institution")) {
            showToast("Please Select your Institution!");
            progressBar.setVisibility(View.GONE);
        } else {
            complete = false;
            userProfile = userInfoRef.document(email);
            userProfile.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            userProfile.update(SefnetContract.FULL_NAME, fName,
                                    SefnetContract.GENDER, gender, SefnetContract.DOB, dobString,
                                    SefnetContract.COURSE, course, "affiliation", affiliation
                                    , SefnetContract.UNIVERSITY, university, "staffNumber", staff);
                            uploadImageToStorage();
                        } else if (token != null && !token.isEmpty()) {
                            userProfile.set(new UserDetails(fName, gender, course,
                                    university, dobString, profileImageUrl, coverImageUrl,
                                    email, token, affiliation, staff, phone))
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                uploadImageToStorage();
                                                complete = true;
                                            } else {
                                                showToast("Something went wrong");
                                            }
                                        }
                                    });
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!complete && profileImageUri == null && coverImageUri == null) {
                                    showToast("Please check your connection");
                                }
                            }
                        }, 4000);
                    } else {
                        showToast("Something went wrong please try again!");
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_COVER && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE_COVER);
        } else if (requestCode == REQUEST_CODE_PROFILE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE_PROFILE);
        } else {
            showToast("Permission Denied");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //noinspection AccessStaticViaInstance
        if (requestCode == REQUEST_CODE_PROFILE && resultCode ==
                getActivity().RESULT_OK && data != null && data.getData() != null) {
            profileImageUri = data.getData();

            showToast("Profile Selected");
            Glide.with(getActivity().getApplicationContext()).load(profileImageUri)
                    .error(R.drawable.img).circleCrop().into(profileImage);

        } else //noinspection AccessStaticViaInstance
            if (requestCode == REQUEST_CODE_COVER && resultCode ==
                    getActivity().RESULT_OK && data != null && data.getData() != null) {
                coverImageUri = data.getData();

                showToast("Cover Selected");
                Glide.with(getActivity().getApplicationContext()).load(coverImageUri)
                        .error(R.drawable.img).centerCrop().into(coverImage);
            }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        gender = parent.getItemAtPosition(position).toString().trim();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void selectDate() {
        int sYear;
        int sMonth;
        int sDay;

        if (current != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(current);
            sYear = cal.get(Calendar.YEAR);
            sMonth = cal.get(Calendar.MONTH);
            sDay = cal.get(Calendar.DAY_OF_MONTH);
        } else {
            sYear = cYear;
            sMonth = cMonth;
            sDay = cDay;
        }
        DatePickerDialog datePicker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar date = Calendar.getInstance();
                date.set(Calendar.YEAR, year);
                date.set(Calendar.MONTH, monthOfYear);
                date.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                dobString = DateFormat.getDateInstance().format(date.getTime());
                current = date.getTime();
                if (current.after(limit)) {
                    showToast("You must be at least 13 years old to sign up.");
                } else {
                    etDate.setText(dobString);
                    etCourse.requestFocus();
                }
            }
        }, sYear, sMonth, sDay);
        datePicker.show();
    }

    private void showToast(CharSequence text) {
        if (getActivity() != null) {
            if (toast == null)
                toast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
            else
                toast.setText(text);
            toast.show();
        }
    }

    private void setUniversity(String university) {
        switch (university) {
            case "Cape Peninsula University of Technology":
                universitySpinner.setSelection(1);
                break;
            case "Central University of Technology":
                universitySpinner.setSelection(2);
                break;
            case "Durban University of Technology":
                universitySpinner.setSelection(3);
                break;
            case "Mangosuthu University of Technology":
                universitySpinner.setSelection(4);
                break;
            case "Monash South Africa":
                universitySpinner.setSelection(5);
                break;
            case "Nelson Mandela University":
                universitySpinner.setSelection(6);
                break;
            case "North-West University":
                universitySpinner.setSelection(7);
                break;
            case "Rhodes University":
                universitySpinner.setSelection(8);
                break;
            case "Sefako Makgatho Health Sciences University":
                universitySpinner.setSelection(9);
                break;
            case "Sol Plaatje University":
                universitySpinner.setSelection(10);
                break;
            case "Stellenbosch University":
                universitySpinner.setSelection(11);
                break;
            case "Tshwane University of Technology":
                universitySpinner.setSelection(12);
                break;
            case "University of Cape Town":
                universitySpinner.setSelection(13);
                break;
            case "University of Fort Hare":
                universitySpinner.setSelection(14);
                break;
            case "University of Johannesburg":
                universitySpinner.setSelection(15);
                break;
            case "University of KwaZulu-Natal":
                universitySpinner.setSelection(16);
                break;
            case "University of Limpopo":
                universitySpinner.setSelection(17);
                break;
            case "University of Mpumalanga":
                universitySpinner.setSelection(18);
                break;
            case "University of Pretoria":
                universitySpinner.setSelection(19);
                break;
            case "University of South Africa":
                universitySpinner.setSelection(20);
                break;
            case "University of the Free State":
                universitySpinner.setSelection(21);
                break;
            case "University of the Western Cape":
                universitySpinner.setSelection(22);
                break;
            case "University of the Witwatersrand":
                universitySpinner.setSelection(23);
                break;
            case "University of Venda":
                universitySpinner.setSelection(24);
                break;
            case "University of Zululand":
                universitySpinner.setSelection(25);
                break;
            case "Vaal University of Technology":
                universitySpinner.setSelection(26);
                break;
            case "Walter Sisulu University":
                universitySpinner.setSelection(27);
                break;
            default:
                universitySpinner.setSelection(0);
        }
    }
}