package com.sesyme.sesyme.data;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.sesyme.sesyme.R;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class Methods {

    private Context mContext;
    private Toast toast;
    private Uri shortLink, flowchartLink;

    public Methods() {
        //Required Constructor
    }

    public Methods(Context context) {
        this.mContext = context;
    }

    public String sentenceCaseForText(String text) {

        if (text == null) return "";

        int pos = 0;
        boolean capitalize = true;
        StringBuilder sb = new StringBuilder(text);

        while (pos < sb.length()) {

            if (capitalize && !Character.isWhitespace(sb.charAt(pos))) {

                sb.setCharAt(pos, Character.toUpperCase(sb.charAt(pos)));
            } else if (!capitalize && !Character.isWhitespace(sb.charAt(pos))) {

                sb.setCharAt(pos, Character.toLowerCase(sb.charAt(pos)));
            }

            capitalize = sb.charAt(pos) == '.' || (capitalize && Character.isWhitespace(sb.charAt(pos)));

            pos++;
        }

        return sb.toString();
    }

    public void LocateString(ArrayList<String> Array, String s) {
        boolean found = false;
        for (int i = (Array.size() - 1); i > -1; i--) {
            String element = Array.get(i);
            if (element.equals(s)) {
                found = true;
            }
        }
        if (!found) {
            Array.add(s);
        }
    }

    public void LocateStringTextView(ArrayList<String> Array, String s, TextView tag) {
        boolean found = false;
        for (int i = (Array.size() - 1); i > -1; i--) {
            String element = Array.get(i);
            if (element.equals(s)) {
                Array.remove(s);
                tag.setBackground(mContext.getDrawable(R.drawable.button_follow));
                tag.setTextColor(mContext.getResources().getColor(R.color.black));
                found = true;
            }
        }
        if (!found) {
            if (Array.size() < 3) {
                Array.add(s);
                tag.setBackground(mContext.getDrawable(R.drawable.bt_ui));
                tag.setTextColor(mContext.getResources().getColor(R.color.white));
            } else {
                Toast.makeText(mContext, "You can only select 3", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void LocateStringInterests(ArrayList<String> Array, String s, ImageView interest) {
        boolean found = false;
        if (Array != null) {
            for (int i = (Array.size() - 1); i > -1; i--) {
                String element = Array.get(i);
                if (element.equals(s)) {
                    Array.remove(i);
                    interest.setVisibility(View.GONE);
                    found = true;
                }
            }
            if (!found) {
                Array.add(s);
                interest.setVisibility(View.VISIBLE);
            }
        } else {
            Toast.makeText(mContext, "Null Array", Toast.LENGTH_SHORT).show();
        }
    }

    public String prepareTags(ArrayList<String> list) {
        return "#" + TextUtils.join(" #", list);
    }


    public void showToast(CharSequence text) {
        if (toast == null)
            toast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
        else
            toast.setText(text);
        toast.show();
    }

    public String covertTimeToText(String dataDate) {

        String convTime = "Moments ago";

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault());
            Date pasTime = dateFormat.parse(dataDate);

            Date nowTime = new Date();

            assert pasTime != null;
            long dateDiff = nowTime.getTime() - pasTime.getTime();

            long second = TimeUnit.MILLISECONDS.toSeconds(dateDiff);
            long minute = TimeUnit.MILLISECONDS.toMinutes(dateDiff);
            long hour = TimeUnit.MILLISECONDS.toHours(dateDiff);
            long day = TimeUnit.MILLISECONDS.toDays(dateDiff);

            if (second < 60) {
                convTime = second + "sec ";
            } else if (minute < 60) {
                convTime = minute + "min ";
            } else if (hour < 24) {
                convTime = hour + "h ";
            } else if (day >= 7) {
                if (day > 29 && day < 360) {
                    convTime = (day / 30) + "month/s ";
                } else if (day > 359) {
                    convTime = (day / 360) + "y ";
                } else {
                    convTime = (day / 7) + "w ";
                }
            } else {
                convTime = day + "d ";
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("ConvertTimeE", Objects.requireNonNull(e.getMessage()));
        }
        return convTime;
    }

    public void generateDeepLinkQuestion(String postId, String title, String name) {
        String link = "https://sesyme.com/"
                + postId + "/&apn=com.sesyme.sesyme";

        Uri image = Uri.parse("https://firebasestorage.googleapis.com/v0/b/mvpsesyme.appspot" +
                ".com/o/icon1.png?alt=media&token=6d864039-cc8a-47e7-9da7-e774f1e78b2f");

        String shareMessage = "Hey\n" + name + " asked this question on Sesyme, "
                + "answer this question on Sesyme app or read " +
                "others' replies.\n\n";

        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(link))
                .setDomainUriPrefix("https://app.sesyme.com")
                // Open links with this app on Android
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                // Open links with com.example.ios on iOS
                .setIosParameters(new DynamicLink.IosParameters.Builder("www.sesyme.com").build())
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle(title)
                                .setDescription(shareMessage)
                                .setImageUrl(image)
                                .build())
                .buildShortDynamicLink().addOnSuccessListener(new OnSuccessListener<ShortDynamicLink>() {
            @Override
            public void onSuccess(ShortDynamicLink shortDynamicLink) {
                Uri link = shortDynamicLink.getShortLink();
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Sesyme");

                    shareIntent.putExtra(Intent.EXTRA_TEXT, link.toString());
                    mContext.startActivity(Intent.createChooser(shareIntent, "choose one"));
                } catch (Exception e) {
                    Toast.makeText(mContext.getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void generateDeepLinkReply(String postId, int Position, String title, String type, String name) {
        String deepLink = "https://sesyme.com/"
                + postId + "/" + Position + "/&apn=com.sesyme.sesyme";

        StringBuilder shareMessage = new StringBuilder();
        shareMessage.append("Hey \n");
        shareMessage.append(name);
        if (type.equals("Answer")) {
            shareMessage.append(" wrote an answer");
        }else {
            shareMessage.append(" wrote a comment");
        }
        shareMessage.append( " on this question, write your answer or read more replies to this " +
                "question and many more at Sesyme." +
                "\n\n");

        Uri image = Uri.parse("https://firebasestorage.googleapis.com/v0/b/mvpsesyme.appspot" +
                ".com/o/icon1.png?alt=media&token=6d864039-cc8a-47e7-9da7-e774f1e78b2f");

        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(deepLink))
                .setDomainUriPrefix("https://app.sesyme.com")
                // Open links with this app on Android
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                // Open links with com.example.ios on iOS
                .setIosParameters(new DynamicLink.IosParameters.Builder("www.sesyme.com").build())
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle(title)
                                .setDescription(shareMessage.toString())
                                .setImageUrl(image)
                                .build())
                .buildShortDynamicLink().addOnSuccessListener(new OnSuccessListener<ShortDynamicLink>() {
            @Override
            public void onSuccess(ShortDynamicLink shortDynamicLink) {
                Uri link = shortDynamicLink.getShortLink();
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Sesyme");

                    shareIntent.putExtra(Intent.EXTRA_TEXT, link.toString());
                    mContext.startActivity(Intent.createChooser(shareIntent, "choose one"));
                } catch (Exception e) {
                    Toast.makeText(mContext.getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void downloadPdfFile(String url, final String extension, final TextView view, final ProgressBar progressBar) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        if (url != null) {
            StorageReference storageRef = storage.getReferenceFromUrl(url);
            List<FileDownloadTask> downloadTasks = storageRef.getActiveDownloadTasks();
            if (downloadTasks.size() < 1) {
                File storagePath = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "SesymeDownloads");
//              Create directory if not exists
                if(!storagePath.exists()) {
                    storagePath.mkdirs();
                }

                Calendar date = Calendar.getInstance();
                String time = DateFormat.getDateInstance().format(date.getTime());
                final File myFile = new File(storagePath,"Sesyme "+ time + "." + extension);

                storageRef.getFile(myFile).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull FileDownloadTask.TaskSnapshot taskSnapshot) {
                        double percent = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        percent = RoundOff(percent);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            progressBar.setProgress((int) percent, true);
                        }else {
                            progressBar.setProgress((int) percent);
                        }
                        view.setVisibility(View.VISIBLE);
                        String progressString = percent + "%";
                        view.setText(progressString);
                    }
                }).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        // Local temp file has been created
                        if (extension.equals("pdf")) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.parse(myFile.getPath()), "application/pdf");
                            PackageManager pm = mContext.getPackageManager();
                            List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
                            if (activities.size() > 0) {
                                mContext.startActivity(intent);
                            } else {
                                // Do something else here. Maybe pop up a Dialog or Toast
                                Toast.makeText(mContext, "No suitable app found to open this file", Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.parse(myFile.getPath()), "image/*");
                            mContext.startActivity(intent);
                        }
                        progressBar.setVisibility(View.GONE);
                        view.setVisibility(View.GONE);
                    }
                })
            .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });
            } else {
                Toast.makeText(mContext, "Please Wait", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mContext, "Arg! where is this file... \n Sorry, File not found", Toast.LENGTH_LONG).show();
        }
    }

    public int dpToPx(int dp){
        Resources r = mContext.getResources();

        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.getDisplayMetrics()
        );
    }

    public Double RoundOff (Double val) {
        return new BigDecimal(val.toString()).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    public boolean isValidEmailId(String email){
        return Pattern.compile("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$").matcher(email).matches();
    }
}