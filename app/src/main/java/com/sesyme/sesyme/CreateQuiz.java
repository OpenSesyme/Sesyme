package com.sesyme.sesyme;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.sesyme.sesyme.data.Methods;
import com.sesyme.sesyme.data.QuizDetails;
import com.sesyme.sesyme.data.SefnetContract;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CreateQuiz extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference quizzesRef;
    private String clusterId, quizId, email, answer, questionId;
    private RelativeLayout titleLayout, questionLayout;
    private EditText etTitle, etQuestion, etOption1, etOption2, etOption3, etOption4;
    private TextView releaseQuiz, btFinish, title;
    private TextView releaseAnswers;
    private TextView next;
    private Date quizDate, answersDate, today;
    private RadioGroup optionsRadio;
    private Calendar c;
    private Methods methods;
    private int i = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_quiz);

        titleLayout = findViewById(R.id.title_layout_quiz);
        questionLayout = findViewById(R.id.quiz_questions_layout);
        etTitle = findViewById(R.id.et_title_quiz);
        etQuestion = findViewById(R.id.et_question_quiz);
        etOption1 = findViewById(R.id.et_option1_quiz);
        etOption2 = findViewById(R.id.et_option2_quiz);
        etOption3 = findViewById(R.id.et_option3_quiz);
        etOption4 = findViewById(R.id.et_option4_quiz);
        releaseQuiz = findViewById(R.id.release_quiz_date);
        TextView cancel = findViewById(R.id.cancel_quiz);
        releaseAnswers = findViewById(R.id.release_answers_date);
        ImageView btQuizDate = findViewById(R.id.quiz_calendar);
        ImageView btAnswersDate = findViewById(R.id.answers_calendar);
        optionsRadio = findViewById(R.id.radio_group_quiz);
        next = findViewById(R.id.bt_next_quiz_button);
        btFinish = findViewById(R.id.finish_create_quiz);
        title = findViewById(R.id.title_create_quiz);
        methods = new Methods(this);
        questionId = "Question " + i;
        btFinish.setVisibility(View.GONE);

        quizzesRef = db.collection(SefnetContract.QUIZZES_DETAILS);

        c = Calendar.getInstance();
        today = c.getTime();
        quizDate = c.getTime();
        answersDate = c.getTime();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(SefnetContract.REFERENCE)) {
            clusterId = intent.getStringExtra(SefnetContract.REFERENCE);
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            email = user.getEmail();
        }

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (quizId != null) {
                    if (i == 1) {
                        quizzesRef.document(quizId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                finish();
                            }
                        });
                    } else {
                        quizzesRef.document(quizId).collection(SefnetContract.QUESTIONS)
                                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                                        @Nullable FirebaseFirestoreException e) {
                                        if (e == null && queryDocumentSnapshots != null) {
                                            for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                                                snapshot.getReference().delete();
                                            }
                                        }
                                    }
                                });
                        quizzesRef.document(quizId).delete();
                    }
                } else {
                    finish();
                }
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (titleLayout.getVisibility() == View.VISIBLE) {
                    saveQuiz();
                } else {
                    addQuestion();
                }
            }
        });

        btFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (i == 1) {
                    if (titleLayout.getVisibility() == View.VISIBLE) {
                        finish();
                    } else {
                        deleteAndExit();
                    }
                } else {
                    addQuestion();
                    finish();
                }
            }
        });

        btAnswersDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectAnswersDate();
            }
        });

        btQuizDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectQuizDate();
            }
        });
    }

    private void addQuestion() {
        String question = etQuestion.getText().toString().trim();
        String option1 = etOption1.getText().toString().trim();
        String option2 = etOption2.getText().toString().trim();
        String option3 = etOption3.getText().toString().trim();
        String option4 = etOption4.getText().toString().trim();
        ArrayList<String> options = new ArrayList<>();
        if (!option1.isEmpty() && !option2.isEmpty() && !option3.isEmpty() && !option4.isEmpty()) {
            options.add(option1);
            options.add(option2);
            options.add(option3);
            options.add(option4);
        }
        if (question.length() < 10) {
            methods.showToast("Please write a detailed question");
            etQuestion.requestFocus();
        } else if (options.size() < 4) {
            methods.showToast("Please fill in all four choices");
            if (option1.isEmpty()) {
                etOption1.requestFocus();
            } else if (option2.isEmpty()) {
                etOption2.requestFocus();
            } else if (option3.isEmpty()) {
                etOption3.requestFocus();
            } else {
                etOption4.requestFocus();
            }
        } else if (onRadioButtonClicked(optionsRadio) == null) {
            methods.showToast("Please select a correct answer for marking purpose");
        } else {
            quizzesRef.document(quizId).collection(SefnetContract.QUESTIONS)
                    .document(questionId).set(new QuizDetails(question,
                    options, onRadioButtonClicked(optionsRadio)));
            methods.showToast("Question added");
            i++;
            questionId = "Question " + i;
            title.setText(questionId);
            optionsRadio.clearCheck();
            etQuestion.setText("");
            etOption1.setText("");
            etOption2.setText("");
            etOption3.setText("");
            etOption4.setText("");
        }
    }

    private void saveQuiz() {
        String titleString = etTitle.getText().toString().trim();
        quizId = String.valueOf(System.currentTimeMillis());
        if (titleString.length() > 3 && clusterId != null) {
            //save the quiz
            quizzesRef.document(quizId).set(new QuizDetails(quizDate, answersDate, clusterId, email, titleString))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            titleLayout.setVisibility(View.GONE);
                            questionLayout.setVisibility(View.VISIBLE);
                            next.setText(getResources().getString(R.string.next_question));
                            title.setText(questionId);
                            btFinish.setVisibility(View.VISIBLE);
                        }
                    });
        } else {
            methods.showToast("Please provide a valid Title");
            etTitle.requestFocus();
        }
    }

    private void selectQuizDate() {
        int sYear = c.get(Calendar.YEAR);
        int sMonth = c.get(Calendar.MONTH);
        int sDay = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar date = Calendar.getInstance();
                date.set(Calendar.YEAR, year);
                date.set(Calendar.MONTH, monthOfYear);
                date.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String dateString = DateFormat.getDateInstance().format(date.getTime());
                quizDate = date.getTime();
                if (quizDate.before(today)) {
                    methods.showToast("Sorry we cannot release in the past.");
                } else {
                    releaseQuiz.setText(dateString);
                }
            }
        }, sYear, sMonth, sDay);
        datePicker.show();
    }

    private void selectAnswersDate() {
        Calendar c = Calendar.getInstance();
        final Date today = c.getTime();
        int sYear = c.get(Calendar.YEAR);
        int sMonth = c.get(Calendar.MONTH);
        int sDay = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar date = Calendar.getInstance();
                date.set(Calendar.YEAR, year);
                date.set(Calendar.MONTH, monthOfYear);
                date.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String dateString = DateFormat.getDateInstance().format(date.getTime());
                answersDate = date.getTime();
                if (answersDate.before(today)) {
                    methods.showToast("Sorry we cannot release in the past.");
                } else {
                    releaseAnswers.setText(dateString);
                }
            }
        }, sYear, sMonth, sDay);
        datePicker.show();
    }

    private void deleteAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You have not added any questions. \n Delete quiz and exit?");
        builder.setPositiveButton("Yes, Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                quizzesRef.document(quizId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        finish();
                    }
                });
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("No, Keep adding  ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public String onRadioButtonClicked(RadioGroup view) {
        // Is the button now checked?
        RadioButton button = findViewById(view.getCheckedRadioButtonId());
        if (button == null) {
            methods.showToast("Please Choose a correct answer for marking purpose");
            return null;
        }
        boolean checked = button.isChecked();

        // Check which radio button was clicked
        switch (button.getId()) {
            case R.id.radio1_quiz:
                if (checked)
                    answer = "Option1";
                break;
            case R.id.radio2_quiz:
                if (checked)
                    answer = "Option2";
                break;
            case R.id.radio3_quiz:
                if (checked)
                    answer = "Option3";
                break;
            case R.id.radio4_quiz:
                if (checked)
                    answer = "Option4";
                break;
            default:
                answer = null;
        }
        return answer;
    }
}
