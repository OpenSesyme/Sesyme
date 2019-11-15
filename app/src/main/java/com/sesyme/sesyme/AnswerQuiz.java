package com.sesyme.sesyme;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.sesyme.sesyme.Adapter.QuizResultsAdapter;
import com.sesyme.sesyme.data.Methods;
import com.sesyme.sesyme.data.ResultsClass;
import com.sesyme.sesyme.data.SefnetContract;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AnswerQuiz extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference questionsRef;
    private TextView title, score, percentView, empty;
    private Methods methods;
    private RadioGroup optionsRadio;
    private RadioButton radio1, radio2, radio3, radio4;
    private LinearLayout dotsLayout;
    private ArrayList<String> list, options, option1, option2, option3, option4, attempted;
    private String quizId, email, selected, answer, creator;
    private RecyclerView resultsRecycler;
    private CardView questionCard;
    private ArrayList<ResultsClass> resultsList;
    private Date releaseAnswers, today;
    private Button next;
    private Boolean answered = false;
    private int i, marks, userMarks, total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_quiz);

        title = findViewById(R.id.question_answer_quiz);
        optionsRadio = findViewById(R.id.radio_group_answer_quiz);
        radio1 = findViewById(R.id.radio1_answer_quiz);
        radio2 = findViewById(R.id.radio2_answer_quiz);
        radio3 = findViewById(R.id.radio3_answer_quiz);
        radio4 = findViewById(R.id.radio4_answer_quiz);
        dotsLayout = findViewById(R.id.dots_answer_quiz);
        questionCard = findViewById(R.id.linearLayout);
        score = findViewById(R.id.score_answer_quiz);
        empty = findViewById(R.id.empty_answer_quiz);
        resultsRecycler = findViewById(R.id.quiz_results_recycler);
        percentView = findViewById(R.id.score_percent_answer_quiz);
        next = findViewById(R.id.btn_next_answer_quiz);
        methods = new Methods(this);
        options = new ArrayList<>();
        attempted = new ArrayList<>();
        score.setVisibility(View.GONE);
        i = 0;
        marks = 0;

        today = Calendar.getInstance().getTime();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(SefnetContract.QUIZ_ID)) {
            quizId = intent.getStringExtra(SefnetContract.QUIZ_ID);
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null){
            email = user.getEmail();
        }

        if (quizId != null) {
            questionsRef = db.document(quizId).collection(SefnetContract.QUESTIONS);
            db.document(quizId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e == null && documentSnapshot != null) {
                        String title = documentSnapshot.getString(SefnetContract.TITLE);
                        creator = documentSnapshot.getString(SefnetContract.CREATOR);
                        releaseAnswers = documentSnapshot.getDate(SefnetContract.RELEASE_ANSWERS);
                        if (documentSnapshot.get("attempted") != null){
                            //noinspection unchecked
                            attempted = (ArrayList<String>) documentSnapshot.get("attempted");
                        }
                        setTitle(title);
                    }
                }
            });

            questionsRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (queryDocumentSnapshots != null) {
                        list = new ArrayList<>();
                        int q = 1;
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                            list.add(snapshot.getId());
                            TextView textView = new TextView(AnswerQuiz.this);
                            textView.setTag(snapshot.getId());
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                                    (methods.dpToPx(20), methods.dpToPx(20));
                            params.setMargins(6, 6, 6, 6);
                            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                            textView.setGravity(Gravity.CENTER);
                            textView.setLayoutParams(params);
                            textView.setText(String.valueOf(q));
                            textView.setBackground(getResources().getDrawable(R.drawable.flag_transparent));
                            dotsLayout.addView(textView);
                            q++;
                        }
                        if (email.equals(creator)){
                            showStats();
                        }else {
                            setUpQuestion(false);
                        }
                    }
                }
            });
        }

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (next.getText().equals("View Answers")){
                    if (releaseAnswers.before(today)) {
                        i = 0;
                        optionsRadio.clearCheck();
                        next.setText(getResources().getString(R.string.next));
                        setUpQuestion(true);
                    }else {
                        String time = methods.covertTimeToText(String.valueOf(releaseAnswers));
                        methods.showToast("Answers will be released in " + time);
                    }
                }else {
                    if (answered) {
                        if (i == (list.size() - 1)) {
                            next.setText("View Answers");
                            displayScore();
                            return;
                        }
                        i++;
                        if (releaseAnswers.before(today)) {
                            setUpQuestion(true);
                        }else {
                            setUpQuestion(false);
                        }
                    } else {
                        selected = onRadioButtonClicked(optionsRadio);
                        if (selected == null) {
                            methods.showToast("Please choose an answer");
                            return;
                        }
                        if (selected.equals(answer)) {
                            marks++;
                        }
                        if (i < list.size()) {
                            TextView textView = dotsLayout.findViewWithTag(list.get(i));
                            HighlightCircle(textView);
                            addAnswer(selected, list.get(i));
                            if (i == (list.size() - 2)) {
                                next.setText("Finish");
                            }
                            if (i == (list.size() - 1)) {
                                next.setText("View Answers");
                                displayScore();
                                return;
                            }
                            i++;
                            setUpQuestion(false);
                        }
                    }
                }
            }
        });
    }

    private void showStats() {
        resultsList = new ArrayList<>();
        questionCard.setVisibility(View.GONE);
        dotsLayout.setVisibility(View.GONE);
        next.setVisibility(View.GONE);
        empty.setVisibility(View.GONE);
        resultsRecycler.setVisibility(View.VISIBLE);
        if (attempted.size() > 0){
            for (int m = 0; m < attempted.size(); m++) {
                final String user = attempted.get(m);
                userMarks = 0;
                questionsRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots != null) {
                            total = queryDocumentSnapshots.size();
                            for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                                answer = snapshot.getString("answer");
                                options = (ArrayList<String>) snapshot.get(SefnetContract.OPTIONS);
                                option1 = (ArrayList<String>) snapshot.get(SefnetContract.OPTION_1);
                                option2 = (ArrayList<String>) snapshot.get(SefnetContract.OPTION_2);
                                option3 = (ArrayList<String>) snapshot.get(SefnetContract.OPTION_3);
                                option4 = (ArrayList<String>) snapshot.get(SefnetContract.OPTION_4);
                                switch (answer) {
                                    case "Option1":
                                        if (LocateString(option1, user)){
                                            userMarks++;
                                        }
                                            break;
                                    case "Option2":
                                        if (LocateString(option2, user)){
                                            userMarks++;
                                        }
                                        break;
                                    case "Option3":
                                        if (LocateString(option3, user)){
                                            userMarks++;
                                        }
                                        break;
                                    case "Option4":
                                        if (option4 != null && LocateString(option4, user)) {
                                            userMarks++;
                                        }
                                        break;
                                }
                            }
                            double percent = (userMarks * 100) / total;
                            String marksString = userMarks + "/" + total + "     " + percent + "%";
                            ResultsClass result = new ResultsClass(user, marksString);
                            resultsList.add(result);

                            QuizResultsAdapter resultsAdapter = new QuizResultsAdapter(AnswerQuiz.this, resultsList);
                            resultsRecycler.setLayoutManager(new LinearLayoutManager(AnswerQuiz.this));
                            resultsRecycler.setAdapter(resultsAdapter);
                        }
                    }
                });
            }
        }else {
            empty.setText("Nobody has attempted this quiz yet");
            empty.setVisibility(View.VISIBLE);
        }
    }

    private void setUpQuestion(final Boolean showCorrect) {
        resultsRecycler.setVisibility(View.GONE);
        questionCard.setVisibility(View.VISIBLE);
        dotsLayout.setVisibility(View.VISIBLE);
        next.setVisibility(View.VISIBLE);
        if (quizId != null && i < list.size()) {
            percentView.setVisibility(View.GONE);
            score.setVisibility(View.GONE);
            optionsRadio.setVisibility(View.VISIBLE);
            radio1.setBackgroundColor(getResources().getColor(R.color.colorTransparent));
            radio2.setBackgroundColor(getResources().getColor(R.color.colorTransparent));
            radio3.setBackgroundColor(getResources().getColor(R.color.colorTransparent));
            radio4.setBackgroundColor(getResources().getColor(R.color.colorTransparent));
            optionsRadio.clearCheck();
            option1 = new ArrayList<>();
            option2 = new ArrayList<>();
            option3 = new ArrayList<>();
            option4 = new ArrayList<>();
            questionsRef.document(list.get(i)).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @SuppressWarnings("unchecked")
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot != null){
                        answered = false;
                        String titleString = documentSnapshot.getString("question");
                        answer = documentSnapshot.getString("answer");
                        options = (ArrayList<String>) documentSnapshot.get(SefnetContract.OPTIONS);
                        option1 = (ArrayList<String>) documentSnapshot.get(SefnetContract.OPTION_1);
                        option2 = (ArrayList<String>) documentSnapshot.get(SefnetContract.OPTION_2);
                        option3 = (ArrayList<String>) documentSnapshot.get(SefnetContract.OPTION_3);
                        option4 = (ArrayList<String>) documentSnapshot.get(SefnetContract.OPTION_4);

                        if (options != null) {
                            String option1 = options.get(0);
                            String option2 = options.get(1);
                            String option3 = options.get(2);
                            String option4 = options.get(3);

                            title.setText(titleString);
                            radio1.setText(option1);
                            radio2.setText(option2);
                            radio3.setText(option3);
                            radio4.setText(option4);
                        }
                        TextView view = dotsLayout.findViewWithTag(documentSnapshot.getId());
                        if (LocateString(option1, email)){
                            radio1.setChecked(true);
                            answered = true;
                            for (int i = 0; i < optionsRadio.getChildCount(); i++) {
                                optionsRadio.getChildAt(i).setEnabled(false);
                            }
                            HighlightCircle(view);
                        }else if (LocateString(option2, email)){
                            radio2.setChecked(true);
                            answered = true;
                            for (int i = 0; i < optionsRadio.getChildCount(); i++) {
                                optionsRadio.getChildAt(i).setEnabled(false);
                            }
                            HighlightCircle(view);
                        }else if (LocateString(option3, email)){
                            radio3.setChecked(true);
                            answered = true;
                            for (int i = 0; i < optionsRadio.getChildCount(); i++) {
                                optionsRadio.getChildAt(i).setEnabled(false);
                            }
                            HighlightCircle(view);
                        }else if (LocateString(option4, email)){
                            radio4.setChecked(true);
                            answered = true;
                            for (int i = 0; i < optionsRadio.getChildCount(); i++) {
                                optionsRadio.getChildAt(i).setEnabled(false);
                            }
                            HighlightCircle(view);
                        }else {
                            for (int i = 0; i < optionsRadio.getChildCount(); i++) {
                                optionsRadio.getChildAt(i).setEnabled(true);
                            }
                            view.setTextColor(getResources().getColor(R.color.iconsColor));
                            view.setBackground(getResources().getDrawable(R.drawable.button_follow));
                            view.setTypeface(null, Typeface.BOLD);
                            view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                        }
                        if (showCorrect) {
                            switch (answer) {
                                case "Option1":
                                    radio1.setBackgroundColor(getResources().getColor(R.color.green));
                                    break;
                                case "Option2":
                                    radio2.setBackgroundColor(getResources().getColor(R.color.green));
                                    break;
                                case "Option3":
                                    radio3.setBackgroundColor(getResources().getColor(R.color.green));
                                    break;
                                case "Option4":
                                    radio4.setBackgroundColor(getResources().getColor(R.color.green));
                                    break;
                            }
                        }
                    }
                }
            });
        }
    }

    private void addAnswer(String selected, String docId) {
        if (!LocateString(attempted, email)){
            attempted.add(email);
            db.document(quizId).update("attempted", attempted);
        }
        switch (selected){
            case "Option1":
                option1.add(email);
                questionsRef.document(docId).update(SefnetContract.OPTION_1, option1);
                break;
            case "Option2":
                option2.add(email);
                questionsRef.document(docId).update(SefnetContract.OPTION_2, option2);
                break;
            case "Option3":
                option3.add(email);
                questionsRef.document(docId).update(SefnetContract.OPTION_3, option3);
                break;
            case "Option4":
                option4.add(email);
                questionsRef.document(docId).update(SefnetContract.OPTION_4, option4);
                break;
        }
    }

    private void HighlightCircle(TextView textView){
        textView.setBackground(getResources().getDrawable(R.drawable.bt_ui));
        textView.setTextColor(getResources().getColor(R.color.white));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        textView.setTypeface(null, Typeface.NORMAL);
    }

    private void displayScore() {
        optionsRadio.setVisibility(View.GONE);
        dotsLayout.setVisibility(View.GONE);
        title.setText("You Scored");
        double percent = (marks * 100) / (list.size());
        String percentString = percent + "%";
        percentView.setText(percentString);
        percentView.setVisibility(View.VISIBLE);
        String myScore = marks + "/" + list.size();
        score.setText(myScore);
        score.setVisibility(View.VISIBLE);
    }

    public String onRadioButtonClicked(RadioGroup view) {
        // Is the button now checked?
        RadioButton button = findViewById(view.getCheckedRadioButtonId());
        if (button == null){
            methods.showToast("Please choose an answer");
            return null;
        }
        boolean checked =  button.isChecked();

        // Check which radio button was clicked
        switch(button.getId()) {
            case R.id.radio1_answer_quiz:
                if (checked)
                    selected = "Option1";
                break;
            case R.id.radio2_answer_quiz:
                if (checked)
                    selected = "Option2";
                break;
            case R.id.radio3_answer_quiz:
                if (checked)
                    selected = "Option3";
                break;
            case R.id.radio4_answer_quiz:
                if (checked)
                    selected = "Option4";
                break;
        }
        return selected;
    }

    private boolean LocateString(ArrayList<String> Array, String s) {
        boolean found = false;
        for (int i = (Array.size() - 1); i > -1; i--) {
            String element = Array.get(i);
            if (element.equals(s)) {
                found = true;
            }
        }

        return found;
    }
}
